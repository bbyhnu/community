package life.majiang.community.Controller;

import life.majiang.community.mapper.UserMapper0;
import life.majiang.community.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @Autowired
    private UserMapper0 userMapper;
    @GetMapping("/")//什么都不输入，默认访问/ 根目录
    public String index(HttpServletRequest request){
        //获取cookie，若能和数据库中的user信息对上，就保持登录状态
        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                String token = cookie.getValue();
                User user = userMapper.findByToken(token);
                if (user != null) {
                    request.getSession().setAttribute("user", user);
                }
                break;
            }
        }

        return "index";
    }


}
