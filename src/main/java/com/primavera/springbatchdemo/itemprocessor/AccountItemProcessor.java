package com.primavera.springbatchdemo.itemprocessor;

import com.primavera.springbatchdemo.entity.Account;
import com.primavera.springbatchdemo.entity.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountItemProcessor implements org.springframework.batch.item.ItemProcessor<Account, Account> {

    @Override
    public Account process(final Account account) {

        account.setStatus("Active");
        return account;
    }

}
