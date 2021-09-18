package life.majiang.community.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangwei
 * 2020/7/10 7:58
 */
@Configuration
public class ESClientConfig {
    @Bean
    public RestHighLevelClient highLevelClientBby(){
        //new一个高级客户端      搞定springboot与elastic集成
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        return client;
    }
}
