# IqTestWeb

IqTestWeb - bu foydalanuvchilarning intellektual salohiyatini (IQ) aniqlash uchun mo'ljallangan veb-ilova. Loyiha Spring Boot freymvorki asosida yaratilgan bo'lib, xavfsizlik, ma'lumotlar bazasi bilan ishlash va tashqi servislar (OAuth2) bilan integratsiyani o'z ichiga oladi.

## 🚀 Texnologiyalar

Loyiha quyidagi texnologiyalar yordamida ishlab chiqilgan:

*   **Backend:** Java 17+, Spring Boot 3.x
*   **Security:** Spring Security (OAuth2: Google & GitHub)
*   **Session Management:** Spring Session (Optional)
*   **Database:** PostgreSQL
*   **ORM:** Spring Data JPA (Hibernate)
*   **Frontend:** Thymeleaf (HTML/CSS)
*   **Logging:** SLF4J / Logback

## 📋 Xususiyatlari

*   **Smart Registration:** OAuth2 orqali bir marta bosish bilan ro'yxatdan o'tish va avtomatik profil yaratish.
*   **IQ Testlar:** Testlarni boshqarish va topshirish imkoniyati.
*   **Rasm yuklash:** Testlar yoki foydalanuvchi profili uchun rasmlarni yuklash (Multipart file upload).
*   **QR Code:** QR kodlar orqali testlarga tezkor kirish (Base URL orqali).
*   **Ma'lumotlar bazasi:** PostgreSQL bilan to'liq integratsiya va avtomatik jadval yangilanishlari (ddl-auto: update).

## 🛠 O'rnatish va Sozlash

Loyihani mahalliy muhitda ishga tushirish uchun quyidagi qadamlarni bajaring:

### 1. Reperozitariyani nusxalash
```bash
git clone https://github.com/your-username/IqTestWeb.git
cd IqTestWeb
```

### 2. Ma'lumotlar bazasini sozlash
PostgreSQL-da `iq_test_db` nomli ma'lumotlar bazasini yarating.

`src/main/resources/application.properties` faylida bazaga ulanish ma'lumotlarini tekshiring:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/iq_test_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. OAuth2 Sozlamalari
Google va GitHub Cloud konsollaridan olingan `Client ID` va `Client Secret` kalitlarini `application.properties` fayliga joylashtiring:
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_SECRET

spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_SECRET
```

### 4. Loyihani ishga tushirish
Maven yordamida loyihani build qiling va ishga tushiring:
```bash
mvn clean install
mvn spring-boot:run
```

Ilova odatda `http://localhost:8080` manzilida ishlaydi.

## 📁 Loyiha Strukturasi

*   `src/main/java` - Java kodlari (Controller, Service, Repository, Entity).
*   `src/main/resources/templates` - Thymeleaf HTML shablonlari.
*   `src/main/resources/application.properties` - Asosiy konfiguratsiya fayli.
*   `uploads/images/` - Yuklangan rasmlar saqlanadigan joy.

## ⚙️ Konfiguratsiya eslatmalari

*   **File Upload:** Maksimal rasm hajmi 10MB qilib belgilangan.
*   **Logging:** Spring Security debug rejimida ishlaydi, bu esa xavfsizlik muammolarini kuzatishga yordam beradi.
*   **JPA:** Hibernate `update` rejimida sozlangan, bazadagi jadvallar avtomatik yaratiladi.

## 📄 Litsenziya

Ushbu loyiha shaxsiy foydalanish va o'rganish uchun mo'ljallangan.