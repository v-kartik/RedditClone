package com.kartik.Reddit.mapper;

import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kartik.Reddit.dto.SubredditDto;
import com.kartik.Reddit.model.Post;
import com.kartik.Reddit.model.Subreddit;

@Mapper(componentModel = "spring")
public interface SubredditMapper {
	
	@Mapping(target = "numberOfPosts", expression = "java(mapPosts(subreddit.getPosts()))")
	SubredditDto mapSubredditToDto(Subreddit subreddit);
	
	default Integer mapPosts(List<Post> numberOfPosts) {return numberOfPosts.size();}
	
	@InheritInverseConfiguration
	@Mapping(target = "posts", ignore = true)
	Subreddit mapDtoToSubreddit(SubredditDto subbredditDto);
}
