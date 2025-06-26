package me.schf.api.controller;

import static me.schf.api.util.PostConverter.toPost;
import static me.schf.api.util.PostConverter.toPostEntity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import me.schf.api.model.PostEntity;
import me.schf.api.service.PostEntityService;
import me.schf.api.util.PostConverter;
import me.schf.api.web.Post;

@RestController
@RequestMapping("/posts")
public class PostController {
	
	private final PostEntityService postEntityService;

	public PostController(PostEntityService postEntityService) {
		super();
		this.postEntityService = postEntityService;
	}

	@PostMapping
	public Post createOrUpdatePost(@Valid @RequestBody Post post) {
		PostEntity toAdd = toPostEntity(post);
		return toPost(postEntityService.add(toAdd));
	}

	@GetMapping("/recent")
	public List<Post> getRecentPosts(@RequestParam(defaultValue = "10") int limit) {
		if (limit < 1 || limit > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be between 1 and 100");
		}
		return postEntityService.getRecentPosts(limit).stream()
				.map(PostConverter::toPost)
				.toList();
	}

    @PostMapping("/search")
    public List<Post> searchPosts(
            @RequestBody Post probe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) List<String> tags
    ) {
        PostEntity probeEntity = toPostEntity(probe);
        return postEntityService.search(probeEntity, from, to, tags)
                .stream()
                .map(PostConverter::toPost)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be null");
		} else if (id.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be blank");
		}
        Optional<PostEntity> optionalPost = postEntityService.findById(id);
        if (optionalPost.isPresent()) {
            postEntityService.delete(optionalPost.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be null");
		} else if (id.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be blank");
		}
        return postEntityService.findById(id)
                .map(PostConverter::toPost)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
