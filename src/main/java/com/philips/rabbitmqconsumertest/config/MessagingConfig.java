package com.philips.rabbitmqconsumertest.config;

import java.util.ArrayList;
import java.util.List;

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

import com.philips.rabbitmqconsumertest.core.ConnectionsFactoriesTenantLoader;
import com.philips.rabbitmqconsumertest.core.Tenant;
import com.philips.rabbitmqconsumertest.services.OrderConsumerService;
import com.philips.rabbitmqconsumertest.services.OrderErrorHandler;


@Configuration
public class MessagingConfig {
	public static final String QUEUE_NAME_SYNC = "order.received";
	public static final String QUEUE_NAME_ASYNC = "order.received";
	public static final String ORDER_EXCHANGE_NAME = "order.direct";
	public static final String ORDER_ROUTING_KEY = "order.direct";
	

	@Bean
	public Queue queueSync() {
		Queue q =  new Queue(QUEUE_NAME_SYNC	);
		return q;
	}
	@Bean
	public Queue queueAsync() {
		Queue q =  new Queue(QUEUE_NAME_ASYNC	);
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
	@Qualifier("asyncConsumers")
	public List<SimpleMessageListenerContainer> containersAsyncConsumers(ConnectionsFactoriesTenantLoader connectionFactory, MessageListenerAdapter messageListenerAdapter) {
		List<Tenant> listTenant = connectionFactory.getListTenants();
		List<SimpleMessageListenerContainer> listListenerContainer = new ArrayList<>();
		for(Tenant tenant : listTenant) {
			generateSimpleMessageListenerContainer(connectionFactory.getTenantConnectionFactory(tenant.getTenantId()), messageListenerAdapter, listListenerContainer, tenant.getTenantId(),tenant.getAsyncConsumers(),QUEUE_NAME_ASYNC);
		}
		return listListenerContainer;
	}

	@Bean
	@Qualifier("syncConsumers")
	public List<SimpleMessageListenerContainer> containersSyncConsumers(ConnectionsFactoriesTenantLoader connectionFactory, MessageListenerAdapter messageListenerAdapter) {
		List<Tenant> listTenant = connectionFactory.getListTenants();
		List<SimpleMessageListenerContainer> listListenerContainer = new ArrayList<>();
		for(Tenant tenant : listTenant) {
			generateSimpleMessageListenerContainer(connectionFactory.getTenantConnectionFactory(tenant.getTenantId()), messageListenerAdapter, listListenerContainer, tenant.getTenantId(),tenant.getSyncConsumers(),QUEUE_NAME_SYNC);
		}
		return listListenerContainer;
	}
	private void generateSimpleMessageListenerContainer(ConnectionFactory connectionFactory,MessageListenerAdapter messageListenerAdapter, List<SimpleMessageListenerContainer> listListenerContainer,	String tenantId,Integer totalConsumers,String queueName) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setErrorHandler(errorHandler());
		container.setConcurrentConsumers(totalConsumers);
		container.setMessageListener(messageListenerAdapter);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		listListenerContainer.add(container);
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
