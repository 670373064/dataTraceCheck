package gov.gfmis.dss.web.service;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

public interface TraceService {
	
	
	/**
	 * 获得数据溯源核对的树
	 * @return
	 */
	public JSONArray getTraceTree() throws Exception ;
	
	/**
	 * 
	 * @param list   报表列表
	 * @return 
	 */
	public boolean getQuery(String tname,String select,String query);
	
	
	

	
	/**
	 * 树结构
	 * @return 
	 */
	public List traceTree() throws Exception;
	/**
	 * selects条件
	 * @return 
	 */
	public JSONArray getSelects(String table_name) throws Exception;
	/**
	 *  获取where操作符
	 */
	public JSONArray getWheres() throws Exception;
	/**
	 * 获取where列
	 */
	public JSONArray getws(String table_name) throws Exception;
	
	public JSONArray getGrid(String tname,String select,String query);
	
	public JSONArray getFields(String tname,String select);
	
	public JSONArray getColumModle(String tname,String select);
	
	//获取溯源数据表格
	public JSONArray getSgrid(String tname,String id);
	//获取溯源标识ID
	public boolean getSid(String tname,String id);
	
    public JSONArray getSourceGrid(String tname,String id);
	
	public JSONArray getSourceFields(String tname,String id);
	
	public JSONArray getSourceCM(String tname,String id);
}
