package life.majiang.community;

import life.majiang.community.mapper.QuestionMapper;
import life.majiang.community.model.Question;
import life.majiang.community.service.ESQuestionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommunityApplicationTests {
    @Autowired
    private ESQuestionService esQuestionService;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    RedisTemplate<Object, Question> questionRedisTemplate;
    @Autowired
    RedisCacheManager redisCacheManager;
    @Test
    public void addPlayer() throws IOException {
        Question question = new Question();
        question.setId(999L);
        question.setTitle("Java真难学");
        //esQuestionService.addQuestion(question);
        esQuestionService.importAll();
    }

    @Test
    public void testRedis01(){
        Question question = questionMapper.selectByPrimaryKey(1L);
        questionRedisTemplate.opsForValue().set("q-01",question);
    }

}
