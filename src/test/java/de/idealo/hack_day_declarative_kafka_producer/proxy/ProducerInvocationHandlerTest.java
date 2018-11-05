package de.idealo.hack_day_declarative_kafka_producer.proxy;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.idealo.hack_day_declarative_kafka_producer.producer.GenericProducer;
import de.idealo.hack_day_declarative_kafka_producer.producer.Producer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProducerInvocationHandlerTest {

    private static final String METHOD_NAME = "send";
    private static final String MESSAGE_KEY = "key";
    private static final String MESSAGE_VALUE = "value";

    private ProducerInvocationHandler<Object, Object> handler;

    @Mock
    private Producer<Object, Object> producer;

    @Before
    public void setUp() {

        handler = new ProducerInvocationHandler<>(producer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckInvokedMethod() throws Throwable {
        handler.invoke(mock(TestInterface.class), getClass().getMethod("invalidMethod"), new Object[]{"foo"});
    }

    @Test
    public void shouldCallSendForOneParameter() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME, Object.class), new Object[]{
            MESSAGE_VALUE});

        verify(producer).send(eq(MESSAGE_VALUE));
    }

    @Test
    public void shouldCallSendForTwoParameters() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME, Object.class, Object.class), new Object[]{
            MESSAGE_KEY, MESSAGE_VALUE});

        verify(producer).send(eq(MESSAGE_KEY), eq(MESSAGE_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckNumberOfArguments() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME, Object.class, Object.class), new Object[]{
            MESSAGE_KEY, MESSAGE_VALUE, "oneTooMany"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckIfZeroArgumentsSupplied() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME, Object.class, Object.class), new Object[]{});
    }

    public void invalidMethod() {}

    private interface TestInterface extends GenericProducer<Object, Object> {}
}