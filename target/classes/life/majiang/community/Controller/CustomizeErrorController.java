package life.majiang.community.Controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by codedrinker on 2019/5/28.
 *  通过实现ErrorController来自定义自己的异常页面
 *  在项目中我们遇到404找不到的错误、或者500服务器错误都需要配置相应的页面给用户一个友好的提示
 *  需要实现ErrorController接口，重写handleError方法
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")//默认就是/error
public class CustomizeErrorController implements ErrorController {

    @Override
    public String getErrorPath() {
        return "error";
    }

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, Model model) {
        HttpStatus status = getStatus(request);

        if (status.is4xxClientError()) {//400是客户端引起的
            model.addAttribute("message", "你这个请求错了吧，要不然换个姿势？");
        }
        if (status.is5xxServerError()) {//500是服务器引起的
            model.addAttribute("message", "服务冒烟了，要不然你稍后再试试！！！");
        }

        return new ModelAndView("error");
    }
    //源码
    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
