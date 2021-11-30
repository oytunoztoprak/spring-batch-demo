package com.primavera.springbatchdemo;

import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.repo.ContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ContactItemWriter implements ItemWriter<Contact> {

    @Value("${app.kafka.output.topic}")
    private String topicName;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    public void write(List<? extends Contact> items) throws Exception {
        contactRepository.saveAll(items);
        log.info("Kafka Write started");
        items.stream()
                .filter(Contact::getWriteToKafka)
                .forEach(item-> kafkaTemplate.send(topicName, "key", item));
    }
}
