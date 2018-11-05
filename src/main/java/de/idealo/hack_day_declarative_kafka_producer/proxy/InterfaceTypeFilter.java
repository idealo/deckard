package de.idealo.hack_day_declarative_kafka_producer.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import static java.util.Arrays.asList;

@RequiredArgsConstructor
public class InterfaceTypeFilter implements TypeFilter {

    private final Class genericProducerClass;

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        return asList(metadataReader.getClassMetadata().getInterfaceNames()).contains(genericProducerClass.getName());
    }
}
