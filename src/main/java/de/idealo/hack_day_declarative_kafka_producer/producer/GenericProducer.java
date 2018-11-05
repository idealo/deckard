package de.idealo.hack_day_declarative_kafka_producer.producer;

public interface GenericProducer<K, V> {

    void send(V data);

    void send(K messageKey, V data);
}
