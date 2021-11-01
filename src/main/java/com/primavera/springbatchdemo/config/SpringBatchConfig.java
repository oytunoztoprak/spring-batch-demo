package com.primavera.springbatchdemo.config;

import com.primavera.springbatchdemo.ContactItemProcessor;
import com.primavera.springbatchdemo.JobCompletionNotificationListener;
import com.primavera.springbatchdemo.entity.Contact;
import com.primavera.springbatchdemo.repo.ContactRepository;
import com.primavera.springbatchdemo.repo.ContactCARepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
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
    public ContactCARepository contactCARepository;

    @Value("${app.chunk-size}")
    private int chunkSize;


    @Value("${file.input}")
    private String fileInput;

    @Bean
    public FlatFileItemReader itemReader() {
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

/*    @Bean
    public JdbcBatchItemWriter itemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO coffee (brand, origin, characteristics) VALUES (:brand, :origin, :characteristics)")
                .dataSource(dataSource)
                .build();
    }*/

    @Bean
    public RepositoryItemWriter itemWriter() {
        return new RepositoryItemWriterBuilder<Contact>().repository(contactRepository).build();
    }

/*    @Bean
    public RepositoryItemWriter itemWriter2() {
        return new RepositoryItemWriterBuilder<Coffee2>().repository(coffeeRepository2).build();
    }*/

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(RepositoryItemWriter<Contact> writer) {
        return stepBuilderFactory.get("step1")
                .<Contact, Contact> chunk(chunkSize)
                .reader(itemReader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(64);
        executor.setMaxPoolSize(64);
        executor.setQueueCapacity(64);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("MultiThreaded-");
        return executor;
    }


    @Bean
    public ContactItemProcessor processor() {
        return new ContactItemProcessor();
    }

/*    @Bean
    public AsyncItemProcessor<Contact, Contact> processor() {
        org.springframework.batch.integration.async.AsyncItemProcessor<Contact, Contact> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(new ContactItemProcessor());
        asyncItemProcessor.setTaskExecutor(taskExecutor());
        return asyncItemProcessor;
    }*/

/*    @Bean
    public AsyncItemWriter<Contact> asyncWriter() {
        AsyncItemWriter<Contact> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(itemWriter());
        return asyncItemWriter;
    }*/

}
