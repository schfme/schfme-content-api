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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.schf.api.service.PostEntityService;
import me.schf.api.util.PostConverter;
import me.schf.api.web.Post;
import me.schf.api.web.PostHeadline;

@RestController
@RequestMapping("/posts/headlines")
@Tag(name = "Post Headlines", description = "Lighterweight previews of blog posts")
public class PostHeadlineController {

	private final PostEntityService postEntityService;

	public PostHeadlineController(PostEntityService postEntityService) {
		this.postEntityService = postEntityService;
	}

	@Operation(
		summary = "Get recent post headlines",
		parameters = {
			@Parameter(
				name = "limit",
				description = "Number of headlines to return (1–100)",
				in = ParameterIn.QUERY,
				required = false,
				schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "100")
			)
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "List of recent post headlines",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostHeadline.class)))),
			@ApiResponse(responseCode = "400", description = "Invalid limit parameter")
		}
	)
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

	@Operation(
		summary = "Get a post headline by ID",
		parameters = {
			@Parameter(
				name = "id",
				description = "ID of the post to retrieve",
				in = ParameterIn.PATH,
				required = true
			)
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "Post headline found",
				content = @Content(schema = @Schema(implementation = PostHeadline.class))),
			@ApiResponse(responseCode = "400", description = "Invalid post ID"),
			@ApiResponse(responseCode = "404", description = "Post not found")
		}
	)
	@GetMapping("/{id}")
	public ResponseEntity<PostHeadline> getPostHeadlineById(@PathVariable String id) {
		if (id == null || id.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must not be null or blank");
		}
		return postEntityService.findById(id)
			.map(PostConverter::toPost)
			.map(Post::postHeadline)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}

