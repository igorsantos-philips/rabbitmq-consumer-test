package com.philips.rabbitmqconsumertest.config;

import java.util.Base64;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.philips.rabbitmqconsumertest.dtos.RequestAsyncProcessorVO;

public class ApplicationMessageConverter extends Jackson2JsonMessageConverter {

	@Override
	public Object fromMessage(Message message) throws MessageConversionException {
		final byte[] decodeMessage = decodeMessage(message.getBody());
		return parseMessage(decodeMessage);
	}

	private byte[] decodeMessage(byte[] encodedMessage) throws MessageConversionException {
		try {
			return Base64.getDecoder().decode(encodedMessage);
		} catch (Exception e) {
			throw new MessageConversionException("The message received is not a Base64 encoded message.", e);
		}
	}

	private RequestAsyncProcessorVO parseMessage(final byte[] decodeMessage) throws MessageConversionException {
		try {
			return this.objectMapper.readValue(decodeMessage, RequestAsyncProcessorVO.class);
		} catch (Exception e) {
			throw new MessageConversionException("The message received is not a type of RequestAsyncProcessorVO class.",
					e);
		}
	}

}
