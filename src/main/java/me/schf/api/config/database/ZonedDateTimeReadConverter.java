package me.schf.api.config.database;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {
	@Override
	public ZonedDateTime convert(Date source) {
		return source.toInstant().atZone(ZoneId.systemDefault());
	}
}