package com.zengfa.platform.util.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询对象,实用于普通分页查询，页面如果使用easyui显示表格时，需要做特殊处理，具体可咨询易理坚.
 */
public class PageReturnDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 返回状态类型.200：成功 .
	 */
	private int status;

	/**
	 * 返回描述信息内容..
	 */
	private String msg = null;
	/**
	 * 数据列表.
	 */
	private List<? extends Object> data;
	/**
	 * 当前查询页.
	 */
	private int currentPage;
	/**
	 * 当前页返回记录数.
	 */
	private int size;
	/**
	 * 数据列表总记录数.
	 */
	private int total;

	/**
	 * 构造函数.
	 */
	public PageReturnDTO() {
	}

	/**
	 * 失败返回值用简洁方式..
	 * 
	 * @param status
	 *            返回状态类型编号.
	 * @param msg
	 *            返回的数据异常信息.
	 * @return 返回值数据传输对象.
	 */
	public static PageReturnDTO NO(int status, String msg) {
		PageReturnDTO dto = new PageReturnDTO();
		dto.setStatus(status);
		dto.setMsg(msg);
		return dto;
	}

	public PageReturnDTO(List<? extends Object> data) {
		this.status = ReturnStatusEnum.OK.getValue();
		this.data = data;
	}

	public PageReturnDTO(List<? extends Object> data, int total) {
		this(data);
		this.total = total;
	}

	public PageReturnDTO(List<? extends Object> data, int total, int size) {
		this(data, total);
		this.size = size;
	}

	public PageReturnDTO(List<? extends Object> data, int total, int size, int currentPage) {
		this(data, total, size);
		this.currentPage = currentPage;
	}

	public PageReturnDTO(int status, List<? extends Object> data, int total, int size, int currentPage) {
		this(data, total, size, currentPage);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<? extends Object> getData() {
		return data;
	}

	public void setData(List<? extends Object> data) {
		this.data = data;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
}
