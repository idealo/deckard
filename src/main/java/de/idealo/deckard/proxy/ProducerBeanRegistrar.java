package de.idealo.deckard.proxy;

import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

import de.idealo.deckard.producer.GenericProducer;

public class ProducerBeanRegistrar implements ApplicationContextAware {

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        KafkaTemplate bean = applicationContext.getBean(KafkaTemplate.class);
        ProxyBeanFactory proxyBeanFactory = new ProxyBeanFactory(bean);

        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        getProducerClasses(applicationContext)
                .stream()
                .map(producerClass -> getBean(applicationContext, proxyBeanFactory, producerClass))
                .forEach(producer -> registerBean(beanFactory, producer));

    }

    private GenericProducer getBean(final ApplicationContext applicationContext, final ProxyBeanFactory proxyBeanFactory, final Class<? extends GenericProducer> producerClass) {
        System.out.println("trying to create producer " + producerClass);
        return proxyBeanFactory.createBean(applicationContext.getClassLoader(), producerClass);
    }

    private <T extends GenericProducer> void registerBean(final ConfigurableListableBeanFactory beanFactory, final T producer) {
        System.out.println("trying to register producer " + producer.getClass());
        beanFactory.registerSingleton(producer.getClass().getCanonicalName(), producer);
    }

    private Set<Class<? extends GenericProducer>> getProducerClasses(final ApplicationContext applicationContext) {
        Reflections reflections = new Reflections(applicationContext.getClassLoader());
        Set<Class<? extends GenericProducer>> producers = reflections.getSubTypesOf(GenericProducer.class);
        System.out.println(producers);
        return producers;
    }

}
