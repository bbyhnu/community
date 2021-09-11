package com.bby.community.Controller;

import com.bby.community.dto.AccessTokenDTO;
import com.bby.community.provider.GithubProvider;
import com.bby.community.provider.dto.GithubUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/callback")
    public String callback(@RequestParam(name="code") String code,
                           @RequestParam(name = "state") String state){
        //用okHttp模拟Post请求
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();//shift+Enter  从中间直接回车
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);

        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser user = githubProvider.getUser(accessToken);
        //GithubUser{name='BobYang', id=67583679, bio='come on', avatarUrl='https://avatars.githubusercontent.com/u/67583679?v=4'}
        System.out.println(user);
        // githubProvider.getAccessToken(new AccessTokenDTO());//Ctrl+Alt+V 对着括号里的对象，快速创建变量
        return "index";//登录成功后返回index
    }
}
