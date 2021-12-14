package com.philips.rabbitmqconsumertest.config;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.services.exceptions.ApplicationException;

public class ApplicationErrorHandler implements ErrorHandler {

	@Override
	public void handleError(Throwable t) {
		if (t.getCause() instanceof ApplicationException) {
			throw new AmqpRejectAndDontRequeueException("DLX OK!!!!", true, t);
		}
	}

}
