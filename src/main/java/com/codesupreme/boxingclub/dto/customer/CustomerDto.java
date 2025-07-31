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
    private String profilePicture;
    private String password;
    private String qrCodeImage;
    private String siteUrl;
    private String expiryMonthlySub;
    @JsonProperty("isDisable")
    private Boolean isDisable;
    private Date createdAt;
}
