package life.majiang.community.Controller;

import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.User;
import life.majiang.community.provider.GithubProvider;
import life.majiang.community.provider.dto.GithubUser;
import life.majiang.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;
    @Autowired
    private GithubProvider githubProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @GetMapping("/callback")
    public String callback(//@RequestParam(name="code") String code,
                           //@RequestParam(name = "state") String state,
                           HttpServletRequest request,
                           HttpServletResponse response){
//        //用okHttp模拟Post请求
//        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();//shift+Enter  从中间直接回车
//        accessTokenDTO.setCode(code);
//        accessTokenDTO.setRedirect_uri(redirectUri);
//        accessTokenDTO.setState(state);
//        accessTokenDTO.setClient_id(clientId);
//        accessTokenDTO.setClient_secret(clientSecret);
//
//        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
//        GithubUser githubUser = githubProvider.getUser(accessToken);
//        // githubProvider.getAccessToken(new AccessTokenDTO());//Ctrl+Alt+V 对着括号里的对象，快速创建变量
//        //GithubUser{name='BobYang', id=67583679, bio='come on', avatarUrl='https://avatars.githubusercontent.com/u/67583679?v=4'}
//        System.out.println(githubUser.getName());
        GithubUser githubUser = new GithubUser();
        githubUser.setName("BobYangAA");
        githubUser.setId((long) 67583679);
        githubUser.setBio("come on");
        githubUser.setAvatarUrl("https://avatars.githubusercontent.com/u/67583679?v=4");
        if (githubUser != null){
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setBio(githubUser.getBio());
            user.setAvatarUrl(githubUser.getAvatarUrl());
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            user.setType("root");
//            userMapper.insertUser(user);
            userService.createOrUpdate(user);
            //登录成功，写cookie和session【修改网页为已登录状态】
            request.getSession().setAttribute("githubUser",githubUser);//在银行注册了信息
            response.addCookie(new Cookie("token",token));//创建一个cookie，用来在index中保持登录状态
            return "redirect:/";//重定向，修改地址栏

        }else{
            //登录失败，重新登陆
            return "redirect:/";
        }

    }
    //用户登出，需要清楚cookie和session
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        request.getSession().invalidate();
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/";
    }
}
