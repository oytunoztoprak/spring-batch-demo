package com.primavera.springbatchdemo.repo;

import com.primavera.springbatchdemo.entity.ContactCA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactCARepository extends JpaRepository<ContactCA,Long> {
}
