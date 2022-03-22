package com.primavera.springbatchdemo.itemwriter;

import com.primavera.springbatchdemo.entity.Account;
import com.primavera.springbatchdemo.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountItemWriter implements ItemWriter<Account> {

    private final AccountRepository accountRepository;

    public void write(List<? extends Account> items) throws Exception {
        accountRepository.saveAll(items);
    }
}
