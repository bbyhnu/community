package life.majiang.community.service;

import life.majiang.community.cache.HotTagCache;
import life.majiang.community.model.Question;
import life.majiang.community.schedule.HotTagTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TagCacheService {
    @Autowired
    private HotTagTasks hotTagTasks;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public List<String> getHots() {
//        List<String> hotTag = hotTagTasks.getHotTag();
        List<String> hotTagList = stringRedisTemplate.opsForList().range("hotTag5", 0, -1);
        if (hotTagList.isEmpty() == true) {
            synchronized (this) {
                if (hotTagList.isEmpty() == true) {
                    System.out.println("从mysql中查询hotTag中。。。。。。");
                    // 从数据库中查询数据
                    hotTagList = hotTagTasks.getHotTag();
//                    System.err.println(questionList);
                    // 放入redis
                    for (String hotTag : hotTagList) {
                        stringRedisTemplate.opsForList().rightPush("hotTag5", hotTag);//【应该是rightPush】
                    }
                    stringRedisTemplate.expire("hotTag5", 60 * 60 * 24, TimeUnit.SECONDS);

                }
            }
        } else {
            System.out.println("从redis中查询hotTag中。。。。。。");
//            System.out.println(questionList);
        }
        return hotTagList;
    }
}
