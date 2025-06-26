package me.schf.api.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import me.schf.api.model.PostEntity;
import me.schf.api.model.TagEntity;
import me.schf.api.util.DtoConverter;
import me.schf.api.web.Post;
import me.schf.api.web.PostHeadline;
import me.schf.api.web.Tag;

class DtoConverterTests {
	
    @Test
    void test_toPost() {
        ZonedDateTime date = ZonedDateTime.now(ZoneId.of("UTC"));
    	
        PostEntity entity = new PostEntity();
        entity.setTitle("Sample Title");
        entity.setDescription("Sample blurb");
        entity.setPublicationDate(date);
        entity.setAuthor("Jane Doe");
        entity.setMarkdownText("**markdown**");
        entity.setSharePost(true);
        entity.setTags(List.of(TagEntity.ART, TagEntity.DEMOSCENE));

        Post post = DtoConverter.toPost(entity);

        assertEquals("Sample Title", post.postHeadline().title());
        assertEquals("Sample blurb", post.postHeadline().blurb());
        assertEquals(date, post.postHeadline().publicationDate());
        assertEquals("Jane Doe", post.author());
        assertEquals("**markdown**", post.markdownText());
        assertTrue(post.sharePost());
        assertEquals(List.of(Tag.ART, Tag.DEMOSCENE), post.tags());
    }

    @Test
    void test_toPostEntity() {
        ZonedDateTime date = ZonedDateTime.now(ZoneId.of("UTC"));

        PostHeadline headline = new PostHeadline("Another Title", date, "Another blurb");
        Post post = new Post(headline, "John Doe", "_markdown_", List.of(Tag.ART), false);

        PostEntity entity = DtoConverter.toPostEntity(post);

        assertEquals("Another Title", entity.getTitle());
        assertEquals("Another blurb", entity.getDescription());
        assertEquals(date, entity.getPublicationDate());
        assertEquals("John Doe", entity.getAuthor());
        assertEquals("_markdown_", entity.getMarkdownText());
        assertFalse(entity.isSharePost());
        assertEquals(List.of(TagEntity.ART), entity.getTags());
    }

	@Test
	void test_toTagEntity() {
		Stream.of(Tag.values()).map(DtoConverter::toTagEntity)
				.forEach(te -> assertNotNull(te, "Bad tag to tag entity mapping."));
	}

	@Test
	void test_toTag() {
		Stream.of(TagEntity.values()).map(DtoConverter::toTag)
				.forEach(te -> assertNotNull(te, "Bad tag entity to tag mapping."));
	}

}
