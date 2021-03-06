package life.majiang.community.schedule;

import life.majiang.community.cache.HotTagCache;
import life.majiang.community.mapper.QuestionMapper;
import life.majiang.community.model.Question;
import life.majiang.community.model.QuestionExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by codedrinker on 2019/8/1.
 */
@Component
@Slf4j
public class HotTagTasks {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private HotTagCache hotTagCache;
//    @Scheduled(fixedRate = 20000)
//    @Scheduled(fixedRate = 1000 * 60 * 60 * 3)
//    public void hotTagSchedule() {
    public List<String> getHotTag() {
        int offset = 0;
        int limit = 20;
        log.info("hotTagSchedule start {}", new Date());
        List<Question> list = new ArrayList<>();

        Map<String, Integer> priorities = new HashMap<>();
        //将tag与对应得分放入map中
        while (offset == 0 || list.size() == limit) {
            list = questionMapper.selectByExampleWithRowbounds(new QuestionExample(), new RowBounds(offset, limit));
            for (Question question : list) {
                String[] tags = StringUtils.split(question.getTag(), ",");
                for (String tag : tags) {
                    Integer priority = priorities.get(tag);
                    if (priority != null) {
                        priorities.put(tag, priority + 10 + 5*question.getCommentCount() + question.getViewCount());
                    } else {
                        priorities.put(tag, 10 + 5*question.getCommentCount() + question.getViewCount());//5 + question.getCommentCount()
                    }
                }
            }
            offset += limit;
        }


//        hotTagCache.setTags(priorities);
//        hotTagCache.getTags().forEach(
//                (k,v)->{
//                    System.out.print(k);
//                    System.out.print(":");
//                    System.out.print(v);
//                    System.out.println();
//                }
//        );
        List<String> newTags = hotTagCache.updateTags(priorities);
        return newTags;
//        log.info("hotTagSchedule stop {}", new Date());
    }
}
