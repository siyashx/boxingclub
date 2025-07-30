package com.codesupreme.boxingclub.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CustomerDto {

    private Long id;

    private String name;
    private String surname;
    private String phoneNumber;
    private String password;
    private String qrCode;
    private String siteUrl;
    @JsonProperty("isDisable")
    private Boolean isDisable;
    private Date createdAt;
}
