package life.majiang.community.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import life.majiang.community.model.Question;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;

@Configuration
public class MyRedisConfig {
    //将redis的默认序列化机制由jdk改为 Jackson
    @Bean
    public RedisTemplate<Object, Question> questionRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        //使用序列化器Jackson2JsonRedisSerializer让redisTemplate支持序列化
        RedisTemplate<Object, Question> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Question> ser = new Jackson2JsonRedisSerializer<Question>(Question.class);
        template.setDefaultSerializer(ser);
        return template;
    }

    //编写我们自己的template
//    @Bean
//    @SuppressWarnings("all")
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
//            throws UnknownHostException {
//
//        //我们为了自己开发方便，一般使用<String,Object>
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//
//        //Json序列化配置
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
//        ObjectMapper om=new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//
//        //String的序列化
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//
//        //key采用String的序列化方式
//        template.setKeySerializer(stringRedisSerializer);
//        //hash的key也采用String的序列化方式
//        template.setHashKeySerializer(stringRedisSerializer);
//        //value的序列化采用jackson
//        template.setValueSerializer(jackson2JsonRedisSerializer);
//        //hash的value也采用jackson
//        template.setHashValueSerializer(jackson2JsonRedisSerializer);
//
//        template.afterPropertiesSet();
//
//        return template;
//    }


    @Primary//要是有多个cacheManager，定义主要的
    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 使用缓存的默认配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 使用 GenericJackson2JsonRedisSerializer 作为序列化器
        config = config.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config);

        return builder.build();
    }
}
