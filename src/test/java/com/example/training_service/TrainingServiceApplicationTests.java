package com.example.training_service;

import com.example.training_service.outbox.OutboxEventRepository;
import com.example.training_service.repository.ExerciseRepository;
import com.example.training_service.repository.ExerciseSetRepository;
import com.example.training_service.repository.TrainingRepository;
import com.example.training_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration," +
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.bootstrap-servers="
})
@ActiveProfiles("test")
class TrainingServiceApplicationTests {

    @MockBean
    private TrainingRepository trainingRepository;

    @MockBean
    private ExerciseSetRepository exerciseSetRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExerciseRepository exerciseRepository;

    @MockBean
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private KafkaProperties kafkaProperties;

    @MockBean(name = "sagaKafkaListenerContainerFactory")
    private ConcurrentKafkaListenerContainerFactory<String, String> sagaKafkaListenerContainerFactory;

    @Test
    void contextLoads() {
    }
}