# Ortak Alışveriş

Aile üyeleri ve ev arkadaşlarının alışveriş ihtiyaçlarını birlikte yönetmesi amacıyla geliştirilen web uygulaması.

Proje geliştirme aşamasındadır. Mevcut sürümde alışveriş ekranı, Java ve Spring Boot ile geliştirilen REST API üzerinden çalışır. Kullanıcı hesapları ve eve özel liste paylaşımı henüz eklenmemiştir.

## Özellikler

- Ürün adı ve miktarı ekleme
- Alınacak ve alınmış ürünleri ayrı görüntüleme
- Ürünleri alındı olarak işaretleme
- Alınmış ürünleri tekrar alınacaklara taşıma
- Ürün silme
- Ürün sayılarını görüntüleme
- Mobil ekranlara uyumlu arayüz
- Sunucu istekleri için yüklenme ve hata mesajları
- Sunucu tarafında ürün bilgilerini doğrulama

## Kullanılan Teknolojiler

- HTML
- CSS
- JavaScript ve Fetch API
- Java 25
- Spring Boot 4.1.1
- Spring Web
- Jakarta Validation
- Maven
- Git ve GitHub
- PostgreSQL
- Spring Data JPA

## Gereksinimler

- JDK 25
- Projeyi indirmek için Git
- PostgreSQL
- ortak_alisveris veritabanı

Projede Maven Wrapper bulunduğundan ayrıca Maven kurulması gerekmez.
İlk çalıştırmada gerekli bağımlılıkların indirilmesi için internet bağlantısı gerekir.

## Kurulum ve Çalıştırma

### 1. Depoyu indirin

```powershell
git clone https://github.com/ErayKeskinbas/ortak-alisveris.git
```

### 2. Sunucu klasörüne geçin

```powershell
cd ortak-alisveris/backend
```

### 3. Uygulamayı başlatın

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

### 4. Tarayıcıda açın

Uygulama:

http://localhost:8080

Ürün API'si:

http://localhost:8080/api/products

Arayüz Spring Boot tarafından sunulduğu için Live Server çalıştırmak gerekmez.
Uygulamayı kullanırken sunucunun çalıştığı terminal açık kalmalıdır.

Sunucuyu durdurmak için terminalde `Ctrl + C` kullanın.

## Proje Yapısı

```text
ortak-alisveris/
├── backend/
│   ├── .mvn/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/eray/ortakalisveris/
│   │   │   │   ├── BackendApplication.java
│   │   │   │   └── ProductController.java
│   │   │   └── resources/
│   │   │       ├── static/
│   │   │       │   ├── index.html
│   │   │       │   ├── style.css
│   │   │       │   └── app.js
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── index.html
├── style.css
├── app.js
├── .gitignore
└── README.md
```

Spring Boot, `backend/src/main/resources/static` klasöründeki arayüz dosyalarını kullanır.

Ana klasördeki HTML, CSS ve JavaScript dosyaları ilk arayüz geliştirme aşamasından kalmıştır. Çalışan sunucunun arayüzünü değiştirmek için `static` klasöründeki dosyalar düzenlenmelidir.

## API

| Metot  | Adres                | Açıklama                         |
| ------ | -------------------- | -------------------------------- |
| GET    | `/api/products`      | Tüm ürünleri listeler            |
| POST   | `/api/products`      | Yeni ürün ekler                  |
| PATCH  | `/api/products/{id}` | Ürünün alındı durumunu günceller |
| DELETE | `/api/products/{id}` | Ürünü siler                      |

### Ürün ekleme isteği

`POST /api/products`

```json
{
  "name": "Süt",
  "quantity": "2 litre"
}
```

Ürün adı zorunludur ve en fazla 80 karakter olabilir.
Miktar isteğe bağlıdır ve en fazla 40 karakter olabilir.

### Örnek ürün yanıtı

```json
{
  "id": 1,
  "name": "Süt",
  "quantity": "2 litre",
  "completed": false
}
```

### Ürünü alındı olarak işaretleme

`PATCH /api/products/1`

```json
{
  "completed": true
}
```

Ürünü tekrar alınacaklara taşımak için `completed` değeri `false` gönderilir.

## Elle Kontrol Adımları

1. Ürün adı ve miktarı girerek listeye ekleyin.
2. Ürünün `/api/products` adresinde göründüğünü kontrol edin.
3. Ürünü alındı olarak işaretleyin.
4. Geri al düğmesiyle alınacaklara taşıyın.
5. Sayfayı yenileyip ürünün korunduğunu kontrol edin.
6. Ürünü silin ve API listesinden de kaldırıldığını kontrol edin.

## Planlanan Geliştirmeler

- Sunucu kodunu controller, service ve repository katmanlarına ayırma
- Kullanıcı kaydı ve giriş
- Ev oluşturma ve davet koduyla katılma
- Eve özel alışveriş listeleri ve erişim kontrolü
- Gerçek zamanlı liste güncellemeleri
- Otomatik testler
- Uygulamayı yayına alma

## Veri Saklama

Ürünler PostgreSQL veritabanında saklanır.

- Sayfa yenilendiğinde ve uygulama sunucusu yeniden başlatıldığında ürünler korunur.
- Uygulamanın çalışması için PostgreSQL sunucusu açık olmalıdır.
- Aynı uygulama sunucusuna bağlanan tarayıcılar aynı listeyi kullanır.
- Diğer tarayıcılardaki değişiklikleri görmek için sayfayı yenilemek gerekir.
- Kullanıcı hesapları ve eve özel erişim kontrolü henüz bulunmaz.

## Geliştirici

[Eray Keskinbaş](https://github.com/ErayKeskinbas)
