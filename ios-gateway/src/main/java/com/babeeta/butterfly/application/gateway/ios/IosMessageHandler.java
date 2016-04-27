package com.babeeta.butterfly.application.gateway.ios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageHandler;
import com.babeeta.butterfly.MessageRouting.Message;

class IosMessageHandler implements MessageHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(IosMessageHandler.class);

	private final ApnService apnService;

	public IosMessageHandler(ApnService apnService) {
		this.apnService = apnService;
	}

	@Override
	public void onMessage(Message message) {
		logger.debug("Apns message arrived.[{}] {}", message.getUid(), message
				.getContent().toStringUtf8());
		apnService.send(message);
	}

}
