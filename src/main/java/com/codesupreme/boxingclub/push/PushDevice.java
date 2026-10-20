package com.codesupreme.boxingclub.push;
import jakarta.persistence.*;
import lombok.*;
@Entity @Getter @Setter
@Table(name="boxing_push_device",uniqueConstraints=@UniqueConstraint(columnNames="installation_id"))
public class PushDevice {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="installation_id",nullable=false,length=64) private String installationId;
    private Long customerId;
    @Column(nullable=false,length=4096) private String token;
    @Column(nullable=false,length=16) private String platform;
    private boolean enabled;
    private long updatedAt;
}
