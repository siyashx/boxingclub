package com.codesupreme.boxingclub.api.customer.controller;

import com.codesupreme.boxingclub.dto.customer.CustomerDto;
import com.codesupreme.boxingclub.service.impl.customer.CustomerServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v7/customer")
public class CustomerController {

    private final CustomerServiceImpl customerServiceImpl;

    public CustomerController(CustomerServiceImpl customerServiceImpl) {
        this.customerServiceImpl = customerServiceImpl;
    }

    // List
    @GetMapping
    public List<CustomerDto> getAllCustomers() {
        return customerServiceImpl.getAllCustomers();
    }

    // Id
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable("customerId") Long customerId) {
        CustomerDto customerDto = customerServiceImpl.getCustomerById(customerId);
        if (customerDto != null) {
            return ResponseEntity.ok(customerDto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Customer doesn't exist with given id..");
    }

    // Update
    @PutMapping("/{customerId}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable("customerId") Long id,
            @RequestBody CustomerDto customerDto
    ) {
        CustomerDto updatedCustomer = customerServiceImpl.updateCustomer(id, customerDto);
        if (updatedCustomer != null) {
            return ResponseEntity.ok(updatedCustomer);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete
    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomerById(@PathVariable("customerId") Long customerId) {
        customerServiceImpl.deleteCustomerById(customerId);
        return ResponseEntity.ok().build();
    }

    // Create
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerDto customerDto) {
        return customerServiceImpl.createCustomer(customerDto);
    }
}
