package life.majiang.community.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import life.majiang.community.cache.QuestionCache;
import life.majiang.community.dto.PaginationDTO;
import life.majiang.community.dto.QuestionDTO;
import life.majiang.community.dto.QuestionQueryDTO;
import life.majiang.community.enums.SortEnum;
import life.majiang.community.exception.CustomizeErrorCode;
import life.majiang.community.exception.CustomizeException;
import life.majiang.community.mapper.QuestionExtMapper;
import life.majiang.community.mapper.QuestionMapper;
import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.PageResult;
import life.majiang.community.model.Question;
import life.majiang.community.model.QuestionExample;
import life.majiang.community.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by codedrinker on 2019/5/7.
 */
@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionExtMapper questionExtMapper;

/*    @Autowired
    private UserMapper userMapper;*/
    @Autowired
    private UserService userService;

    @Autowired
    private QuestionCache questionCache;
    
    @Autowired
    private RedisTemplate<Object, Question> questionRedisTemplate;
    @Autowired
    private RedisCacheManager redisCacheManager;

    //??????redis
    public Question saveToRedis(){
            Question question = questionMapper.selectByPrimaryKey(1L);
            questionRedisTemplate.opsForValue().set("q-01",question);
            return question;
    }
    //??????redis
    public Question saveToRedis2(){
        Question question = questionMapper.selectByPrimaryKey(8L);
        Cache cache = redisCacheManager.getCache("q-08");
        cache.put("q:08",question);
        return question;
    }
    @Cacheable(cacheNames = "q-13")
    public Question saveToRedis3(Long id ){
        Question question = questionMapper.selectByPrimaryKey(id);
        return question;
    }

    //????????????????????????
    public PageResult<Question> selUserQuestions(Long id, Integer page, Integer size) {
        Page ps = PageHelper.startPage(page, size);

        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria().andCreatorEqualTo(id);
        questionExample.setOrderByClause("gmt_create desc");
        List<Question> questions = questionMapper.selectByExample(questionExample);
        if (questions.size() == 0) {
            throw new CustomizeException(CustomizeErrorCode.QUESTIONS_NOT_FOUND);
        }
        //List<Question> questions = questionMapper.listById(id);
        PageResult<Question> result = new PageResult<Question>();
        result.setRows(questions);
        result.setTotal(ps.getTotal());
        result.setPages(ps.getPages());
        result.setCurrentPage(page);
        return result;
    }
    public PageResult<Question> selAllQuestions(String search, Integer page, Integer size, String tag, String sort) {
        //top10??????redis??????
        if ("top10".equals(sort)){
            PageResult<Question> result = new PageResult<Question>();
            result.setRows(findTop10());
            result.setTotal(1L);
            result.setPages(1);
            result.setCurrentPage(1);
            return result;
        }
        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        if (StringUtils.isNotBlank(tag)) {
            tag = tag.replace("+", "").replace("*", "").replace("?", "");
            questionQueryDTO.setTag(tag);
        }

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase().equals(sort)) {
                questionQueryDTO.setSort(sort);

                if (sortEnum == SortEnum.HOT7) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }
//        questionQueryDTO.setPage(0);
        questionQueryDTO.setSize(size);
        Page ps = PageHelper.startPage(page, size);///
//        QuestionExample questionExample = new QuestionExample();
//        questionExample.setOrderByClause("gmt_create desc");
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        if (questions.size() == 0) {
            throw new CustomizeException(CustomizeErrorCode.QUESTIONS_NOT_FOUND);
        }
        //List<Question> questions = questionMapper.list();
        PageResult<Question> result = new PageResult<Question>();
        result.setRows(questions);
        result.setTotal(ps.getTotal());
        result.setPages(ps.getPages());
        result.setCurrentPage(page);
        return result;
    }
    //??????redis??????????????????
    public List<Question> findTop10(){
        List<Question> questionList = questionRedisTemplate.opsForList().range("questionHot10",0,-1);
//        redisTemplate.setValueSerializer(redisSerializer);
        // ?????????????????????????????????????????????????????????????????????redis????????????
        if(questionList.isEmpty()==true){
            synchronized (this){
                if(questionList.isEmpty()==true){
                    System.out.println("???mysql?????????Top10?????????????????????");
                    // ???????????????????????????
                    questionList = questionExtMapper.selectTop10();
//                    System.err.println(questionList);
                    // ??????redis
                    for (Question question : questionList) {
                        questionRedisTemplate.opsForList().rightPush("questionHot10",question);//????????????rightPush???
                    }
                    questionRedisTemplate.expire("questionHot10",60*30 , TimeUnit.SECONDS);

                }
            }
        }else{
            System.out.println("???redis?????????Top10?????????????????????");
//            System.out.println(questionList);
        }
        return questionList;
    }
    public List<QuestionDTO> fromQuestionToQuestionDTO(List<Question> questions){
//        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);

        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            Long creator = question.getCreator();
            User user = userService.getUser(creator);
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);//?????????question??????????????????questionDTO
            questionDTO.setDescription("");
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }


    //?????????????????????????????????????????????
    public List<QuestionDTO> list(List<Question> questions){
//        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
//        List<Question> questions = questionMapper.list();
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userService.getUser(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);//?????????question??????????????????questionDTO
            questionDTO.setDescription("");
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }
    public PaginationDTO list(String search, String tag, String sort, Integer page, Integer size) {

        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }

        PaginationDTO paginationDTO = new PaginationDTO();

        Integer totalPage;

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        if (StringUtils.isNotBlank(tag)) {
            tag = tag.replace("+", "").replace("*", "").replace("?", "");
            questionQueryDTO.setTag(tag);
        }

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase().equals(sort)) {
                questionQueryDTO.setSort(sort);

                if (sortEnum == SortEnum.HOT7) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }

        Integer totalCount = questionExtMapper.countBySearch(questionQueryDTO);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);
        Integer offset = page < 1 ? 0 : size * (page - 1);
        questionQueryDTO.setSize(size);
        questionQueryDTO.setPage(offset);
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userService.getUser(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);//?????????question??????????????????questionDTO
            questionDTO.setDescription("");
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }
        List<QuestionDTO> stickies = questionCache.getStickies();
        if (stickies != null && stickies.size() != 0) {
            questionDTOList.addAll(0, stickies);
        }
        paginationDTO.setData(questionDTOList);
        return paginationDTO;
    }

    public PaginationDTO list(Long userId, Integer page, Integer size) {
        PaginationDTO paginationDTO = new PaginationDTO();

        Integer totalPage;

        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria()
                .andCreatorEqualTo(userId);
        Integer totalCount = (int) questionMapper.countByExample(questionExample);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);

        //size*(page-1)
        Integer offset = size * (page - 1);
        QuestionExample example = new QuestionExample();
        example.createCriteria()
                .andCreatorEqualTo(userId);
        List<Question> questions = questionMapper.selectByExampleWithRowbounds(example, new RowBounds(offset, size));
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userService.getUser(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        paginationDTO.setData(questionDTOList);
        return paginationDTO;
    }
    //??????QuestionController???question??????????????????????????????????????????
    public QuestionDTO getById(Long id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        User user = userService.getUser(question.getCreator());
        questionDTO.setUser(user);
        return questionDTO;
    }

    public void createOrUpdate(Question question) {
        if (question.getId() == null) {
            // ??????
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            question.setViewCount(0);
            question.setLikeCount(0);//????????????????????????0
            question.setCommentCount(0);//??????????????????0
            question.setSticky(0);
            questionMapper.insert(question);
        } else {
            // ??????

            Question dbQuestion = questionMapper.selectByPrimaryKey(question.getId());
            if (dbQuestion == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }

            if (dbQuestion.getCreator().longValue() != question.getCreator().longValue()) {
                throw new CustomizeException(CustomizeErrorCode.INVALID_OPERATION);
            }

            Question updateQuestion = new Question();
            updateQuestion.setGmtModified(System.currentTimeMillis());
            updateQuestion.setTitle(question.getTitle());
            updateQuestion.setDescription(question.getDescription());
            updateQuestion.setTag(question.getTag());
            QuestionExample example = new QuestionExample();
            example.createCriteria()
                    .andIdEqualTo(question.getId());//????????????id???question?????????updateQuestion????????????
            int updated = questionMapper.updateByExampleSelective(updateQuestion, example);
            if (updated != 1) {//?????????????????????????????????????????????????????????????????????
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

//    public void incView(Long id) {
//        Question question = new Question();
//        question.setId(id);
//        question.setViewCount(1);
//        questionExtMapper.incView(question);
//    }
    //??????redis???????????????
    public void incView(Long id) {
        String key = "question:"+id;
        //??????redis???increment??????
        long n=this.questionRedisTemplate.opsForValue().increment(key);
//        log.info("key={}???????????????{}",key,n);
//        Question question = new Question();
//        question.setId(id);
//        question.setViewCount(1);
//        questionExtMapper.incView(question);
    }

    //??????tag?????????????????????????????????????????????????????????
    //???????????????null????????????List?????????????????????"|"?????????
    //????????????????????? where id != #{id} and tag regexp #{tag} ?????????List<Question>???List<QuestionDTO>
    public List<QuestionDTO> selectRelated(QuestionDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        String[] tags = StringUtils.split(queryDTO.getTag(), ",");//??????????????????StringUtils.replace(",","|")???
        String regexpTag = Arrays
                .stream(tags)
                .filter(StringUtils::isNotBlank)
                .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));
        Question question = new Question();
        question.setId(queryDTO.getId());
        question.setTag(regexpTag);

        List<Question> questions = questionExtMapper.selectRelated(question);
        List<QuestionDTO> questionDTOS = questions.stream().map(q -> {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }



}
