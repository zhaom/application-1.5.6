package com.babeeta.butterfly.application.app.record.entity;

import java.util.Date;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
/***
 * 添加createAt and delay
 * @update zeyong.xia
 * @date 2011-9-22
 */
@Entity(value = "MessageRecord", noClassnameStored = true)
public class MessageRecord {
	@Id
	private String messageId;

	@Indexed
	private String appId;

	@Indexed
	private String recipient;

	@Indexed
	private String status;

	private Date lastModified;

	private int totalSubMessage;
	private int appAckedCount;
	private int ackedCount;

	private String dataType;
	private byte[] content;
	private int expire;
	private boolean broadcastFlag;
	
	/////////zeyong.xia add
	private Date createAt;//创建时间
	
	private int delay;//延时时间，分钟，应该小于24×60

	public void setBroadcastFlag(boolean broadcastFlag) {
		this.broadcastFlag = broadcastFlag;
	}

	public boolean getBroadcastFlag() {
		return broadcastFlag;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public int getTotalSubMessage() {
		return totalSubMessage;
	}

	public void setTotalSubMessage(int totalSubMessage) {
		this.totalSubMessage = totalSubMessage;
	}

	public int getAckedCount() {
		return ackedCount;
	}

	public void setAckedCount(int ackedCount) {
		this.ackedCount = ackedCount;
	}

	public int getAppAckedCount() {
		return appAckedCount;
	}

	public void setAppAckedCount(int appAckedCount) {
		this.appAckedCount = appAckedCount;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	
}
