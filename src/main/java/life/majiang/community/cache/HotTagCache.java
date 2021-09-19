package life.majiang.community.cache;

import life.majiang.community.dto.HotTagDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by codedrinker on 2019/8/2.
 */

@Component
@Data
public class HotTagCache {
//    private List<String> hots = new ArrayList<>();
//    private Map<String,Integer> tags = new HashMap<>();//用来测试输出的

    public List<String> updateTags(Map<String, Integer> tags) {
        int max = 5;//5   热点标签个数
        //https://www.cnblogs.com/Elliott-Su-Faith-change-our-life/p/7472265.html
        //PriorityQueue实现了Queue接口,其通过堆实现，具体说是通过完全二叉树（complete binary tree）
        // 实现的小顶堆,，也就意味着可以通过数组来作为PriorityQueue的底层实现。
        PriorityQueue<HotTagDTO> priorityQueue = new PriorityQueue<>(max);

        tags.forEach((name, priority) -> {
            HotTagDTO hotTagDTO = new HotTagDTO();
            hotTagDTO.setName(name);
            hotTagDTO.setPriority(priority);
            //如果优先队列成员不足5个，则直接加入
            if (priorityQueue.size() < max) {
                priorityQueue.add(hotTagDTO);
            } else {
                //已经5个了，
                HotTagDTO minHot = priorityQueue.peek();//获取但不删除队首元素，也就是队列中权值最小的那个元素
                if (hotTagDTO.compareTo(minHot) > 0) {
                    priorityQueue.poll();//获取并删除队首元素
                    priorityQueue.add(hotTagDTO);//向优先队列中插入元素
                    //新加入的元素x可能会破坏小顶堆的性质，因此需要进行调整。调整的过程为：从k指定的位置开始，
                    // 将x逐层与当前点的parent进行比较并交换，直到满足x >= queue[parent]为止。
                    // 注意这里的比较可以是元素的自然顺序，也可以是依靠比较器的顺序。
                }
            }
        });


        List<String> sortedTags = new ArrayList<>();

        HotTagDTO poll = priorityQueue.poll();
        while (poll != null) {
            sortedTags.add(0, poll.getName());
            poll = priorityQueue.poll();
        }
//        System.out.println(sortedTags);
//        hots = sortedTags;
        return sortedTags;
    }
}
