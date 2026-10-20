package com.codesupreme.boxingclub.push;
import com.codesupreme.boxingclub.dao.customer.CustomerRepository;
import com.codesupreme.boxingclub.model.customer.Customer;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
@Service
public class PushAuth {
    private final CustomerRepository customers;
    public PushAuth(CustomerRepository customers) {this.customers=customers;}
    public Customer requireCustomer(String header) {
        try {
            if(header==null || !header.startsWith("Basic ")) throw new IllegalArgumentException();
            String value=new String(Base64.getDecoder().decode(header.substring(6)),StandardCharsets.UTF_8);
            int split=value.indexOf(':');
            Customer customer=customers.findById(Long.parseLong(value.substring(0,split))).orElseThrow();
            if(Boolean.TRUE.equals(customer.getIsDisable()) || customer.getPassword()==null ||
                !MessageDigest.isEqual(customer.getPassword().getBytes(StandardCharsets.UTF_8),value.substring(split+1).getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException();
            return customer;
        } catch(Exception ignored) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Customer authentication required");
        }
    }
}
