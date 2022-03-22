package com.primavera.springbatchdemo.config;

import com.primavera.springbatchdemo.entity.Account;
import com.primavera.springbatchdemo.itemprocessor.AccountItemProcessor;
import com.primavera.springbatchdemo.itemprocessor.ContactItemProcessor;
import com.primavera.springbatchdemo.itemwriter.AccountItemWriter;
import com.primavera.springbatchdemo.itemwriter.ContactItemWriter;
import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.repo.AccountRepository;
import com.primavera.springbatchdemo.repo.ContactRepository;
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
@RequiredArgsConstructor
public class AccountJobConfig {

    private final AccountItemWriter accountItemWriter;
    private final AccountItemProcessor accountItemProcessor;

    private final String jobName = "AccountFileImportJob";
    private final String itemReaderName = "accountItemReader";
    private final String filePath = "account.csv";
    private final String [] fileFields = {"id","name"};

    @Value("${app.chunk-size}")
    private int chunkSize;

    @Bean
    public FlatFileItemReader<Account> accountItemReader() {
        return new FlatFileItemReaderBuilder().name(itemReaderName)
                .resource(new ClassPathResource(filePath)) // Normally should point to a configurable directory with file pattern matching
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(fileFields)
                .fieldSetMapper(new BeanWrapperFieldSetMapper() {{
                    setTargetType(Account.class);
                }})
                .build();
    }

    @Bean
    public Job accountFileImportJob(JobBuilderFactory jobBuilderFactory) {
        return jobBuilderFactory
                .get(jobName)
                .incrementer(new RunIdIncrementer())
                .flow(asyncAccountJobStep(null))
                .end()
                .build();
    }

    @Bean
    public Step asyncAccountJobStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("AccountProcessStep")
                .<Account, Future<Account>>chunk(chunkSize)
                .reader(accountItemReader())
                .processor(asyncAccountProcessor())
                .writer(asyncAccountWriter())
                .build();
    }

    @Bean
    @StepScope
    public TaskExecutor accountTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(128);
        executor.setMaxPoolSize(128);
        executor.setQueueCapacity(128);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Thread-");
        return executor;
    }

    @Bean
    public AsyncItemProcessor<Account, Account> asyncAccountProcessor() {
        AsyncItemProcessor<Account, Account> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(accountItemProcessor);
        asyncItemProcessor.setTaskExecutor(accountTaskExecutor());
        return asyncItemProcessor;
    }

    @Bean
    public AsyncItemWriter<Account> asyncAccountWriter() {
        AsyncItemWriter<Account> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(accountItemWriter);
        return asyncItemWriter;
    }

}
