<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/jstlcore.jsp"%>
<c:forEach items="${articleList}" var="article">

	<!-- CARD -->
	<div class="article article-card col-sm-3 card <c:if test="${!empty cookie.viewType.value && cookie.viewType.value eq 'LIST'}">hide</c:if>" onclick="goToPage('${article.articleUrlPath}')">
		<img src="${article.thumbnailUrlPath}" class="card-img-top" alt="..." onerror="getErrorImageUrl(this, 'thumbnail')">
		<div class="article-card-body card-body">
			<p class="card-text d-flex aic gap-2">
				<c:if test="${article.flagPrivate eq 'Y'}">
					<span class="badge bg-dark text-white flex-shrink-0">Private</span>
				</c:if>
				<span><c:out value="${article.title}"/></span>
			</p>
		</div>
	</div>

	<!-- LIST -->
	<div class="article article-list <c:if test="${empty cookie.viewType.value || cookie.viewType.value eq 'CARD'}">hide</c:if>" onclick="goToPage('${article.articleUrlPath}')">
		<div class="article-list-body d-flex jcs">
			<img src="${article.thumbnailUrlPath}" alt="..." onerror="getErrorImageUrl(this, 'thumbnail')">
			<div class="article-list-content-wrapper">
				<div class="title d-flex aic gap-2">
					<c:if test="${article.flagPrivate eq 'Y'}">
						<span class="badge bg-dark text-white flex-shrink-0" style="font-size: 0.75rem;">Private</span>
					</c:if>
					<span class="text-truncate"><c:out value="${article.title}"/></span>
				</div>
				<div class="content">
					${article.content}
				</div>
				<div class="date d-flex aic">
					${article.categoryName}<font class="middot">&middot;</font>${article.registrationDate}
				</div>
			</div>
		</div>
	</div>
</c:forEach>
<c:if test="${isLastArticle}">
	<script>
		$(window).off('scroll.pagination');
	</script>
</c:if>