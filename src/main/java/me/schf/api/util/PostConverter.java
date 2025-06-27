package me.schf.api.util;


import java.util.List;
import java.util.Optional;

import me.schf.api.model.PostEntity;
import me.schf.api.model.TagEntity;
import me.schf.api.web.Post;
import me.schf.api.web.PostHeadline;
import me.schf.api.web.Tag;

public class PostConverter {

	private PostConverter() {
		super();
	}

	public static Post toPost(PostEntity postEntity) {
		PostHeadline postHeadline = new PostHeadline(
				postEntity.getTitle(), 
				postEntity.getPublicationDate(),
				postEntity.getDescription()
			);
		
		List<Tag> tags = Optional.ofNullable(postEntity.getTags())
				.orElseGet(List::of)
		        .stream()
		        .map(PostConverter::toTag)
		        .toList();
		
		return new Post(
				postHeadline, 
				postEntity.getAuthor(), 
				postEntity.getMarkdownText(), 
				tags, 
				postEntity.isSharePost()
			);
	}
	
	public static PostEntity toPostEntity(Post post) {
		PostEntity postEntity = new PostEntity();
		if (post.postHeadline() != null) {
			postEntity.setDescription(post.postHeadline().blurb());
			postEntity.setPublicationDate(post.postHeadline().publicationDate());
			postEntity.setTitle(post.postHeadline().title());
		}
		postEntity.setAuthor(post.author());
		postEntity.setMarkdownText(post.markdownText());
		postEntity.setSharePost(Boolean.TRUE.equals(post.sharePost()));
		
		List<TagEntity> tagEntities = Optional.ofNullable(post.tags())
		        .orElseGet(List::of)
		        .stream()
		        .map(PostConverter::toTagEntity)
		        .toList();
		
		postEntity.setTags(tagEntities);
		
		return postEntity;
	}
	
	public static TagEntity toTagEntity(Tag tag) {
		return TagEntity.valueOf(tag.name());
	}
	
	public static Tag toTag(TagEntity tagEntity) {
		return Tag.valueOf(tagEntity.name());
	}

}
