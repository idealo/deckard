# Deckard
## Declarative Kafka Resource Definitions

The Deckard library enables you to easily create Kafka producers by just declaring interfaces like this:

````java
@KafkaProducer(topic="my.topic")
public interface MyProducer extends GenericProducer<String, MyDto> {}
````

This will provide your application with a functioning message producer for Kafka.

GenericProducer provides the following methods for your convenience

    void send(MyDto data);
    void send(String aKey, MyDto data);


### How To:

You only have to define your producers via interfaces as shown above.
Then add the annotation `@KafkaProducer` to your interface to configure the target topic.

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
- configurable, individual Serializers per Producer
- integration of Kafka Encryption as provided by team SSO
- IDE integration
