package com.primavera.springbatchdemo.config;


import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.itemprocessor.ContactItemProcessor;
import com.primavera.springbatchdemo.itemwriter.ContactItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ContactFileWriteJobConfig {

    private final ContactItemProcessor contactItemProcessor;

    private final String jobName = "ContactFileWriteJob";
    private final String itemReaderName = "contactItemReader";
    private final String filePath = "contact.csv";
    private final String [] fileFields = {"id","firstName", "lastName", "state"};

    @Value("${app.chunk-size}")
    private int chunkSize;

    private Resource outputResource = new FileSystemResource("/Users/whitesnake/Downloads/contacts.csv");

    @Bean
    public FlatFileItemWriter<Contact> writer() {
        //Create writer instance
        FlatFileItemWriter<Contact> writer = new FlatFileItemWriter<>();

        //Set output file location
        writer.setResource(outputResource);
        writer.setHeaderCallback(writer1 -> writer1.write("id|firstName|lastName|state"));

        //All job repetitions should "append" to same output file
        writer.setAppendAllowed(false);


        //Name field values sequence based on object properties
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter("|");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName"});
                    }
                });
            }
        });
        return writer;
    }

    @Bean
    public FlatFileItemReader<Contact> contactItemReader() {
        return new FlatFileItemReaderBuilder().name(itemReaderName)
                .resource(new ClassPathResource(filePath)) // Normally should point to a configurable directory with file pattern matching
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(fileFields)
                .fieldSetMapper(new BeanWrapperFieldSetMapper() {{
                    setTargetType(Contact.class);
                }})
                .build();
    }

    @Bean
    public Job contactFileImportJob(JobBuilderFactory jobBuilderFactory) {
        return jobBuilderFactory
                .get(jobName)
                .incrementer(new RunIdIncrementer())
                .flow(asyncContactJobStep(null))
                .end()
                .build();
    }

    @Bean
    public Step asyncContactJobStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("ContactProcessStep")
                .<Contact, Contact>chunk(chunkSize)
                .reader(contactItemReader())
                .processor(contactItemProcessor)
                .writer(writer())
                .build();
    }

}
