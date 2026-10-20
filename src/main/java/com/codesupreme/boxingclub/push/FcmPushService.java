package com.codesupreme.boxingclub.push;
import com.codesupreme.boxingclub.dao.customer.CustomerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
public class FcmPushService {
    private static final Logger log=LoggerFactory.getLogger(FcmPushService.class);
    private final PushDeviceRepository devices;
    private final PushDeliveryRepository deliveries;
    private final CustomerRepository customers;
    private final ObjectMapper json;
    private final boolean enabled;
    private final String projectId;
    private FirebaseMessaging messaging;
    public FcmPushService(PushDeviceRepository devices,PushDeliveryRepository deliveries,CustomerRepository customers,ObjectMapper json,
        @Value("${boxing.fcm.enabled:true}") boolean enabled,@Value("${boxing.fcm.project-id:bakuappcraft-notifications}") String projectId) {
        this.devices=devices;this.deliveries=deliveries;this.customers=customers;this.json=json;this.enabled=enabled;this.projectId=projectId;
    }
    private synchronized FirebaseMessaging messaging() throws Exception {
        if(messaging==null) {
            FirebaseApp app=FirebaseApp.getApps().stream().filter(a->"boxing-push".equals(a.getName())).findFirst().orElse(null);
            if(app==null) app=FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).setProjectId(projectId).build(),"boxing-push");
            messaging=FirebaseMessaging.getInstance(app);
        }
        return messaging;
    }
    public boolean sendToCustomer(String customerId,String title,String body,String eventKey) {
        try {return enqueue(Long.parseLong(customerId),title,body,eventKey)>0;} catch(NumberFormatException error) {return false;}
    }
    public synchronized int enqueue(Long customerId,String title,String body,String eventKey) {
        if(!enabled) return 0;
        if(eventKey==null || eventKey.length()>160) throw new IllegalArgumentException("Invalid push event");
        List<PushDevice> targets=customerId==null ? devices.findByEnabledTrue() : devices.findByCustomerIdAndEnabledTrue(customerId);
        long now=System.currentTimeMillis();int count=0;
        for(PushDevice device:targets) {
            if(deliveries.existsByEventKeyAndDeviceId(eventKey,device.getId())) continue;
            Map<String,String> data=new LinkedHashMap<>();
            data.put("title",limit(title,100));data.put("body",limit(body,400));data.put("eventKey",eventKey);
            data.put("type",eventKey.startsWith("membership-expiry:") ? "membership_expiry" : "club_notification");
            data.put("audience",customerId==null ? "public" : "customer");
            if(customerId!=null) data.put("recipientCustomerId",customerId.toString());
            PushDelivery delivery=new PushDelivery();delivery.setDeviceId(device.getId());delivery.setCustomerId(customerId);
            delivery.setEventKey(eventKey);delivery.setState("PENDING");delivery.setNextAttemptAt(now);delivery.setExpiresAt(now+86400000L);
            try {delivery.setPayload(json.writeValueAsString(data));} catch(Exception error) {throw new IllegalArgumentException("Invalid payload");}
            deliveries.save(delivery);count++;
        }
        return count;
    }
    @Scheduled(fixedDelayString="${boxing.fcm.worker-delay-ms:5000}")
    public void deliverDue() {
        if(!enabled) return;
        for(PushDelivery delivery:deliveries.findTop50ByStateAndNextAttemptAtLessThanEqualOrderByIdAsc("PENDING",System.currentTimeMillis())) deliver(delivery);
    }
    private void deliver(PushDelivery delivery) {
        PushDevice device=devices.findById(delivery.getDeviceId()).orElse(null);
        long now=System.currentTimeMillis();
        boolean active=device!=null && device.isEnabled() && (device.getCustomerId()==null || customers.findById(device.getCustomerId()).map(c->!Boolean.TRUE.equals(c.getIsDisable())).orElse(false));
        if(!active || delivery.getExpiresAt()<=now || delivery.getCustomerId()!=null && !Objects.equals(delivery.getCustomerId(),device.getCustomerId())) {
            delivery.setState("EXPIRED");deliveries.save(delivery);return;
        }
        try {
            Map<String,String> data=json.readValue(delivery.getPayload(),new TypeReference<Map<String,String>>(){});
            data.put("expiresAt",Long.toString(delivery.getExpiresAt()));
            Message.Builder message=Message.builder().setToken(device.getToken()).putAllData(data);
            if("ios".equals(device.getPlatform())) {
                message.setApnsConfig(ApnsConfig.builder().setAps(Aps.builder().setContentAvailable(true)
                    .setAlert(ApsAlert.builder().setTitle(data.get("title")).setBody(data.get("body")).build()).setSound("default").build())
                    .putHeader("apns-expiration",Long.toString(delivery.getExpiresAt()/1000)).build());
            } else {
                message.setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH)
                    .setRestrictedPackageName("com.bakuappcraft.boxingclub888").setTtl(delivery.getExpiresAt()-now).build());
            }
            messaging().send(message.build());delivery.setState("SENT");
        } catch(Exception error) {
            MessagingErrorCode code=error instanceof FirebaseMessagingException ? ((FirebaseMessagingException)error).getMessagingErrorCode() : null;
            if(code==MessagingErrorCode.UNREGISTERED || code==MessagingErrorCode.SENDER_ID_MISMATCH || code==MessagingErrorCode.INVALID_ARGUMENT) {
                delivery.setState("FAILED");
                if(code==MessagingErrorCode.UNREGISTERED) {device.setEnabled(false);devices.save(device);}
            } else {
                delivery.setAttempts(delivery.getAttempts()+1);
                delivery.setNextAttemptAt(now+Math.min(300000L,5000L*(1L<<Math.min(6,delivery.getAttempts()-1))));
                if(delivery.getAttempts()>=8) delivery.setState("FAILED");
            }
            // Never log tokens, credentials, customer names or membership text.
            log.warn("FCM delivery failed: id={}, state={}, code={}",delivery.getId(),delivery.getState(),code==null ? error.getClass().getSimpleName() : code.name());
        }
        deliveries.save(delivery);
    }
    private static String limit(String value,int size) {
        return value==null ? "" : value.codePoints().limit(size).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();
    }
}
