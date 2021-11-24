package com.philips.rabbitmqconsumertest.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.services.OrderConsumerService;
import com.philips.rabbitmqconsumertest.services.OrderErrorHandler;


@Configuration
public class MessagingConfig {
	public static final String QUEUE_NAME = "order.received";
	public static final String ORDER_EXCHANGE_NAME = "order.direct";
	public static final String ORDER_ROUTING_KEY = "order.direct";
	

	@Bean
	public Queue queue() {
		Queue q =  new Queue(QUEUE_NAME	);
		return q;
	}
	
	
	@Bean
	public Exchange exchange() {
		return new TopicExchange(ORDER_EXCHANGE_NAME);
	}

	@Bean
	public Binding binding(@Autowired Queue queue, @Autowired Exchange exchange ) {
		return BindingBuilder.bind(queue).to(exchange).with(ORDER_ROUTING_KEY).noargs();
	}
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	@Qualifier("messageTemplateImp")
	public AmqpTemplate messageTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}

	@Bean
	public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter messageListenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(QUEUE_NAME);
		container.setErrorHandler(errorHandler());
		container.setConcurrentConsumers(10);
		container.setMessageListener(messageListenerAdapter);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return container;
	}
	@Bean
	public ErrorHandler errorHandler() {
	    return new OrderErrorHandler();
	}	
	@Bean
	public MessageListenerAdapter listenerAdapter(OrderConsumerService receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	// create MessageListenerContainer using default connection factory
//	@Bean
//	public MessageListenerContainer messageListenerContainer(Queue queue,ConnectionFactory connectionFactory,OrderConsumerService orderConsumerService) {
//		DirectMessageListenerContainer simpleMessageListenerContainer = new DirectMessageListenerContainer();
//		simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
//		simpleMessageListenerContainer.setQueues(queue);
//		simpleMessageListenerContainer.setMessageListener(orderConsumerService);
//		simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
//		return simpleMessageListenerContainer;
//
//	}

	// create custom connection factory
	/*
	 * @Bean ConnectionFactory connectionFactory() { CachingConnectionFactory
	 * cachingConnectionFactory = new CachingConnectionFactory("localhost");
	 * cachingConnectionFactory.setUsername(username);
	 * cachingConnectionFactory.setUsername(password); return
	 * cachingConnectionFactory; }
	 */

	// create MessageListenerContainer using custom connection factory
	/*
	 * @Bean MessageListenerContainer messageListenerContainer() {
	 * SimpleMessageListenerContainer simpleMessageListenerContainer = new
	 * SimpleMessageListenerContainer();
	 * simpleMessageListenerContainer.setConnectionFactory(connectionFactory());
	 * simpleMessageListenerContainer.setQueues(queue());
	 * simpleMessageListenerContainer.setMessageListener(new RabbitMQListner());
	 * return simpleMessageListenerContainer;
	 * 
	 * }
	 */
}
