Ortak Alışveriş

[![Java Testleri](https://github.com/ErayKeskinbas/ortak-alisveris/actions/workflows/tests.yml/badge.svg)](https://github.com/ErayKeskinbas/ortak-alisveris/actions/workflows/tests.yml)

Aile üyeleri ve ev arkadaşları için geliştirilen, eve özel ortak alışveriş listesi uygulaması.

Kullanıcılar hesap oluşturabilir, yeni bir ev kurabilir veya davet koduyla mevcut bir eve katılabilir. Aynı evin üyeleri ortak liste üzerinde çalışırken farklı evlerin verileri birbirinden ayrılır.

**[Canlı uygulamayı aç](https://ortak-alisveris.onrender.com)**

## Kullanım

1. Canlı uygulamada **Kayıt ol** bağlantısını açın.
2. Adınızı, e-posta biçiminde bir adresi ve en az 12 karakterli şifrenizi girin.
3. Oluşturduğunuz hesapla giriş yapın.
4. Bir ev oluşturun veya size verilen davet koduyla bir eve katılın.
5. Ürünleri ekleyin, alınanları işaretleyin ve listenizi ev üyeleriyle paylaşın.

E-posta doğrulaması bulunmaz; uygulama e-posta veya doğrulama kodu göndermez. Deneme için `deneme@example.com` gibi bir adres kullanılabilir. Her kullanıcı şu anda yalnızca bir eve üye olabilir.

## Özellikler

- Kullanıcı kaydı, giriş ve çıkış
- BCrypt ile şifre özetleme
- Sunucu tarafında oturum yönetimi ve CSRF koruması
- Ev oluşturma ve davet koduyla katılma
- Eve özel alışveriş listeleri
- Ürün adı ve miktarı ekleme
- Ürünleri alındı olarak işaretleme ve geri alma
- Ürün silme
- PostgreSQL ile kalıcı kayıt
- Sayfa görünürken 5 saniyede bir otomatik liste güncelleme
- Mobil ekranlara uyumlu arayüz
- Flyway ile veritabanı şeması sürümleme
- GitHub Actions ile otomatik test ve derleme

## Teknolojiler

| Katman           | Kullanılan teknolojiler                             |
| ---------------- | --------------------------------------------------- |
| Arayüz           | HTML, CSS, JavaScript, Fetch API                    |
| Sunucu           | Java 25, Spring Boot 4.1.1, Spring Web MVC          |
| Kimlik doğrulama | Spring Security, oturum tabanlı giriş, BCrypt       |
| Veri erişimi     | Spring Data JPA, Hibernate, PostgreSQL              |
| Şema yönetimi    | Flyway                                              |
| Test             | JUnit 5, MockMvc, Spring Security Test, H2          |
| Derleme ve yayın | Maven Wrapper, GitHub Actions, Docker, Render, Neon |

## Uygulama Yapısı

```text
Tarayıcı
   │ HTTP / JSON + oturum çerezi
   ▼
Spring Security
   ▼
Controller → Service → Repository → PostgreSQL
```

Ürün erişiminde ev kimliği istemciden alınmaz. Sunucu, giriş yapan kullanıcının ev üyeliğini belirler ve ürün sorgularını bu eve göre sınırlar. Başka bir eve ait ürünün kimliği gönderilse bile değiştirme ve silme işlemleri reddedilir.

Flyway yeni veritabanlarının tablolarını oluşturur; Hibernate tablo yapısını doğrular.

## Yerel Kurulum

### Gereksinimler

- JDK 25
- PostgreSQL; proje geliştirilirken PostgreSQL 18 kullanılmıştır
- Git

Maven Wrapper projeye dahildir. Ayrıca Maven kurulması gerekmez. İlk derlemede bağımlılıkları indirmek için internet bağlantısı gerekir.

### 1. Depoyu indirin

```powershell
git clone https://github.com/ErayKeskinbas/ortak-alisveris.git
cd ortak-alisveris/backend
```

### 2. Veritabanını oluşturun

pgAdmin üzerinden `ortak_alisveris` adında boş bir veritabanı oluşturun. Alternatif olarak yetkili bir PostgreSQL oturumunda çalıştırın:

```sql
CREATE DATABASE ortak_alisveris;
```

Tabloları elle oluşturmayın. İlk uygulama başlangıcında Flyway oluşturacaktır. Bu adımlar boş bir veritabanına kurulum içindir; önceden tabloları bulunan bir veritabanı ayrıca başlangıç sürümü tanımlamayı gerektirir.

### 3. Bağlantı ayarlarını tanımlayın

Varsayılan yerel adres `jdbc:postgresql://localhost:5432/ortak_alisveris`, kullanıcı adı `postgres` şeklindedir.

PowerShell terminalinde şifrenizi gizli girişle tanımlayın:

```powershell
$dbSecret = Read-Host "PostgreSQL sifreni gir" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $dbSecret).Password
```

Adres veya kullanıcı adını değiştirmek gerekiyorsa aynı terminalde tanımlayın:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/ortak_alisveris"
$env:DB_USERNAME = "postgres"
```

Şifreyi kaynak dosyalarına yazmayın. Ortam değişkenleri bu terminal oturumu için geçerlidir. Yerel HTTP kullanımında `prod` profilini etkinleştirmeyin; bu profil HTTPS oturum çerezlerini zorunlu tutar.

### 4. Başlatın

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux, `DB_PASSWORD` ortam değişkeni tanımlandıktan sonra:

```bash
bash ./mvnw spring-boot:run
```

Tarayıcıda [http://localhost:8080](http://localhost:8080) adresini açın. Arayüz Spring Boot tarafından sunulur; Live Server gerekmez.

## Ortam Değişkenleri

| Değişken                 | Açıklama                                         |
| ------------------------ | ------------------------------------------------ |
| `DB_URL`                 | JDBC biçiminde PostgreSQL bağlantı adresi        |
| `DB_USERNAME`            | Veritabanı kullanıcı adı                         |
| `DB_PASSWORD`            | Veritabanı şifresi                               |
| `PORT`                   | Uygulama portu; varsayılan `8080`                |
| `SPRING_PROFILES_ACTIVE` | İnternet yayını için `prod`; testler için `test` |

`prod` profilinde üç veritabanı değişkeni de gereklidir. Neon bağlantısında JDBC adresi SSL parametresi içermelidir; örnek biçim:

```text
jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
```

## Testler

`backend` klasöründe Windows için:

```powershell
.\mvnw.cmd "-Dspring.profiles.active=test" clean test
```

macOS / Linux için:

```bash
bash ./mvnw -Dspring.profiles.active=test clean test
```

Test profili geçici H2 veritabanını kullanır. Testler için PostgreSQL sunucusu veya gerçek veritabanı şifresi gerekmez. Tablo yapısı Flyway dosyalarından oluşturulur.

Mevcut 22 testin kapsadığı başlıca davranışlar:

- Kullanıcının yalnızca kendi evinin ürünlerini görmesi
- Aynı evin üyelerinin ortak listeye erişmesi
- Başka evin ürününü değiştirme ve silmenin engellenmesi
- Eve üye olmayan kullanıcının ürün listesine erişememesi
- Şifrelerin BCrypt özeti olarak kaydedilmesi
- E-posta normalleştirme ve aynı adresle tekrar kaydın engellenmesi
- Ev oluşturma, davetle katılma ve ikinci eve üyeliğin engellenmesi
- Doğru ve yanlış şifreyle giriş
- Giriş yapılmadan korumalı API erişiminin engellenmesi
- CSRF doğrulaması olmadan ürün eklemenin reddedilmesi
- Çıkışta oturumun geçersiz kılınması

GitHub Actions, `main` dalına gönderimlerde ve bu dala açılan pull request'lerde testleri çalıştırır ve uygulamayı paketler.

## API Özeti

| Metot  | Adres                 | İşlev                                   |
| ------ | --------------------- | --------------------------------------- |
| GET    | `/api/csrf`           | CSRF doğrulama bilgisini alır           |
| POST   | `/api/auth/register`  | Kullanıcı oluşturur                     |
| POST   | `/login`              | Form verileriyle oturum açar            |
| POST   | `/logout`             | Oturumu kapatır                         |
| GET    | `/api/household`      | Kullanıcının ev üyeliğini getirir       |
| POST   | `/api/household`      | Ev oluşturur                            |
| POST   | `/api/household/join` | Davet koduyla eve katılır               |
| GET    | `/api/products`       | Kullanıcının evindeki ürünleri listeler |
| POST   | `/api/products`       | Eve ürün ekler                          |
| PATCH  | `/api/products/{id}`  | Ürünün alındı durumunu değiştirir       |
| DELETE | `/api/products/{id}`  | Ürünü siler                             |

Korumalı adresler giriş oturumu gerektirir. Veri değiştiren isteklerde CSRF doğrulaması gerekir. Giriş isteği form verisi, kayıt ve ürün işlemleri JSON kullanır.

## Proje Klasörleri

```text
.github/workflows/                 GitHub Actions
backend/Dockerfile                 Yayın için Docker tanımı
backend/src/main/java/             Controller, service, repository ve entity sınıfları
backend/src/main/resources/static/  Çalışan arayüz
backend/src/main/resources/db/migration/  Flyway SQL dosyaları
backend/src/test/java/              Otomatik testler
backend/src/test/resources/         Geçici veritabanı ayarları
```

Çalışan arayüzü değiştirmek için `backend/src/main/resources/static` altındaki dosyaları düzenleyin. Depo kökündeki HTML, CSS ve JavaScript dosyaları ilk geliştirme aşamasından kalan kopyalardır.

## Yayın ve Mevcut Sınırlar

- Uygulama Render Free, veritabanı Neon Free üzerinde çalışır.
- Ücretsiz servis uykuya geçtiğinde ilk açılış gecikebilir.
- Yayındaki veritabanı yerel geliştirme veritabanından ayrıdır.
- Oturumlar sunucu belleğinde tutulur; yeniden başlatma veya yeniden yayın sonrası giriş yapmak gerekebilir. Ürünler veritabanında korunur.
- Liste güncellemeleri WebSocket yerine 5 saniyelik aralıklarla sorgulanır. Arka plandaki sekmeler sorgulamayı duraklatır.
- E-posta doğrulama, şifre sıfırlama, evden ayrılma ve davet kodu yenileme henüz bulunmaz.

## Geliştirici

[Eray Keskinbaş](https://github.com/ErayKeskinbas)
