package com.zn.sso.services;


import com.zn.sso.entities.TaotaoResult;
import com.zn.sso.entities.TbUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {

	//TaotaoResult checkData(String content, Integer type);
	TaotaoResult createUser(TbUser user);
	TaotaoResult userLogin(String username, String password, HttpServletRequest request, HttpServletResponse response);
	TaotaoResult getUserByToken(String token);

}
