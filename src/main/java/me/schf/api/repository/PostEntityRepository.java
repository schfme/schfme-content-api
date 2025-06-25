package me.schf.api.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import me.schf.api.model.PostEntity;

public interface PostEntityRepository extends MongoRepository<PostEntity, String>, QueryByExampleExecutor<PostEntity> {

	List<PostEntity> findAllByOrderByPublicationDateDesc(Pageable pageable);

}
