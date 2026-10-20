package com.codesupreme.boxingclub.service.impl.notification;

import com.codesupreme.boxingclub.dao.notification.NotificationRepository;
import com.codesupreme.boxingclub.dto.notification.NotificationDto;
import com.codesupreme.boxingclub.model.notification.Notification;
import org.modelmapper.ModelMapper;
import com.codesupreme.boxingclub.push.FcmPushService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationImpl {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final FcmPushService push;

    public NotificationImpl(NotificationRepository notificationRepository, ModelMapper modelMapper, FcmPushService push) {
        this.notificationRepository = notificationRepository;
        this.modelMapper = modelMapper;
        this.push = push;
    }

    // Admin/API compatibility: returns every notification.
    public List<NotificationDto> getAllNotification() {
        return notificationRepository.findAll().stream()
                .map(det -> modelMapper.map(det, NotificationDto.class))
                .toList();
    }

    public List<NotificationDto> getPublicNotifications() {
        return notificationRepository.findPublicNotifications().stream()
                .map(det -> modelMapper.map(det, NotificationDto.class))
                .toList();
    }

    public List<NotificationDto> getNotificationsForCustomer(String customerId) {
        return notificationRepository.findNotificationsForCustomer(customerId).stream()
                .map(det -> modelMapper.map(det, NotificationDto.class))
                .toList();
    }

    public NotificationDto getNotificationById(Long id) {
        Optional<Notification> optional = notificationRepository.findById(id);
        return optional.map(det -> modelMapper.map(det, NotificationDto.class)).orElse(null);
    }

    public NotificationDto saveNotification(NotificationDto dto) {
        Notification det = modelMapper.map(dto, Notification.class);
        det = notificationRepository.save(det);
        String target=det.getCustomerId();
        try {
            Long customerId=target==null || target.isBlank() || "all".equalsIgnoreCase(target.trim()) ? null : Long.valueOf(target);
            int queued=push.enqueue(customerId,det.getTitle(),det.getMessage(),"notification:"+det.getId());
            if(queued>0) {det.setPushSent(true);notificationRepository.save(det);}
        } catch(Exception ignored) {
            // Saving the in-app notification must not depend on provider availability.
        }
        return modelMapper.map(det, NotificationDto.class);
    }

    public NotificationDto updateNotification(Long notificationId, NotificationDto notificationDto) {
        Optional<Notification> optional = notificationRepository.findById(notificationId);
        if (optional.isPresent()) {
            Notification notification = optional.get();

            if (notificationDto.getCustomerId() != null) {
                notification.setCustomerId(notificationDto.getCustomerId());
            }
            if (notificationDto.getTitle() != null) {
                notification.setTitle(notificationDto.getTitle());
            }
            if (notificationDto.getMessage() != null) {
                notification.setMessage(notificationDto.getMessage());
            }
            if (notificationDto.getCreatedAt() != null) {
                notification.setCreatedAt(notificationDto.getCreatedAt());
            }

            notification = notificationRepository.save(notification);
            return modelMapper.map(notification, NotificationDto.class);
        }
        return null;
    }

    public Boolean deleteNotification(Long id) {
        Optional<Notification> optional = notificationRepository.findById(id);
        if (optional.isPresent()) {
            notificationRepository.delete(optional.get());
            return true;
        }
        return false;
    }
}
