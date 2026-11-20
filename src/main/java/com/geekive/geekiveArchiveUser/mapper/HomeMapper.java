package com.geekive.geekiveArchiveUser.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geekive.geekiveArchiveUser.geekiveCustom.GeekiveMap;

@Mapper
public interface HomeMapper {
	public List<GeekiveMap> selectNewArticle(@Param("viewerUserUid") String viewerUserUid);
	public List<GeekiveMap> selectRandomArchive();
}
