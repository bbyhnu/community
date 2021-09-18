package life.majiang.community.Controller;

import life.majiang.community.dto.PaginationDTO;
import life.majiang.community.dto.QuestionDTO;
import life.majiang.community.model.Notification;
import life.majiang.community.model.PageResult;
import life.majiang.community.model.Question;
import life.majiang.community.model.User;
import life.majiang.community.service.NotificationService;
import life.majiang.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by codedrinker on 2019/5/15.
 */
@Controller
public class ProfileController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/profile/{action}")
    public String profile(HttpServletRequest request,
                          @PathVariable(name = "action") String action,
                          Model model,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        //我的提问
        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");
//            PaginationDTO paginationDTO = questionService.list(user.getId(), page, size);
//            model.addAttribute("pagination", questionList);
            //我先用我的翻页方式实现
            PageResult<Question> questionPageResult = questionService.selUserQuestions(user.getId(),page,size);
            List<QuestionDTO> questionList = questionService.fromQuestionToQuestionDTO(questionPageResult.getRows());
            model.addAttribute("page", questionPageResult);
            model.addAttribute("questions", questionList);
        } else if ("replies".equals(action)) {
            //PaginationDTO paginationDTO = notificationService.list(user.getId(), page, size);
            PageResult<Notification> notificationPageResult = notificationService.selUserNotifications(user.getId(), page, size);
//            Long unreadCount = notificationService.unreadCount();//后来不从这读了，直接从拦截器取unreadCount
            model.addAttribute("section", "replies");
//            model.addAttribute("pagination", paginationDTO);
            model.addAttribute("sectionName", "最新回复");
            model.addAttribute("page", notificationPageResult);
            model.addAttribute("notifications", notificationPageResult.getRows());
        }
        return "profile";
    }
}
