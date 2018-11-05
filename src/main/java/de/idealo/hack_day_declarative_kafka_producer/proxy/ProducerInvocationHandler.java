package de.idealo.hack_day_declarative_kafka_producer.proxy;

import de.idealo.hack_day_declarative_kafka_producer.producer.Producer;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.lang.String.format;

@RequiredArgsConstructor
public class ProducerInvocationHandler<K, V> implements InvocationHandler {

    private static final int MAX_NUMBER_OF_ARGUMENTS = 2;

    private final Producer<K, V> producer;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (!"send".equals(method.getName())) {
            throw new IllegalArgumentException(format("Method %s does not exist.", method.getName()));
        }

        if (args.length == 1) {
            producer.send((V) args[0]);
        } else if (args.length == MAX_NUMBER_OF_ARGUMENTS) {
            producer.send((K) args[0], (V) args[1]);
        } else {
            throw new IllegalArgumentException("Invalid number of arguments.");
        }

        return 0;
    }
}
