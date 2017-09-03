package gov.gfmis.dss.web.service.impl;

import gov.gfmis.dss.web.service.TraceService;

import gov.gfmis.fap.util.XMLData;


import java.io.*;
import java.sql.*;  
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import oracle.sql.CLOB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import com.ufgov.gmap.db.base.BaseDao;

public class TraceServiceImpl implements TraceService {
	private BaseDao dao;

	public void setDao(BaseDao dao) {
		this.dao = dao;
	}

	public JSONArray getTraceTree() throws Exception {
		List reportList = this.traceTree();
		// 将list中的数据根据module_code键组织为树结构
		JSONArray result = toTreeStruct(reportList);
		return result;
	}

	private JSONArray toTreeStruct(List list) {
		List newList = new ArrayList();
		int length = 3;
		while (true) {
			if (list.size() == newList.size()) {
				break;
			}
			for (int i = 0; i < list.size(); i++) {
				XMLData data = (XMLData) list.get(i);
				if (data.get("module_code").toString().length() == length) {
					newList.add(data);
				}
			}
			length += 3;
		}
		JSONArray result = tree(newList, 6, "");
		return result;
	}

	private JSONArray tree(List l, int length, String instand) {
		JSONArray array = new JSONArray();
		for (int i = 0; i < l.size(); i++) {
			XMLData x = (XMLData) l.get(i);
			if (x.get("module_code").toString().length() == length
					&& x.get("module_code").toString().startsWith(instand)) {
				l.remove(i);
				i--;
				JSONObject o = new JSONObject();
				o.put("id", x.get("module_code"));
				o.put("text", x.get("module_name"));
//				o.put("compo_id", x.get("compo_id"));
				o.put("param", x.get("param"));
//				o.put("checked", new Boolean(false));
//				o.put("objecttype", x.get("objecttype"));
				JSONArray test = tree(l, length + 3, x.get("module_code")
						.toString());
			if (test.size() != 0) {
				o.put("leaf", new Boolean(false));
				o.put("cls", "folder");
				o.put("children", test);
			}else{
				o.put("leaf", new Boolean(true));
			}

				array.add(o);
			}
		}
		return array;
	}

	public boolean getQuery(String tname,String select,String query) {
		boolean flag =false;
		String str = "select "+select+" from "+tname+" "+query;
		List queryList;
		try {
			queryList = dao.queryBySql(str, null);
			if(queryList.size()>0)
			{
			   flag =true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	
	
	
	
		/**
		 * 获取数据溯源核对树目录
		 * @throws Exception 
		 */
		public List traceTree() throws Exception
		{		    
		    String strSql = "select distinct sbj_classify,sbjcode from DSS_RESOURCE_REGISTE_JC ";
		    List tlist=  dao.queryBySql(strSql,null);
		    List traceList = new ArrayList();
		    int code = 300;	
			try {
			   for( int i=0;i<tlist.size();i++){						
				code = code + 1;
				Map data = (Map)tlist.get(i);
				String sbj_classify = (String)data.get("sbj_classify");
				String sbjcode = (String)data.get("sbjcode");
		        XMLData mData = new XMLData();
		        mData.put("module_id", "311" + code);
				mData.put("module_code", "311" + code);
				mData.put("module_name", sbj_classify);
				mData.put("enabled", "1");
				mData.put("param", sbjcode);
				mData.put("folder", "false");
				traceList.add(mData);
				strSql = "select distinct data_type,dtypecode from DSS_RESOURCE_REGISTE_JC where sbj_classify=? ";
				List slist=  dao.queryBySql(strSql,new Object[] { sbj_classify });
				int icode = 300;
				for( int j=0;j<slist.size();j++){	
					icode = icode + 1;		
					data = (Map)slist.get(j);
					String data_type = (String)data.get("data_type");
					String dtypecode = (String)data.get("dtypecode");
			        mData = new XMLData();
			        mData.put("module_id", "311"+ code + icode);
					mData.put("module_code", "311"+ code + icode);
					mData.put("module_name", data_type);
					mData.put("enabled", "1");
					mData.put("param", dtypecode);
					mData.put("folder", "false");
					traceList.add(mData);
					strSql = "select distinct resource_name,BUSI_CLASSIFY from DSS_RESOURCE_REGISTE_JC where sbj_classify=? and data_type=? ";
					List dlist=  dao.queryBySql(strSql,new Object[] { sbj_classify,data_type });
					int kcode = 300;
					for( int k=0;k<dlist.size();k++){	
						kcode = kcode + 1;		
						data = (Map)dlist.get(k);
						String resource_name = (String)data.get("resource_name");
						String BUSI_CLASSIFY = (String)data.get("BUSI_CLASSIFY");
				        mData = new XMLData();
				        mData.put("module_id", "311"+ code + icode + kcode);
						mData.put("module_code", "311"+ code + icode + kcode);
						mData.put("module_name", BUSI_CLASSIFY);
						mData.put("enabled", "1");
						mData.put("param", resource_name);
						mData.put("folder", "true");
						traceList.add(mData);													
					}//end for k
					
				}//end for j
				
			}//end for i			    
		   
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
	    return traceList;
	}
	/**
	 * selects条件
	 * @return 
	 */
	public JSONArray getSelects(String table_name) throws Exception{
		String strSql = "select distinct column_name,nvl(comments,column_name) comments from user_col_comments where table_name ='"+ table_name +"' and COLUMN_NAME not like 'ATTR%' " +
				"and  COLUMN_NAME not like 'HOLD%' and  COLUMN_NAME not like '%REMARK%' and  COLUMN_NAME !='BUSINESS_ID' ";
	    List tlist=  dao.queryBySql(strSql,null);
	    JSONArray result = new JSONArray();
	    int index = 1;
	    JSONObject o = new JSONObject();
		o.put("column_name", "*");
		o.put("comments", "全部列");
		result.add(o);
		try {
			for( int i=0;i<tlist.size();i++){						
				Map data = (Map)tlist.get(i);
				String column_name = (String)data.get("column_name");
				String comments = (String)data.get("comments");
				o = new JSONObject();
				o.put("column_name", column_name);
				o.put("comments", comments);																	
				result.add(index, o);				
				index++;
			}
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			return result;			
	}
	
	/**
	 * 获取where列
	 */
	public JSONArray getws(String table_name) throws Exception{
		String strSql = "select distinct column_name,nvl(comments,column_name) comments from user_col_comments where table_name ='"+ table_name +"' and COLUMN_NAME not like 'ATTR%' " +
				"and  COLUMN_NAME not like 'HOLD%' and  COLUMN_NAME not like '%REMARK%' and  COLUMN_NAME !='BUSINESS_ID' and  COLUMN_NAME !='ID' ";
	    List tlist=  dao.queryBySql(strSql,null);
	    JSONArray result = new JSONArray();
	    int index = 0;
		try {
			for( int i=0;i<tlist.size();i++){						
				Map data = (Map)tlist.get(i);
				String column_name = (String)data.get("column_name");
				String comments = (String)data.get("comments");
				JSONObject o = new JSONObject();
				o.put("column_name", column_name);
				o.put("comments", comments);	
				if (result.size() > 0) {
					result.add(index, o);
				} else {
					result.add(o);
				}			
				index++;
			}
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			return result;			
	}
	/**
	 * 获取where操作符
	 * @param table_name
	 * @return
	 * @throws Exception
	 */
	public JSONArray getWheres() throws Exception{
		String strSql = "select WHERES from DSS_WHERES";
	    List tlist=  dao.queryBySql(strSql,null);
	    JSONArray result = new JSONArray();
	    int index = 0;
		try {
			for( int i=0;i<tlist.size();i++){						
				Map data = (Map)tlist.get(i);
				String wheres = (String)data.get("WHERES");				
				JSONObject o = new JSONObject();
				o.put("wheres", wheres);	
				if (result.size() > 0) {
					result.add(index, o);
				} else {
					result.add(o);
				}			
				index++;
			}
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			return result;			
	}
	
	public JSONArray getGrid(String tname,String select,String query) {
		String scrips = "select "+select+",id from "+tname+" "+query;
		System.out.println("scrips:"+scrips);
		JSONArray result = new JSONArray();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		
		int index = 0;
		try {
			con = dao.getMyDataSource().getConnection();
			//System.out.println(1);
			ps = con.prepareStatement(scrips);		
			//System.out.println(2);
			rs = ps.executeQuery();
			//System.out.println(3);
			ResultSetMetaData metaData = rs.getMetaData();
			int ncolumns = metaData.getColumnCount();
			//System.out.println("ncolumns:"+ncolumns);
			while(rs.next()) {
				JSONObject o = new JSONObject();
				for (int i = 1; i <= ncolumns; i++) {
					o.put(metaData.getColumnName(i),rs.getString(i));										
				}//end for
				if (result.size() > 0) {
					result.add(index, o);
				} else {
					result.add(o);
				}			
				index++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			rs = null;
			ps = null;
			con = null;
		}
		//System.out.println("result:"+result.toString());
		return result;
	}
	
	public JSONArray getFields(String tname,String select) {
		String scrips = "select "+select+",id from "+tname+" where rownum=1";//id不设置在查询条件中，但放在表格中，用于追溯查询
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		JSONArray result = new JSONArray();
	    int index = 0;
		try {
			con = dao.getMyDataSource().getConnection();
			ps = con.prepareStatement(scrips);			
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int ncolumns = metaData.getColumnCount();
			for (int i = 1; i <= ncolumns; i++) {
				JSONObject o = new JSONObject();
				o.put("name", metaData.getColumnName(i));				
				if (result.size() > 0) {
					result.add(index, o);
				} else {
					result.add(o);
				}			
				index++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			rs = null;
			ps = null;
			con = null;
		}		
		return result;
	}
	
	public JSONArray getColumModle(String tname,String select) {		
		JSONArray result =  getFields( tname, select);
		JSONArray aresult = new JSONArray();
		try {
			String strSql = "select distinct column_name,nvl(comments,column_name) comments from user_col_comments where table_name ='"+ tname +"'";
			List tlist = dao.queryBySql(strSql, null);
			int index=0;
			for(int i=0;i<result.size();i++){
				JSONObject obj = (JSONObject)result.get(i);
				String name = (String) obj.get("name");
				for(int j=0;j<tlist.size();j++){
					Map data = (Map)tlist.get(j);
					String column_name = (String)data.get("column_name");
					if(column_name.equals(name)){
						String comments = (String)data.get("comments");
						JSONObject o = new JSONObject();
						o.put("header", comments);
						o.put("dataIndex", column_name);	
						if (aresult.size() > 0) {
							aresult.add(index, o);
						} else {
							aresult.add(o);
						}
						break;
					}//end if												
				}//end for j
				index++;
			}//end for i
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("aresult:"+aresult.toString());
		return aresult;
	}
	
	//获取溯源数据表格
	public JSONArray getSgrid(String tname,String id) {
		JSONArray js= new JSONArray();
		JSONObject json = new JSONObject();
		json.put("ds",getSourceGrid(tname,id));
		json.put("fs",getSourceFields(tname,id));
		json.put("cm",getSourceCM(tname,id));
		js.add(json);
		System.out.println("getSgrid json:"+js.toString());
		return js;
	}
	
	//是否有溯源标识ID的信息
	public boolean getSid(String tname,String id) {
		boolean flag=false;
		Connection con = null;
		try {
			//调用存储过程
			  DataSource ds = dao.getMyDataSource();
			  con = ds.getConnection();
//			Hashtable ht = new Hashtable();   
//	          ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");   
//	          ht.put(Context.PROVIDER_URL, "t3://127.0.0.1:7001");            
//	          Context context=new InitialContext(ht);
//			  DataSource  ds = (DataSource) context.lookup("webglDS_A6");  //配置的JNID名   
//	            con = ds.getConnection("weblogic", "weblogic");  //登陆weblogic的用户名、密码   
			  Statement stmt=con.createStatement();  
	          String sql="select nvl(business_id,0) from "+ tname +" where id='"+ id +"' ";  
	          ResultSet rs=stmt.executeQuery(sql);  	         
	          while (rs.next()){ 
//	        	  oracle.sql.CLOB clob = (oracle.sql.CLOB)rs.getObject(1); 
//	        	  String st = ClobToString(clob);
	        	  String st=rs.getString(1);
	        	  System.out.println("business_id"+st);
	        	  if(st.equals("0")){
	        		  
	        	  }else{
	        		  flag=true;  
	        	  }	              
	          }
	          rs.close();
	          stmt.close();
	          con.close();
	          rs=null;
	          stmt=null;
	          con=null;
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();		
		}
		return flag;
	}
	
	//oracle.sql.Clob类型转换成String类型  
	  public static String ClobToString(CLOB clob) throws SQLException, IOException {	   
		   String reString = "";
		   Reader is = clob.getCharacterStream();// 得到流
		   BufferedReader br = new BufferedReader(is);
		   String s = br.readLine();
		   StringBuffer sb = new StringBuffer();
		   while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
			   sb.append(s);
			   s = br.readLine();
		   }
		   reString = sb.toString();
		   return reString;
	  }
	  
	  //获取溯源标识business_id
	  public String getBusinessId(String tname,String id) {
		boolean flag=false;
		Connection con = null;
		String st ="";
		try {
			//调用存储过程
			  DataSource ds = dao.getMyDataSource();
			  con = ds.getConnection();
//			Hashtable ht = new Hashtable();   
//	          ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");   
//	          ht.put(Context.PROVIDER_URL, "t3://127.0.0.1:7001");            
//	          Context context=new InitialContext(ht);
//			  DataSource  ds = (DataSource) context.lookup("webglDS_A6");  //配置的JNID名   
//	          con = ds.getConnection("weblogic", "weblogic");  //登陆weblogic的用户名、密码   
			  
			  Statement stmt=con.createStatement();  
	          String sql="select nvl(business_id,0) from "+ tname +" where id='"+ id +"' ";  
	          System.out.println("getBusinessId:"+sql);
	          ResultSet rs=stmt.executeQuery(sql);  	         
	          while (rs.next()){ 
//	        	  oracle.sql.CLOB clob = (oracle.sql.CLOB)rs.getObject(1); 
//	        	   st = ClobToString(clob);	 
	        	  st=rs.getString(1);
	          }
	          rs.close();
	          stmt.close();
	          con.close();
	          rs=null;
	          stmt=null;
	          con=null;
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();		
		}
		return st;

	}
	  /**
	   * 获取表格数据
	   */
	  public JSONArray getSourceGrid(String tname,String id) {
		  JSONArray tablearr = new JSONArray();
	  
		  
		  String st2 = getBusinessId(tname,id);//溯源标识ID
		  System.out.println("st2:"+st2);
		  String[] as = st2.split(";");	
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection con = null;
		
		  for(int w=0;w<as.length;w++){
			  JSONArray result = new JSONArray();
			  System.out.println("as:"+as[w]);
			String[] bs=as[w].split("#");
			bs[2]=bs[2].replace(",","','");
			StringBuffer sb = new StringBuffer("'");
			sb.append(bs[2]).append("'");//主键的值
			//System.out.println(sb.toString());	
			String scrips="";
			try {
				if(bs[0].indexOf("@")>-1){//业务系统表
					scrips = "select * from "+ bs[0] +" where "+bs[1]+" in ("+sb.toString() +")";
					System.out.println("scrips grid1:"+scrips);
				}else{//数据仓库表
				    JSONArray js2 = getws(bs[0]);//表对应的列名和列中文名					
				    StringBuffer sb2 = new StringBuffer("'");
				    for(int j=0;j<js2.size();j++){
				    	JSONObject obj = (JSONObject)js2.get(j);
						String name = (String) obj.get("column_name");
						sb2.append(name).append(",");
					}
					sb2.deleteCharAt(sb2.lastIndexOf(","));
					scrips = "select "+sb2.toString()+" from "+ bs[0] +" where id in in ("+sb.toString() +")";
					System.out.println("scrips grid2:"+scrips);
				}
				int index = 0;
				con = dao.getMyDataSource().getConnection();
				ps = con.prepareStatement(scrips);		
				rs = ps.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int ncolumns = metaData.getColumnCount();
				System.out.println("ncolumns:"+ncolumns);
				while(rs.next()) {
					JSONObject o = new JSONObject();
					for (int i = 1; i <= ncolumns; i++) {
						o.put(metaData.getColumnName(i),rs.getString(i));										
					}//end for
					if (result.size() > 0) {
						result.add(index, o);
					} else {
						result.add(o);
					}			
					index++;
				}//end while
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				rs = null;
				ps = null;
				con = null;
			}
			JSONObject json = new JSONObject();
			System.out.println("tablearr ds:"+result.toString());
			json.put("tname",bs[0]);
			json.put("ds",result);
			tablearr.add(w,json);
		  }//end for w
		  
			System.out.println("tablearr grid:"+tablearr.toString());
			return tablearr;
		}//getSourceGrid
		
		public JSONArray getSourceFields(String tname,String id) {
			JSONArray tablearr = new JSONArray();
			
			  String st2 = getBusinessId(tname,id);//溯源标识ID
			  String[] as = st2.split(";");	
				PreparedStatement ps = null;
				ResultSet rs = null;
				Connection con = null;
			try {
			  for(int w=0;w<as.length;w++){
				  JSONArray result = new JSONArray();
				String[] bs=as[w].split("#");
				bs[2]=bs[2].replace(",","','");
				StringBuffer sb = new StringBuffer("'");
				sb.append(bs[2]).append("'");//主键的值
				//System.out.println(sb.toString());	
				String scrips="";
				if(bs[0].indexOf("@")>-1){//业务系统表
					scrips = "select * from "+ bs[0] +" where rownum=1";
					System.out.println("scrips fields1:"+scrips);
				}else{//数据仓库表
				    JSONArray js2 = getws(bs[0]);
					StringBuffer sb2 = new StringBuffer("'");
				    for(int j=0;j<js2.size();j++){
				    	JSONObject obj = (JSONObject)js2.get(j);
						String name = (String) obj.get("column_name");
						sb2.append(name).append(",");
					}
					sb2.deleteCharAt(sb2.lastIndexOf(","));
					scrips = "select "+sb2.toString()+" from"+ bs[0] +" where rownum=1";
					System.out.println("scrips fields2:"+scrips);												    
				}//end else
//			String scrips = "select t."+select+",id from "+tname+" t where rownum=1";//id不设置在查询条件中，但放在表格中，用于追溯查询
		        int index = 0;

				con = dao.getMyDataSource().getConnection();
				ps = con.prepareStatement(scrips);			
				rs = ps.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int ncolumns = metaData.getColumnCount();
				for (int i = 1; i <= ncolumns; i++) {
					JSONObject o = new JSONObject();
					o.put("name", metaData.getColumnName(i));				
					if (result.size() > 0) {
						result.add(index, o);
					} else {
						result.add(o);
					}			
					index++;
				}//end for i
				JSONObject json = new JSONObject();
				json.put("tname",bs[0]);
				json.put("fields",result);
				tablearr.add(w,json);
			  }//end for w
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				rs = null;
				ps = null;
				con = null;
			}	
			System.out.println("tablearr field:"+tablearr.toString());
			return tablearr;
		}
		
		public JSONArray getSourceCM(String tname,String id) {	
			JSONArray tablearr = new JSONArray();
			  String st2 = getBusinessId(tname,id);//溯源标识ID
			  String[] as = st2.split(";");	
				PreparedStatement ps = null;
				ResultSet rs = null;
				Connection con = null;
			try {
			  for(int w=0;w<as.length;w++){
				  JSONArray result = new JSONArray();
				String[] bs=as[w].split("#");
				bs[2]=bs[2].replace(",","','");
				StringBuffer sb = new StringBuffer("'");
				sb.append(bs[2]).append("'");//主键的值
				//System.out.println(sb.toString());	
				String scrips="";
				JSONArray aresult = new JSONArray();
				if(bs[0].indexOf("@")>-1){//业务系统表					
//					String strSql = "select distinct column_name,nvl(comments,column_name) comments from user_col_comments where table_name ='"+ bs[0] +"'";
//					List tlist = dao.queryBySql(strSql, null);
//					int index=0;
					JSONArray bresult = getSourceFields(tname,id);
					JSONObject bobj = (JSONObject)bresult.get(0);
					result = (JSONArray) bobj.get("fields");
					for(int i=0;i<result.size();i++){
						JSONObject obj = (JSONObject)result.get(i);
						String name = (String) obj.get("name");						
						JSONObject o = new JSONObject();
						o.put("header", name);
						o.put("dataIndex", name);	
						aresult.add(i, o);																														
					}//end for i
				}else{//数据仓库表												    				
					String strSql = "select distinct column_name,nvl(comments,column_name) comments from user_col_comments where table_name ='"+ bs[0] +"'";
					List tlist = dao.queryBySql(strSql, null);
					int index=0;
					JSONArray bresult = getSourceFields(tname,id);
					JSONObject bobj = (JSONObject)bresult.get(0);
					result = (JSONArray) bobj.get("fields");					
					for(int i=0;i<result.size();i++){
						JSONObject obj = (JSONObject)result.get(i);
						String name = (String) obj.get("name");
						for(int j=0;j<tlist.size();j++){
							Map data = (Map)tlist.get(j);
							String column_name = (String)data.get("column_name");
							if(column_name.equals(name)){
								String comments = (String)data.get("comments");
								JSONObject o = new JSONObject();
								o.put("header", comments);
								o.put("dataIndex", column_name);	
								if (aresult.size() > 0) {
									aresult.add(index, o);
								} else {
									aresult.add(o);
								}
								break;
							}//end if												
						}//end for j
						index++;
					}//end for i
				}//end else
				JSONObject json = new JSONObject();
				json.put("tname",bs[0]);
				json.put("cm",aresult);
				tablearr.add(w,json);
				
			  }//end for w
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("tablearr cm:"+tablearr.toString());
			return tablearr;
		}
}
