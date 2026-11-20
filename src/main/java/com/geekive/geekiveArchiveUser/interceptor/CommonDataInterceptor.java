package com.geekive.geekiveArchiveUser.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.geekive.geekiveArchiveUser.common.Util;
import com.geekive.geekiveArchiveUser.geekiveCustom.GeekiveMap;
import com.geekive.geekiveArchiveUser.mapper.service.ArchiveService;

@Component
public class CommonDataInterceptor implements HandlerInterceptor {

	private final ArchiveService archiveService;

    public CommonDataInterceptor(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String archiveName 			= Util.extractArchiveNameFromUrl(request);
		String categoryUrlPath 		= Util.extractCategoryNameFromUrl(request);
		String articleNumber		= Util.extractArticleNumberFromUrl(request);
		String userUid				= "";
		String currentArchiveLogo 	= "";
		String categoryUid			= "";
		String articleUid			= "";

		
		// get user uid or it's gonna send redirect
		GeekiveMap gMap = new GeekiveMap();
		gMap.put("archiveName", archiveName);
		userUid = archiveService.selectUserUid(gMap);
		if(Util.isEmpty(userUid)) {
			response.sendRedirect("/");
			return false;
		}
		gMap.put("userUid", userUid);
		currentArchiveLogo = archiveService.selectArchiveLogo(gMap);
		
		String viewerUserUid = Util.getSignedInUserUid(request);
		boolean isMyArchvie = userUid.equals(viewerUserUid);
		gMap.put("viewerUserUid", viewerUserUid);

		// get category uid or it's gonna send redirect
		if(Util.isNotEmpty(categoryUrlPath)) {
			gMap.put("categoryUrlPath"	, categoryUrlPath);
			categoryUid = archiveService.selectCategoryUid(gMap);
			if(Util.isEmpty(categoryUid)) {
				response.sendRedirect("/" + archiveName);
				return false;
			}
		}
		
		// check article number exists and if not, it's gonna send redirect
		if(Util.isNotEmpty(articleNumber)) {
			gMap.put("articleNumber", articleNumber);
			gMap.put("categoryUid"	, categoryUid);
			if(!archiveService.checkArticleExistence(gMap)) {
				response.sendRedirect("/" + archiveName + "/" + categoryUrlPath);
				return false;
			}else {
				articleUid = archiveService.selectArticleUid(gMap);
			}
		}

		request.setAttribute("currentArchiveUserUid"	, userUid);
		request.setAttribute("currentCategoryUid"		, categoryUid);
		request.setAttribute("currentArchiveName"		, archiveName);
		request.setAttribute("currentArchiveLogo"		, currentArchiveLogo);
		request.setAttribute("currentArticleNumber"		, articleNumber);
		request.setAttribute("currentArticleUid"		, articleUid);
		request.setAttribute("isMyArchvie"				, isMyArchvie);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		/*
		request.removeAttribute("currentArchiveUserUid");
		request.removeAttribute("currentCategoryUid");
		*/
	}
}
