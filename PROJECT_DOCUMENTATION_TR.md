# EarthquakeCheck - Detayli Proje Dokumantasyonu

## 1) Projenin Amaci
`EarthquakeCheck`, bina bilgilerini alarak deprem riski acisindan bir degerlendirme cikarmayi hedefleyen bir Spring Boot REST API projesidir. Mevcut durumda uygulama temel katmanlariyla (controller, service, repository, entity, DTO) kurulmus bir iskelet yapidadir; asil risk degerlendirme algoritmasi icin servis katmaninda yer ayrilmistir.

Bu yapi, deprem dayanikliligi degerlendirmesini adim adim gelistirmek icin uygun bir temel saglar:
- API ile veri alma
- Is kurallari/algoritma ile skorlama
- Sonuc uretme ve kalicilastirma
- Gerekirse frontend ve harita sistemleriyle entegre calisma

---

## 2) Kullanilan Teknolojiler

## 2.1 Programlama Dili ve Platform
- **Java 17**
- **Spring Boot 3.4.5**
- **Maven** (yapi ve bagimlilik yonetimi)

## 2.2 Spring Modulleri
- `spring-boot-starter-web`: REST endpointleri icin
- `spring-boot-starter-validation`: girdi dogrulama icin (simdilik aktif validasyon anotasyonlari eklenmemis)
- `spring-boot-starter-data-jpa`: ORM ve repository katmani
- `spring-boot-starter-test`: test altyapisi
- `spring-boot-devtools`: gelistirme kolayligi

## 2.3 Veritabani
- **H2 In-Memory Database**
  - Baglanti: `jdbc:h2:mem:earthquakecheck`
  - Uygulama kapaninca bellek verisi silinir (kalici saklama degildir).

## 2.4 Yardimci Kutuphaneler
- **Lombok** (`@Data` gibi anotasyonlarla boilerplate kod azaltma)

---

## 3) Proje Mimarisi
Proje katmanli bir yapi izler:

1. **Controller Katmani**
   - HTTP isteklerini alir
   - DTO ile service katmanina aktarir
2. **Service Katmani**
   - Is kurallari ve algoritma burada calisir
3. **Repository Katmani**
   - Sonuclarin/verilerin veritabani islemleri
4. **Model/Entity Katmani**
   - JPA ile tablo karsiligi veri siniflari
5. **DTO Katmani**
   - API giris/cikis veri tasima nesneleri

Bu ayirim sayesinde kod bakimi, test yazimi ve algoritma gelistirme daha duzenli yapilabilir.

---

## 4) Paket ve Sinif Bazli Detayli Dokum

## 4.1 Uygulama Giris Noktasi
- `EarthquakeCheckApplication`
  - `@SpringBootApplication` ile Spring uygulamasini baslatir.
  - Projenin bootstrapping sinifidir.

## 4.2 Controller Katmani
- `controller/BuildingController`
  - Base path: `/api/building`
  - Endpoint: `POST /evaluate`
  - Girdi: `BuildingRequest` (JSON)
  - Cikti: `EvaluationResult` (JSON)
  - `@CrossOrigin(origins = "*")` ile tum originlere aciktir.

### Davranis
Controller sadece istegi alip `EvaluationService`'e iletir. Is kurali controller'da tutulmamis; bu dogru bir ayrimdir.

## 4.3 Service Katmani
- `service/EvaluationService`
  - `evaluateBuilding(BuildingRequest request)` metod kontratini tanimlar.

- `service/impl/EvaluationServiceImpl`
  - Servis implementasyonu bulunur.
  - Mevcut durumda `return null` doner.
  - Kod icinde PGA alma, siniflandirma ve degerlendirme algoritmasi eklenecegi belirtilmis.

### Teknik Yorum
Bu sinif projenin asil zeka katmanidir. Deprem risk algoritmasi burada isleyecek sekilde tasarlanmistir.

## 4.4 DTO Katmani
- `DTO/BuildingRequest`
  - Alanlar:
    - `yearBuilt` (int)
    - `floorCount` (int)
  - API'den gelen girdi modelidir.

- `DTO/EvaluationResult`
  - Alanlar:
    - `id` (Long)
    - `result` (String)
    - `message` (String)
  - Hem API cikisi hem de JPA entity olarak kullanilmistir.

### Teknik Yorum
`EvaluationResult` sinifinin hem DTO hem entity amaciyla kullanilmasi kucuk projelerde hiz kazandirir; ancak buyudukce API modeli ile veritabani modeli ayrilmasi daha temiz bir mimari saglar.

## 4.5 Model Katmani
- `model/Building`
  - JPA entity
  - Alanlar:
    - `id`
    - `address`
    - `yearBuilt`
    - `buildingType`
    - `latitude`
    - `longitude`

### Teknik Yorum
Bu entity su an endpoint akisinda aktif kullanilmiyor; ileride bina kaydi, konum bazli analiz ve tarihsel takip icin kritik olabilir.

## 4.6 Repository Katmani
- `repository/EvaluationResultRepository`
  - `JpaRepository<EvaluationResult, Long>`
  - Sonuclarin veritabani islemlerini saglar.
  - Mevcut akista servis tarafinda henuz kullanilmiyor.

## 4.7 Test Katmani
- `EarthquakeCheckApplicationTests`
  - Sadece `contextLoads` testi var.
  - Bu test uygulamanin Spring context'inin acildigini dogrular.

---

## 5) API Dokumantasyonu

## 5.1 Endpoint
- **Method:** `POST`
- **URL:** `http://localhost:8081/api/building/evaluate`
- **Content-Type:** `application/json`

## 5.2 Ornek Request
```json
{
  "yearBuilt": 1999,
  "floorCount": 6
}
```

## 5.3 Beklenen Response (Hedeflenen Yapi)
```json
{
  "id": 1,
  "result": "ORTA_RISK",
  "message": "Bina icin guclendirme ve detayli muhendislik analizi onerilir."
}
```

> Not: Mevcut implementasyonda servis `null` dondurdugu icin su an bu response uretilmez.

---

## 6) Mevcut Ozellikler
- Spring Boot tabanli REST API altyapisi
- Katmanli mimari iskeleti (controller/service/repository)
- H2 in-memory veritabani baglantisi
- JPA entity/repository tanimlari
- CORS acik endpoint
- Test altyapisinin temel kurulumu

---

## 7) Eksik / Gelistirilmesi Gereken Ozellikler
- `EvaluationServiceImpl` icinde deprem risk algoritmasinin gerceklenmesi
- Sonucun repository uzerinden kalicilastirilmasi
- Girdi validasyonu (`@NotNull`, `@Min`, `@Max` vb.)
- Hata yonetimi (`@ControllerAdvice`, standard error response)
- Birim testleri ve entegrasyon testlerinin yazilmasi
- API dokumani (Swagger/OpenAPI) eklenmesi

---

## 8) Algoritmalar (Mevcut ve Hedeflenen)

## 8.1 Mevcut Durum
Kod tabaninda deprem riskini hesaplayan calisan bir algoritma **henuz yoktur**. Yalnizca algoritma eklenecegine dair yer tutucu yorum vardir.

## 8.2 Hedeflenen Algoritma Bilesenleri
Kod yorumuna gore planlanan akis:
1. **PGA (Peak Ground Acceleration) verisi alma**
2. **Siniflandirma**
3. **Nihai degerlendirme**

## 8.3 Onerilen Ornek Skorlama Algoritmasi
Asagidaki yaklasim mevcut mimariye dogrudan uyarlanabilir:

1. **Girdi Normalizasyonu**
   - `yearBuilt` ve `floorCount` degerlerini aralik bazli puana cevir.
2. **PGA Risk Katkisi**
   - Konuma bagli PGA degerini risk puanina map et.
3. **Toplam Risk Skoru**
   - `riskScore = ageScore * w1 + floorScore * w2 + pgaScore * w3`
4. **Esik Bazli Siniflandirma**
   - 0-30: Dusuk
   - 31-60: Orta
   - 61-100: Yuksek
5. **Aciklayici Mesaj Uretimi**
   - Sonuca gore aksiyon metni don.

## 8.4 Onerilen Algoritma Karmasikligi
- Zaman karmasikligi: **O(1)** (sabit sayida ozellik ile hesaplama)
- Alan karmasikligi: **O(1)**

Bu nedenle API cagri basina performans maliyeti dusuktur.

---

## 9) Veri Modeli ve Veritabani Notlari

## 9.1 `Building` Entity
Potansiyel kullanim alanlari:
- Bina envanteri olusturma
- Konuma gore risk haritalama
- Zaman icinde yeniden degerlendirme

## 9.2 `EvaluationResult` Entity
Mevcutta degerlendirme sonucunu tutmak icin uygun bir temel yapi:
- `result`: risk sinifi
- `message`: aciklayici yorum

Gerekli olabilecek ek alanlar:
- `score` (sayisal risk puani)
- `evaluatedAt` (tarih-saat)
- `buildingId` veya bina ozeti
- `pgaValue`

---

## 10) Uygulama Konfigurasyonu
`application.properties`:
- Uygulama adi: `EarthquakeCheck`
- Port: `8081`
- Veritabani: H2 in-memory
- JPA schema stratejisi: `ddl-auto=update`

### Yorum
Gelistirme asamasinda uygun, uretimde:
- kalici veritabani (PostgreSQL/MySQL)
- profile bazli ayarlar (`application-dev`, `application-prod`)
- migration araci (Flyway/Liquibase) onerilir.

---

## 11) Guvenlik ve Operasyonel Notlar
- Su an endpoint tum originlere acik (`*` CORS).
- Kimlik dogrulama/yetkilendirme mekanizmasi yok.
- Uretim ortami icin oneriler:
  - Spring Security + JWT/OAuth2
  - CORS origin sinirlama
  - Rate limiting
  - Request/response loglama ve gozlemlenebilirlik (Actuator + metrics)

---

## 12) Test Stratejisi (Oneri)
- **Birim testleri**
  - `EvaluationServiceImpl` icin esik deger, sinir deger ve gecersiz girdi senaryolari
- **Web katmani testleri**
  - `@WebMvcTest` ile endpoint contract testleri
- **Entegrasyon testleri**
  - H2 ile repository + service birlikte test

Kritik senaryolar:
- Cok eski bina + yuksek kat + yuksek PGA => yuksek risk
- Yeni bina + az kat + dusuk PGA => dusuk risk
- Negatif yas/kat gibi gecersiz girdiler => 400 Bad Request

---

## 13) Yol Haritasi (Oncelik Sirasi)
1. `evaluateBuilding` algoritmasini calisir hale getirme
2. Request validasyonu ve global hata yonetimi
3. Sonuc kaydi (repository save)
4. Test kapsaminda buyume
5. OpenAPI dokumani
6. Guvenlik katmani
7. Harici PGA servis entegrasyonu

---

## 14) Son Degerlendirme
`EarthquakeCheck` su anda dogru teknolojilerle baslatilmis, katmanli mimari prensiplerine uygun bir backend temelidir. Projenin en kritik eksigi, servis katmaninda algoritmanin henuz uygulanmamis olmasidir. Bu adim tamamlandiginda proje, deprem risk degerlendirmesi icin pratik bir API'ye donusebilir ve sonraki asamada veriye dayali daha gelismis analizlere genisletilebilir.

