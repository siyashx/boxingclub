package com.codesupreme.boxingclub.push;

import com.codesupreme.boxingclub.dao.customer.CustomerRepository;
import com.codesupreme.boxingclub.model.customer.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PushTest {
    final PushDeviceRepository devices = mock(PushDeviceRepository.class);
    final PushDeliveryRepository deliveries = mock(PushDeliveryRepository.class);
    final CustomerRepository customers = mock(CustomerRepository.class);
    final ObjectMapper json = new ObjectMapper();
    final FcmPushService push = new FcmPushService(devices, deliveries, customers, json, true, "test-project");

    Customer customer(long id) {
        Customer customer = new Customer();customer.setId(id);customer.setPassword("test:password");return customer;
    }
    String auth(long id) {
        return "Basic " + Base64.getEncoder().encodeToString((id+":test:password").getBytes(StandardCharsets.UTF_8));
    }
    PushDevice device(long id, Long customerId) {
        PushDevice device = new PushDevice();device.setId(id);device.setCustomerId(customerId);
        device.setInstallationId("installation-1234567890");device.setToken("test-device-token-1234567890");
        device.setPlatform("ios");device.setEnabled(true);return device;
    }
    PushDelivery job(Long customerId) {
        PushDelivery job = new PushDelivery();job.setId(1L);job.setDeviceId(1L);job.setCustomerId(customerId);
        job.setExpiresAt(System.currentTimeMillis()+60000);job.setState("PENDING");
        job.setPayload("{\"title\":\"Club\",\"body\":\"Reminder\"}");return job;
    }
    void due(PushDelivery job, PushDevice device) {
        when(devices.findById(1L)).thenReturn(Optional.of(device));
        when(deliveries.findTop50ByStateAndNextAttemptAtLessThanEqualOrderByIdAsc(eq("PENDING"),anyLong())).thenReturn(List.of(job));
    }

    @Test void authenticationChecksPasswordAndDisabledState() {
        Customer customer = customer(7);when(customers.findById(7L)).thenReturn(Optional.of(customer));
        PushAuth auth = new PushAuth(customers);
        assertEquals(7L,auth.requireCustomer(auth(7)).getId());
        assertThrows(ResponseStatusException.class,()->auth.requireCustomer("Basic invalid"));
        assertThrows(ResponseStatusException.class,()->auth.requireCustomer("Basic "+Base64.getEncoder().encodeToString("7:wrong".getBytes(StandardCharsets.UTF_8))));
        customer.setIsDisable(true);
        assertThrows(ResponseStatusException.class,()->auth.requireCustomer(auth(7)));
    }
    @Test void registrationDerivesOwnerFromCredentialsAndRetiresDuplicateToken() {
        Customer customer = customer(7);when(customers.findById(7L)).thenReturn(Optional.of(customer));
        PushDevice device = device(1,7L), old = device(2,null);
        when(devices.findByInstallationId(device.getInstallationId())).thenReturn(Optional.of(device));
        when(devices.findByToken(device.getToken())).thenReturn(List.of(device,old));
        PushController controller = new PushController(new PushAuth(customers), devices);
        var request = new PushController.DeviceRequest(device.getInstallationId(),device.getToken(),"ios",true);
        assertThrows(ResponseStatusException.class,()->controller.register(null,request));
        controller.register(auth(7),request);
        assertEquals(7L,device.getCustomerId());assertFalse(old.isEnabled());
        verify(devices).save(old);
    }
    @Test void anotherCustomerCannotRevokeAnInstallation() {
        when(customers.findById(8L)).thenReturn(Optional.of(customer(8)));
        PushDevice device=device(1,7L);when(devices.findByInstallationId(device.getInstallationId())).thenReturn(Optional.of(device));
        new PushController(new PushAuth(customers),devices).revoke(auth(8),
            new PushController.DeviceRequest(device.getInstallationId(),device.getToken(),"ios",false));
        assertTrue(device.isEnabled());assertEquals(7L,device.getCustomerId());verify(devices,never()).save(any());
    }
    @Test void remindersQueueForEveryOwnedDeviceAndDeduplicateRetries() throws Exception {
        when(devices.findByCustomerIdAndEnabledTrue(7L)).thenReturn(List.of(device(1,7L),device(2,7L)));
        when(deliveries.existsByEventKeyAndDeviceId("membership-expiry:7",2L)).thenReturn(true);
        assertEquals(1,push.enqueue(7L,"Reminder 🥊","Member only","membership-expiry:7"));
        var captor=org.mockito.ArgumentCaptor.forClass(PushDelivery.class);
        verify(deliveries).save(captor.capture());
        var payload=json.readTree(captor.getValue().getPayload());
        assertEquals("7",payload.get("recipientCustomerId").asText());
        assertEquals("customer",payload.get("audience").asText());
        assertEquals("Reminder 🥊",payload.get("title").asText());
        verify(devices,never()).findByEnabledTrue();
    }
    @Test void accountChangeOrLogoutCancelsQueuedPersonalNotification() {
        PushDelivery job=job(7L);PushDevice device=device(1,8L);
        when(customers.findById(8L)).thenReturn(Optional.of(customer(8)));due(job,device);
        push.deliverDue();assertEquals("EXPIRED",job.getState());
        verify(deliveries).save(job);
    }
    @Test void revokedDevicesDoNotReceiveEvenPublicMessages() {
        PushDelivery job=job(null);PushDevice device=device(1,null);device.setEnabled(false);due(job,device);
        push.deliverDue();assertEquals("EXPIRED",job.getState());
    }
    @Test void transientFailureIsRetriedAndSuccessIsRecorded() throws Exception {
        PushDelivery job=job(7L);PushDevice device=device(1,7L);
        when(customers.findById(7L)).thenReturn(Optional.of(customer(7)));due(job,device);
        FirebaseMessaging messaging=mock(FirebaseMessaging.class);ReflectionTestUtils.setField(push,"messaging",messaging);
        when(messaging.send(any(Message.class))).thenThrow(new IllegalStateException("unavailable")).thenReturn("accepted");
        push.deliverDue();assertEquals("PENDING",job.getState());assertEquals(1,job.getAttempts());
        assertTrue(job.getNextAttemptAt()>System.currentTimeMillis());
        push.deliverDue();assertEquals("SENT",job.getState());
    }
    @Test void generalClubNewsIncludesGuestsWithoutPersonalRecipient() throws Exception {
        when(devices.findByEnabledTrue()).thenReturn(List.of(device(1,null),device(2,7L)));
        assertEquals(2,push.enqueue(null,"Club news","Open day","notification:15"));
        var captor=org.mockito.ArgumentCaptor.forClass(PushDelivery.class);
        verify(deliveries,times(2)).save(captor.capture());
        for(var job:captor.getAllValues()) {
            var payload=json.readTree(job.getPayload());
            assertEquals("public",payload.get("audience").asText());assertFalse(payload.has("recipientCustomerId"));
        }
    }
}
