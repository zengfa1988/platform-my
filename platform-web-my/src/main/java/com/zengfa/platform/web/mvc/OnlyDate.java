package com.zengfa.platform.web.mvc;

import java.io.Serializable;
import java.util.Date;

import com.zengfa.platform.util.DateTime;

/**
 * 精确到天的日期(如:2013-01-01).
 * 
 * @author 阿海
 * 
 */
public class OnlyDate implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long time;

	public OnlyDate() {
		this(System.currentTimeMillis());
	}

	public OnlyDate(java.util.Date date) {
		this(date.getTime());
	}

	public OnlyDate(String datetime) {
		this(getMillis(datetime));
		// System.err.println("here3");
		// new Exception("here3").printStackTrace();
	}

	public OnlyDate(long time) {
		this.time = time;
	}

	public long getTime() {
		return this.time;
	}

	protected static long getMillis(String datetime) {
		System.out.println("111111111111111111111");
		if (DateTime.isDateTime(datetime)) {
			return DateTime.getTimestamp(datetime);
		}
		else if (DateTime.isDate(datetime)) {
			return DateTime.getTimestamp(datetime + " 00:00:00");
		}
		else {
			throw new IllegalArgumentException("非法参数[" + datetime + "].");
		}
	}

	public Date toDate() {
		return new Date(time);
	}

	@Override
	public String toString() {
		return DateTime.getTime(time).substring(0, 10);
	}

}
