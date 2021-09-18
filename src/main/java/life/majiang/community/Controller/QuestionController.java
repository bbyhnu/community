package life.majiang.community.Controller;

import life.majiang.community.dto.CommentDTO;
import life.majiang.community.dto.QuestionDTO;
import life.majiang.community.enums.CommentTypeEnum;
import life.majiang.community.exception.CustomizeErrorCode;
import life.majiang.community.exception.CustomizeException;
import life.majiang.community.model.Question;
import life.majiang.community.service.CommentService;
import life.majiang.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by codedrinker on 2019/5/21.
 *  这里其实缺少了一个功能，当点击问题下面的tag时，会跳到页面/(page=1,tag=${tag}
 *  那么应该根据tag查出所有相关问题并进行展示
 */
@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") String id, Model model) {
        Long questionId = null;
        try {
            questionId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new CustomizeException(CustomizeErrorCode.INVALID_INPUT);
        }
        QuestionDTO questionDTO = questionService.getById(questionId);
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(questionId, CommentTypeEnum.QUESTION);
        //累加阅读数
        questionService.incView(questionId);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);
        return "question";
    }
    @GetMapping("testRedis")
    public @ResponseBody Question question01(){
        Question question = questionService.saveToRedis();
        return question;
    }
    @GetMapping("testRediss")
    public @ResponseBody Question question02(){
        Question question = questionService.saveToRedis2();
        return question;
    }
    @GetMapping("testRedisss/{id}")
    public @ResponseBody Question question03(@PathVariable(name = "id") String id){
        Question question = questionService.saveToRedis3(Long.parseLong(id));
        return question;
    }
}
