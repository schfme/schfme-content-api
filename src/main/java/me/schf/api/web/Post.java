package me.schf.api.web;

import java.util.List;

public record Post(
		PostHeadline postHeadline, 
		String author, 
		String markdownText, 
		List<Tag> tags, 
		boolean sharePost
	) {}