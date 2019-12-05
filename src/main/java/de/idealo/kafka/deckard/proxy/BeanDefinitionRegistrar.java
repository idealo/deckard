package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.GenericProducer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private final ClassGraph classGraph = new ClassGraph().enableAllInfo();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        getProducerClasses()
                .forEach(producerClass -> registerBean(registry, producerClass));
    }

    private Collection<Class<?>> getProducerClasses() {
        ScanResult scanResult = classGraph.scan();
        List<Class<?>> classList = scanResult
                .getClassesImplementing(GenericProducer.class.getName())
                .stream()
                .map(this::getClass)
                .collect(Collectors.toList());

        scanResult.close();
        return classList;
    }

    @SneakyThrows
    private Class<?> getClass(final ClassInfo classInfo) {
        return Class.forName(classInfo.getName());
    }

    private void registerBean(BeanDefinitionRegistry registry, Class<?> beanClass) {
        String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());

        GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
        proxyBeanDefinition.setBeanClass(beanClass);

        ConstructorArgumentValues args = new ConstructorArgumentValues();

        args.addGenericArgumentValue(this.getClass().getClassLoader());
        args.addGenericArgumentValue(beanClass);
        proxyBeanDefinition.setConstructorArgumentValues(args);

        proxyBeanDefinition.setFactoryBeanName(ProducerProxyBeanFactory.DEFAULT_FACTORY_BEAN_NAME);
        proxyBeanDefinition.setFactoryMethodName("createBean");
        proxyBeanDefinition.setDestroyMethodName("close");
        proxyBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        proxyBeanDefinition.setLazyInit(true);

        registry.registerBeanDefinition(beanName, proxyBeanDefinition);
    }
}
