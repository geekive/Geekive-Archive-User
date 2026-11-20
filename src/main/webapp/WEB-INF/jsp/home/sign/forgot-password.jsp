<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/jstlcore.jsp"%>
<c:set var="pageContent">
	<script>
		window.addEventListener('pageshow', (event) => {
			if (event.persisted) window.location.reload();
		});

		$(function(){
			<c:if test="${not empty resetError}">
				openModal({type : 'alert', message : '<spring:message code="${resetError}" javaScriptEscape="true"/>'});
			</c:if>
			const timer = $('#codeTimer');
			if (!timer.data('seconds')) return;

			const expiresAt = Date.now() + Number(timer.data('seconds')) * 1000;
			const updateTimer = () => {
				const seconds = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));
				timer.text(Math.floor(seconds / 60) + ':' + String(seconds % 60).padStart(2, '0'));
				if (seconds === 0) {
					clearInterval(interval);
					$('#code').prop('readonly', true);
					$('#btn-check').prop('disabled', true);
				}
			};
			const interval = setInterval(updateTimer, 1000);
			updateTimer();
		});
	</script>
	<div class="sign-body-wrapper d-flex jcc aic">
		<div class="field-group-wrapper">
			<div class="d-flex jcc aic"><spring:message code="home.password.reset.title"/></div>
			<form id="reset-email-form" method="post" action="<c:url value='/sign/forgot-password/send'/>" autocomplete="off" novalidate>
				<input type="hidden" name="resetToken" value="${resetToken}">
				<div class="field-wrapper">
					<input type="email" name="email" class="form-control" maxlength="254" required
						value="<c:out value='${resetEmail}'/>"
						placeholder="<spring:message code='home.signup.email'/>" aria-label="<spring:message code='home.signup.email'/>"
						${codeRequested ? 'readonly' : ''}>
				</div>
			</form>
			<form method="post" action="<c:url value='/sign/forgot-password/verify'/>" novalidate>
				<input type="hidden" name="resetToken" value="${resetToken}">
				<div class="field-wrapper d-flex">
					<div class="code-wrapper">
						<input type="text" id="code" name="code" class="form-control" inputmode="numeric" autocomplete="one-time-code"
							pattern="[0-9]{6}" maxlength="6" required placeholder="<spring:message code='home.signup.code'/>"
							oninput="this.value = this.value.replace(/[^0-9]/g, '')"
							aria-label="<spring:message code='home.signup.code'/>" ${codeRequested ? '' : 'readonly'}>
						<c:if test="${codeRequested}">
							<span id="codeTimer" data-seconds="${remainingCodeSeconds}"></span>
						</c:if>
					</div>
					<button type="submit" form="reset-email-form" class="btn btn-outline-dark text-nowrap">
						<spring:message code="${codeRequested ? 'home.signup.button.resend' : 'home.signup.button.send'}"/>
					</button>
					<button type="submit" id="btn-check" class="btn btn-outline-dark text-nowrap" ${codeRequested ? '' : 'disabled'}>
						<spring:message code="home.signup.button.check"/>
					</button>
				</div>
			</form>
		</div>
	</div>
</c:set>
<%@ include file="/WEB-INF/jsp/home/common/layout.jsp" %>
