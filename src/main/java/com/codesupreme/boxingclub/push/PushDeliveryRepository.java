package com.codesupreme.boxingclub.push;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PushDeliveryRepository extends JpaRepository<PushDelivery,Long> {
    boolean existsByEventKeyAndDeviceId(String eventKey,Long deviceId);
    List<PushDelivery> findTop50ByStateAndNextAttemptAtLessThanEqualOrderByIdAsc(String state,long now);
}
