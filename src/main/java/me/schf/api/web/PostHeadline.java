package me.schf.api.web;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostHeadline(

		@NotBlank(message = "Title must not be blank")
	    @Size(max = 100, message = "Title cannot have more than 100 characters")
	    String title,

	    @NotNull(message = "Publication date must not be null")
	    ZonedDateTime publicationDate,

	    @Size(max = 300, message = "Blurb cannot have more than 300 characters")
	    String blurb
	) {}