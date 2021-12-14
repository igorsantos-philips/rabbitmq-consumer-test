package com.philips.rabbitmqconsumertest.config.impl;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import com.philips.rabbitmqconsumertest.config.ConnectionFactoryConfigurer;
import com.philips.rabbitmqconsumertest.config.ApplicationProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConnectionFactoryConfigurator;

@Configuration
@ConditionalOnProperty(value = ConnectionFactoryConfigurer.NATIVE_CONFIGURATION, havingValue = "true", matchIfMissing = false)
public class NativeConnectionFactoryConfigurerImpl implements ConnectionFactoryConfigurer {

	private static final String ADDRESSES = "addresses";

	private final ApplicationProperties properties;

	@Autowired
	public NativeConnectionFactoryConfigurerImpl(ApplicationProperties tenantConfiguration) {
		this.properties = tenantConfiguration;
	}

	@Override
	public CachingConnectionFactory configure(final String tenantId) {
		final ConnectionFactory connectionFactory = new ConnectionFactory();
		ConnectionFactoryConfigurator.load(connectionFactory, this.properties.getRabbitmq(), "");

		final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
		cachingConnectionFactory.setAddresses(this.properties.getRabbitmq().get(ADDRESSES));
		cachingConnectionFactory.setVirtualHost(tenantId);

		return cachingConnectionFactory;
	}

}
