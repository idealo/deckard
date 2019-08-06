package de.idealo.deckard.proxy;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.producer.Producer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProducerInvocationHandlerTest {

    private static final String METHOD_NAME_SEND = "send";
    private static final String METHOD_NAME_SEND_EMPTY = "sendEmpty";
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
    @Ignore
    public void shouldCheckInvokedMethod() throws Throwable {
        handler.invoke(mock(TestInterface.class), getClass().getMethod("invalidMethod"), new Object[]{"foo"});
    }

    @Test
    public void shouldCallSendForOneParameter() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class), new Object[]{
            MESSAGE_VALUE});

        verify(producer).send(eq(MESSAGE_VALUE));
    }

    @Test
    public void shouldCallSendForTwoParameters() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class, Object.class), new Object[]{
            MESSAGE_KEY, MESSAGE_VALUE});

        verify(producer).send(eq(MESSAGE_KEY), eq(MESSAGE_VALUE));
    }

    @Test
    public void shouldCallSendEmptyOnSendTombstone() throws NoSuchMethodException {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND_EMPTY, Object.class), new Object[]{
                MESSAGE_KEY});

        verify(producer).sendEmpty(eq(MESSAGE_KEY));
    }

    @Test
    public void shouldCallClose() throws NoSuchMethodException {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod("close"), null);

        verify(producer).close();
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void shouldCheckNumberOfArguments() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class, Object.class), new Object[]{
            MESSAGE_KEY, MESSAGE_VALUE, "oneTooMany"});
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void shouldCheckIfZeroArgumentsSupplied() throws Throwable {
        handler.invoke(mock(TestInterface.class), Producer.class.getMethod(METHOD_NAME_SEND, Object.class, Object.class), new Object[]{});
    }

    public void invalidMethod() {}

    private interface TestInterface extends GenericProducer<Object, Object> {}
}