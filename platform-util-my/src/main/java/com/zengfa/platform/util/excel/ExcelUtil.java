package com.zengfa.platform.util.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;


/**
 * 导出excel功能. <br>
 * 
 * @author admin <br>
 *
 * @see
 * @since JDK 1.6
 */
public class ExcelUtil {
	/**
	 * 导出监听器
	 * 
	 * @author admin
	 * 
	 */
	public static abstract class ExportMonitor {
		/**
		 * 导出前
		 * @param totalRows 需要导出的总数
		 */
		public abstract void before(int totalRows);
		
		/**
		 * 处理进度
		 * 
		 * @param totalRows
		 *            总行数
		 * @param exportedRows
		 *            已经导出的行数
		 * @param e
		 *            异常，非空表示导出报错了
		 */
		public abstract void doProgress(int exportedRows, Exception e);
		
		/**
		 * 导出后
		 * @param totalRows
		 * @param e
		 */
		public abstract void finish(int totalRows,int exportedRows,Exception e);
	}

	/**
	 * 
	 * @param monitor
	 * @param count
	 *            索引0:总数,1:1已经导出的条数
	 * @param wb
	 * @param sheetName
	 * @param headStyle
	 * @param titleStyle
	 * @param dataStyle
	 * @param head
	 * @param titleFields
	 * @param fieldTitleMap
	 * @param dataList
	 */
	private static void exportExcelBySheet(ExportMonitor monitor, int[] count, HSSFWorkbook wb, String sheetName,
			HSSFCellStyle headStyle, HSSFCellStyle titleStyle, HSSFCellStyle dataStyle, String head,
			String[] fields, String[] titles, List<?> dataList) {
		try {
			if (monitor != null) {
				monitor.doProgress(count[0], null);
			}
			HSSFFont font = wb.getFontAt((short)0);  
			String s=font.getFontName()+","+font.getFontHeightInPoints();
			HSSFSheet sheet = wb.createSheet(sheetName); // --->创建了一个工作簿

			// sheet.setColumnWidth((short) 3, 20 * 256); //
			// ---》设置单元格宽度，因为一个单元格宽度定了那么下面多有的单元格高度都确定了所以这个方法是sheet的

			sheet.setDefaultRowHeight((short) 500); // ---->有得时候你想设置统一单元格的高度，就用这个方法

			int startRowIndex = 0;
			if (StringUtils.isNotEmpty(head)) {
				// 表格第一行
				HSSFRow row1 = sheet.createRow(startRowIndex); // --->创建一行

				// 四个参数分别是：起始行，起始列，结束行，结束列
				sheet.addMergedRegion(new CellRangeAddress(startRowIndex, startRowIndex, 0, fields.length - 1));
				row1.setHeightInPoints(25);
				HSSFCell cell1 = row1.createCell((short) 0, HSSFCell.CELL_TYPE_STRING);

				cell1.setCellStyle(headStyle);
				cell1.setCellValue(head);
				startRowIndex++;
			}
	
			
			// 标题栏
			HSSFRow titleRow = sheet.createRow(startRowIndex);
	
			for (int i = 0; i < fields.length; i++) {
				String field = fields[i];
				String title = titles[i];
				if (title == null)
					title = field;
				HSSFCell cell = titleRow.createCell((short) i, HSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(titleStyle);
				cell.setCellValue(title);
			}
			for (Object data : dataList) {
				if (data == null)
					continue;
				if (data instanceof Map) {
					Map map = (Map) data;
					if (map.isEmpty())
						continue;
				}

				count[0]++;
				if (monitor != null) {
					monitor.doProgress(count[0], null);
				}

				HSSFRow row = sheet.createRow(++startRowIndex);
				row.setHeight((short) 400);
				for (int i = 0; i < titles.length; i++) {
					String field = fields[i];
					try {
						Object value = PropertyUtils.getProperty(data, field);
						if (value == null)	value = "";
						short type = HSSFCell.CELL_TYPE_STRING;
						HSSFCell cell = row.createCell((short) i, type);
						if (value instanceof BigDecimal) {
							dataStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
							cell.setCellStyle(dataStyle);
						    cell.setCellValue(((BigDecimal)value).doubleValue());
						}else{
						   cell.setCellStyle(dataStyle);
						   cell.setCellValue(value.toString().trim());
						}
						  
						
						
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}

			for (int i = 0; i < titles.length; i++) {

				if (monitor != null) {
					monitor.doProgress(count[0], null);
				}
				//自动调整
				sheet.autoSizeColumn(i);
			}

		} catch (Exception e) {
			if (monitor != null)
				monitor.doProgress(count[0], e);
			e.printStackTrace();
		}
	}

	/**
	 * 导出excel
	 * @param monitor 导出进度监听器（可为空）
	 * @param head 头部（可为空）
	 * @param exportFields 导出字段（非空）
	 * @param fieldTitleMap 字段名对应的标题名称映射（非空）
	 * @param dataList 数据列表
	 * @param saveDir
	 * @return
	 */
	public static synchronized List<File> exportExcel(ExportMonitor monitor, String head, String[] fields,
			String[] titles, List<?> dataList, File saveDir) {
		if (saveDir == null) {
			String tempDir = System.getProperty("user.dir");
			saveDir = new File(tempDir, "temp");
		}

		if (!saveDir.exists()) {
			saveDir.mkdirs();
		} 
		if(monitor!=null)
			monitor.before(dataList.size());
		int[] exportCount = new int[1];

		List<File> xlsFileList = new ArrayList<File>();

		int maxSize = 60000;
		int xlsCount = dataList.size() / maxSize;
		if (dataList.size() % maxSize >= 0)
			xlsCount++;
		try {
			DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String now = format.format(new Date());

			for (int i = 0; i < xlsCount; i++) {
				int from = i * maxSize;
				int to = Math.min(from + maxSize, dataList.size());
				List list = new ArrayList(dataList.subList(from, to));

				String fileName = now + "_"+ (i + 1) + ".xls";
				if (xlsCount == 1)
					fileName = now  + ".xls";
				File file = new File(saveDir, fileName);

				FileOutputStream fos = new FileOutputStream(file);
				export2Excel(exportCount, monitor, fos, head, fields, titles, list);
				
				xlsFileList.add(file);
			}
			if(monitor!=null)
				monitor.finish(dataList.size(),exportCount[0],null);
		} catch (Exception e) {
			e.printStackTrace();
			if(monitor!=null)
				monitor.finish(dataList.size(),exportCount[0],e);
		} finally {
			dataList.clear();

		}

		return xlsFileList;
	}
	

	/**
	 * 导出excel
	 * @param monitor 导出进度监听器（可为空）
	 * @param head 头部（可为空）
	 * @param exportFields 导出字段（非空）
	 * @param fieldTitleMap 字段名对应的标题名称映射（非空）
	 * @param dataList 数据列表
	 * @param saveDir
	 * @return
	 */
	public static synchronized File mergeExportExcel( ExportMonitor  monitor, 
																String  head, 
														 List<String[]> fields,
														 List<String[]> titles,
												    Map<String,List<?>> list,
																   File saveDir,
														       String[] sheetNames) {
	
		if (saveDir == null) {
			String tempDir = System.getProperty("user.dir");
			saveDir = new File(tempDir, "temp");
		}
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		} 
		if(monitor!=null)
			monitor.before(list.size());
		int[] exportCount = new int[1];
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String now = format.format(new Date());
		String fileName = now  + ".xls";
		File file = new File(saveDir, fileName);
		try {
		    FileOutputStream fos = new FileOutputStream(file);
		    mergeExport2Excel(exportCount, monitor, fos, head, fields, titles, list,sheetNames);
			if(monitor!=null)
				monitor.finish(list.size(),1,null);
		   
		} catch (Exception e) {
			e.printStackTrace();
			if(monitor!=null)
				monitor.finish(list.size(),1,e);
		} finally {
			list.clear();

		}
		return file;
	}
	
	/**
	 * 导出excel
	 * 
	 * @author lizhihua2 2014-8-1 上午10:06:54 <br>
	 * @param excelOut
	 * @param head
	 * @param titles
	 * @param fields
	 * @param list
	 */
	private static synchronized void export2Excel(int[]  exportCount,
											   ExportMonitor monitor,
											    OutputStream excelOut,
												      String head,
												    String[] fields,
												    String[] titles,
												     List<?> list) {
		if (titles == null || titles.length == 0 || fields == null || fields.length==0 || list == null) {
			return;
		}
		HSSFWorkbook wb = new HSSFWorkbook(); // --->创建了一个excel文件

		// 头部样式
		HSSFCellStyle headStyle = createHeadStyle(wb);

		// 标题样式
		HSSFCellStyle titleStyle = createTitleStyle(wb);

		// 数据样式
		HSSFCellStyle dataStyle = createDataStyle(wb);
		HSSFDataFormat format = wb.createDataFormat(); // --->单元格内容格式
		short textFormat = format.getFormat("@");
		dataStyle.setDataFormat(textFormat);

		// 分表导出，因为excel最大行数是65535
		int maxSize = 65000;
		int sheetCount = list.size() / maxSize;
		if (list.size() % maxSize > 0) sheetCount++;
		sheetCount=sheetCount==0?1:sheetCount;
		int totalCount = list.size();
		for (int i = 0; i < sheetCount; i++) {
			int from = i * maxSize;
			int to = Math.min(from + maxSize, list.size());
			List row = list.subList(from, to);
			exportExcelBySheet(monitor, exportCount, wb, head + (i + 1), headStyle, titleStyle, dataStyle, head,
					fields, titles, row);
		}
		try {
			wb.write(excelOut);
			excelOut.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			list.clear();
			try {
				excelOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 导出excel
	 * 
	 * @author lizhihua2 2014-8-1 上午10:06:54 <br>
	 * @param excelOut
	 * @param head
	 * @param titleFields
	 * @param fieldTitleMap
	 * @param dataList
	 */
	private static synchronized void mergeExport2Excel(int[] exportCount,
												   ExportMonitor monitor, 
													OutputStream excelOut,
													      String head,
												  List<String[]> fileds,
												  List<String[]> titles,
										     Map<String,List<?>> list,
												        String[] sheetNames) {
		if (fileds == null || fileds.size() == 0 || titles == null || titles.size()==0|| list == null) {
			return;
		}
		HSSFWorkbook wb = new HSSFWorkbook(); // --->创建了一个excel文件

		// 头部样式
		HSSFCellStyle headStyle = createHeadStyle(wb);

		// 标题样式
		HSSFCellStyle titleStyle = createTitleStyle(wb);

		// 数据样式
		HSSFCellStyle dataStyle = createDataStyle(wb);
		HSSFDataFormat format = wb.createDataFormat(); // --->单元格内容格式
		short textFormat = format.getFormat("@");
		dataStyle.setDataFormat(textFormat);

		// 分表导出，因为excel最大行数是65535
		int maxSize = 65000;
		int sheetCount=0;
	
		for (int i = 0; i < list.size(); i++) {
			List<?> data=list.get(String.valueOf(i));
			sheetCount=data.size() / maxSize+(data.size() % maxSize > 0 ? 1 : 0);
			sheetCount=sheetCount==0?1:sheetCount;
			String sheetName=sheetNames!=null&&sheetNames.length>=i?sheetNames[i]:"Sheet";
			head=sheetNames!=null&&sheetNames.length>=i?sheetNames[i]:head;
			for (int j = 0; j < sheetCount; j++) {
				int from = j * maxSize;
				int to = Math.min(from + maxSize, data.size());
				List row = data.subList(from, to);
				exportExcelBySheet(monitor, exportCount, wb, sheetName, headStyle, titleStyle, dataStyle, head,fileds.get(i), titles.get(i), row);

			}
		}
		try {
			wb.write(excelOut);
			excelOut.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			list.clear();
			try {
				excelOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 创建头部样式
	 * 
	 * @param wb
	 * @return
	 */
	private static HSSFCellStyle createHeadStyle(HSSFWorkbook wb) {
		// 头部样式
		HSSFCellStyle headStyle = wb.createCellStyle(); // 样式对象
		headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
		headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
		// 设置字体格式
		Font font = wb.createFont();
		// 设置字体样式
		font.setFontHeightInPoints((short) 20); // --->设置字体大小
		font.setFontName("宋体"); // ---》设置字体，是什么类型例如：宋体
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headStyle.setFont(font); // --->将字体格式加入到style1中
		return headStyle;
	}

	/**
	 * 创建标题栏样式
	 * 
	 * @param wb
	 * @return
	 */
	private static HSSFCellStyle createTitleStyle(HSSFWorkbook wb) {
		// 头部样式
		HSSFCellStyle titleStyle = wb.createCellStyle(); // 样式对象
		titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
		// 设置字体格式
		Font font = wb.createFont();
		// 设置字体样式
		font.setFontHeightInPoints((short) 15); // --->设置字体大小
		font.setFontName("宋体"); // ---》设置字体，是什么类型例如：宋体

		titleStyle.setFont(font); // --->将字体格式加入到style1中
		titleStyle.setWrapText(false); // 设置是否能够换行，能够换行为true

		titleStyle.setBorderBottom((short) 1); // 设置下划线，参数是黑线的宽度
		titleStyle.setBorderLeft((short) 1); // 设置左边框
		titleStyle.setBorderRight((short) 1); // 设置有边框
		titleStyle.setBorderTop((short) 1); // 设置下边框

		return titleStyle;
	}

	/**
	 * 创建数据样式
	 * 
	 * @param wb
	 * @return
	 */
	private static HSSFCellStyle createDataStyle(HSSFWorkbook wb) {
		HSSFCellStyle dataStyle = wb.createCellStyle(); // 样式对象
		dataStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
		dataStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
		// 设置字体格式
		Font font = wb.createFont();
		// 设置字体样式
		font.setFontHeightInPoints((short) 12); // --->设置字体大小
		font.setFontName("宋体"); // ---》设置字体，是什么类型例如：宋体

		dataStyle.setFont(font); // --->将字体格式加入到style1中
		dataStyle.setWrapText(true); // 设置是否能够换行，能够换行为true

		dataStyle.setBorderBottom((short) 1); // 设置下划线，参数是黑线的宽度
		dataStyle.setBorderLeft((short) 1); // 设置左边框
		dataStyle.setBorderRight((short) 1); // 设置有边框
		dataStyle.setBorderTop((short) 1); // 设置下边框

		return dataStyle;
	}
	
	public static File export(ExportMonitor monitor, String head, String[] fields,String[] titles, List<?> dataList, File saveDir) {
		List<File> xlsFiles = exportExcel(null, head, fields, titles, dataList, saveDir);
		if (xlsFiles.size() > 1) {
			File zipFile=new File(saveDir.getParentFile(),"batchExport.zip");
			return zipFile;
		} else if (xlsFiles.size() == 1) {
			return xlsFiles.get(0);
		}
		
		return null;
	}

	public static  File mergeExport(ExportMonitor monitor,
											 String head,
								     List<String[]> fields,
									 List<String[]> titles, 
							    Map<String,List<?>> list, 
										       File saveDir,
										   String[] sheetNames) {
		return mergeExportExcel(monitor, head, fields, titles, list, saveDir,sheetNames);
		 
	}

}
