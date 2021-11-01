package com.primavera.springbatchdemo.config;

import com.primavera.springbatchdemo.ContactItemProcessor;
import com.primavera.springbatchdemo.ContactItemWriter;
import com.primavera.springbatchdemo.JobCompletionNotificationListener;
import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.repo.ContactRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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
    private ContactItemWriter contactItemWriter;


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
                .get("Asynchronous Processing JOB")
                .incrementer(new RunIdIncrementer())
                .flow(asyncStep(null))
                .end()
                .build();
    }

    @Bean
    public Step asyncStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("Read-->Process-->Write")
                .<Contact, Future<Contact>>chunk(chunkSize)
                .reader(itemReader())
                .processor(asyncProcessor())
                .writer(asyncWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(64);
        executor.setMaxPoolSize(64);
        executor.setQueueCapacity(64);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Thread-");
        return executor;
    }

    @Bean
    public AsyncItemProcessor<Contact, Contact> asyncProcessor() {
        AsyncItemProcessor<Contact, Contact> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(new ContactItemProcessor());
        asyncItemProcessor.setTaskExecutor(taskExecutor());
        return asyncItemProcessor;
    }

    @Bean
    public AsyncItemWriter<Contact> asyncWriter() {
        AsyncItemWriter<Contact> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(contactItemWriter);
        return asyncItemWriter;
    }

}
