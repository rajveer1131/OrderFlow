package com.example.OrderFlow.OrderService.Event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.example.OrderFlow.Config.Kafka.KafkaTopicConfig;

@Service
public class OrderEventProducer {



    private final KafkaTemplate<String, OrderStatusChangedEvent> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, OrderStatusChangedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderStatusChanged(
            OrderStatusChangedEvent event
    ) {
        kafkaTemplate.send(
                KafkaTopicConfig.ORDER_STATUS_TOPIC,
                event.getOrderId().toString(),
                event
        );
    }
}


