package com.babeeta.butterfly.application.app.reliablepush.dao.impl;


import java.util.List;

import com.babeeta.butterfly.application.app.record.entity.MessageRecord;
import com.babeeta.butterfly.application.app.reliablepush.dao.ReliablePushDao;
import com.babeeta.butterfly.application.app.reliablepush.entity.ReliablePushBean;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/***
 * 可靠投递
 * @author zeyong.xia
 * @date 2011-9-19
 */
public class ReliablePushDaoImpl extends BasicDaoImpl implements ReliablePushDao{
 
	private static final String DB_NAME="reliable_push";
	
	public ReliablePushDaoImpl()
	{
		datastore = morphia.createDatastore(mongo, DB_NAME);
		datastore.ensureIndexes();
	}
	
	/***
	 * 查询未ack的消息id
	 * @param aid
	 * @param cid
	 * @return
	 */
	public List<ReliablePushBean> queryNotAppAckMessageId(String aid,String cid)
	{
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);
		Query<ReliablePushBean> query=this.datastore.createQuery(ReliablePushBean.class)
		                              .filter("status", "DELIVERING")
		                              .filter("key", key);
		return query.asList();
//		List<ReliablePushBean> list=new ArrayList<ReliablePushBean>();
////		BasicDBObject key = new BasicDBObject();
////		key.put("aid", aid);
////		key.put("cid", cid);
//		DBCursor c=mongo.getDB(DB_NAME).getCollection(DB_NAME).find(new BasicDBObject("key",key),new BasicDBObject("status","DELIVERING"));
//		ReliablePushBean bean=null;
//		DBObject obj=null;
//		while(c.hasNext())
//		{
//			bean=new ReliablePushBean();
//			obj=c.next();
//			bean.setId(obj.get("_id").toString());
//			bean.setAge(0);//TODO
//			bean.setExpiredAt((Date)(obj.get("expiredAt")));
//			//bean.setKey(obj.get("key").toString());
//			bean.setMessage((byte[])obj.get("message"));
//			bean.setParentId(obj.get("parentId").toString());
//			bean.setStatus(obj.get("status").toString());
//			bean.setType(0);
//			list.add(bean);
//		}
//		return list;
	}	
	
	/***
	 * 锁定消息状态
	 */
	public void updateStatus(List<ReliablePushBean> list)
	{
		if(list!=null&&list.size()>0)
		{
			DBCollection  co=mongo.getDB(DB_NAME).getCollection(DB_NAME);
			for(ReliablePushBean bean :list)
			{
				co.update(new BasicDBObject("_id",bean.getId()), new BasicDBObject("status","EXPIRED"));
			}
		}
	}
	/***
	 * 重新设置消息状态
	 * 设定是应该多给10秒过期时间
	 */
	public void updateStatusToPush(List<ReliablePushBean> list)
	{
		//TODO
		if(list!=null&&list.size()>0)
		{
			DBCollection  co=mongo.getDB(DB_NAME).getCollection(DB_NAME);
			for(ReliablePushBean bean :list)
			{
				co.update(new BasicDBObject("_id",bean.getId()), new BasicDBObject("status","DELIVERING"));
			}
		}
	}
	

	/***
	 * 修改cid
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 */
	public void updateCid(String oldCid,String newCid,String aid)
	{
		BasicDBObject key=new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", oldCid);
		
		Query<ReliablePushBean> querys=this.datastore.createQuery(ReliablePushBean.class)
        .filter("status", "DELIVERING")
        .filter("key", key);
		List<ReliablePushBean> list=querys.asList();
		if(list!=null&&list.size()>0)
		{
			for(ReliablePushBean b :list)
			{
				Query<ReliablePushBean> query=this.datastore.createQuery(ReliablePushBean.class).filter("_id", b.getId());
				BasicDBObject keys=new BasicDBObject();
				key.put("aid", aid);
				key.put("cid", newCid);
				UpdateOperations<ReliablePushBean> ops = datastore
				.createUpdateOperations(ReliablePushBean.class);
				ops.set("key", keys);
				this.datastore.update(query, ops);
			}
		}
	}
}
