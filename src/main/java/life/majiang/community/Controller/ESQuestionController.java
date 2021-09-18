package life.majiang.community.Controller;

import life.majiang.community.dto.QuestionDTO;
import life.majiang.community.model.Question;
import life.majiang.community.service.CommentService;
import life.majiang.community.service.ESQuestionService;
import life.majiang.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("ESQuestion")
public class ESQuestionController {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private ESQuestionService esQuestionService;

    @Autowired
    private CommentService commentService;

/*    @RequestMapping("importAll")
    public String importAll() throws IOException {
        esQuestionService.importAll();
        return "success";
    }*/
    @RequestMapping("/search/{keyword}/{pageNum}/{pageSize}")
    public String list(@PathVariable("keyword") String keyword,
                                          @PathVariable("pageNum") Integer pageNum,
                                          @PathVariable("pageSize")Integer pageSize,
                                          Model model) throws IOException {
        esQuestionService.importAll();
        List<Question> list = esQuestionService.highLightSearch("title", keyword, pageNum, pageSize);
        List<QuestionDTO> questionDTOS = questionService.fromQuestionToQuestionDTO(list);
        model.addAttribute("questions", questionDTOS);
        return "esIndex";
    }

}
