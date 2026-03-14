package kz.file.parser.service;

import kz.file.parser.model.CsvAnalysisEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitSender {

  private final RabbitTemplate rabbitTemplate;

  @Value("${app.rabbit.exchange}")
  private String exchangeName;
  @Value("${app.rabbit.routing-key}")
  private String routingKey;

  public void send(CsvAnalysisEvent event) {
    rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
  }
}
