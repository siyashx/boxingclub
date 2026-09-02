package com.codesupreme.boxingclub.model.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;
    private String title;

    @Column(length = 1000)
    private String message;

    @Column(length = 160)
    private String eventKey;

    private Boolean pushSent;
    private Date createdAt;
}
