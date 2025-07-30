package com.codesupreme.boxingclub.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductDto {

    private Long id;

    private String title;
    private Double price;
    private String imageUrl;
    @JsonProperty("isDisable")
    private Boolean isDisable;
    private Date createdAt;
}
