# EarthquakeCheck

## Kurulum

### Çevresel Değişkenler

Bu proje çalıştırılmadan önce aşağıdaki çevresel değişkenlerin tanımlanması gerekmektedir:

**Veritabanı Ayarları:**
```
DB_URL=jdbc:postgresql://localhost:5432/veritabani_adi
DB_USERNAME=kullanici_adi
DB_PASSWORD=sifre
```

**Google Maps API:**
```
GOOGLE_API_KEY=google_api_anahtari
```

### Çalıştırma

Geliştirme ortamında çalıştırmak için:
```
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

Üretim ortamında çalıştırmak için:
```
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

### Docker Kullanımı

Docker ile çalıştırmak için:
```
docker-compose up -d
```
