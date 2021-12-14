package com.philips.rabbitmqconsumertest.core;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.philips.rabbitmqconsumertest.config.ApplicationProperties;
import com.philips.rabbitmqconsumertest.config.ApplicationProperties.Tenant;
import com.philips.rabbitmqconsumertest.config.ConnectionFactoryConfigurer;

@Configuration
public class TenantConnectionFactoryLoader implements InitializingBean {

	private ConcurrentMap<String, ConnectionFactory> factories;

	private final ConnectionFactoryConfigurer connectionFactoryConfigurer;
	private final ApplicationProperties properties;

	@Autowired
	public TenantConnectionFactoryLoader(final ConnectionFactoryConfigurer connectionFactoryConfigurer,
			final ApplicationProperties properties) {
		this.connectionFactoryConfigurer = connectionFactoryConfigurer;
		this.properties = properties;
	}

	public void afterPropertiesSet() throws Exception {
		this.factories = this.properties.getTenants().stream()
				.collect(Collectors.toConcurrentMap(Tenant::getId, this::createTenantConnectionFactory));
	}

	private ConnectionFactory createTenantConnectionFactory(final Tenant tenant) {
		return connectionFactoryConfigurer.configure(tenant.getId());
	}

	public ConnectionFactory getTenantConnectionFactory(final String tenantId) {
		if (!this.factories.containsKey(tenantId)) {
			throw new RuntimeException("There is no Tenant with ID [" + tenantId + "]");
		}
		return this.factories.get(tenantId);
	}

	public List<Tenant> getTenants() {
		return this.properties.getTenants();
	}

}
