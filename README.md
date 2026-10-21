## Sunucu bağlantısı

Arayüz, Spring Boot REST API üzerinden ürünleri listeler, ekler,
alındı durumunu değiştirir ve siler.

Veriler şu anda sunucunun belleğinde tutulur.
Sunucu yeniden başlatıldığında liste sıfırlanır.

## Sunucuyla çalıştırma

Gereksinim: JDK 25

Terminalde backend klasörüne geçip çalıştırın:

    .\mvnw.cmd spring-boot:run

Tarayıcıdan http://localhost:8080 adresini açın.

Sunucunun sunduğu arayüz dosyaları:
backend/src/main/resources/static
