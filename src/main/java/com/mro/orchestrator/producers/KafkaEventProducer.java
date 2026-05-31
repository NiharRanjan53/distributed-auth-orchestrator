package com.mro.orchestrator.producers;

import com.mro.orchestrator.config.KafkaConfig;
import com.mro.orchestrator.events.RawDocumentIngestedEvent;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public  void publishrawDocumentIngestedEvent(RawDocumentIngestedEvent event){

        String traceId = Span.current().getSpanContext().getTraceId();

        String correlationId = MDC.get("correlationId");

        if (correlationId == null) {
            correlationId = traceId;
        }

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(
                        KafkaConfig.INGESTION_TOPIC,
                        event.getJobId(),
                        event
                );

        record.headers().add("traceId", traceId.getBytes(StandardCharsets.UTF_8));

        record.headers().add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8));

        record.headers().add("sourceService", "distributed-auth-orchestrator" .getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record)
                .whenComplete((result, err) -> {
                    if (err != null) {
                        log.error(
                                "Failed to publish RawDocumentIngestedEvent. jobId={}",
                                event.getJobId(),
                                err
                        );
                    } else {
                        log.info(
                                "Successfully published RawDocumentIngestedEvent. topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }

}
