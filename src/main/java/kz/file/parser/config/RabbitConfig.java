package kz.file.parser.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  @Value("${app.rabbit.queue}")
  private String queueName;

  @Value("${app.rabbit.exchange}")
  private String exchangeName;

  @Value("${app.rabbit.routing-key}")
  private String routingKey;
  @Value("${app.rabbit.tariffs.queue}")
  private String tariffsQueueName;
  @Value("${app.rabbit.tariffs.exchange}")
  private String tariffsExchangeName;
  @Value("${app.rabbit.tariffs.routing-key}")
  private String tariffsRoutingKey;

  @Bean
  public Queue pdfLinesQueue() {
    return new Queue(queueName, true);
  }

  @Bean
  public Queue tariffsQueue() {
    return new Queue(tariffsQueueName, true);
  }

  @Bean
  public DirectExchange pdfLinesExchange() {
    return new DirectExchange(exchangeName);
  }

  @Bean
  public DirectExchange tariffsExchange() {
    return new DirectExchange(tariffsExchangeName);
  }

  @Bean
  public Binding pdfLinesBinding(Queue pdfLinesQueue, DirectExchange pdfLinesExchange) {
    return BindingBuilder.bind(pdfLinesQueue).to(pdfLinesExchange).with(routingKey);
  }

  @Bean
  public Binding tariffsBinding(Queue tariffsQueue, DirectExchange tariffsExchange) {
    return BindingBuilder.bind(tariffsQueue).to(tariffsExchange).with(tariffsRoutingKey);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory,
      MessageConverter jsonMessageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter);
    return template;
  }
}
