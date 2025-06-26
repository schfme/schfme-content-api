package me.schf.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import me.schf.api.service.PostEntityService;
import me.schf.api.util.PostConverter;
import me.schf.api.web.Post;
import me.schf.api.web.PostHeadline;

@RestController
@RequestMapping("/posts/headlines")
public class PostHeadlineController {
	
	private final PostEntityService postEntityService;
	
	public PostHeadlineController(PostEntityService postEntityService) {
		super();
		this.postEntityService = postEntityService;
	}

    @GetMapping("/recent")
    public List<PostHeadline> getRecentPostHeadlines(@RequestParam(defaultValue = "10") int limit) {
		if (limit < 1 || limit > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be between 1 and 100");
		}
        return postEntityService.getRecentPosts(limit)
                .stream()
                .map(PostConverter::toPost)
                .map(Post::postHeadline)
                .toList();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PostHeadline> getPostHeadlineById(@PathVariable String id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be null");
		} else if (id.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be blank");
		}
        return postEntityService.findById(id)
                .map(PostConverter::toPost)
                .map(Post::postHeadline)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
