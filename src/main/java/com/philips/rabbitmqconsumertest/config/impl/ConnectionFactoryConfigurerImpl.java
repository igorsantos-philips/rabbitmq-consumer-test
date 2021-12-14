package com.philips.rabbitmqconsumertest.config.impl;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.CachingConnectionFactoryConfigurer;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryCustomizer;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import com.philips.rabbitmqconsumertest.config.ConnectionFactoryConfigurer;

@Configuration
@ConditionalOnProperty(value = ConnectionFactoryConfigurer.NATIVE_CONFIGURATION, havingValue = "false", matchIfMissing = true)
public class ConnectionFactoryConfigurerImpl implements ConnectionFactoryConfigurer {

	/*
	 * private final BeanFactory beanFactory;
	 * 
	 * @Autowired public ConnectionFactoryConfigurerImpl(BeanFactory beanFactory) {
	 * this.beanFactory = beanFactory; }
	 */

	// private final BeanFactory beanFactory;
	private final RabbitConnectionFactoryBeanConfigurer rabbitConnectionFactoryBeanConfigurer;
	private final CachingConnectionFactoryConfigurer rabbitCachingConnectionFactoryConfigurer;
	private final ObjectProvider<ConnectionFactoryCustomizer> connectionFactoryCustomizers;

	@Autowired
	public ConnectionFactoryConfigurerImpl(RabbitConnectionFactoryBeanConfigurer rabbitConnectionFactoryBeanConfigurer,
			CachingConnectionFactoryConfigurer rabbitCachingConnectionFactoryConfigurer,
			ObjectProvider<ConnectionFactoryCustomizer> connectionFactoryCustomizers, BeanFactory beanFactory) {
		this.rabbitConnectionFactoryBeanConfigurer = rabbitConnectionFactoryBeanConfigurer;
		this.rabbitCachingConnectionFactoryConfigurer = rabbitCachingConnectionFactoryConfigurer;
		this.connectionFactoryCustomizers = connectionFactoryCustomizers;
		// this.beanFactory = beanFactory;
	}

	@Override
	public CachingConnectionFactory configure(final String tenantId) {
		/*
		 * final CachingConnectionFactory cachingConnectionFactory =
		 * beanFactory.getBean(CachingConnectionFactory.class);
		 * cachingConnectionFactory.setVirtualHost(tenantId);
		 * System.out.println(cachingConnectionFactory);
		 */

		final RabbitConnectionFactoryBean connectionFactoryBean = new RabbitConnectionFactoryBean();
		rabbitConnectionFactoryBeanConfigurer.configure(connectionFactoryBean);
		connectionFactoryBean.afterPropertiesSet();
		final com.rabbitmq.client.ConnectionFactory connectionFactory = connectionFactoryBean
				.getRabbitConnectionFactory();
		connectionFactoryCustomizers.orderedStream().forEach((customizer) -> customizer.customize(connectionFactory));

		final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
		rabbitCachingConnectionFactoryConfigurer.configure(cachingConnectionFactory);
		cachingConnectionFactory.setVirtualHost(tenantId);

		return cachingConnectionFactory;
	}

}
