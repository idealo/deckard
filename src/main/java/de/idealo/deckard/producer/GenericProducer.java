package de.idealo.deckard.producer;

public interface GenericProducer<K, V> {

    void send(V data);

    void send(K messageKey, V data);

    void sendEmpty(K messageKey);

    void close();
}
