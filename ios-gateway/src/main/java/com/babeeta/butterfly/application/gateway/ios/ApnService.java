package com.babeeta.butterfly.application.gateway.ios;

import com.babeeta.butterfly.MessageRouting.Message;

public interface ApnService {
	void send(Message message);
}
