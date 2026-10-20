# Boxing Club backend — FCM yerləşdirmə

Bu dəyişiklik mobil `888 Boxing Club 1.0.1 (2)` ilə birlikdə istifadə olunur. Backend lokal qovluqda dəyişdirilib, VPS-ə yerləşdirilməyib.

## Firebase server girişi

Mobil fayllarla eyni Firebase layihəsindən (`bakuappcraft-notifications`) **Firebase Admin service-account JSON** lazımdır. Firebase → Project settings → Service accounts bölməsindən əldə olunur. Eyni layihə üçün Zakaz/Ələhbər serverində istifadə etdiyiniz, FCM göndərmə icazəsi olan service account da uyğundur. JSON-u mobil layihəyə və Git-ə qoymayın.

Mövcud Docker işə salma konfiqurasiyasına bunları əlavə edin (qalan port, DB, şəbəkə, TLS və volume ayarlarınızı saxlayın):

```sh
--mount type=bind,src=/root/secrets/firebase-admin.json,dst=/run/secrets/firebase-admin.json,readonly \
-e GOOGLE_APPLICATION_CREDENTIALS=/run/secrets/firebase-admin.json \
-e BOXING_FCM_ENABLED=true \
-e BOXING_FCM_PROJECT_ID=bakuappcraft-notifications
```

`/root/secrets/firebase-admin.json` sizin VPS-də saxladığınız real service-account faylı olmalıdır. Hostda yalnız lazım olan istifadəçi onu oxusun. Bu fayl `.p8`, `google-services.json` və `GoogleService-Info.plist` fayllarından fərqlidir.

Firebase Cloud Messaging API (HTTP v1) aktiv, service account-da FCM göndərmə icazəsi və VPS-dən Google OAuth/FCM ünvanlarına HTTPS çıxışı olmalıdır.

## Serveri yeniləmək

1. Mövcud DB-nin normal ehtiyat nüsxəsini saxlayın.
2. Bu backend kodunu VPS-də layihənizə köçürün. Köhnə OneSignal service faylı çıxarılıb; yalnız yeni faylları əlavə etmək köhnə faylı silmir.
3. Mövcud qaydanızla server JAR-ını və Docker image-ni hazırlayıb konteyneri yenidən yaradın; yuxarıdakı env/volume ayarları yeni konteynerə tətbiq olunsun. Lokal yoxlama üçün Java 17 ilə `./mvnw test` istifadə olunub.
4. `ddl-auto=update` yeni `boxing_push_device` və `boxing_push_delivery` cədvəllərini yaradır. DB istifadəçisinin buna icazəsi olmalıdır. Mövcud üzv/bildiriş cədvəlləri saxlanılır.
5. Firebase-də iOS **Boxing Club** qeydində mövcud team-scoped APNs açarını yükləyin. Mobil layihənin `docs/store-release-az.md` sənədində dəqiq Key ID/Team ID var.
6. Tətbiqə daxil olun və admin paneldən ayrıca test bildirişi yaradın. Köhnə OneSignal versiyasında qalmış sessiya varsa, bir dəfə çıxıb yenidən daxil olun.

## Davranış və diaqnostika

- `PUT /api/v10/push/devices`: cihazı yeniləyir; şəxsi üzvlük qeydiyyatı `Authorization: Basic base64(customerId:password)` ilə yoxlanır. Sorğu və loglarda şifrə/token göstərməyin. HTTPS saxlanmalıdır.
- `POST /api/v10/push/revoke`: öz quraşdırmasını deaktiv edir. Mobil çıxış tokeni də yeniləyir.
- `POST /api/v10/notification`: yeni elan üçün durable FCM işləri yaradır. `customerId=null`, boş və ya `all` ümumi, rəqəmli üzv ID-si şəxsi bildirişdir.
- Mövcud üzvlük scheduler-i Bakı vaxtı ilə əvvəlki qaydada işləyir. Şəxsi xatırlatma yalnız həmin üzvün cihazlarına gedir.
- Worker hər 5 saniyədə ən çox 50 hazır işi götürür. Müvəqqəti xətalara artan fasilə ilə maksimum 8 cəhd edir. Bildiriş 24 saatdan sonra göndərilmir. Hesabı dəyişmiş/çıxmış/deaktiv cihaz üçün şəxsi köhnə iş ləğv olunur.
- `notification.pushSent=true` göndərişin növbəyə qəbul olunduğunu göstərir; çatdırılmanın vəziyyəti `boxing_push_delivery.state` (`PENDING/SENT/FAILED/EXPIRED`) cədvəlindədir. `SENT` FCM-in qəbuludur, ekranda görünməyə dair cihaz qəbzi deyil.
- Xətalar `FCM delivery failed` kimi delivery ID, state və code ilə loglanır; token və şifrələr loglanmır. Credentials/API/APNs ayarları işlək server və cihazda ayrıca təsdiqlənməlidir.
- `BOXING_FCM_ENABLED=false` push worker və növbələməni söndürür; tətbiqdaxili bildiriş siyahısı işləməyə davam edir.
