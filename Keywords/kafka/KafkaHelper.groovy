package kafka

import com.kms.katalon.core.annotation.Keyword
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import java.time.Duration

/**
 * Custom Keyword yang membuat Katalon bertindak sebagai KAFKA CONSUMER.
 * Membaca pesan dari sebuah topic pada Kafka broker, lalu mengembalikan
 * daftar pesan yang berhasil dikonsumsi.
 *
 * Dependency yang dibutuhkan (tambahkan lewat Katalon Studio > Project > Settings > Dependencies > Maven):
 *   - org.apache.kafka:kafka-clients:3.7.0
 */
class KafkaHelper {

    /**
     * Konsumsi pesan dari Kafka topic.
     *
     * @param bootstrapServers alamat broker, contoh: "localhost:9092"
     * @param topic            nama topic yang akan dikonsumsi
     * @param groupId          consumer group id
     * @param pollTimeoutMs    lama waktu polling per batch (ms)
     * @param maxEmptyPolls    jumlah polling kosong berturut-turut sebelum berhenti menunggu pesan baru
     * @return List<Map> berisi [key, value, partition, offset, timestamp] untuk tiap pesan
     */
    @Keyword
    static List<Map> consumeMessages(String bootstrapServers, String topic, String groupId,
                                      long pollTimeoutMs = 3000, int maxEmptyPolls = 3) {
        Properties props = new Properties()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName())
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName())
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 'earliest')
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, 'true')

        List<Map> results = []
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)
        try {
            consumer.subscribe(Collections.singletonList(topic))

            int emptyPolls = 0
            while (emptyPolls < maxEmptyPolls) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeoutMs))
                if (records.isEmpty()) {
                    emptyPolls++
                    continue
                }
                emptyPolls = 0
                for (ConsumerRecord<String, String> record : records) {
                    results.add([
                        key      : record.key(),
                        value    : record.value(),
                        partition: record.partition(),
                        offset   : record.offset(),
                        timestamp: record.timestamp()
                    ])
                }
            }
        } finally {
            consumer.close()
        }
        return results
    }
}
