package me.schf.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import me.schf.api.model.PostEntity;
import me.schf.api.repository.PostEntityRepository;


@ExtendWith(MockitoExtension.class)
class PostEntityServiceTests {
	
    @Mock
    private PostEntityRepository postEntityRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PostEntityService postEntityService;

    @Test
    void test_add_savesEntity() {
        PostEntity post = new PostEntity();
        when(postEntityRepository.save(post)).thenReturn(post);

        PostEntity result = postEntityService.add(post);
        assertEquals(post, result);
        verify(postEntityRepository).save(post);
    }

    @Test
    void test_getRecentPosts_returnsLimitedResults() {
        List<PostEntity> expected = List.of(new PostEntity(), new PostEntity());
        when(postEntityRepository.findAllByOrderByPublicationDateDesc(PageRequest.of(0, 2))).thenReturn(expected);

        List<PostEntity> actual = postEntityService.getRecentPosts(2);
        assertEquals(expected, actual);
    }

    @Test
    void test_delete_withValidId_deletes() {
        PostEntity post = new PostEntity();
        post.setId("abc123");

        postEntityService.delete(post);
        verify(postEntityRepository).deleteById("abc123");
    }

    @Test
    void test_delete_withoutId_throwsException() {
        PostEntity post = new PostEntity();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> postEntityService.delete(post));
        assertEquals("Cannot delete post without ID.", ex.getMessage());
    }

    @Test
    void test_findById_returnsOptional() {
        PostEntity post = new PostEntity();
        when(postEntityRepository.findById("id123")).thenReturn(Optional.of(post));

        Optional<PostEntity> result = postEntityService.findById("id123");
        assertTrue(result.isPresent());
        assertEquals(post, result.get());
    }

    @Test
    void test_search_filtersByTitle_andEscapesRegex() {
        PostEntity probe = new PostEntity();
        probe.setTitle(".*dangerous.*");

        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
                
        when(mongoTemplate.find(any(Query.class), eq(PostEntity.class)))
                .thenReturn(List.of(new PostEntity()));

        List<PostEntity> result = postEntityService.search(probe, from, to);

        assertNotNull(result);
        verify(mongoTemplate).find(argThat(query -> {
            String q = query.toString();
            return q.contains("\\\\Q.*dangerous.*\\\\E"); // regex safely escaped
        }), eq(PostEntity.class));
    }

    @Test
    void test_search_redosAttempt_safeDueToPatternQuote() {
        PostEntity probe = new PostEntity();
        // Evil input to trigger regex backtracking
        probe.setTitle("a".repeat(10000) + "(.*)+");

        when(mongoTemplate.find(any(Query.class), eq(PostEntity.class)))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> postEntityService.search(probe, null, null));

        verify(mongoTemplate).find(any(Query.class), eq(PostEntity.class));
    }

    @Test
    void test_search_nosqlInjectionAttempt_isTreatedAsPlainText() {
        PostEntity probe = new PostEntity();
        probe.setTitle("{$ne:null}");

        List<PostEntity> expected = List.of(new PostEntity());

        when(mongoTemplate.find(any(Query.class), eq(PostEntity.class))).thenReturn(expected);

        List<PostEntity> result = postEntityService.search(probe, null, null);

        assertEquals(expected, result);
        verify(mongoTemplate).find(Mockito.<Query>argThat(query -> {
            String q = query.toString();
            return q.contains("\\\\Q{$ne:null}\\\\E"); // properly escaped by Pattern.quote
        }), eq(PostEntity.class));
    }

}
