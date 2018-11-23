package com.zengfa.platform.web.userinfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zengfa.platform.util.ListUtil;

public class UriListChecker {

	private Set<String> uris;
	private List<String> folders;

	public UriListChecker(List<String> uriList) {
		this.uris = this.getUris(uriList);
		this.folders = this.getFolders(uriList);
	}

	protected Set<String> getUris(List<String> uriList) {
		uriList = ListUtil.defaultList(uriList);
		Set<String> set = new HashSet<String>();
		for (String uri : uriList) {
			if (!uri.endsWith("/")) {
				set.add(uri);
			}
		}
		return set;
	}

	protected List<String> getFolders(List<String> uriList) {
		uriList = ListUtil.defaultList(uriList);
		List<String> list = new ArrayList<String>();
		for (String uri : uriList) {
			if (uri.endsWith("/")) {
				list.add(uri);
			}
		}
		return list;
	}

	public boolean exists(String uri) {
		boolean isResource = MvcResourceUtil.isResource(uri);
		
		if(uri.equals("/")){
			return true;
		}
		
		if (isResource) {
			return true;
		}
		if (uris.contains(uri)) {
			return true;
		}
		
		
		
		for (String folder : folders) {
			if (uri.startsWith(folder)) {
				return true;
			}
		}
		return false;
	}
}
