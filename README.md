# Deckard
## Declarative Kafka Resource Definitions

This library provides an easy solution to write messages to Kafka in a purely declarative and transparent manner.
It lets you write Kafka messages without having to worry much about complicated configurations. 

The Deckard library enables you to easily create Kafka producers by just declaring interfaces like this:

````java
@KafkaProducer(topic = "my.topic")
public interface MyProducer extends GenericProducer<String, MyDto> {}
````

This will provide your application with a functioning message producer for Kafka.

GenericProducer provides the following methods for your convenience

````java
void send(V data)
void send(K messageKey, V data)
void sendEmpty(K messageKey) // sends tomb stone message 
````

### How To:

You only have to define your producers via interfaces as shown above.
Then add the annotation `@KafkaProducer` to your interface to configure the target topic.

**_And that's it!_** 
  
Everything else will be handled automatically. You may configure target brokers and so on as usual
via the properties which are provided by Spring Kafka. They will be picked up by our
bootstrapping mechanism. 

### Notes:
#### Serializers
You may set a default serializer for your keys and values as usual via the Spring Kafka properties. 
However, if you want to use different serializers on individual producers, you can also specify them in the producer definition:

````java
@KafkaProducer(topic = "my.topic", keySerializer = LongSerializer.class, valueSerializer = JsonSerializer.class)
public interface MyProducer extends GenericProducer<String, MyDto> {}
````

#### Bootstrap Servers
Per default, KafkaProducers will use bootstrap servers resolved from the KafkaProperties where producer bootstrap servers 
from `spring.kafka.producer.bootstrap-servers` overwrite global bootstrap servers from `spring.kafka.bootstrap-servers`. 
If you need to define different bootstrap servers per KafkaProducer, you can also specify them in the producer definition:
 
````java
@KafkaProducer(topic = "my.topic", bootstrapServers = "localhost:9092")
public interface MyProducer extends GenericProducer<String, MyDto> {}
````

#### Property Placeholder Support
The parameters `topic` and bootstrapServers are also able to resolve property placeholders:
````java
@KafkaProducer(topic = "${topic.name.from.property}", bootstrapServers = "${bootstrap-servers.from.property}")
public interface MyProducer extends GenericProducer<String, String> {}
````
  

#### Samples

You can find a showcase for deckard [in this sample project](https://code.eu.idealo.com/projects/UDS/repos/hack_day_declarative_kafka_producer_showcase/browse).

#### Notes on Versioning and Compatibility

Deckard was originally built on Spring Kafka 2.0.x which is only compatible with Spring Boot 2.0.x.
To go forward with development for Spring Boot 2.1.x, but also support projects based on Spring Boot 2.0.x, we introduced the following versioning scheme:

- 0.x -> supports Spring Kafka 2.1.x and is compatible with Spring Boot 2.0.x
- 1.x -> supports Spring Kafka 2.2.x and is compatible with Spring Boot 2.1.x

We will try to add new features in both versions as long as possible.

#### About the project

This project was the result of our _A&L HackDay_ in October 2018 and January 2019. Participants were:
- Marcus Janke (Postman)
- Alexander Lüdeke (Postman)
- Richard Remus (Postman)
- Gerald Sander (Postman)
- Nabil Tawfik (SSO)
- Daniel Hübner (Engineering Coach)

Team Postman will continue to develop this, since we're already using it in production. 

__However, you are welcome to join in with your ideas and create pull requests!__

#### Planned Features:
- integration of Kafka Encryption as provided by team SSO
- IDE integration
