package me.schf.api.controller;

import java.time.ZonedDateTime;
import java.util.List;

import me.schf.api.model.PostEntity;
import me.schf.api.model.TagEntity;
import me.schf.api.web.Post;
import me.schf.api.web.PostHeadline;
import me.schf.api.web.Tag;

public class TestPostFactory {

	private TestPostFactory() {
		super();
	}
	
	public static ZonedDateTime dummyZdt() {
		return ZonedDateTime.parse("2025-06-25T22:39:22.849193-05:00");
	}


	public static PostHeadline dummyPostHeadline() {
		return new PostHeadline("dummy title", dummyZdt(), "dummy blurb.");
	}

	public static Post dummyPost() {
		return new Post(
				dummyPostHeadline(), 
				"steve minecraft", 
				"dummy **markdown** text for testing.",
				List.of(Tag.ART, Tag.ARTWARE),
				Boolean.FALSE
			);
	}
	
	public static PostEntity dummyPostEntity() {
		PostEntity postEntity = new PostEntity();
		postEntity.setAuthor("steve minecraft");
		postEntity.setDescription("dummy blurb.");
		postEntity.setMarkdownText("dummy **markdown** text for testing.");
		postEntity.setPublicationDate(dummyZdt());
		postEntity.setSharePost(Boolean.FALSE);
		postEntity.setTags(List.of(TagEntity.ART, TagEntity.ARTWARE));
		postEntity.setTitle("dummy title");
		return postEntity;
	}
}
