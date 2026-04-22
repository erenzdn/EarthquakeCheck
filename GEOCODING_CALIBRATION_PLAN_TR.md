# Geocoding Min-Match-Score Kalibrasyon Plani

Bu not, `geocoding.nominatim.min-match-score` esigini olculebilir sekilde kalibre etmek icin uygulanacak adimlari tanimlar.

## Konfigurasyon

- Ana konfigurasyon:
  - `geocoding.nominatim.min-match-score=${GEOCODING_NOMINATIM_MIN_MATCH_SCORE:0.45}`
- Ortam bazli override:
  - Dev: `GEOCODING_NOMINATIM_MIN_MATCH_SCORE=0.40` (baslangic)
  - Prod: `GEOCODING_NOMINATIM_MIN_MATCH_SCORE=0.55` (baslangic)

## Toplanacak Metrikler (1 Hafta)

Structured log alanlari:

- `addressHash` (PII-safe, SHA-256 kisaltilmis)
- `bestScore`
- `threshold`
- `decision` (`accepted` | `fallback_low_score` | `fallback_no_result`)
- `selectedType`
- `selectedClass`
- `usedDefaultCoordinates`

Gunluk / haftalik raporlanacak KPI'lar:

1. Toplam geocoding istegi
2. `accepted` orani
3. `fallback_low_score` orani
4. `fallback_no_result` orani

## Manuel Ornekleme

Haftalik loglardan rasgele secim yap:

- `accepted` kayitlardan 30 ornek
- `fallback` (low_score + no_result) kayitlardan 30 ornek

Her kayit icin:

- Beklenen koordinat tutarliligi (dogru/yanlis)
- Kritik yanlis eslesme var mi?
- Fallback gercekten gerekli miydi?

## Esik Ayar Stratejisi

Iteratif ayar adimi `0.05`:

- Fallback orani gereksiz yuksekse: esigi `0.05` dusur
- Yanlis eslesme artarsa: esigi `0.05` yuksel

Degisiklikten sonra en az 2-3 gun yeniden olcum yap, sonra ikinci adimi karar ver.

## Baslangic Esik Onerisi

- `DEV min-match-score = 0.40`
- `PROD min-match-score = 0.55`

Gerekce:

- Dev ortaminda daha fazla adayin kabul edilmesi, test ve veri gozlemi hizini artirir.
- Prod ortaminda yanlis pozitifleri azaltmak icin daha korumaci esik kullanilir.
