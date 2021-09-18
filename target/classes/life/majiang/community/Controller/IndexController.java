package life.majiang.community.Controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import life.majiang.community.cache.HotTagCache;
import life.majiang.community.dto.QuestionDTO;
import life.majiang.community.mapper.QuestionMapper;
import life.majiang.community.mapper.UserMapper0;
import life.majiang.community.model.PageResult;
import life.majiang.community.model.Question;
import life.majiang.community.model.User;
import life.majiang.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private UserMapper0 userMapper;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private HotTagCache hotTagCache;

    @GetMapping("/")//什么都不输入，默认访问/ 根目录
    public String index(HttpServletRequest request,
                        Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "10") Integer size,
                        @RequestParam(name = "tag", required = false) String tag,
                        @RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "sort", required = false) String sort){// required = false 不传值后台也不会报错
        PageResult<Question> questionPageResult = questionService.selAllQuestions(search, page, size ,tag, sort);
        List<QuestionDTO> questionList = questionService.fromQuestionToQuestionDTO(questionPageResult.getRows());
        List<String> tags = hotTagCache.getHots();
        model.addAttribute("page", questionPageResult);
//        List<QuestionDTO> questionList = questionService.list(0,5);
        model.addAttribute("questions", questionList);
        model.addAttribute("search", search);//search也要放进去，因为搜索状态下的翻页时还需要search信息
        model.addAttribute("tag", tag);
        model.addAttribute("tags", tags);
        model.addAttribute("sort", sort);
        return "index";
    }


}
