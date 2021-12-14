package com.philips.rabbitmqconsumertest.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

@FunctionalInterface
public interface ConnectionFactoryConfigurer {
	
	String NATIVE_CONFIGURATION = "philips.native-configuration";

	CachingConnectionFactory configure(final String tenantId);

}
