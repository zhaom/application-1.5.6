package com.babeeta.butterfly.application.app.pusher;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageRouting;
import com.babeeta.butterfly.MessageSender;
import com.babeeta.butterfly.application.app.MessageContext;
import com.babeeta.butterfly.application.app.record.MessageRecordService;
import com.babeeta.butterfly.application.app.tag.TagResult;
import com.babeeta.butterfly.application.app.tag.TagService;
import com.babeeta.butterfly.application.app.tag.TagServiceImpl;
import com.babeeta.butterfly.application.reliable.ReliablePush;
import com.babeeta.butterfly.application.reliable.ReliablePushImpl;
import com.babeeta.butterfly.router.network.MessageSenderImpl;
import com.google.protobuf.ByteString;

public class MessagePusher {
	private final static Logger logger = LoggerFactory
			.getLogger(MessagePusher.class);
	private static MessageSender MESSAGE_SENDER = new MessageSenderImpl();
	private static final MessagePusher defaultInstance = new MessagePusher();

	private static final TagService tagService = new TagServiceImpl();

	public final ThreadPoolExecutor executor = new ThreadPoolExecutor(32, 64,
			5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(1000));

	public static MessagePusher getDefaultInstance() {
		return defaultInstance;
	}

	public void unicast(String clientId, MessageContext mtx) {
		MessageRecordService.getDefaultInstance()
				.getDao().updateDelivering(mtx.getMessageId(), 1);

		MessageRouting.Message.Builder builder = MessageRouting.Message
				.newBuilder()
				.setDate(System.currentTimeMillis())
				.setExpire(mtx.getExpire())
				.setFrom(mtx.getSender() + "@" + "service.app")
				.setUid(mtx.getMessageId())
				.setParentId(mtx.getParentId());

		MessageRouting.Message msg = null;

		builder.setContent(ByteString.copyFrom(mtx.getContent()));

		builder.setTo(new StringBuilder(clientId).append(".")
				.append(mtx.getSender())
				.append("@gateway.dev").toString());

		msg = builder.build();
		ReliablePush reliablePush = ReliablePushImpl
				.getDefaultInstance();
		boolean saveResult = reliablePush.saveMessage(msg,
				mtx.getSender(),
				clientId);
		logger.debug("[{}] ReliablePush[{}] [{}]",
				new Object[] {
						mtx.getMessageId(),
						saveResult,
						new StringBuilder(mtx.getSender()).append(".")
								.append(clientId)
								.toString() });
		if (!saveResult) {
			logger.warn("[{}]failed to persistence.", mtx.getMessageId());
		}

		MESSAGE_SENDER.send(msg);
	}

	private final UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			logger.error("[broadcast]Uncaught exception: ", e);
		}
	};

	private String getGroupTagListString(String tagList, String appId) {
		// prepare group name list string
		StringBuilder groupNameList = new StringBuilder();
		if (tagList.indexOf(",") > -1) {
			String[] tagArray = tagList.split(",");
			boolean append = false;
			for (String tag : tagArray) {
				if (append) {
					groupNameList.append(",");
				} else {
					append = true;
				}
				groupNameList.append(appId + "@" + tag);
			}
		} else {
			groupNameList.append(appId + "@" + tagList);
		}

		return groupNameList.toString();
	}

	public void broadcast(final MessageContext mtx) {
		try {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setName("Broadcast");
					Thread.currentThread().setUncaughtExceptionHandler(
							uncaughtExceptionHandler);
					String group;
					if (mtx.getRecipient().indexOf("$") == 0)
					{
						group = mtx.getRecipient();
					}
					else
					{
						group = getGroupTagListString(
								mtx.getRecipient(), mtx.getSender());
					}
					TagResult result = tagService.listDevice(group);
					if (result.isSuccess()) {
						String[] deviceList = result.getStringList();
						if (deviceList == null || deviceList.length == 0) {
							logger.error("[broadcast] device list empty.");
						} else {
							logger.info(
									"[broadcast] message [{}] will be send to [{}] devices.",
									mtx.getMessageId(), deviceList.length);
							MessageRecordService
									.getDefaultInstance()
									.getDao()
									.updateDelivering(mtx.getMessageId(),
											deviceList.length);

							for (String device : deviceList) {
								MessageContext sendMtx = new MessageContext();
								sendMtx.setMessageId(java.util.UUID
										.randomUUID()
										.toString()
										.replaceAll("-", ""));
								sendMtx.setParentId(mtx.getMessageId());
								sendMtx.setSender(mtx.getSender());
								sendMtx.setRecipient(device);
								sendMtx.setDataType(mtx.getDataType());
								sendMtx.setExpire(mtx.getExpire());
								sendMtx.setContent(mtx.getContent());
								sendMtx.setBroadcastFlag(false);
								sendMtx.setDelay(0);
								sendMtx.setExpire(mtx.getExpire());

								unicast(device, sendMtx);
							}
							logger.info(
									"[broadcast] message [{}] sent.",
									mtx.getMessageId());
						}
					} else {
						logger.error("[broadcast] get device list failed.");
					}

				}
			});
		} catch (RejectedExecutionException e) {
			logger.error(
					"[broadcast]Broadcast message failed in start new thread.",
							e);
		}
	}
}
