package com.codesupreme.boxingclub.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationDto {

    private Long id;

    private String customerId;
    private String message;
    private Date createdAt;

}
