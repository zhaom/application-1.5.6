package com.babeeta.butterfly.application.app.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageHandler;
import com.babeeta.butterfly.MessageRouting.Message;
import com.babeeta.butterfly.application.app.record.MessageRecordService;

public class MessageStatusListener implements MessageHandler {
	private final static Logger logger = LoggerFactory
			.getLogger(MessageStatusListener.class);

	public MessageStatusListener() {
	}

	@Override
	public void onMessage(final Message message) {
		logger.debug(
				"[Message Status Listener] recevice message: FROM {} TO {}",
				message.getFrom(), message.getTo());
		if (message.getTo().indexOf("@app") > -1) {
			String messageId = message.getTo().substring(0,
					message.getTo().indexOf("@"));
			String newStatus = message.getContent().toStringUtf8();
			boolean isUpdate = false;
			logger.debug(
					"[Message Status Listener] change status of message [{}] to {}",
					messageId, newStatus);
			if (newStatus.equalsIgnoreCase("EXPIRED")) {
				isUpdate = MessageRecordService.getDefaultInstance()
						.getDao().updateExpired(messageId);
			} else if (newStatus.equalsIgnoreCase("APP_ACKED")) {
				isUpdate = MessageRecordService.getDefaultInstance()
						.getDao().updateAppAcked(messageId);
			} else if (newStatus.equalsIgnoreCase("ACKED")) {
				isUpdate = MessageRecordService.getDefaultInstance()
						.getDao().updateAcked(messageId);
			} else {
				logger.info("Invalid status. {}", newStatus);
			}

			if (isUpdate) {
				logger.info(
						"New status [{}] of message [{}] update successful.",
						newStatus, messageId);
			} else {
				logger.info("New status [{}] of message [{}] update failed.",
						newStatus, messageId);
			}
		} else {
			logger.error("[Message Status Listener] something wrong in DNS. message sent to wrong place.");
		}
	}

}
