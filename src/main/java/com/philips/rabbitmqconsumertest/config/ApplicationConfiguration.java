package com.philips.rabbitmqconsumertest.config;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.core.TenantConnectionFactoryLoader;
import com.philips.rabbitmqconsumertest.dtos.RequestAsyncProcessorVO;
import com.philips.rabbitmqconsumertest.services.MessageListenerProcessor;

@Configuration
public class ApplicationConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

	public static final String QUEUE_NAME_SYNC = "his-emr.sync";
	public static final String QUEUE_NAME_ASYNC = "his-emr.async";
	public static final String ORDER_EXCHANGE_NAME = "his-emr.direct";
	public static final String ROUTING_KEY_ASYNC = "ASYNC";
	public static final String ROUTING_KEY_SYNC = "ASYNC_WITH_LOADING";

	@Autowired
	private MessageListenerProcessor messageListenerProcessor;

	public Queue queueAsync() {
		return QueueBuilder.durable(QUEUE_NAME_ASYNC).quorum().build();
	}

	public Queue queueSync() {
		return QueueBuilder.durable(QUEUE_NAME_SYNC).quorum().build();
	}

	public Exchange exchange() {
		return ExchangeBuilder.directExchange(ORDER_EXCHANGE_NAME).build();
	}

	public Binding bindingAsync() {
		return BindingBuilder.bind(queueAsync()).to(exchange()).with(ROUTING_KEY_ASYNC).noargs();
	}

	public Binding bindingSync() {
		return BindingBuilder.bind(queueSync()).to(exchange()).with(ROUTING_KEY_SYNC).noargs();
	}

	@Bean
	public MessageConverter messageConverter() {
		return new ApplicationMessageConverter();
	}

	@Bean
	public ErrorHandler errorHandler() {
		return new ApplicationErrorHandler();
	}

	@Bean
	public Map<String, MessageListenerContainer> messageListenerContainers(	TenantConnectionFactoryLoader connectionFactory) {
		final Map<String, MessageListenerContainer> containeres = new TreeMap<>();
		connectionFactory.getTenants().forEach(tenant -> {
			final ConnectionFactory factory = connectionFactory.getTenantConnectionFactory(tenant.getId());
			containeres.put(tenant.getId(), createMessageListenerContainer(factory, QUEUE_NAME_ASYNC, tenant.getAsyncConsumers()));
			containeres.put(tenant.getId(), createMessageListenerContainer(factory, QUEUE_NAME_SYNC, tenant.getSyncConsumers()));
		});
		return containeres;
	}

	private MessageListenerContainer createMessageListenerContainer(final ConnectionFactory connectionFactory,
			final String queueName,final String consumers) {

		final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setAmqpAdmin(createAmqpAdmin(connectionFactory));
		container.setQueueNames(queueName);
		container.setErrorHandler(errorHandler());
		container.setConcurrency(consumers);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

		container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
			RequestAsyncProcessorVO requestMessage;
			try {
				requestMessage = (RequestAsyncProcessorVO) messageConverter().fromMessage(message);
				this.messageListenerProcessor.process(channel, message.getMessageProperties().getDeliveryTag(),
						requestMessage);
			} catch (MessageConversionException e) {
				LOGGER.error("An error occurred when trying to convert the message.", e);
			} catch (Exception e) {
				LOGGER.error("An error occurred when trying to process the message.", e);
			}
		});

		container.afterPropertiesSet();
		container.start();

		return container;

	}

	private AmqpAdmin createAmqpAdmin(final ConnectionFactory connectionFactory) {
		final AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
		amqpAdmin.declareQueue(queueAsync());
		amqpAdmin.declareQueue(queueSync());
		amqpAdmin.declareExchange(exchange());
		amqpAdmin.declareBinding(bindingAsync());
		amqpAdmin.declareBinding(bindingSync());
		return amqpAdmin;
	}

}
