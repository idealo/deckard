package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.Producer;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class ProducerInvocationHandler<K, V> implements InvocationHandler {

    private final Producer<K, V> producer;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "send":
                if (args.length == 1) {
                    producer.send((V) args[0]);
                } else {
                    producer.send((K) args[0], (V) args[1]);
                }
                break;
            case "sendEmpty":
                producer.sendEmpty((K) args[0]);
                break;
            case "close":
                producer.close();
               break;
        }
        return 0;
    }
}