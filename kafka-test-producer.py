"""
Helper script untuk mengirim beberapa pesan uji ke Kafka topic lokal,
supaya TC06_Kafka_Consumer_Test di Katalon ada data yang bisa dikonsumsi.

Cara pakai:
    pip install kafka-python
    python kafka-test-producer.py

Pastikan broker Kafka lokal sudah jalan (docker-compose-kafka.yml) sebelum
menjalankan script ini.
"""

import json
import time
from kafka import KafkaProducer

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "test-topic"

producer = KafkaProducer(
    bootstrap_servers=BOOTSTRAP_SERVERS,
    value_serializer=lambda v: json.dumps(v).encode("utf-8"),
)

sample_messages = [
    {"id": 1, "message": "Pesan uji pertama untuk Katalon Kafka Consumer"},
    {"id": 2, "message": "Pesan uji kedua untuk Katalon Kafka Consumer"},
    {"id": 3, "message": "Pesan uji ketiga untuk Katalon Kafka Consumer"},
]

for msg in sample_messages:
    producer.send(TOPIC, value=msg)
    print(f"Terkirim ke topic '{TOPIC}': {msg}")
    time.sleep(0.5)

producer.flush()
producer.close()
print("Selesai mengirim semua pesan uji.")
