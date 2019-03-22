package de.idealo.deckard.producer;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Producer<K, V> {

    private final KafkaTemplate<K, V> template;
    private final String topic;

    public void send(V value) {
        template.send(topic, value);
    }

    public void send(K key, V value) {
        template.send(topic, key, value);
    }

    public void sendEmpty(K key) {
        template.send(topic, key, null);
    }
}
