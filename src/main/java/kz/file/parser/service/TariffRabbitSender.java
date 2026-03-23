package kz.file.parser.service;

import kz.file.parser.model.TariffEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TariffRabbitSender {

  private final RabbitTemplate rabbitTemplate;

  @Value("${app.rabbit.tariffs.exchange}")
  private String exchangeName;
  @Value("${app.rabbit.tariffs.routing-key}")
  private String routingKey;

  public void send(TariffEvent event) {
    rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
  }
}
