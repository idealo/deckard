package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProducerInvocationHandlerTest {

    private static final String METHOD_NAME_SEND = "send";
    private static final String METHOD_NAME_SEND_EMPTY = "sendEmpty";
    private static final String MESSAGE_KEY = "key";
    private static final String MESSAGE_VALUE = "value";

    private ProducerInvocationHandler<Object, Object> handler;

    @Mock
    private Producer<Object, Object> producer;

    @BeforeEach
    public void setUp() {

        handler = new ProducerInvocationHandler<>(producer);
    }

    @Test
    @Disabled
    void shouldCheckInvokedMethod() throws Throwable {
        handler.invoke(mock(TestInterface.class), getClass().getMethod("invalidMethod"), new Object[]{"foo"});
    }

    @Test
    void shouldCallSendForOneParameter() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class), new Object[]{
                MESSAGE_VALUE});

        verify(producer).send(eq(MESSAGE_VALUE));
    }

    @Test
    void shouldCallSendForTwoParameters() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class, Object.class), new Object[]{
                MESSAGE_KEY, MESSAGE_VALUE});

        verify(producer).send(eq(MESSAGE_KEY), eq(MESSAGE_VALUE));
    }

    @Test
    void shouldCallSendEmptyOnSendTombstone() throws NoSuchMethodException {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND_EMPTY, Object.class), new Object[]{
                MESSAGE_KEY});

        verify(producer).sendEmpty(eq(MESSAGE_KEY));
    }

    @Test
    void shouldCallClose() throws NoSuchMethodException {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod("close"), null);

        verify(producer).close();
    }

    @Test
    @Disabled
    void shouldCheckNumberOfArguments() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class, Object.class), new Object[]{
                MESSAGE_KEY, MESSAGE_VALUE, "oneTooMany"});
    }

    @Test
    @Disabled
    void shouldCheckIfZeroArgumentsSupplied() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class, Object.class), new Object[]{});
    }

    public void invalidMethod() {
    }

    private interface TestInterface extends GenericProducer<Object, Object> {
    }
}
