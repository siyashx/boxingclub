package com.codesupreme.boxingclub.service.inter.customer;

import com.codesupreme.boxingclub.dto.customer.CustomerDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CustomerServiceInter {

    List<CustomerDto> getAllCustomers();
    CustomerDto getCustomerById(Long id);
    CustomerDto updateCustomer(Long id, CustomerDto customerDto);
    ResponseEntity<CustomerDto> createCustomer(CustomerDto customerDto);
    void deleteCustomerById(Long id);
    Boolean isCustomerPhoneNumberTaken(String phoneNumber);
    CustomerDto findCustomerByPhoneNumber(String phoneNumber);
}
