package de.idealo.kafka.deckard.proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
class BeanDefinitionRegistrarTest {

    @Mock
    private AnnotationMetadata metadata;

    @Mock
    private BeanDefinitionRegistry registry;

    @Captor
    private ArgumentCaptor<GenericBeanDefinition> definitionCaptor;

    @Test
    void shouldRegisterDestroyMethod() {
        BeanDefinitionRegistrar registrar = new BeanDefinitionRegistrar();

        registrar.registerBeanDefinitions(metadata, registry);

        verify(registry, atLeastOnce()).registerBeanDefinition(anyString(), definitionCaptor.capture());

        GenericBeanDefinition definition = definitionCaptor.getValue();

        assertThat(definition.getDestroyMethodName()).isEqualTo("close");
    }
}
