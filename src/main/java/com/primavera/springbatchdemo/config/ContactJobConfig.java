//package com.primavera.springbatchdemo.config;
//
//
//import com.primavera.springbatchdemo.entity.Contact;
//import com.primavera.springbatchdemo.itemprocessor.ContactItemProcessor;
//import com.primavera.springbatchdemo.itemwriter.ContactItemWriter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.integration.async.AsyncItemProcessor;
//import org.springframework.batch.integration.async.AsyncItemWriter;
//import org.springframework.batch.item.file.FlatFileItemReader;
//import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
//import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Future;
//import java.util.concurrent.ThreadPoolExecutor;
//
//
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
//public class ContactJobConfig {
//
//    private final ContactItemWriter contactItemWriter;
//    private final ContactItemProcessor contactItemProcessor;
//
//    private final String jobName = "ContactFileImportJob";
//    private final String itemReaderName = "contactItemReader";
//    private final String filePath = "contact.csv";
//    private final String [] fileFields = {"id","firstName", "lastName", "state"};
//
//    @Value("${app.chunk-size}")
//    private int chunkSize;
//
//    @Bean
//    public FlatFileItemReader<Contact> contactItemReader() {
//        return new FlatFileItemReaderBuilder().name(itemReaderName)
//                .resource(new ClassPathResource(filePath)) // Normally should point to a configurable directory with file pattern matching
//                .linesToSkip(1)
//                .delimited()
//                .delimiter(",")
//                .names(fileFields)
//                .fieldSetMapper(new BeanWrapperFieldSetMapper() {{
//                    setTargetType(Contact.class);
//                }})
//                .build();
//    }
//
//    @Bean
//    public Job contactFileImportJob(JobBuilderFactory jobBuilderFactory) {
//        return jobBuilderFactory
//                .get(jobName)
//                .incrementer(new RunIdIncrementer())
//                .flow(asyncContactJobStep(null))
//                .end()
//                .build();
//    }
//
//    @Bean
//    public Step asyncContactJobStep(StepBuilderFactory stepBuilderFactory) {
//        return stepBuilderFactory
//                .get("ContactProcessStep")
//                .<Contact, Future<Contact>>chunk(chunkSize)
//                .reader(contactItemReader())
//                .processor(asyncContactProcessor())
//                .writer(asyncContactWriter())
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public TaskExecutor contactTaskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(128);
//        executor.setMaxPoolSize(128);
//        executor.setQueueCapacity(128);
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        executor.setThreadNamePrefix("Thread-");
//        return executor;
//    }
//
//    @Bean
//    public AsyncItemProcessor<Contact, Contact> asyncContactProcessor() {
//        AsyncItemProcessor<Contact, Contact> asyncItemProcessor = new AsyncItemProcessor<>();
//        asyncItemProcessor.setDelegate(contactItemProcessor);
//        asyncItemProcessor.setTaskExecutor(contactTaskExecutor());
//        return asyncItemProcessor;
//    }
//
//    @Bean
//    public AsyncItemWriter<Contact> asyncContactWriter() {
//        AsyncItemWriter<Contact> asyncItemWriter = new AsyncItemWriter<>();
//        asyncItemWriter.setDelegate(contactItemWriter);
//        return asyncItemWriter;
//    }
//
//}
