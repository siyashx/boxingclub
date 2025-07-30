package com.codesupreme.boxingclub.service.impl.customer;

import com.codesupreme.boxingclub.dao.customer.CustomerRepository;
import com.codesupreme.boxingclub.dto.customer.CustomerDto;
import com.codesupreme.boxingclub.model.customer.Customer;
import com.codesupreme.boxingclub.service.inter.customer.CustomerServiceInter;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerServiceInter {

    private final CustomerRepository customerRepository;

    private final ModelMapper modelMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, ModelMapper modelMapper) {
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public List<CustomerDto> getAllCustomers() {
        List<CustomerDto> listDto = new ArrayList<>();

        List<Customer> listEntity = customerRepository.findAll();

        for (Customer entity : listEntity) {
            CustomerDto dto = modelMapper.map(entity, CustomerDto.class);
            listDto.add(dto);
        }
        return listDto;
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        Customer entity = customerRepository.findById(id).orElse(null);
        if (entity != null) {
            return modelMapper.map(entity, CustomerDto.class);
        }
        return null;
    }

    @Override
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Optional<Customer> customerOptional = customerRepository.findById(id);

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();

            if (customerDto.getName() != null) {
                customer.setName(customerDto.getName());
            }

            if (customerDto.getSurname() != null) {
                customer.setSurname(customerDto.getSurname());
            }

            if (customerDto.getPhoneNumber() != null) {
                customer.setPhoneNumber(customerDto.getPhoneNumber());
            }

            if (customerDto.getProfilePicture() != null) {
                customer.setProfilePicture(customerDto.getProfilePicture());
            }

            if (customerDto.getQrCodeImage() != null) {
                customer.setQrCodeImage(customerDto.getQrCodeImage());
            }

            if (customerDto.getSiteUrl() != null) {
                customer.setSiteUrl(customerDto.getSiteUrl());
            }

            if (customerDto.getPassword() != null) {
                customer.setPassword(customerDto.getPassword());
            }

            if (customerDto.getIsDisable() != null) {
                customer.setIsDisable(customerDto.getIsDisable());
            }

            customer = customerRepository.save(customer);
            return modelMapper.map(customer, CustomerDto.class);
        }
        return null;
    }

    @Override
    public ResponseEntity<CustomerDto> createCustomer(CustomerDto customerDto) {
        Customer customerEntity = modelMapper.map(customerDto, Customer.class);
        Customer savedCustomer = customerRepository.save(customerEntity);
        return ResponseEntity.ok(modelMapper.map(savedCustomer, CustomerDto.class));
    }

    @Override
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Boolean isCustomerPhoneNumberTaken(String phoneNumber) {
        return customerRepository.findCustomerByPhoneNumber(phoneNumber) != null;
    }

    @Override
    public CustomerDto findCustomerByPhoneNumber(String phoneNumber) {
        Customer customer = customerRepository.findCustomerByPhoneNumber(phoneNumber);
        if (customer != null) {
            return modelMapper.map(customer, CustomerDto.class);
        }
        return null;
    }
}
