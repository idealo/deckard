# Deckard
## Declarative Kafka Resource Definitions

The Deckard library enables you to easily create Kafka producers by just declaring interfaces like this:

````java
@KafkaProducer(topic="my.topic")
public interface MyProducer extends GenericProducer<String, MyDto> {
 
    void send(MyDto data);
    void send(String aKey, MyDto data);

}
````

This will provide your application with a functioning message producer for Kafka.

### How To:

First you define your producers via interfaces as shown above, don't forget the annotation
`@KafkaProducer` to configure the target topic.

Next you need to annotate your application class with `@EnableKafkaProducers`.

**_And that's it!_** 
  
Everything else will be handled automatically. You may configure target brokers and so on as usual
via the properties which are provided by Spring Kafka. They will be picked up by our
bootstrapping mechanism. 

### Notes:
#### Serializers
Right now, the default value serializer is the `StringSerializer`. 
If you want something else (like `JsonSerializer`) you can just define it in your application yaml.
As of now, Deckard supports only one serializer per application, i.e. all producers in your app will
utilize the specified serializer.

#### Custom Kafka Templates
You may provide your own Kafka Templates in the Spring Context. If they match the
required types for message keys and values, they will be wired into the
respective producers automatically. 
