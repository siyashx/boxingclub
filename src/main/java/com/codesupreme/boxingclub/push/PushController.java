package com.codesupreme.boxingclub.push;
import com.codesupreme.boxingclub.model.customer.Customer;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
@RestController @RequestMapping("/api/v10/push")
public class PushController {
    private final PushAuth auth;
    private final PushDeviceRepository devices;
    public PushController(PushAuth auth,PushDeviceRepository devices) {this.auth=auth;this.devices=devices;}
    public record DeviceRequest(String installationId,String token,String platform,boolean enabled) {}
    @PutMapping("/devices") @Transactional
    public synchronized Map<String,Boolean> register(@RequestHeader(value="Authorization",required=false) String header,@RequestBody DeviceRequest request) {
        if(request.installationId()==null || !request.installationId().matches("[a-zA-Z0-9-]{16,64}") || request.token()==null || request.token().length()<20 || request.token().length()>4096 || !Set.of("ios","android").contains(Objects.toString(request.platform(),""))) throw new IllegalArgumentException();
        Customer customer=header==null ? null : auth.requireCustomer(header);
        PushDevice device=devices.findByInstallationId(request.installationId()).orElseGet(PushDevice::new);
        // An anonymous registration cannot detach another customer's installation.
        if(customer==null && device.getCustomerId()!=null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        for(PushDevice other:devices.findByToken(request.token())) {
            if(!Objects.equals(other.getId(),device.getId())) {other.setEnabled(false);devices.save(other);}
        }
        device.setInstallationId(request.installationId());device.setToken(request.token());device.setPlatform(request.platform());
        device.setCustomerId(customer==null ? null : customer.getId());device.setEnabled(request.enabled());device.setUpdatedAt(System.currentTimeMillis());
        devices.save(device);return Map.of("registered",true);
    }
    @PostMapping("/revoke") @Transactional
    public synchronized Map<String,Boolean> revoke(@RequestHeader(value="Authorization",required=false) String header,@RequestBody DeviceRequest request) {
        Customer customer=header==null ? null : auth.requireCustomer(header);
        devices.findByInstallationId(Objects.toString(request.installationId(),"")).ifPresent(device->{
            boolean owner=customer!=null && Objects.equals(device.getCustomerId(),customer.getId());
            boolean guest=device.getCustomerId()==null && request.token()!=null && MessageDigest.isEqual(device.getToken().getBytes(StandardCharsets.UTF_8),request.token().getBytes(StandardCharsets.UTF_8));
            if(owner || guest) {device.setEnabled(false);device.setCustomerId(null);devices.save(device);}
        });
        return Map.of("revoked",true);
    }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> invalid() {return Map.of("error","Invalid device registration");}
}
