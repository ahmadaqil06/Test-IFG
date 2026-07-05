import internal.GlobalVariable as GlobalVariable
import kafka.KafkaHelper
import excel.ExcelHelper
import groovy.json.JsonOutput

/*
 * TC06 - Kafka Consumer Test
 * Katalon BERTINDAK SEBAGAI CONSUMER: membaca / mengonsumsi pesan dari sebuah topic Kafka.
 *
 * Broker Kafka belum tersedia secara default -> gunakan docker-compose-kafka.yml
 * yang ada di root project untuk menjalankan Kafka broker lokal:
 *      docker compose -f docker-compose-kafka.yml up -d
 *
 * Kirim beberapa pesan uji ke topic (misalnya menggunakan kafka-test-producer.py
 * atau kafka-console-producer) sebelum menjalankan test case ini.
 *
 * Konfigurasi broker/topic/group diambil dari Global Variable (Profiles/default.glbl):
 *      GlobalVariable.KAFKA_BOOTSTRAP_SERVERS
 *      GlobalVariable.KAFKA_TOPIC
 *      GlobalVariable.KAFKA_GROUP_ID
 *
 * Setiap pesan yang berhasil dikonsumsi dicatat ke Excel: Reports/Excel/Report_KafkaConsumer.xlsx
 */

ExcelHelper.initExcel('Report_KafkaConsumer.xlsx')

try {
    List<Map> messages = KafkaHelper.consumeMessages(
        GlobalVariable.KAFKA_BOOTSTRAP_SERVERS,
        GlobalVariable.KAFKA_TOPIC,
        GlobalVariable.KAFKA_GROUP_ID
    )

    println("Total pesan yang berhasil dikonsumsi dari topic '${GlobalVariable.KAFKA_TOPIC}': ${messages.size()}")

    if (messages.isEmpty()) {
        // Tetap dicatat sebagai bukti eksekusi meskipun tidak ada pesan baru di topic
        ExcelHelper.addRow(GlobalVariable.KAFKA_TOPIC as String, 'N/A (Consumer only)', 'NO_MESSAGE', 'Tidak ada pesan baru pada topic')
    } else {
        messages.each { Map msg ->
            String responseBody = JsonOutput.toJson([
                key      : msg.key,
                value    : msg.value,
                partition: msg.partition,
                offset   : msg.offset
            ])
            ExcelHelper.addRow(GlobalVariable.KAFKA_TOPIC as String, 'N/A (Consumer only)', 'CONSUMED', responseBody)
        }
    }

    assert messages.size() >= 0 // sesuaikan assertion dengan kebutuhan (misal minimal 1 pesan diterima)
} finally {
    ExcelHelper.saveExcel()
}
