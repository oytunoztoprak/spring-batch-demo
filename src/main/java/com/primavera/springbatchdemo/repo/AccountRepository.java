package com.primavera.springbatchdemo.repo;

import com.primavera.springbatchdemo.entity.Account;
import com.primavera.springbatchdemo.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
}
