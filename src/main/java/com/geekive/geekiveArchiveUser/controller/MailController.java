package com.geekive.geekiveArchiveUser.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.geekive.geekiveArchiveUser.common.Constants;
import com.geekive.geekiveArchiveUser.common.MessageUtil;
import com.geekive.geekiveArchiveUser.common.Util;
import com.geekive.geekiveArchiveUser.geekiveCustom.GeekiveMap;
import com.geekive.geekiveArchiveUser.mapper.service.MailService;

@Controller
@RequestMapping(value = "/email")
public class MailController {
	
	@Autowired
	private MailService mailService;

	@ResponseBody
	@PostMapping(value = {"", "/"})
	public GeekiveMap email(HttpServletRequest request, HttpServletResponse response, @RequestBody GeekiveMap gMap) throws Exception{
		String type 	= gMap.getString("purpose");
		String email 	= gMap.getString("email");
		try {
			if(Util.isEmpty(email)) {
				throw new Exception("Error. Server hasn't received the email address.");
			}
			
			if(!Constants.EMAIL_PURPOSE_SIGNUP.equals(type)) {
				throw new Exception("Error. Please contact the administrator.<br/>Possible cause : Unable to determine the purpose of the email transmission.");
			}

			String code = Util.verificationCodeGenerator();
			sendCode(request, response, type, email, code);
			gMap.put("code", code);
			gMap.setResultMessage(MessageUtil.getMessage("email.common.code.sent"));
		} catch (Exception e) {
			gMap.put("resultCode"	, 0);
			gMap.put("resultMessage", e.getMessage());
		}
		return gMap;
	}
	
	void sendCode(HttpServletRequest request, HttpServletResponse response, String type, String email, String code) throws Exception{
		switch (type) {
		case Constants.EMAIL_PURPOSE_SIGNUP :
			mailService.sendSignupCode(request, response, email, code);
			break;
		case Constants.EMAIL_PURPOSE_PASSWORD :
			mailService.sendPasswordResetCode(request, response, email, code);
			break;
		default:
			throw new IllegalArgumentException("Unknown email purpose.");
		}
	}
}
