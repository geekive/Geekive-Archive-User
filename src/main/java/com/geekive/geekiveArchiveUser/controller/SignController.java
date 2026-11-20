package com.geekive.geekiveArchiveUser.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import com.geekive.geekiveArchiveUser.common.Constants;
import com.geekive.geekiveArchiveUser.common.JwtUtil;
import com.geekive.geekiveArchiveUser.common.MessageUtil;
import com.geekive.geekiveArchiveUser.common.Util;
import com.geekive.geekiveArchiveUser.geekiveCustom.GeekiveMap;
import com.geekive.geekiveArchiveUser.mapper.service.SignService;

@Controller
@RequestMapping("/sign")
public class SignController {
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Resource
	SignService signService;

	private static final Logger logger = LoggerFactory.getLogger(SignController.class);
	private static final String RESET_STATE = SignController.class.getName();
	private static final long CODE_VALIDITY = TimeUnit.MINUTES.toMillis(3);
	private static final long RESET_VALIDITY = TimeUnit.MINUTES.toMillis(10);
	private static final long RESEND_DELAY = TimeUnit.MINUTES.toMillis(1);
	private static final int MAX_ATTEMPTS = 5;
	private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
	private static final Pattern PASSWORD = Pattern.compile(
			"(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?:{}|<>_-])[A-Za-z0-9!@#$%^&*(),.?:{}|<>_-]{10,72}");

	private final Map<String, Long> nextMailByUser = new HashMap<>();

	@Autowired
	MailController mailController;

	@GetMapping(value = "/signup")
	public String signupGet() throws Exception{
		return "home/sign/signup";
	}
	
	@ResponseBody
	@PostMapping(value = "/signup")
	public GeekiveMap signupPost(@RequestBody GeekiveMap gMap) throws Exception{
		try {
			gMap.put("userUid", Util.generateUID("USR"));
			signService.insertUser(gMap);
			gMap.setResultMessage(MessageUtil.getMessage("home.signup.complete"));
		} catch (Exception e) {
			gMap.setResultCode(0);
			gMap.setResultMessage(e.getMessage());
		}
		return gMap;
	}
	
	@GetMapping(value = "/signin")
	public String signinGet() throws Exception{
		return "home/sign/signin";
	}
	
	@ResponseBody
	@PostMapping(value = "/signin")
	public GeekiveMap signinPost(HttpServletResponse response, HttpSession session, @RequestBody GeekiveMap gMap) throws Exception{
		GeekiveMap userMap = null;
		try {
			userMap = signService.selectUser(gMap);	
			if(Util.isEmpty(userMap)) {
				throw new Exception(MessageUtil.getMessage("home.signin.message.fail"));
			}else {
				/* cookie */
				ResponseCookie cookie = jwtUtil.generateToken(userMap.getString("userUid"));
				response.addHeader("Set-Cookie", cookie.toString());
				
				/* session */
				session.setAttribute("isSignedIn"	, true);
				session.setAttribute("userMap"		, userMap);
			}
		} catch (Exception e) {
			gMap.setResultCode(0);
			gMap.setResultMessage(e.getMessage());
		}	
		return gMap;
	}
	
	@GetMapping(value = "/signout")
	public String signout(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception{
		session.invalidate();
		jwtUtil.invalidateToken(request, response);
		return "redirect:/";
	}

	@ResponseBody
	@PostMapping(value = "/checkEmailExistence")
	public GeekiveMap checkEmailExistence(HttpServletRequest request, @RequestBody GeekiveMap gMap) throws Exception{
		try {
			if(signService.checkEmailExistence(gMap)) {
				throw new Exception(MessageUtil.getMessage("home.signup.message.email.exist"));
			}
		} catch (Exception e) {
			gMap.setResultCode(0);
			gMap.setResultMessage(e.getMessage());
		}
		return gMap;
	}
	
	@ResponseBody
	@PostMapping(value = "/checkArchiveNameExistence")
	public GeekiveMap checkArchiveNameExistence(HttpServletRequest request, @RequestBody GeekiveMap gMap) throws Exception{
		try {
			if(signService.checkArchiveNameExistence(gMap)) {
				throw new Exception(MessageUtil.getMessage("home.signup.message.archive.exist"));
			}
			if(Arrays.asList(Constants.BANNED_ARCHIVE_NAME).contains(gMap.getString("archiveName"))) {
				throw new Exception(MessageUtil.getMessage("home.signup.message.archive.exist"));
			}
		} catch (Exception e) {
			gMap.setResultCode(0);
			gMap.setResultMessage(e.getMessage());
		}
		return gMap;
	}

	@GetMapping(value = "/forgot-password")
	public String forgotPassword(HttpSession session, ModelMap modelMap) {
		synchronized (WebUtils.getSessionMutex(session)) {
			ResetState state = getState(session);
			if (!state.token.equals(modelMap.remove("continueReset"))) {
				state.clear();
				state.token = UUID.randomUUID().toString();
			}
			modelMap.put("resetToken", state.token);
			modelMap.put("codeRequested", state.code != null && state.expiresAt > System.currentTimeMillis());
			modelMap.put("resetEmail", state.email);
			modelMap.put("remainingCodeSeconds",
					Math.max(0, (state.expiresAt - System.currentTimeMillis() + 999) / 1000));
		}
		return "home/sign/forgot-password";
	}

	@PostMapping(value = "/forgot-password/send")
	public String sendCode(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			@RequestParam String resetToken, @RequestParam String email, RedirectAttributes redirect) {
		email = email.trim();
		synchronized (WebUtils.getSessionMutex(session)) {
			ResetState state = getState(session);
			long now = System.currentTimeMillis();
			if (!state.token.equals(resetToken)) {
				redirect.addFlashAttribute("resetError", "home.password.reset.expired");
			} else if (email.isEmpty()) {
				redirect.addFlashAttribute("resetError", "home.signup.message.email.fill");
			} else if (email.length() > 254 || !EMAIL.matcher(email).matches()) {
				redirect.addFlashAttribute("resetError", "home.signin.message.email.format");
			} else if (now < state.nextSendAt) {
				redirect.addFlashAttribute("resetError", "home.password.reset.wait");
			} else {
				state.clear();
				state.nextSendAt = now + RESEND_DELAY;
				state.email = email;
				state.code = Util.verificationCodeGenerator();
				try {
					GeekiveMap userMap = signService.selectPasswordResetUser(email);
					if (userMap != null && reserveMail(userMap.getString("userUid"), now)) {
						mailController.sendCode(request, response, Constants.EMAIL_PURPOSE_PASSWORD, email, state.code);
						state.userUid = userMap.getString("userUid");
						state.password = userMap.getString("password");
					}
				} catch (Exception e) {
					logger.warn("Could not send a password reset code ({})", e.getClass().getSimpleName());
				}
				state.expiresAt = System.currentTimeMillis() + CODE_VALIDITY;
			}
			if (state.token.equals(resetToken)) {
				redirect.addFlashAttribute("continueReset", state.token);
			}
		}
		return "redirect:/sign/forgot-password";
	}

	@PostMapping(value = "/forgot-password/verify")
	public String verifyCode(HttpServletRequest request, HttpSession session, @RequestParam String resetToken,
			@RequestParam String code, RedirectAttributes redirect) {
		synchronized (WebUtils.getSessionMutex(session)) {
			ResetState state = getState(session);
			long now = System.currentTimeMillis();
			if (!state.token.equals(resetToken) || state.code == null || state.expiresAt <= now) {
				redirect.addFlashAttribute("resetError", "home.password.reset.expired");
			} else if (state.userUid != null && state.code.equals(code.trim())) {
				request.changeSessionId();
				state.code = null;
				state.email = null;
				state.token = UUID.randomUUID().toString();
				state.expiresAt = now + RESET_VALIDITY;
				return "redirect:/sign/reset-password";
			} else if (code.trim().isEmpty()) {
				redirect.addFlashAttribute("continueReset", state.token);
				redirect.addFlashAttribute("resetError", "home.signup.message.code.fill");
			} else {
				if (++state.attempts >= MAX_ATTEMPTS) {
					state.clear();
				}
				redirect.addFlashAttribute("continueReset", state.token);
				redirect.addFlashAttribute("resetError", "home.password.reset.invalidCode");
			}
		}
		return "redirect:/sign/forgot-password";
	}

	@GetMapping(value = "/reset-password")
	public String resetPassword(HttpSession session, ModelMap modelMap, RedirectAttributes redirect) {
		synchronized (WebUtils.getSessionMutex(session)) {
			ResetState state = getState(session);
			if (state.isVerified()) {
				modelMap.put("resetToken", state.token);
				return "home/sign/reset-password";
			}
		}
		redirect.addFlashAttribute("resetError", "home.password.reset.expired");
		return "redirect:/sign/forgot-password";
	}

	@PostMapping(value = "/reset-password")
	public String updatePassword(HttpSession session, @RequestParam String resetToken, @RequestParam String password,
			@RequestParam String passwordCheck, RedirectAttributes redirect) {
		synchronized (WebUtils.getSessionMutex(session)) {
			ResetState state = getState(session);
			if (!state.token.equals(resetToken) || !state.isVerified()) {
				redirect.addFlashAttribute("resetError", "home.password.reset.expired");
				return "redirect:/sign/forgot-password";
			}
			if (password.isEmpty()) {
				redirect.addFlashAttribute("resetError", "home.signup.message.password.fill");
				return "redirect:/sign/reset-password";
			}
			if (password.length() < 10) {
				redirect.addFlashAttribute("resetError", "home.signup.message.password.min");
				return "redirect:/sign/reset-password";
			}
			if (!PASSWORD.matcher(password).matches()) {
				redirect.addFlashAttribute("resetError", "home.signup.message.password.format");
				return "redirect:/sign/reset-password";
			}
			if (!password.equals(passwordCheck)) {
				redirect.addFlashAttribute("resetError", "home.signup.message.password.match");
				return "redirect:/sign/reset-password";
			}

			GeekiveMap gMap = new GeekiveMap();
			gMap.put("userUid", state.userUid);
			gMap.put("password", state.password);
			gMap.put("newPassword", password);
			try {
				int updated = signService.updateResetPassword(gMap);
				state.clear();
				if (updated == 1) {
					return "redirect:/sign/signin";
				}
			} catch (RuntimeException e) {
				logger.warn("Could not update a reset password ({})", e.getClass().getSimpleName());
				redirect.addFlashAttribute("resetError", "home.password.reset.updateFailed");
				return "redirect:/sign/reset-password";
			}
		}
		redirect.addFlashAttribute("resetError", "home.password.reset.expired");
		return "redirect:/sign/forgot-password";
	}

	private synchronized boolean reserveMail(String userUid, long now) {
		nextMailByUser.values().removeIf(nextSendAt -> nextSendAt <= now);
		if (nextMailByUser.containsKey(userUid)) {
			return false;
		}
		nextMailByUser.put(userUid, now + RESEND_DELAY);
		return true;
	}

	private ResetState getState(HttpSession session) {
		ResetState state = (ResetState) session.getAttribute(RESET_STATE);
		if (state == null) {
			state = new ResetState();
			session.setAttribute(RESET_STATE, state);
		}
		return state;
	}

	private static class ResetState implements Serializable {
		private static final long serialVersionUID = 1L;
		private String token = UUID.randomUUID().toString();
		private String code;
		private String email;
		private String userUid;
		private String password;
		private long expiresAt;
		private long nextSendAt;
		private int attempts;

		private boolean isVerified() {
			return userUid != null && code == null && expiresAt > System.currentTimeMillis();
		}

		private void clear() {
			code = null;
			email = null;
			userUid = null;
			password = null;
			expiresAt = 0;
			attempts = 0;
		}
	}
}
