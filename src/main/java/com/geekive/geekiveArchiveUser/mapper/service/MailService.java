package com.geekive.geekiveArchiveUser.mapper.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.geekive.geekiveArchiveUser.common.MessageUtil;

@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendSignupCode(HttpServletRequest request, HttpServletResponse response, String email, String code)
			throws ServletException, IOException, MessagingException {
		sendCode(request, response, email, code, "signup-code.jsp", "email.signup.subject");
	}

	public void sendPasswordResetCode(HttpServletRequest request, HttpServletResponse response, String email, String code)
			throws ServletException, IOException, MessagingException {
		sendCode(request, response, email, code, "password-reset-code.jsp", "email.password.subject");
	}

	private void sendCode(HttpServletRequest request, HttpServletResponse response, String email, String code,
			String template, String subjectKey) throws ServletException, IOException, MessagingException {
		StringWriter html = new StringWriter();
		PrintWriter writer = new PrintWriter(html);
		HttpServletResponseWrapper templateResponse = new HttpServletResponseWrapper(response) {
			@Override
			public PrintWriter getWriter() {
				return writer;
			}
		};
		request.getRequestDispatcher("/WEB-INF/jsp/common/email/" + template).include(request, templateResponse);

		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setTo(email);
		helper.setSubject(MessageUtil.getMessage(subjectKey));
		helper.setText(html.toString().replace("{{code}}", code), true);
		javaMailSender.send(message);
	}
}
