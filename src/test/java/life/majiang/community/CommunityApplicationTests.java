package life.majiang.community;

import life.majiang.community.mapper.QuestionExtMapper;
import life.majiang.community.mapper.QuestionMapper;
import life.majiang.community.model.Question;
import life.majiang.community.model.User;
import life.majiang.community.service.ESQuestionService;
import life.majiang.community.service.QuestionService;
import life.majiang.community.service.RedisTempleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    RedisCacheManager redisCacheManager;
    @Autowired
    QuestionExtMapper questionExtMapper;
    @Autowired
    RedisTempleService redisTempleService;
    @Autowired
    QuestionService questionService;
    @Test
    public void addPlayer() throws IOException {
        Question question = new Question();
        question.setId(999L);
        question.setTitle("Java真难学");
        //esQuestionService.addQuestion(question);
        esQuestionService.importAll();
    }

/*    @Test
    public void testRedis01(){
        Question question = questionMapper.selectByPrimaryKey(1L);
        questionRedisTemplate.opsForValue().set("q-01",question);
    }*/
    @Test
    public void testRedis01(){
//        Question question = questionService.saveToRedis3(1L);
//        User user = questionService.getUser(1L);
//        questionRedisTemplate.opsForValue().set("q-01",question);
    }
    /*
    https://www.cnblogs.com/mcjhcnblogs/p/13306914.html
    思路：

    SpringBoot集成redis不再多说，基本思路就是用户首次访问从数据库中取值，之后每次进行判断只要redis
    中有数据，就从redis中取值，其中涉及到多线程访问早造成的内存穿透问题，采用双重检查的形式解决。
     */
    @Test
    public void testRedis02Hot30(){
        RedisSerializer redisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        List<Question> questionList = (List<Question>)redisTemplate.opsForValue().get("questionHot10");
//        redisTemplate.setValueSerializer(redisSerializer);
        // 防止首次访问该接口有大量用户，造成内存穿透，使redis没有效果
        if(null == questionList){
            synchronized (this){
                if(null == questionList){
                    System.out.println("从mysql中查询数据中。。。。。。");
                    // 从数据库中查询数据
                    questionList = questionExtMapper.selectTop10();
                    System.err.println(questionList);
                    // 放入redis
                    redisTemplate.opsForValue().set("questionHot10",questionList);
                }
            }
        }else{
            System.out.println("从redis中查询数据中。。。。。。");
        }
    }

/*    @Test
    public void testRedis03Hot10(){

        List<Question> questionList = (List<Question>)redisTempleService.get("questionHot10",Question.class);
//        redisTemplate.setValueSerializer(redisSerializer);
        // 防止首次访问该接口有大量用户，造成内存穿透，使redis没有效果
        if(null == questionList){
            synchronized (this){
                if(null == questionList){
                    System.out.println("从mysql中查询数据中。。。。。。");
                    // 从数据库中查询数据
                    questionList = questionExtMapper.selectTop10();
                    System.err.println(questionList);
                    // 放入redis
                    redisTempleService.set("questionHot10",questionList,600, TimeUnit.SECONDS);
                }
            }
        }else{
            System.out.println("从redis中查询数据中。。。。。。");
            System.out.println(questionList);
        }
    }*/
//https://blog.csdn.net/qq_41879167/article/details/89564762
    //【决定用这个了】
    @Test
    public void testRedis04Hot10(){

        List<Question> questionList = questionRedisTemplate.opsForList().range("questionHot10",0,-1);
//        redisTemplate.setValueSerializer(redisSerializer);
        // 防止首次访问该接口有大量用户，造成内存穿透，使redis没有效果
        if(questionList.isEmpty()==true){
            synchronized (this){
                if(questionList.isEmpty()==true){
                    System.out.println("从mysql中查询数据中。。。。。。");
                    // 从数据库中查询数据
                    questionList = questionExtMapper.selectTop10();
                    System.err.println(questionList);
                    // 放入redis
                    for (Question question : questionList) {
                        questionRedisTemplate.opsForList().leftPush("questionHot10",question);
                    }
                    questionRedisTemplate.expire("questionHot10",60 , TimeUnit.SECONDS);

                }
            }
        }else{
            System.out.println("从redis中查询数据中。。。。。。");
            System.out.println(questionList);
        }
    }





}
