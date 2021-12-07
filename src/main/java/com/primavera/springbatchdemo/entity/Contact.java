package com.primavera.springbatchdemo.entity;

import com.sun.xml.bind.v2.model.core.ID;
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
public class Contact implements Serializable,Persistable {


    @Id
    private Long id;
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_generator")
    //@SequenceGenerator(name="seq_generator", sequenceName = "dummy_seq",allocationSize = 10000)

    private String firstName;
    private String lastName;
    private String state;
    private String status;

    @Column(name="write_to_kafka")
    private Boolean writeToKafka = false;

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
