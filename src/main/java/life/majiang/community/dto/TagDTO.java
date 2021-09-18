package life.majiang.community.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by codedrinker on 2019/6/5.
 *  创建了一类标签
 *  categoryName是标签所处类(如spring)
 *  tags是其类下的具体标签(如ioc，aop)
 */
@Data
public class TagDTO {
    private String categoryName;
    private List<String> tags;
}
