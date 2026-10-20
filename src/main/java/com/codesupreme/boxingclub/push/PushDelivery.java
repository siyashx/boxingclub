package com.codesupreme.boxingclub.push;
import jakarta.persistence.*;
import lombok.*;
@Entity @Getter @Setter
@Table(name="boxing_push_delivery",uniqueConstraints=@UniqueConstraint(columnNames={"event_key","device_id"}),
    indexes=@Index(name="boxing_push_due",columnList="state,next_attempt_at"))
public class PushDelivery {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="event_key",nullable=false,length=160) private String eventKey;
    @Column(name="device_id",nullable=false) private Long deviceId;
    private Long customerId;
    @Column(nullable=false,length=4096) private String payload;
    @Column(nullable=false,length=16) private String state;
    @Column(name="next_attempt_at") private long nextAttemptAt;
    private long expiresAt;
    private int attempts;
}
