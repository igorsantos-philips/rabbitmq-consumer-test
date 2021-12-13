package com.philips.rabbitmqconsumertest.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.BatchMessagingMessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.core.ConnectionsFactoriesTenantLoader;
import com.philips.rabbitmqconsumertest.core.Tenant;
import com.philips.rabbitmqconsumertest.services.OrderConsumerService;
import com.philips.rabbitmqconsumertest.services.OrderErrorHandler;
import com.rabbitmq.client.Channel;

@Configuration
public class MessagingConfig {

	public static final String QUEUE_NAME_SYNC = "his-emr.sync";
	public static final String QUEUE_NAME_ASYNC = "his-emr.async";
	public static final String ORDER_EXCHANGE_NAME = "his-emr.direct";

	// @Bean
	public Queue queueAsync() {
		return QueueBuilder.durable(QUEUE_NAME_ASYNC).quorum().build();
	}

	// @Bean
	public Queue queueSync() {
		return QueueBuilder.durable(QUEUE_NAME_SYNC).quorum().build();
	}

	// @Bean
	public Exchange exchange() {
		return ExchangeBuilder.directExchange(ORDER_EXCHANGE_NAME).build();
	}

	// @Bean
	public Binding bindingAsync() {
		return BindingBuilder.bind(queueAsync()).to(exchange()).with("ASYNC").noargs();
	}

	// @Bean
	public Binding bindingSync() {
		return BindingBuilder.bind(queueSync()).to(exchange()).with("ASYNC_WITH_LOADING").noargs();
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	
	@Bean
	public ErrorHandler errorHandler() {
		return new OrderErrorHandler();
	}	

	/*
	 * @Bean
	 * 
	 * @Qualifier("OrderConsumers") public Map<String, MessageListener>
	 * listOrderConsumerService(ConnectionsFactoriesTenantLoader connectionFactory)
	 * { Map<String, MessageListener> list = new TreeMap<>(); for(Tenant tenant :
	 * connectionFactory.getTenants()) { list.put(tenant.getTenantId(), new
	 * OrderConsumerService(tenant.getTenantId())); } return list; }
	 * 
	 * @Bean
	 * 
	 * @Qualifier("messageTemplateImp") public AmqpTemplate
	 * messageTemplate(ConnectionFactory connectionFactory, MessageConverter
	 * messageConverter) { RabbitTemplate rabbitTemplate = new
	 * RabbitTemplate(connectionFactory);
	 * rabbitTemplate.setMessageConverter(messageConverter); return rabbitTemplate;
	 * }
	 */

	/*
	 * @Bean
	 * 
	 * @Qualifier("asyncConsumers") public List<SimpleMessageListenerContainer>
	 * containersAsyncConsumers(ConnectionsFactoriesTenantLoader
	 * connectionFactory, @Qualifier("OrderConsumers") Map<String,MessageListener>
	 * messageListenerAdapter) { List<Tenant> listTenant =
	 * connectionFactory.getTenants(); List<SimpleMessageListenerContainer>
	 * listListenerContainer = new ArrayList<>(); for(Tenant tenant : listTenant) {
	 * generateSimpleMessageListenerContainer(connectionFactory.
	 * getTenantConnectionFactory(tenant.getTenantId()),
	 * messageListenerAdapter.get(tenant.getTenantId()), listListenerContainer,
	 * tenant.getTenantId(),tenant.getAsyncConsumers(),QUEUE_NAME_ASYNC); } return
	 * listListenerContainer; }
	 */
	@Bean
	// @Qualifier("syncConsumers")
	public SimpleMessageListenerContainer containersSyncConsumers(
			ConnectionsFactoriesTenantLoader connectionFactory) {
		List<Tenant> listTenant = connectionFactory.getTenants();
		List<SimpleMessageListenerContainer> listListenerContainer = new ArrayList<>();
		listTenant.forEach(tenant -> {
			final ConnectionFactory factory = connectionFactory.getTenantConnectionFactory(tenant.getTenantId());
			generateSimpleMessageListenerContainer(factory, listListenerContainer, tenant.getTenantId(),
					tenant.getAsyncConsumers(), QUEUE_NAME_ASYNC);
			/*generateSimpleMessageListenerContainer(factory, listListenerContainer, tenant.getTenantId(),
					tenant.getSyncConsumers(), QUEUE_NAME_SYNC);*/
		});
		return listListenerContainer.get(0);
	}

	private void generateSimpleMessageListenerContainer(ConnectionFactory connectionFactory,
			List<SimpleMessageListenerContainer> listListenerContainer, String tenantId, Integer totalConsumers,
			String queueName) {
		final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueueNames(queueName);
		//container.setExposeListenerChannel(true);
		container.setErrorHandler(errorHandler());
		container.setConcurrentConsumers(3);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		
		// 1
		container.setMessageListener(new MessageListenerAdapter(new OrderConsumerService(tenantId), messageConverter()));
		
		// 2
		//container.setMessageListener(new OrderConsumerService(tenantId));		
		
		// 3
		/*container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
			byte[] messageBody = message.getBody();
			System.out.println("MESSAGE >>> " + messageBody);
			final Boolean success = true;// mythMqReceiveService.processMessage(messageBody);
			if (success) {
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			}
		});*/

		// 4
		/*container.setMessageListener(new ChannelAwareMessageListener() {
			@Override
			public void onMessage(Message message, Channel channel) throws Exception {
				byte[] body = message.getBody();
				System.out.println("MESSAGE >>> " + body);
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			}
		});*/

		final AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
		container.setAmqpAdmin(amqpAdmin);
		amqpAdmin.declareQueue(queueAsync());
		amqpAdmin.declareQueue(queueSync());
		amqpAdmin.declareExchange(exchange());
		amqpAdmin.declareBinding(bindingAsync());
		amqpAdmin.declareBinding(bindingSync());

		listListenerContainer.add(container);
	}

	/*
	 * @Bean public MessageListenerAdapter listenerAdapter(OrderConsumerService
	 * receiver) {
	 * 
	 * return new MessageListenerAdapter(receiver, "receiveMessage"); }
	 */

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
