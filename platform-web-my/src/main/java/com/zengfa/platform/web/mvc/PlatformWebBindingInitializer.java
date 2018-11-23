package com.zengfa.platform.web.mvc;

import java.util.Date;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

public class PlatformWebBindingInitializer extends
		ConfigurableWebBindingInitializer {

	@Override
	public void initBinder(WebDataBinder binder, WebRequest request) {
		super.initBinder(binder, request);
		binder.registerCustomEditor(Date.class, new DefaultDateEditor());
	}

}
