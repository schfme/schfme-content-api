package me.schf.api.model;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "posts")
public class PostEntity {

	@Id
	private String id;

	private String title;
	private String description;

	@Field("publication_date")
	private ZonedDateTime publicationDate;

	private String author;

	private boolean sharePost;

	@Field("markdown_text")
	private String markdownText;

	private List<TagEntity> tags;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ZonedDateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(ZonedDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isSharePost() {
		return sharePost;
	}

	public void setSharePost(boolean sharePost) {
		this.sharePost = sharePost;
	}

	public String getMarkdownText() {
		return markdownText;
	}

	public void setMarkdownText(String markdownText) {
		this.markdownText = markdownText;
	}

	public List<TagEntity> getTags() {
		return tags;
	}

	public void setTags(List<TagEntity> tags) {
		this.tags = tags;
	}
}
