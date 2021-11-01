package com.primavera.springbatchdemo;

import com.primavera.springbatchdemo.entity.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContactItemProcessor implements org.springframework.batch.item.ItemProcessor<Contact, Contact> {

    @Override
    public Contact process(final Contact contact) throws Exception {



        if ("CA".equals(contact.getState())) {
            contact.setStatus("NOK");
        } else {
            contact.setStatus("OK");
        }

        if ("AL".equals(contact.getState())) {
            contact.setWriteToKafka(true);
        }

        log.info("Sleeping 1 ms");
        Thread.sleep(1);
        return contact;
    }

}
