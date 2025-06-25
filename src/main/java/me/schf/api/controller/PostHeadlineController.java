package me.schf.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.schf.api.service.PostEntityService;
import me.schf.api.util.DtoConverter;
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
        return postEntityService.getRecentPosts(limit)
                .stream()
                .map(DtoConverter::toPost)
                .map(Post::postHeadline)
                .toList();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PostHeadline> getPostHeadlineById(@PathVariable String id) {
        return postEntityService.findById(id)
                .map(DtoConverter::toPost)
                .map(Post::postHeadline)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
