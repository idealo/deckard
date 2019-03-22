package de.idealo.deckard.proxy;

import de.idealo.deckard.producer.Producer;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class ProducerInvocationHandler<K, V> implements InvocationHandler {

    private static final int MAX_NUMBER_OF_ARGUMENTS = 2;

    private final Producer<K, V> producer;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if ("send".equals(method.getName())) {
            if (args.length == 1) {
                producer.send((V) args[0]);
            } else if (args.length == MAX_NUMBER_OF_ARGUMENTS) {
                producer.send((K) args[0], (V) args[1]);
            }
        } else if ("sendEmpty".equals(method.getName())) {
            producer.sendEmpty((K) args[0]);
        }

        return 0;
    }
}