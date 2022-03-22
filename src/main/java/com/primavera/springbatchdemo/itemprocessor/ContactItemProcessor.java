package com.primavera.springbatchdemo.itemprocessor;

import com.primavera.springbatchdemo.entity.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContactItemProcessor implements org.springframework.batch.item.ItemProcessor<Contact, Contact> {

    @Override
    public Contact process(final Contact contact) {



        if ("CA".equals(contact.getState())) {
            contact.setStatus("NOK");
        } else {
            contact.setStatus("OK");
        }

        return contact;
    }

}
