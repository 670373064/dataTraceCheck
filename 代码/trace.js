Ext.onReady(function(){
	
	
//左侧树开始----------------------
	var biServerTree = new Ext.tree.TreePanel({
		id:'biServerTree',
		region:'west',
		border:true,
		bodyStyle:'padding:16px 0 0 18px;',
		rootVisible: true,
		autoScroll : true,
		animate : false,
		useArrows : false,
		root:{
			nodeType: 'async',
			text : "全部",
			id:-1
		},
		dataUrl:'../getTree.action',
		listeners:{
			'click': function(node, event){				
				if(node.leaf == true){				
					var win  = getSimpleWin(node.attributes.param);
					win.show();
				}
            }
		},
		tbar:[
				{
					text : '展开',
					iconCls : 'expand-allIcon',
					handler : function() {
						biServerTree.root.expand(true, true);
					}
				},'-',{
					text : '收缩',
					iconCls : 'collapse-allIcon',
					handler : function() {
						biServerTree.root.collapse(true, false);
					}
				}
				,'-',{
						text : '刷新',
						iconCls : 'arrow_refreshIcon',
						handler : function() {
							biServerTree.root.reload();
						}
					}
					
		      ]
	});//biServerTree
	biServerTree.root.expand(true,true);

	
	biServerTree.on('click', function(node, event) { 
    }, biServerTree);
	

     
    /*****************************************/        
   function getSimpleWin(table_name){		
   	var comboStore = new Ext.data.JsonStore({//
 	       fields : [
 	           'column_name' ,'comments'
 	       ],
 	       root:'',
 	       url:'../getSelects.action?table_name='+table_name,
 	       autoLoad:true
 	   }); 
   	var wheresStore = new Ext.data.JsonStore({//
	       fields : [
	           'wheres' 
	       ],
	       root:'',
	       url:'../getWheres.action',
	       autoLoad:true
	   }); 
   	var wsStore = new Ext.data.JsonStore({//
	       fields : [
	           'column_name' ,'comments'
	       ],
	       root:'',
	       url:'../getws.action?table_name='+table_name,
	       autoLoad:true
	   }); 
    var selectscombo = new Ext.form.MultiSelect({
    	fieldLabel:"select列选",
        width: 200,
        editable: false,
        store: comboStore,
        displayField:'comments',
		valueField:'column_name',
        mode: 'local',
        triggerAction: 'all',
        name:"select",
        id:"select",
        allowBlank: false,
        emptyText: '请选择',
        maxHeight:200 //下拉框的最大高度
    });
 	var formPanel = new Ext.form.FormPanel({
 		region:'center',
 		width:450,
 		height:500,
 		border:false,
 		bodyStyle:'padding:5px 5px 0px 50px',
 		method:'post',
 		defaults:{
 			xtype:'textfield',
 			labelAlign:'left',
 			labelWidth:80,
 	        width:220
 		},
 		items:[{				
				fieldLabel:'取数表名',
	            name:"tname",
	            id:"tname",
				readOnly:true,
				value:table_name
   		},
   		selectscombo
   		,{
			xtype:'combo',
			store:wheresStore,
			fieldLabel:"where操作符",
			mode: 'local',
			displayField:'wheres',
			valueField:'wheres',
			triggerAction :'all',
            name:"where",
            id:"where",
            editable :false,
            emptyText:'emptyText',
            listeners:{
            	select:function(){
            		var query = Ext.getCmp('query');
            		query.setValue(query.getValue()+" "+this.getValue());
            	}
            }
		},{
			xtype:'combo',
			store:wsStore,
			fieldLabel:"where列",
			mode: 'local',
			displayField:'comments',
			valueField:'column_name',
			triggerAction :'all',
            name:"ws",
            id:"ws",
            editable :false,
            emptyText:'emptyText',
            listeners:{
            	select:function(){
            		var query = Ext.getCmp('query');
            		query.setValue(query.getValue()+" "+this.getValue());
            	}
            }
		},{
 			fieldLabel:"where条件",
             name:"query",
             id:"query",
             height:100
 		}]
 	}); 	
 	
	var simpleWin = new Ext.Window({
		id:'sim_win',
		title:'数据溯源核对设置查询条件',
		width:500,
		height:350,
		resizable:false,
		modal:true,
		autoScroll:false,
		bodyStyle:'background-color:#fff;padding:5px 5px 0 5px;',
		buttonAlign : 'right',
		//items:[sinPanel],
		items:[formPanel],
		buttons:[{
			text:'确定',
			handler:function(){
				Ext.Ajax.request({
 					url:'../getQuery.action',
 					params:{
   	 					tname:Ext.getCmp("tname").getValue(),
   	 				    select:Ext.getCmp("select").getValue(),
   	 			        query:Ext.getCmp("query").getValue()  	 						
 					},
 					success:function(response,options){
 						var txt = Ext.util.JSON.decode(response.responseText);
 						
   	 					Ext.getCmp("tnames").setValue(Ext.getCmp("tname").getValue());
   	   					Ext.getCmp("selects").setValue(Ext.getCmp("select").getValue());
   	   					Ext.getCmp("querys").setValue(Ext.getCmp("query").getValue());
   	   				    getSourcetable();
   	   				    simpleWin.destroy();						
 					},
 					failure:function(){
 						Ext.Msg.show({
 		                    title : '错误提示',
 		                    msg : '查询失败，请检查查询条件!!!',
 		                    buttons : Ext.Msg.OK,
 		                    icon : Ext.Msg.ERROR
 		                });
 					    simpleWin.destroy();
 					}
 				});   
				
			}
		},{
			text:'取消',
			handler:function(){
				simpleWin.destroy();
			}
		}]
	});
   		return simpleWin;
}//getSimpleWin
     
//左侧树结束-----------------------------

//右侧表格开始------------
	
   function getSourcetable(){
	 var myname = Ext.getCmp("tnames").getValue();
	 Ext.Ajax.request({
		url:'../getGrid.action',
		params:{
			tname:myname,
		    select:Ext.getCmp("selects").getValue(),
	        query:Ext.getCmp("querys").getValue(),   	 						
		},
		success:function(response,options){
			var json = Ext.util.JSON.decode(response.responseText);
		    var sm = new Ext.grid.RowSelectionModel({
				singleSelect : true,
				listeners: { 
					rowselect: function(sm, row, rec) {
						var uid=rec.get('ID');
						var icount=1;						
						getSourceWin(myname,uid,icount);//
					},
					rowdeselect: function(sm, row, rec) { 
					} 
				} 
			});
	
			var cm = new Ext.grid.ColumnModel( json.columModle);
		    var ds = new Ext.data.JsonStore({
		    data:json.items,
		    fields:json.fieldsNames
		    });
		    var parentGrid = new Ext.grid.GridPanel({
		       	id:"parentgrid",
	    	    region: 'center',
	    	    split: true,
	    	    border:false,
	    	    loadMask : true,
	    		sm : sm,
	    	    cm:cm,
	    	    ds:ds,
	    	    clicksToEdit: 1,
	    	    tbar:[myname]
	       });//parentGrid
		    panel1.add(parentGrid);
		    panel1.doLayout();
		},//success
		failure:function(){
			Ext.Msg.show({
	            title : '错误提示',
	            msg : '查询失败，请检查查询条件!!!',
	            buttons : Ext.Msg.OK,
	            icon : Ext.Msg.ERROR
	        });
		}
	  });
   }//getSourcetable
	 
	  /**********************************************/
	  function getSource(json,tname){


	  }//getSource

	  /**********************************************/
	  function getSourceWin(tname,uid,icount){//获取溯源标识ID
		  Ext.Ajax.request({
				url:'../getSid.action',
				params:{
					tname:tname,
					uid:uid
			         	 						
				},
				success:function(response,options){
					var json = Ext.util.JSON.decode(response.responseText);
					if(json.result=="true"){
						getSourceAll(tname,uid,icount);//	
					}else{
						Ext.Msg.alert('提示',"没有数据溯源信息!!!");
					}
				},
				failure:function(){
					Ext.Msg.show({
			            title : '错误提示',
			            msg : '没有数据溯源信息!!!',
			            buttons : Ext.Msg.OK,
			            icon : Ext.Msg.ERROR
			        });
				}
			  });  	  
	  }
	  /**********************************************/
	  function getSourceAll(tname,uid,icount){	 //获取溯源多表格 
		  Ext.Ajax.request({
				url:'../getSgrid.action',
				params:{
					tname:tname,
					uid:uid
			         	 						
				},
				success:function(response,options){
//					alert(response.responseText);
					var ajson = Ext.util.JSON.decode(response.responseText);				
                    var panel = new Ext.Panel({
        				region:'center',
        				layout:'fit',
        				border:false,
        				id:'apanel'//,
        			});
                    for (var i=0;i<ajson[0].fs.length;i++){
//                    alert(ajson[0].fs.length);
                    	var sourcetable=ajson[0].fs[i].tname;//追溯的来源表名称
	                    var sm = new Ext.grid.RowSelectionModel({
	        				singleSelect : true,
	        				listeners: { 
	        					rowselect: function(sm, row, rec) {
	        						if(sourcetable.indexOf("@")>-1){
	        							alert("已经追溯到源业务系统");
	        						}else{
	        							var uid=rec.get('ID');
		        						icount = icount+1;
		        						var win  = getSourceWin(sourcetable,uid,icount);//
		        						win.show();
	        						}	        						
	        					},
	        					rowdeselect: function(sm, row, rec) { 
	        					} 
	        				} 
	        			});//sm
	
	        			var cm = new Ext.grid.ColumnModel(ajson[0].cm[i].cm);
	        		    var ds = new Ext.data.JsonStore({
	        		    data:ajson[0].ds[i].ds,
	        		    fields:ajson[0].fs[i].fields
	        		    });
	        		   
        		    	var compoGrid = new Ext.grid.GridPanel({
        		    		id:"grid"+i,
        				    region: 'center',
        				    width:800,//
        					height:300,//
        				    split: true,
        				    border:false,
        				    loadMask : true,
        					sm : sm,
        				    cm:cm,
        				    ds:ds,
        				    tbar:[sourcetable]
        			    });
        			    panel.add(Ext.getCmp("grid"+i));
                  }//end for  i
                    panel.doLayout();
	        		  var sourceWin = new Ext.Window({
	        			  id:"hello"+icount,
	        				title:'数据溯源核对'+icount,
	        				width:1000,
	        				height:300,
	        				resizable:false,
	        				modal:true,
	        				autoScroll:true,
	        				bodyStyle:'background-color:#fff;padding:5px 5px 0 5px;',
	        				buttonAlign : 'right',
	        				items:[panel]
	        		  });
	        		  sourceWin.show();
				},
				failure:function(){
					Ext.Msg.show({
			            title : '错误提示',
			            msg : '查询失败!!!',
			            buttons : Ext.Msg.OK,
			            icon : Ext.Msg.ERROR
			        });
				}
			  }); 
		  
	  }//getSourceAll
		   
//右侧表格结束----------
	var panel1=new Ext.Panel({id:"sourcetable",layout:'fit',region:'center'});
	var panel = new Ext.Panel({
		region:'center',
		layout:'border',
		border:false,
		id:'pp',
		items:[biServerTree,panel1,{id: "tnames",xtype: "hidden"},{id: "selects",xtype: "hidden"},{id: "querys",xtype: "hidden"}]
	});
	var view = new Ext.Viewport({
		layout:'border',
		items : [panel]
	});
//	panel.render("container");
	panel.doLayout();
});