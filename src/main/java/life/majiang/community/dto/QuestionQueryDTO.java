package life.majiang.community.dto;

import lombok.Data;

/**
 * Created by codedrinker on 2019/7/1.
 * 用来搜索问题的
 */
@Data
public class QuestionQueryDTO {
    private String search;
    private String sort;
    private Long time;
    private String tag;
    private Integer page;
    private Integer size;
}
