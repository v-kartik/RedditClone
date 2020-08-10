package com.kartik.Reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kartik.Reddit.dto.SubredditDto;
import com.kartik.Reddit.exceptions.SpringRedditException;
import com.kartik.Reddit.mapper.SubredditMapper;
//import com.kartik.Reddit.mapper.SubredditMapper;
import com.kartik.Reddit.model.Subreddit;
import com.kartik.Reddit.repository.SubredditRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class SubredditService {
	
	private final SubredditRepository subredditRepository;
	private final SubredditMapper subredditMapper;
	
	@Transactional
	public SubredditDto save(SubredditDto subredditDto) {
		Subreddit save = subredditRepository.save(subredditMapper.mapDtoToSubreddit(subredditDto));
		subredditDto.setId(save.getId());
		return subredditDto;
		
	}
	
	/*
	private Subreddit mapSubredditDto(SubredditDto subredditDto) {
		// TODO Auto-generated method stub
		return Subreddit.builder().name(subredditDto.getName())
		 				.description(subredditDto.getDescription())
		 				.build();
		
		
	}
	*/
	
	@Transactional(readOnly = true)
	public List<SubredditDto> getAll() {
		// TODO Auto-generated method stub
		return subredditRepository.findAll()
					.stream()
					.map(subredditMapper::mapSubredditToDto) 
					.collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
	public SubredditDto getSubreddit(Long id) {
		// TODO Auto-generated method stub
		Subreddit subreddit = subredditRepository.findById(id)
				.orElseThrow(() -> new SpringRedditException("No Subreddit found with id - " + id));
		return subredditMapper.mapSubredditToDto(subreddit);
	}

	/*
	private SubredditDto mapToDto(Subreddit subreddit) {
		// TODO Auto-generated method stub
		return SubredditDto.builder().name(subreddit.getName())
					.id(subreddit.getId())
					.numberOfPosts(subreddit.getPosts().size())
					.build();
	}
	*/
}
