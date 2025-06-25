package me.schf.api.service.util;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import me.schf.api.util.DtoConverter;
import me.schf.api.web.PostHeadline;

public class DtoConverterTests {
	
	@Test
	public void test_toPost() {
		
		PostHeadline postHeadline = new PostHeadline("a title", ZonedDateTime.now(), "a blurb");
		
		DtoConverter.toPost(null);
		
	}

}
