package life.majiang.community.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import life.majiang.community.CommunityApplication;
import life.majiang.community.cache.QuestionCache;
import life.majiang.community.mapper.QuestionExtMapper;
import life.majiang.community.mapper.QuestionMapper;
import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.Question;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ESQuestionService {
    @Resource
    private RestHighLevelClient client;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionExtMapper questionExtMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QuestionCache questionCache;

    private static final String QUESTION_INDEX = "question_latest";

    //????????????????????????ES
    public boolean addQuestion(Question question) throws IOException {

        String id = Long.toString(question.getId());
        IndexRequest request = new IndexRequest(QUESTION_INDEX).id(id).source(beanToMap(question));
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        client.indices();
        System.out.println(JSONObject.toJSON(response));
        return false;
    }
    /**
     * ???????????????????????????????????????ES
     */
    public boolean importAll() throws IOException {
        //deleteAllQuestion();
        List<Question> list = questionExtMapper.selectAll();
        for (Question question : list) {
            addQuestion(question);
        }
        return true;
    }
    public List<Question> highLightSearch(String field, String keyword, Integer pageNum, Integer pageSize) {
        List<Question> resList = new ArrayList<>();

        // 1. ?????? SearchRequest
        SearchRequest searchRequest = new SearchRequest(QUESTION_INDEX);

        // 2. ?????? SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 3. ???SearchSourceBuilder????????????????????????
        // ??????term????????????
        // searchSourceBuilder.query(QueryBuilders.termQuery(field, keyword));
        // ??????term???????????????????????????????????????????????????match??????
        searchSourceBuilder.query(QueryBuilders.matchQuery(field, keyword));
        // ??????????????????
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // ??????????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(field);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // 4. ??? SearchSourceBuilder ????????? SearchRequest
        searchRequest.source(searchSourceBuilder);
        try {
            // 5. ??????
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            // 6. ??????????????????
            SearchHit[] hits = response.getHits().getHits();
            for (SearchHit hit : hits) {
                // ?????????????????????????????????
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                // ?????? ??????field????????????????????????
                Text[] fragments = highlightFields.get(field).fragments();
                // ??????
                StringBuilder builder = new StringBuilder();
                for (Text text : fragments) {
                    builder.append(text);
                }
                // ???????????????????????????????????????
                Map<String, Object> source = hit.getSourceAsMap();
                source.put(field, builder.toString());
                //map??????json?????????
                String str = JSON.toJSONString(source);
                // ??????????????????????????????
                Question question = JSONObject.parseObject(str, Question.class);
                resList.add(question);
            }
        } catch (IOException e) {
            System.out.println("===??????????????????===");
            e.printStackTrace();
        }
        return resList;
    }
    public List<Question> pageSearch(String field, String keyword, Integer pageNum, Integer pageSize) {
        List<Question> resList = new ArrayList<>();

        // 1. ?????? SearchRequest
        SearchRequest searchRequest = new SearchRequest(QUESTION_INDEX);

        // 2. ?????? SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 3. ???SearchSourceBuilder????????????????????????
        // ??????term????????????
        // searchSourceBuilder.query(QueryBuilders.termQuery(field, keyword));
        // ??????term???????????????????????????????????????????????????match??????
        searchSourceBuilder.query(QueryBuilders.matchQuery(field, keyword));
        // ??????????????????
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 4. ??? SearchSourceBuilder ????????? SearchRequest
        searchRequest.source(searchSourceBuilder);
        try {
            // 5. ??????
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            // 6. ??????????????????
            SearchHit[] hits = response.getHits().getHits();
            for (SearchHit hit : hits) {
                // ??????????????????????????????
                Question question = JSONObject.parseObject(hit.getSourceAsString(), Question.class);
                resList.add(question);
            }
        } catch (IOException e) {
            System.out.println("===??????????????????===");
            e.printStackTrace();
        }
        return resList;
    }
    /**
     * ?????????map
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = new HashMap<>();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                if (beanMap.get(key) != null) {
                    map.put(key + "", beanMap.get(key));
                }
            }
        }
        return map;
    }

    public boolean deleteAllQuestion() throws IOException {
        //1?????????????????????
        DeleteIndexRequest request = new DeleteIndexRequest(QUESTION_INDEX);
        //2???????????????????????? Indicesclient,?????????????????????
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        return delete.isAcknowledged();
    }


}
