package com.codesupreme.boxingclub.dao.customer;

import com.codesupreme.boxingclub.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findCustomerByPhoneNumber(String phoneNumber);

}
