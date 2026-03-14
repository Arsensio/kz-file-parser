package kz.file.parser.service;

import kz.file.parser.model.PdfTariffEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitSender {

  private final RabbitTemplate rabbitTemplate;
  private final String exchangeName;
  private final String routingKey;

  public RabbitSender(
      RabbitTemplate rabbitTemplate,
      @Value("${app.rabbit.exchange}") String exchangeName,
      @Value("${app.rabbit.routing-key}") String routingKey) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchangeName = exchangeName;
    this.routingKey = routingKey;
  }

  public void send(PdfTariffEvent event) {
    rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
  }
}
