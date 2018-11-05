package de.idealo.hack_day_declarative_kafka_producer;

import de.idealo.hack_day_declarative_kafka_producer.stereotype.EnableKafkaProducers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableKafkaProducers
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
