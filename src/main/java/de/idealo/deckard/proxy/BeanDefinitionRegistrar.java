package de.idealo.deckard.proxy;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import de.idealo.deckard.producer.GenericProducer;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        getProducerClasses()
                .forEach(producerClass -> registerBean(registry, producerClass));

    }

    private Collection<Class<?>> getProducerClasses() {
        ScanResult scanResult = new ClassGraph()
                .disableJarScanning()
                .enableAllInfo()
                .scan();

        return scanResult
                .getClassesImplementing(GenericProducer.class.getName())
                .stream()
                .map(this::getClass)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private Class<?> getClass(final ClassInfo classInfo) {
        return Class.forName(classInfo.getName());
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
