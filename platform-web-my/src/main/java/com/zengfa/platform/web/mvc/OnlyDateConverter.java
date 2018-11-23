package com.zengfa.platform.web.mvc;

import org.springframework.core.convert.converter.Converter;

public class OnlyDateConverter implements Converter<String, OnlyDate> {
	@Override
	public OnlyDate convert(String source) {
		return new OnlyDate(source);
	}
}
