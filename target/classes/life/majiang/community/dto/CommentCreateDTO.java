package life.majiang.community.dto;

import lombok.Data;

/**
 * Created by codedrinker on 2019/5/30.
 * 这个是浏览器ajax传回来的Comment对象
 */
@Data
public class CommentCreateDTO {
    private Long parentId;
    private String content;
    private Integer type;
}
