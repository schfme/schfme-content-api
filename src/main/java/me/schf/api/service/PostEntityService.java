package me.schf.api.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import me.schf.api.model.PostEntity;
import me.schf.api.repository.PostEntityRepository;

@Service
public class PostEntityService {

	private final PostEntityRepository postEntityRepository;
	private final MongoTemplate mongoTemplate;

	public PostEntityService(PostEntityRepository postEntityRepository, MongoTemplate mongoTemplate) {
		this.postEntityRepository = postEntityRepository;
		this.mongoTemplate = mongoTemplate;
	}

	public PostEntity add(PostEntity postEntity) {
		return postEntityRepository.save(postEntity);
	}

	public List<PostEntity> getRecentPosts(int limit) {
		Pageable pageable = PageRequest.of(0, limit);
		return postEntityRepository.findAllByOrderByPublicationDateDesc(pageable);
	}

	public List<PostEntity> search(PostEntity probe, ZonedDateTime fromDate, ZonedDateTime toDate) {
		Query query = new Query();

		if (probe.getAuthor() != null) {
			query.addCriteria(Criteria.where("author").is(probe.getAuthor()));
		}

		if (probe.getTitle() != null) {
			String escapedInput = Pattern.quote(probe.getTitle());
			query.addCriteria(Criteria.where("title").regex(escapedInput, "i"));
		}

		if (fromDate != null || toDate != null) {
			Criteria dateCriteria = Criteria.where("publication_date");
			if (fromDate != null && toDate != null) {
				dateCriteria.gte(fromDate).lte(toDate);
			} else if (fromDate != null) {
				dateCriteria.gte(fromDate);
			} else {
				dateCriteria.lte(toDate);
			}
			query.addCriteria(dateCriteria);
		}

		return mongoTemplate.find(query, PostEntity.class);
	}

	public void delete(PostEntity postEntity) {
		if (postEntity.getId() != null) {
			postEntityRepository.deleteById(postEntity.getId());
		} else {
			throw new IllegalArgumentException("Cannot delete post without ID.");
		}
	}

	public Optional<PostEntity> findById(String id) {
		return postEntityRepository.findById(id);
	}
	
	public Optional<PostEntity> findByTitle(String id) {
		return postEntityRepository.findByTitle(id);
	}
}
