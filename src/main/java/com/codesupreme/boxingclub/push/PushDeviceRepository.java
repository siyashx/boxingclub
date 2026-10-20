package com.codesupreme.boxingclub.push;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PushDeviceRepository extends JpaRepository<PushDevice,Long> {
    Optional<PushDevice> findByInstallationId(String installationId);
    List<PushDevice> findByEnabledTrue();
    List<PushDevice> findByCustomerIdAndEnabledTrue(Long customerId);
    List<PushDevice> findByToken(String token);
}
