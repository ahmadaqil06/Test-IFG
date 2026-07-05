# JsonPlaceholder_Kafka_Test — Katalon Studio Project

Project uji kemampuan teknis (test code) menggunakan **Katalon Studio**, mencakup:

1. **Pengujian RESTful API** ke [JSONPlaceholder](https://jsonplaceholder.typicode.com) — Katalon berperan sebagai **producer** (POST/PUT/DELETE — mengirim data) dan **consumer** (GET — membaca data).
2. **Pengujian Kafka** — Katalon bertindak sebagai **consumer**, membaca pesan dari sebuah topic Kafka. Karena broker belum tersedia, disediakan `docker-compose-kafka.yml` untuk menjalankan broker Kafka lokal.

Semua request di-parameterize menggunakan variable Object Repository (`${namaVariable}`), nilainya diambil dari Data Files pada folder **`Data Files/Request`**, dan setiap response dicatat otomatis ke file **Excel** dengan kolom:

| No | Endpoint | Request Payload | Status Code | Response Body | Timestamp |
|----|----------|------------------|-------------|----------------|-----------|

---

## Struktur Project

```
JsonPlaceholder_Kafka_Test/
├── Object Repository/API Requests/     # Request object, pakai ${variable}
│   ├── POST_Create_Post.rs             # producer
│   ├── PUT_Update_Post.rs              # producer
│   ├── DELETE_Post.rs                  # producer
│   ├── GET_Post_By_Id.rs               # consumer
│   └── GET_Comments_By_PostId.rs       # consumer
├── Data Files/Request/                 # Data test untuk tiap request (CSV)
│   ├── CreatePostData.csv
│   ├── UpdatePostData.csv
│   ├── DeletePostData.csv
│   ├── GetPostData.csv
│   └── GetCommentsData.csv
├── Test Cases/
│   ├── API/
│   │   ├── TC01_Create_Post_Producer
│   │   ├── TC02_Update_Post_Producer
│   │   ├── TC03_Delete_Post_Producer
│   │   ├── TC04_Get_Post_Consumer
│   │   └── TC05_Get_Comments_Consumer
│   └── Kafka/
│       └── TC06_Kafka_Consumer_Test
├── Keywords/
│   ├── excel/ExcelHelper.groovy        # custom keyword export ke Excel
│   └── kafka/KafkaHelper.groovy        # custom keyword Kafka consumer
├── Test Suites/
│   ├── TS_API_Tests.ts
│   └── TS_Kafka_Tests.ts
├── Profiles/default.glbl               # Global Variable (config Kafka)
├── docker-compose-kafka.yml            # Kafka broker lokal (karena belum ada broker)
├── kafka-test-producer.py              # helper kirim pesan uji ke Kafka
└── Reports/Excel/                      # hasil laporan Excel akan tersimpan di sini
```

---

## Cara Membuka Project di Katalon Studio

1. Buka **Katalon Studio** → `File > Open Project` → arahkan ke folder `JsonPlaceholder_Kafka_Test` (folder yang berisi file `.prj`).
2. Jika Katalon meminta re-index / konversi, biarkan proses selesai — Katalon akan otomatis membaca struktur Object Repository, Test Cases, Data Files, dan Keywords yang sudah dibuat.

## Setup Dependency (WAJIB sebelum run)

Project ini butuh 2 library eksternal. Tambahkan lewat menu **Project > Settings > Dependencies > Maven**:

| Group ID | Artifact ID | Version | Kegunaan |
|---|---|---|---|
| `org.apache.poi` | `poi-ooxml` | `5.2.5` | Menulis file Excel (.xlsx) |
| `org.apache.kafka` | `kafka-clients` | `3.7.0` | Kafka consumer client |

Setelah ditambahkan, klik **Update Dependencies** lalu tunggu Katalon mengunduh library-nya.

---

## Menjalankan Test — RESTful API (Producer & Consumer)

Tidak perlu setup tambahan, langsung jalankan salah satu:

- `Test Cases/API/TC01_Create_Post_Producer` — POST (producer)
- `Test Cases/API/TC02_Update_Post_Producer` — PUT (producer)
- `Test Cases/API/TC03_Delete_Post_Producer` — DELETE (producer)
- `Test Cases/API/TC04_Get_Post_Consumer` — GET (consumer)
- `Test Cases/API/TC05_Get_Comments_Consumer` — GET (consumer)

Atau jalankan sekaligus semuanya lewat Test Suite `Test Suites/TS_API_Tests`.

Setiap test case akan:
1. Membaca data dari file CSV yang sesuai di `Data Files/Request`.
2. Mem-bind nilai data tersebut ke variable `${...}` pada Object Repository.
3. Mengirim request ke `https://jsonplaceholder.typicode.com`.
4. Mencatat hasilnya (endpoint, payload, status code, response, timestamp) ke file Excel di `Reports/Excel/`.

## Menjalankan Test — Kafka Consumer

Karena belum ada broker Kafka yang tersedia, gunakan Kafka lokal via Docker:

```bash
# 1. Jalankan broker Kafka lokal
docker compose -f docker-compose-kafka.yml up -d

# 2. (Opsional) kirim beberapa pesan uji ke topic "test-topic"
pip install kafka-python
python kafka-test-producer.py
```

Lalu jalankan `Test Cases/Kafka/TC06_Kafka_Consumer_Test` di Katalon Studio. Test case ini akan:
1. Terhubung ke broker (`localhost:9092`, bisa diubah lewat `GlobalVariable.KAFKA_BOOTSTRAP_SERVERS` di Profile `default`).
2. Subscribe ke topic (`test-topic`, bisa diubah lewat `GlobalVariable.KAFKA_TOPIC`).
3. Membaca (consume) semua pesan yang ada.
4. Mencatat setiap pesan ke Excel `Reports/Excel/Report_KafkaConsumer.xlsx`.

Jika ingin memakai broker Kafka lain (bukan lokal), cukup ubah value `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_TOPIC`, dan `KAFKA_GROUP_ID` di **Profiles > default**, tidak perlu ubah script.

---

## Format Laporan Excel

Setiap test case menghasilkan 1 file Excel terpisah di `Reports/Excel/`:

- `Report_CreatePost.xlsx`
- `Report_UpdatePost.xlsx`
- `Report_DeletePost.xlsx`
- `Report_GetPost.xlsx`
- `Report_GetComments.xlsx`
- `Report_KafkaConsumer.xlsx`

Dengan kolom: **No, Endpoint, Request Payload, Status Code, Response Body, Timestamp**.

---

## Catatan

- Dataset/data uji (file CSV di `Data Files/Request`) dapat diubah bebas sesuai kebutuhan.
- Struktur ini dirancang agar mudah dikembangkan: menambah request baru cukup tambah `.rs` di Object Repository, data CSV baru di `Data Files/Request`, dan test case baru yang memanggil `ExcelHelper`.
