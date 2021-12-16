package com.philips.rabbitmqconsumertest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ErrorHandler;

import com.philips.rabbitmqconsumertest.config.ApplicationProperties.Tenant;
import com.philips.rabbitmqconsumertest.core.TenantConnectionFactoryLoader;
import com.philips.rabbitmqconsumertest.dtos.RequestAsyncProcessorVO;
import com.philips.rabbitmqconsumertest.services.MessageListenerProcessor;
import com.rabbitmq.client.Channel;

@ExtendWith(SpringExtension.class)
class ApplicationConfigurationTest {
	@InjectMocks
	private ApplicationConfiguration configuration;
	
	@Mock
	private TenantConnectionFactoryLoader tenantConnectionFactoryLoader;
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ConnectionFactory connectionFactory ;
	
	@Mock
	private MessageListenerProcessor messageListenerProcessor;
	
	@Spy
	private AmqpAdmin amqpAdmin ;
	
	private  Queue asyncQueue;
	private  Queue syncQueue;
	private  Exchange exchange;
	private  Binding asyncBiding;
	private  Binding syncBiding;
	private  MessageConverter messageConverter;
	private  ErrorHandler errorHandler;
	private  Map<String, MessageListenerContainer> mapMesseageListeners;
	private Tenant t1 ;
	private Tenant t2;
	public List<Tenant> listTenant;
	
	
	public  ApplicationConfigurationTest () throws Exception {
		asyncQueue =  QueueBuilder.durable(ApplicationConfiguration.QUEUE_NAME_ASYNC).quorum().build();
		syncQueue =  QueueBuilder.durable(ApplicationConfiguration.QUEUE_NAME_SYNC).quorum().build();
		exchange = ExchangeBuilder.directExchange(ApplicationConfiguration.ORDER_EXCHANGE_NAME).build();
		asyncBiding =BindingBuilder.bind(asyncQueue).to(exchange).with(ApplicationConfiguration.ROUTING_KEY_ASYNC).noargs();
		syncBiding =BindingBuilder.bind(syncQueue).to(exchange).with(ApplicationConfiguration.ROUTING_KEY_SYNC).noargs();
		listTenant = generateListTenant();
		messageConverter = new ApplicationMessageConverter(); 
		errorHandler = new ApplicationErrorHandler();

		
		
	}

	private List<Tenant> generateListTenant() {
		t1 = new Tenant("Tenant1","1-3","3-5");
		t2 = new Tenant("Tenant2","1-3","3-5");
		
		List<Tenant> listTemp = new ArrayList<>();
		listTemp.add(t1);
		listTemp.add(t2);
		
		return listTemp;
	}

	private  Map<String, MessageListenerContainer> generateMapMessageListeners() throws Exception {
		Map<String, MessageListenerContainer> mapTemp = new TreeMap<>();
		for(Tenant tenant : listTenant) {
			
				mapTemp.put(tenant.getId(), generateMessageLister( ApplicationConfiguration.QUEUE_NAME_ASYNC,tenant.getAsyncConsumers()));
				mapTemp.put(tenant.getId(), generateMessageLister( ApplicationConfiguration.QUEUE_NAME_SYNC, tenant.getSyncConsumers()));
			
		}
		
		return mapTemp;
	}
	private  MessageListenerContainer generateMessageLister(String queueName, String consumers) throws Exception {
		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueueNames(queueName);
		container.setErrorHandler(errorHandler);
		container.setConcurrency(consumers);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

		container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
//			try {
//				this.messageListenerProcessor.process(channel, message.getMessageProperties().getDeliveryTag(),any(RequestAsyncProcessorVO.class));
//			} catch (MessageConversionException e) {
//				LOGGER.error("An error occurred when trying to convert the message.", e);
//			} catch (Exception e) {
//				LOGGER.error("An error occurred when trying to process the message.", e);
//			}
		});
		return container;
	}
	@Test
	void testQueueAsync() {
		Queue temp = configuration.queueAsync();
		assertEquals(asyncQueue.getActualName(), temp.getActualName());
		assertEquals(asyncQueue.getArguments(), temp.getArguments());
		assertEquals(asyncQueue.getName(), temp.getName());
		
	}

	@Test
	void testQueueSync() {
		Queue temp = configuration.queueSync();
		assertEquals(syncQueue.getActualName(), temp.getActualName());
		assertEquals(syncQueue.getArguments(), temp.getArguments());
		assertEquals(syncQueue.getName(), temp.getName());
	}

	@Test
	void testExchange() {
		Exchange temp = configuration.exchange();
		assertEquals(exchange.getName(), temp.getName());
		assertEquals(exchange.getArguments(), temp.getArguments());
		assertEquals(exchange.getType(), temp.getType());
	}

	@Test
	void testBindingAsync() {
		Binding temp = configuration.bindingAsync();
		assertEquals(asyncBiding.getArguments(), temp.getArguments());
		assertEquals(asyncBiding.getDestination(), temp.getDestination());
		assertEquals(asyncBiding.getDestinationType(), temp.getDestinationType());
		assertEquals(asyncBiding.getExchange(), temp.getExchange());
		assertEquals(asyncBiding.getRoutingKey(), temp.getRoutingKey());
	}

	@Test
	void testBindingSync() {
		Binding temp = configuration.bindingSync();
		assertEquals(syncBiding.getArguments(), temp.getArguments());
		assertEquals(syncBiding.getDestination(), temp.getDestination());
		assertEquals(syncBiding.getDestinationType(), temp.getDestinationType());
		assertEquals(syncBiding.getExchange(), temp.getExchange());
		assertEquals(syncBiding.getRoutingKey(), temp.getRoutingKey());
	}

	@Test
	void testMessageConverter() {
		MessageConverter temp = configuration.messageConverter();
		assertEquals(messageConverter.getClass().getSimpleName(), temp.getClass().getSimpleName());
	}

	@Test
	void testErrorHandler() {
		ErrorHandler temp = configuration.errorHandler();
		assertEquals(errorHandler.getClass().getSimpleName(), temp.getClass().getSimpleName());
	}

	@Test
	void testMessageListenerContainers() throws Exception {
		doNothing().when(messageListenerProcessor).process(any(Channel.class),anyLong(), any(RequestAsyncProcessorVO.class));
		doNothing().when(connectionFactory).clearConnectionListeners();
		when(tenantConnectionFactoryLoader.getTenants()).thenReturn(listTenant);
		when(tenantConnectionFactoryLoader.getTenantConnectionFactory(t1.getId())).thenReturn(connectionFactory);
		when(tenantConnectionFactoryLoader.getTenantConnectionFactory(t2.getId())).thenReturn(connectionFactory);
		when(amqpAdmin.declareQueue(any(Queue.class))).thenReturn(new String());
		doNothing().when(amqpAdmin).declareExchange(any(Exchange.class));
		doNothing().when(amqpAdmin).declareBinding(any(Binding.class));
		mapMesseageListeners = generateMapMessageListeners();
		amqpAdmin = new RabbitAdmin(connectionFactory);
		
		Map<String, MessageListenerContainer> temp = configuration.messageListenerContainers(tenantConnectionFactoryLoader);
		assertEquals(mapMesseageListeners.size(), temp.size());
		assertTrue(temp.containsKey(t1.getId()));
		assertTrue(temp.containsKey(t2.getId()));
	}

}

