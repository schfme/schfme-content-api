package me.schf.api.config.database;

import org.springframework.core.convert.converter.Converter;

import me.schf.api.model.TagEntity;

public class TagReadConverter implements Converter<String, TagEntity> {

	@Override
	public TagEntity convert(String source) {
		return TagEntity.valueOf(source.toUpperCase());
	}

}