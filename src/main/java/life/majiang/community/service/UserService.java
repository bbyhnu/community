package life.majiang.community.service;

import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.User;
import life.majiang.community.model.UserExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by codedrinker on 2019/5/23.
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    //判断user是否在表中，如果在就更新登录信息，如果在就插入
    public void createOrUpdate(User user) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andAccountIdEqualTo(user.getAccountId())
                .andTypeEqualTo(user.getType());
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() == 0) {
            // 插入
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
        } else {
            //更新
            User dbUser = users.get(0);
            User updateUser = new User();
            updateUser.setGmtModified(System.currentTimeMillis());
            updateUser.setAvatarUrl(user.getAvatarUrl());
            updateUser.setName(user.getName());
            updateUser.setToken(user.getToken());
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
        }
    }
    @Cacheable(cacheNames = "user",key = "#creator")
    public User getUser(Long creator) {
        User user = userMapper.selectByPrimaryKey(creator);
        return user;
    }
}
