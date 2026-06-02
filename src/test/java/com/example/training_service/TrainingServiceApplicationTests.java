package com.example.training_service;

import com.example.training_service.repository.ExerciseRepository;
import com.example.training_service.repository.ExerciseSetRepository;
import com.example.training_service.repository.TrainingRepository;
import com.example.training_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
        // Empty bootstrap-servers: disables all Kafka config/components via @ConditionalOnExpression
        "spring.kafka.bootstrap-servers="
})
@ActiveProfiles("test")
class TrainingServiceApplicationTests {

    // JPA repositories mocked because DataSource/JPA autoconfiguration is excluded
    @MockitoBean
    private TrainingRepository trainingRepository;

    @MockitoBean
    private ExerciseSetRepository exerciseSetRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ExerciseRepository exerciseRepository;

    // Redis connection factory mocked so RedisConfig can build RedisTemplate/CacheManager
    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void contextLoads() {
    }
}
