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

  @Bean
  public Queue pdfLinesQueue() {
    return new Queue(queueName, true);
  }

  @Bean
  public DirectExchange pdfLinesExchange() {
    return new DirectExchange(exchangeName);
  }

  @Bean
  public Binding pdfLinesBinding(Queue pdfLinesQueue, DirectExchange pdfLinesExchange) {
    return BindingBuilder.bind(pdfLinesQueue).to(pdfLinesExchange).with(routingKey);
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
