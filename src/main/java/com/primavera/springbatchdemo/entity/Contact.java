package com.primavera.springbatchdemo.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
@Entity
public class Contact implements Serializable {

    @Id
    //@GeneratedValue (strategy = GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_generator")
    @SequenceGenerator(name="seq_generator", sequenceName = "dummy_seq",allocationSize = 10000)
    private Long id;
    private String firstName;
    private String lastName;
    private String state;
    private String status;

    @Column(name="write_to_kafka")
    private Boolean writeToKafka = false;
}
