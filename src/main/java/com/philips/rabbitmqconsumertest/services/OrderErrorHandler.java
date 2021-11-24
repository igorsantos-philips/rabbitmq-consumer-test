package com.philips.rabbitmqconsumertest.services;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.services.exceptions.OrderException;

public class OrderErrorHandler implements ErrorHandler {

	@Override
	public void handleError(Throwable t) {
		if(t.getCause() instanceof OrderException) {
			   throw new AmqpRejectAndDontRequeueException("DLX OK!!!!", true, t);
		}
		
	}

}
