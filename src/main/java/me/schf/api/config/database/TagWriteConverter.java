package me.schf.api.config.database;

import org.springframework.core.convert.converter.Converter;

import me.schf.api.model.TagEntity;

public class TagWriteConverter implements Converter<TagEntity, String> {

	@Override
	public String convert(TagEntity source) {
		return source.name().toLowerCase();
	}

}