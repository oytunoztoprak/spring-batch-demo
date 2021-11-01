package com.primavera.springbatchdemo;

import com.primavera.springbatchdemo.entity.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class ContactItemProcessor implements ItemProcessor<Contact, Contact> {

    @Override
    public Contact process(final Contact contact) throws Exception {
        String firstName = contact.getFirstName().toUpperCase();
        String lastName = contact.getLastName().toUpperCase();
        String state = contact.getState().toUpperCase();

        Contact transformedContact = new Contact(firstName, lastName, state);
        log.info("Converting ( {} ) into ( {} )", contact, transformedContact);
        log.info("Sleeping 1 ms");
        Thread.sleep(1);
        return transformedContact;
    }

}
