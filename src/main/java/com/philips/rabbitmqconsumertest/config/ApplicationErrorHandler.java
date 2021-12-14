package com.philips.rabbitmqconsumertest.config;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.services.exceptions.OrderException;

public class ApplicationErrorHandler implements ErrorHandler {

	@Override
	public void handleError(Throwable t) {
		if (t.getCause() instanceof OrderException) {
			throw new AmqpRejectAndDontRequeueException("DLX OK!!!!", true, t);
		}
	}

}
