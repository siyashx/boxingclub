package com.codesupreme.boxingclub.dao.notification;

import com.codesupreme.boxingclub.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByEventKey(String eventKey);

    @Query("""
            select n
            from Notification n
            where n.customerId is null
               or trim(n.customerId) = ''
               or lower(trim(n.customerId)) = 'all'
            order by n.createdAt desc, n.id desc
            """)
    List<Notification> findPublicNotifications();

    @Query("""
            select n
            from Notification n
            where n.customerId is null
               or trim(n.customerId) = ''
               or lower(trim(n.customerId)) = 'all'
               or n.customerId = :customerId
            order by n.createdAt desc, n.id desc
            """)
    List<Notification> findNotificationsForCustomer(
            @Param("customerId") String customerId
    );
}
