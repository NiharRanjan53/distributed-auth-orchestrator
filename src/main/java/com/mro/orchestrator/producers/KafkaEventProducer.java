package com.mro.orchestrator.producers;

import com.mro.orchestrator.config.KafkaConfig;
import com.mro.orchestrator.events.RawDocumentIngestedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public  void publishrawDocumentIngestedEvent(RawDocumentIngestedEvent rawDocumentIngestedEvent){
        kafkaTemplate.send(KafkaConfig.INGESTION_TOPIC, rawDocumentIngestedEvent.getJobId(), rawDocumentIngestedEvent)
                .whenComplete((result, err) -> {
                    if (err != null) {
                        System.out.println("Error publishing RawDocumentIngestedEvent: " + err.getMessage());
                    }
                });
    }

}
