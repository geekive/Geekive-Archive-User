<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/jstlcore.jsp"%>
<c:set var="pageContent">
	<c:choose>
		<c:when test="${articleMap.flagDeleted eq 'N'}">
			<div class="article-view-header-wrapper">
				<div class="article-view-title-wrapper d-flex jcc aic">
					<c:if test="${articleMap.flagPrivate eq 'Y'}">
						<span class="badge bg-dark text-white me-2 flex-shrink-0">Private</span>
					</c:if>
					<span><c:out value="${articleMap.title}"/></span>
				</div>
				<div class="article-view-info-wrapper d-flex jcb aic">
					<div>${articleMap.registrationDate}</div>
					<div class="d-flex aic gap-3">
						<fmt:formatNumber value="${articleMap.viewCount}" var="viewCount"/>
						<span class="text-nowrap"><spring:message code="archive.article.views" arguments="${viewCount}" argumentSeparator="|"/></span>
						<i id="btn-share" class="bi bi-clipboard" role="button"></i>
					</div>
				</div>
			</div>
			<div class="article-body-wrapper">
				${articleMap.content}
			</div>
		</c:when>
		<c:otherwise>
			<div class="article-view-header-wrapper"></div>
			<div class="article-body-wrapper d-flex flex-column mb-3 jcc aic">
				  <div class="p-2"><spring:message code="archive.view.deleted01"/></div>
				  <div class="p-2"><spring:message code="archive.view.deleted02"/></div>	
			</div>
			<script>
				let $second = $('#second');
				let second  = 5;
				const updateCountdown = () => {
				    $second.html(second);
				    if (second-- === 0) goToPage('/');
				};
				updateCountdown();
				setInterval(updateCountdown, 1000);
			</script>
		</c:otherwise>
	</c:choose>
	<script>
		$(function(){
			$('#btn-share').on('click', () => {
				let url = window.location.href;
				navigator.clipboard.writeText(url).then(() => {
					openModal({
						type 		: 'alert'
						, title 	: '<spring:message code="archive.view.modal.clipboard.title"/>'
						, message	: '<spring:message code="archive.view.modal.clipboard.message"/>'
					})
				})
			})
		})
	</script>
</c:set>
<%@ include file="/WEB-INF/jsp/archive/common/layout.jsp" %>
