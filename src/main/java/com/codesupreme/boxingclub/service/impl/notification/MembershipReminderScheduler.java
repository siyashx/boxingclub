package com.codesupreme.boxingclub.service.impl.notification;

import com.codesupreme.boxingclub.dao.customer.CustomerRepository;
import com.codesupreme.boxingclub.dao.notification.NotificationRepository;
import com.codesupreme.boxingclub.model.customer.Customer;
import com.codesupreme.boxingclub.model.notification.Notification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MembershipReminderScheduler {

    private static final ZoneId BAKU_ZONE = ZoneId.of("Asia/Baku");
    private static final String TITLE = "Üzvlük xatırlatması";

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final OneSignalPushService oneSignalPushService;

    public MembershipReminderScheduler(
            CustomerRepository customerRepository,
            NotificationRepository notificationRepository,
            OneSignalPushService oneSignalPushService
    ) {
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.oneSignalPushService = oneSignalPushService;
    }

    @Scheduled(
            cron = "${membership.reminder.cron:0 0 10 * * *}",
            zone = "${membership.reminder.zone:Asia/Baku}"
    )
    public void sendDailyMembershipReminders() {
        LocalDate today = LocalDate.now(BAKU_ZONE);

        for (Customer customer : customerRepository.findAll()) {
            if (Boolean.TRUE.equals(customer.getIsDisable())) {
                continue;
            }

            LocalDate expiryDate = parseExpiryDate(customer.getExpiryMonthlySub());
            if (expiryDate == null) {
                continue;
            }

            long daysRemaining = ChronoUnit.DAYS.between(today, expiryDate);
            if (daysRemaining < 0 || daysRemaining > 3) {
                continue;
            }

            sendReminder(customer, expiryDate, (int) daysRemaining);
        }
    }

    private void sendReminder(
            Customer customer,
            LocalDate expiryDate,
            int daysRemaining
    ) {
        String customerId = String.valueOf(customer.getId());
        String eventKey = String.format(
                "membership-expiry:%s:%s:%d",
                customerId,
                expiryDate,
                daysRemaining
        );

        Notification notification = notificationRepository
                .findByEventKey(eventKey)
                .orElseGet(() -> notificationRepository.save(
                        Notification.builder()
                                .customerId(customerId)
                                .title(TITLE)
                                .message(buildMessage(customer, daysRemaining))
                                .eventKey(eventKey)
                                .pushSent(false)
                                .createdAt(new Date())
                                .build()
                ));

        if (Boolean.TRUE.equals(notification.getPushSent())) {
            return;
        }

        boolean sent = oneSignalPushService.sendToCustomer(
                customerId,
                notification.getTitle(),
                notification.getMessage(),
                eventKey
        );

        if (sent) {
            notification.setPushSent(true);
            notificationRepository.save(notification);
        }
    }

    private String buildMessage(Customer customer, int daysRemaining) {
        String name = Optional.ofNullable(customer.getName())
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse("üzvümüz");

        if (daysRemaining == 0) {
            return String.format(
                    "Hörmətli %s. 888 Boxing Club üzvlüyünüz bu gün bitir.",
                    name
            );
        }

        return String.format(
                "Hörmətli %s. 888 Boxing Club üzvlüyünüzün bitməsinə %d gün qalıb.",
                name,
                daysRemaining
        );
    }

    private LocalDate parseExpiryDate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String value = rawValue.trim();

        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(BAKU_ZONE).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        if (value.length() >= 10) {
            String firstTen = value.substring(0, 10);
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(firstTen, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        System.err.println("Üzvlük bitmə tarixi oxunmadı: " + value);
        return null;
    }
}
