package com.babeeta.butterfly.application.app.record;

import com.babeeta.butterfly.application.app.record.dao.MessageRecordDao;
import com.babeeta.butterfly.application.app.record.dao.impl.MessageRecordDaoImpl;

public class MessageRecordService {
	private static final MessageRecordDao messageRecordDao = new MessageRecordDaoImpl();

	private static final MessageRecordService defaultInstance = new MessageRecordService();

	private MessageRecordService() {

	}

	public static MessageRecordService getDefaultInstance() {
		return defaultInstance;
	}

	public MessageRecordDao getDao() {
		return messageRecordDao;
	}
}
