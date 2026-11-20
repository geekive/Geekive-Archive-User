package com.geekive.geekiveArchiveUser.mapper.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geekive.geekiveArchiveUser.geekiveCustom.GeekiveMap;
import com.geekive.geekiveArchiveUser.mapper.HomeMapper;


@Service
public class HomeService implements HomeMapper{
	
	@Autowired
	HomeMapper homeMapper;

	@Override
	public List<GeekiveMap> selectNewArticle(String viewerUserUid) {
		return homeMapper.selectNewArticle(viewerUserUid);
	}

	@Override
	public List<GeekiveMap> selectRandomArchive() {
		return homeMapper.selectRandomArchive();
	}
}
