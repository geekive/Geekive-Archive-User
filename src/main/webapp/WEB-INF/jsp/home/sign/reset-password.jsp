<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/jstlcore.jsp"%>
<c:set var="pageContent">
	<c:if test="${not empty resetError}">
		<script>
			$(function(){
				openModal({type : 'alert', message : '<spring:message code="${resetError}" javaScriptEscape="true"/>'});
			});
		</script>
	</c:if>
	<div class="sign-body-wrapper d-flex jcc aic">
		<div class="field-group-wrapper">
			<div class="d-flex jcc aic"><spring:message code="home.password.reset.change"/></div>
			<form method="post" action="<c:url value='/sign/reset-password'/>" novalidate>
				<input type="hidden" name="resetToken" value="${resetToken}">
				<div class="field-wrapper">
					<input type="password" name="password" class="form-control" autocomplete="new-password" required
						placeholder="<spring:message code='home.signup.password'/>"
						aria-label="<spring:message code='home.signup.password'/>"
						oninput="this.value = this.value.replace(/[^A-Za-z0-9!@#$%^&*(),.?:{}|<>_-]/g, '')">
				</div>
				<div class="field-wrapper">
					<input type="password" name="passwordCheck" class="form-control"
						autocomplete="new-password" required placeholder="<spring:message code='home.signup.password.check'/>"
						aria-label="<spring:message code='home.signup.password.check'/>">
				</div>
				<div class="field-wrapper d-flex jcc">
					<button type="submit" class="btn btn-outline-dark reset-action"><spring:message code="home.password.reset.change"/></button>
				</div>
			</form>
		</div>
	</div>
</c:set>
<%@ include file="/WEB-INF/jsp/home/common/layout.jsp" %>
