package de.idealo.deckard.proxy;

import de.idealo.deckard.proxy.InterfaceTypeFilter;
import org.junit.Test;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InterfaceTypeFilterTest {

    @Test
    public void shouldMatchInterface() {
        InterfaceTypeFilter interfaceTypeFilter = new InterfaceTypeFilter(TestInterface.class);

        MetadataReader metadataReader = mock(MetadataReader.class);
        MetadataReaderFactory metadataReaderFactory = mock(MetadataReaderFactory.class);
        ClassMetadata classMetadata = mock(ClassMetadata.class);
        
        when(classMetadata.getInterfaceNames()).thenReturn(new String[]{TestInterface.class.getName()});
        when(metadataReader.getClassMetadata()).thenReturn(classMetadata);

        assertThat(interfaceTypeFilter.match(metadataReader, metadataReaderFactory)).isTrue();
    }

    private static interface TestInterface {
    }
}