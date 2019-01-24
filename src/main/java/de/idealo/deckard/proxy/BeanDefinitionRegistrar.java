package de.idealo.deckard.proxy;

import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import de.idealo.deckard.producer.GenericProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        getProducerClasses()
                .forEach(producerClass -> registerBean(registry, producerClass));

    }

    private Set<Class<? extends GenericProducer>> getProducerClasses() {
        Reflections reflections = new Reflections(this.getClass().getClassLoader());
        Set<Class<? extends GenericProducer>> producers = reflections.getSubTypesOf(GenericProducer.class);
        return producers;
    }

    private void registerBean(BeanDefinitionRegistry registry, Class<?> beanClass) {
        log.warn("registering bean {}", beanClass);
        String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());

        GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
        proxyBeanDefinition.setBeanClass(beanClass);

        ConstructorArgumentValues args = new ConstructorArgumentValues();

        args.addGenericArgumentValue(this.getClass().getClassLoader());
        args.addGenericArgumentValue(beanClass);
        proxyBeanDefinition.setConstructorArgumentValues(args);

        proxyBeanDefinition.setFactoryBeanName("proxyBeanFactory");
        proxyBeanDefinition.setFactoryMethodName("createBean");
        proxyBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        registry.registerBeanDefinition(beanName, proxyBeanDefinition);
    }
}
