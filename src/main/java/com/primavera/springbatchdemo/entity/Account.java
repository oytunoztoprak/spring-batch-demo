package com.primavera.springbatchdemo.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@Persistent
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account implements Serializable,Persistable {


    @Id
    private Long id;
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_generator")
    //@SequenceGenerator(name="seq_generator", sequenceName = "dummy_seq",allocationSize = 10000)

    private String name;
    private String type;
    private String status;


    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }


}
