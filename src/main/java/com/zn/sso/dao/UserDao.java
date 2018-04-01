package com.zn.sso.dao;

import com.zn.sso.entities.TbUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


public interface UserDao {

    @Insert(value = "INSERT INTO tb_user (id,username,password,phone,email,created,updated) VALUES(#{user.id},#{user.username},#{user.password},#{user.phone},#{user.email},NOW(),NOW())")
    int insert(@Param("user") TbUser user);

    @Select(value = "SELECT * FROM tb_user WHERE username=#{0} LIMIT 1")
    TbUser getByName(String userName);
}
