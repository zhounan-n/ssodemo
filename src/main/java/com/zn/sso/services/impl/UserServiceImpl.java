package com.zn.sso.services.impl;

import com.zn.sso.dao.JedisClient;
import com.zn.sso.dao.UserDao;
import com.zn.sso.entities.TaotaoResult;
import com.zn.sso.entities.TbUser;
import com.zn.sso.services.UserService;
import com.zn.sso.utils.CookieUtils;
import com.zn.sso.utils.HttpClientUtil;
import com.zn.sso.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;


/**
 * 用户管理Service
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao tbUserMapper;

    @Autowired
    private JedisClient jedisClient;

    @Value("${REDIS_USER_SESSION_KEY}")
    private String REDIS_USER_SESSION_KEY;
    @Value("${SSO_SESSION_EXPIRE}")
    private Integer SSO_SESSION_EXPIRE;


    //！！！ 单点登录demo 下面的account=content不合适 使用请自行修改
    /*public TaotaoResult checkData(String content, Integer type) {
        //对数据进行校验：1、2、3分别代表username、phone、email
        //用户名校验
        String account = "";
        if (1 == type) {
            account = content;
            //电话校验
        } else if (2 == type) {
            account = content;
            //email校验
        } else {
            account = content;
        }
        //执行查询
        List<TbUser> list = tbUserMapper.selectByExample(example);
        if (list == null || list.size() == 0) {
            return TaotaoResult.ok(true);
        }
        return TaotaoResult.ok(false);
    }
*/
    @Override
    public TaotaoResult createUser(TbUser user) {
        user.setUpdated(new Date());
        user.setCreated(new Date());
        //md5加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        tbUserMapper.insert(user);
        return TaotaoResult.ok();
    }

    /**
     * 用户登录
     */
    @Override
    public TaotaoResult userLogin(String username, String password,
                                  HttpServletRequest request, HttpServletResponse response) {

        TbUser user = tbUserMapper.getByName(username);
        //如果没有此用户名
        if (null == user) {
            return TaotaoResult.build(400, "用户名或密码错误");
        }
        //比对密码
        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            return TaotaoResult.build(400, "用户名或密码错误");
        }
        //生成token
        String token = UUID.randomUUID().toString();
        //保存用户之前，把用户对象中的密码清空。
        user.setPassword(null);
        //把用户信息写入redis
        jedisClient.set(REDIS_USER_SESSION_KEY + ":" + token, JsonUtils.objectToJson(user));
        //设置session的过期时间
        jedisClient.expire(REDIS_USER_SESSION_KEY + ":" + token, SSO_SESSION_EXPIRE);

        //添加写cookie的逻辑，cookie的有效期是关闭浏览器就失效。
        CookieUtils.setCookie(request, response, "TT_TOKEN", token);

        //返回token
        return TaotaoResult.ok(token);
    }


    @Override
    public TaotaoResult getUserByToken(String token) {

        //根据token从redis中查询用户信息
        String json = jedisClient.get(REDIS_USER_SESSION_KEY + ":" + token);
        //判断是否为空
        if (StringUtils.isBlank(json)) {
            return TaotaoResult.build(400, "此session已经过期，请重新登录");
        }
        //更新过期时间
        jedisClient.expire(REDIS_USER_SESSION_KEY + ":" + token, SSO_SESSION_EXPIRE);
        //返回用户信息
        return TaotaoResult.ok(JsonUtils.jsonToPojo(json, TbUser.class));
    }



}
