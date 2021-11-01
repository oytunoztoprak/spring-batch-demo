package com.primavera.springbatchdemo.config;

import com.primavera.springbatchdemo.ContactItemProcessor;
import com.primavera.springbatchdemo.ContactItemWriter;
import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.repo.ContactRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public ContactRepository contactRepository;

    @Autowired
    public ContactItemWriter contactItemWriter;

    @Autowired
    public ContactItemProcessor contactItemProcessor;



    @Value("${app.chunk-size}")
    private int chunkSize;

    @Value("${file.input}")
    private String fileInput;

    @Bean
    public FlatFileItemReader<Contact> itemReader() {
        return new FlatFileItemReaderBuilder().name("coffeeItemReader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .delimiter(",")
                .names(new String[]{"firstName", "lastName", "state"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper() {{
                    setTargetType(Contact.class);
                }})
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory) {
        return jobBuilderFactory
                .get("Sync Processing JOB")
                .incrementer(new RunIdIncrementer())
                .flow(step(null))
                .end()
                .build();
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Read-->Process-->Write")
                .<Contact, Contact>chunk(chunkSize)
                .reader(itemReader())
                .processor(contactItemProcessor)
                .writer(contactItemWriter)
                .build();
    }





}
