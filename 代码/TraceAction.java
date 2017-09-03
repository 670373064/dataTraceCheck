package gov.gfmis.dss.web.action;

import gov.gfmis.dss.menu.util.StringUtils;
import gov.gfmis.dss.web.service.TraceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionSupport;

public class TraceAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	private TraceService traceService;

	public void setTraceService(TraceService traceService) {
		this.traceService = traceService;
	}

	// 获得报表树
	public void getTree() {
		HttpServletResponse resp = ServletActionContext.getResponse();
//		resp.setContentType("appliction/x-json");
//		resp.setContentType("text/javascript");
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");
		try {
			PrintWriter out = resp.getWriter();
			JSONArray result = traceService.getTraceTree();
			out.write(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 获得select列
	public void getSelects() {		
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");
		HttpServletRequest req = ServletActionContext.getRequest();
		String table_name =req.getParameter("table_name");
		try {
			PrintWriter out = resp.getWriter();
			JSONArray result = traceService.getSelects(table_name);
			System.out.println(result.toString());
			out.write(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取where操作符
	 */
	public void getWheres() {			
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");
		try {
			PrintWriter out = resp.getWriter();
			JSONArray result = traceService.getWheres();
			System.out.println(result.toString());
			out.write(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 获得select列
	public void getws() {		
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");
		HttpServletRequest req = ServletActionContext.getRequest();
		String table_name =req.getParameter("table_name");
		try {
			PrintWriter out = resp.getWriter();
			JSONArray result = traceService.getws(table_name);
			System.out.println(result.toString());
			out.write(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void getQuery(){
		boolean flag = false;
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");
		
		HttpServletRequest req = ServletActionContext.getRequest();
		String tname = req.getParameter("tname");
		String select = req.getParameter("select");
		String query = req.getParameter("query");
		select = select.substring(0,1).equals("*")?"*":select;
		JSONObject result = new JSONObject();
		flag = traceService.getQuery(tname,select,query);
		result.put("result", String.valueOf(flag));
		System.out.println(result.toString());
		
		try {
			PrintWriter	out = resp.getWriter();
			out.write(result.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getGrid() throws Exception {
		HttpServletRequest req = ServletActionContext.getRequest();
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");

		String sStart = req.getParameter("start");
		String sLimit = req.getParameter("limit");
		String tname = req.getParameter("tname");
		System.out.println(tname);
		String select = req.getParameter("select");
		String query = req.getParameter("query");
//		select = select.substring(0,1).equals("*")?"*":select;
		System.out.println("select:"+select);
		StringBuffer sb=new StringBuffer("");
		if(select.substring(0,1).equals("*")){
			JSONArray js= traceService.getws(tname);
			for(int i=0;i<js.size();i++){
				JSONObject obj = (JSONObject)js.get(i);
				String name = (String) obj.get("column_name");
				sb.append(name).append(",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
		}
		select=sb.toString();
		System.out.println("select2:"+select);
		JSONArray alist = traceService.getGrid(tname,select,query);
		System.out.println("alist:"+alist.toString());
		int start = 0;
		int limit = 50;

		try {
			//modify by yinhang 20130826
			if(sStart != null && !sStart.equals("")){
				start = Integer.parseInt(sStart);
			}
			if(sLimit != null && !sLimit.equals("")){
				limit = Integer.parseInt(sLimit);
			}
			
		} catch (Exception e) {
			System.out.println("数字转换出错");
		}

		try {
			PrintWriter out = resp.getWriter();
			List compoList = new ArrayList();
			int count = alist.size();
			if(start<count){
				compoList = alist.subList(start, (start+limit)>count?count:(start+limit));
			}
			
			JSONObject result = new JSONObject();
			result.put("totalCount",count + "");
			result.put("items", compoList);
			JSONArray fList = traceService.getFields(tname,select);
			System.out.println("fList:"+fList.toString());
			JSONArray cList = traceService.getColumModle(tname,select);
			System.out.println("cList:"+cList.toString());
			result.put("fieldsNames", fList);
			result.put("columModle", cList);
			
			System.out.println(result.toString());
			out.write(result.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//获取溯源数据表格
	public void getSgrid() {
		HttpServletRequest req = ServletActionContext.getRequest();
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");

		String tname = req.getParameter("tname");
		String uid = req.getParameter("uid");			
		System.out.println(tname);			
		JSONArray js= traceService.getSgrid(tname,uid);
		try {
			PrintWriter	out = resp.getWriter();
			out.write(js.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//获取溯源标识ID
	public void getSid() {
		HttpServletRequest req = ServletActionContext.getRequest();
		HttpServletResponse resp = ServletActionContext.getResponse();
		resp.setHeader("ContentType", "text/html");
		resp.setCharacterEncoding("utf-8");

		String tname = req.getParameter("tname");
		String uid = req.getParameter("uid");			
		System.out.println(tname);			
		boolean flag= traceService.getSid(tname,uid);
		JSONObject result = new JSONObject();
		result.put("result", String.valueOf(flag));
		System.out.println(result.toString());
		
		try {
			PrintWriter	out = resp.getWriter();
			out.write(result.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
