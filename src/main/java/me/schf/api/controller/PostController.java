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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.schf.api.model.PostEntity;
import me.schf.api.service.PostEntityService;
import me.schf.api.util.PostConverter;
import me.schf.api.web.Post;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "API for managing blog posts")
public class PostController {
	
	private final PostEntityService postEntityService;

	public PostController(PostEntityService postEntityService) {
		this.postEntityService = postEntityService;
	}

	@Operation(summary = "Create or update a post",
		requestBody = @RequestBody(
			required = true,
			content = @Content(
				schema = @Schema(implementation = Post.class)
			)
		),
		responses = {
			@ApiResponse(responseCode = "200", description = "Post created or updated", 
				content = @Content(schema = @Schema(implementation = Post.class))),
			@ApiResponse(responseCode = "400", description = "Invalid post data")
		})
	@PostMapping
	public Post createOrUpdatePost(@Valid @org.springframework.web.bind.annotation.RequestBody Post post) {
		PostEntity toAdd = toPostEntity(post);
		return toPost(postEntityService.add(toAdd));
	}

	@Operation(summary = "Get recent posts",
		parameters = {
			@Parameter(
				name = "limit", 
				description = "Number of posts to return (1-100)",
				in = ParameterIn.QUERY,
				required = false,
				schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "100")
			)
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "List of recent posts",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = Post.class)))),
			@ApiResponse(responseCode = "400", description = "Invalid limit parameter")
		})
	@GetMapping("/recent")
	public List<Post> getRecentPosts(@RequestParam(defaultValue = "10") int limit) {
		if (limit < 1 || limit > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be between 1 and 100");
		}
		return postEntityService.getRecentPosts(limit).stream()
				.map(PostConverter::toPost)
				.toList();
	}

	@Operation(summary = "Search posts with filters",
		    requestBody = @RequestBody(
		        description = "Post probe to filter on",
		        required = false,
		        content = @Content(schema = @Schema(implementation = Post.class))
		    ),
		    parameters = {
		        @Parameter(
		            name = "from",
		            description = "Filter posts from this date/time (ISO-8601 format)",
		            in = ParameterIn.QUERY,
		            required = false,
		            schema = @Schema(type = "string", format = "date-time")
		        ),
		        @Parameter(
		            name = "to",
		            description = "Filter posts up to this date/time (ISO-8601 format)",
		            in = ParameterIn.QUERY,
		            required = false,
		            schema = @Schema(type = "string", format = "date-time")
		        ),
		        @Parameter(
		            name = "title",
		            description = "Filter posts by title",
		            in = ParameterIn.QUERY,
		            required = false,
		            schema = @Schema(type = "string")
		        ),
		        @Parameter(
		            name = "author",
		            description = "Filter posts by author",
		            in = ParameterIn.QUERY,
		            required = false,
		            schema = @Schema(type = "string")
		        ),
		        @Parameter(
		            name = "sharePost",
		            description = "Filter posts by sharePost flag",
		            in = ParameterIn.QUERY,
		            required = false,
		            schema = @Schema(type = "boolean")
		        )
		    },
		    responses = {
		        @ApiResponse(responseCode = "200", description = "List of matching posts",
		            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Post.class))))
		    })
		@GetMapping
		public List<Post> getPosts(
		    @RequestParam(required = false) String title,
		    @RequestParam(required = false) String author,
		    @RequestParam(required = false) Boolean sharePost,
		    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
		    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to
		) {
		    PostEntity probeEntity = new PostEntity();
		    probeEntity.setTitle(title);
		    probeEntity.setAuthor(author);
		    if (sharePost != null) {
		        probeEntity.setSharePost(sharePost);
		    }
		    return postEntityService.search(probeEntity, from, to)
		        .stream()
		        .map(PostConverter::toPost)
		        .toList();
		}

    @Operation(summary = "Delete a post by ID",
		parameters = {
			@Parameter(
				name = "id",
				description = "ID of the post to delete",
				in = ParameterIn.PATH,
				required = true
			)
		},
		responses = {
			@ApiResponse(responseCode = "204", description = "Post deleted successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid post ID"),
			@ApiResponse(responseCode = "404", description = "Post not found")
		})
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

    @Operation(summary = "Get a post by ID",
		parameters = {
			@Parameter(
				name = "id",
				description = "ID of the post to retrieve",
				in = ParameterIn.PATH,
				required = true
			)
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "Post found",
				content = @Content(schema = @Schema(implementation = Post.class))),
			@ApiResponse(responseCode = "400", description = "Invalid post ID"),
			@ApiResponse(responseCode = "404", description = "Post not found")
		})
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
