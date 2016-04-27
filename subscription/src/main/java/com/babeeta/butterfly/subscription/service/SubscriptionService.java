package com.babeeta.butterfly.subscription.service;

import com.babeeta.butterfly.subscription.entity.Binding;

public interface SubscriptionService {

	public Binding bind(Binding binding) throws Exception;

	public String unbind(Binding binding) throws Exception;
}
