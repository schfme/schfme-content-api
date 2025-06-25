package me.schf.api.web;

import java.time.ZonedDateTime;

public record PostHeadline(
		String title, 
		ZonedDateTime publicationDate, 
		String blurb
	) {}
