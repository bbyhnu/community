package life.majiang.community.provider;

import com.alibaba.fastjson.JSON;
import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.provider.dto.GithubUser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GithubProvider {
    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    public String getAccessToken(AccessTokenDTO accessTokenDTO){

        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setRedirect_uri(redirectUri);
        //使用OkHttp做Post请求
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(accessTokenDTO));
        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(body)
                .build();
        if (false){
            return "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&";
        }
        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
            //access_token=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&scope=user&token_type=bearer
            System.out.println(string);
            String accessToken = string.split("&")[0].split("=")[1];
            return accessToken;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GithubUser getUser(String accessToken){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/user")
                .header("Authorization","token "+accessToken)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            GithubUser githubUser = JSON.parseObject(string, GithubUser.class);//自动将string解析为java类对象

            return githubUser;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
