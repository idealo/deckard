package de.idealo.hack_day_declarative_kafka_producer.proxy;

import de.idealo.hack_day_declarative_kafka_producer.producer.GenericProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
public class BeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        String[] basePackages = getBasePackages(metadata);

        ClassPathScanningCandidateComponentProvider provider = getClassPathScanningProvider();

        Arrays.stream(basePackages)
                .map(provider::findCandidateComponents)
                .flatMap(Collection::stream)
                .forEach(beanDefinition -> registerBean(registry, beanDefinition));
    }

    private String[] getBasePackages(AnnotationMetadata metadata) {

        return new String[]{
                ((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName()};

    }

    private ClassPathScanningCandidateComponentProvider getClassPathScanningProvider() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false, environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isIndependent() && metadata.isInterface();
            }
        };
        provider.addIncludeFilter(new InterfaceTypeFilter(GenericProducer.class));

        return provider;
    }

    private void registerBean(BeanDefinitionRegistry registry, BeanDefinition beanDefinition) {
        try {
            Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
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
        } catch (ClassNotFoundException e) {
            log.error("Can't find class.", e);
        }
    }
}
