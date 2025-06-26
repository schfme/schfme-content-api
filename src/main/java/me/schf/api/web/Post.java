package me.schf.api.web;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record Post(
	    @Valid
	    @NotNull(message = "Post headline must not be null")
	    PostHeadline postHeadline,

	    @NotBlank(message = "Author must not be blank")
	    @Size(max = 50, message = "Author cannot have more than 50 characters")
	    String author,

	    @NotBlank(message = "Markdown text must not be blank")
	    @Size(max = 20000, message = "Markdown text cannot have more than 20,000 characters")
	    String markdownText,

	    @NotNull(message = "Tags must not be null")
	    @Size(min = 1, message = "At least one tag is required")
	    List<Tag> tags,

	    Boolean sharePost
	) {}