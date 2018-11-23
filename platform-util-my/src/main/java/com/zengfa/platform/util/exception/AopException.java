package com.zengfa.platform.util.exception;

import com.zengfa.platform.util.bean.Result;

/**
 * aop截面异常
 * @author lichangwen
 *
 */
public class AopException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Result result;

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	/**
	 * Creates object.
	 */
	public AopException(String message) {
		super(message);
	}

	public AopException(Result result, Throwable e) {
		super(result.getStack(e),e);
		this.result = result;
	}
}
