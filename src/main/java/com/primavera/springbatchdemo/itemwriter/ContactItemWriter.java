package com.primavera.springbatchdemo.itemwriter;

import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.repo.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactItemWriter implements ItemWriter<Contact> {

    @Autowired
    private final ContactRepository contactRepository;


    public void write(List<? extends Contact> items) throws Exception {
        contactRepository.saveAll(items);
    }
}
