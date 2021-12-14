package com.philips.rabbitmqconsumertest.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "philips")
public class ApplicationProperties {

	private boolean nativeConfiguration;
	private List<Tenant> tenants;
	private Map<String, String> rabbitmq;

	public boolean isNativeConfiguration() {
		return nativeConfiguration;
	}

	public void setNativeConfiguration(boolean nativeConfiguration) {
		this.nativeConfiguration = nativeConfiguration;
	}

	public List<Tenant> getTenants() {
		return tenants;
	}

	public void setTenants(List<Tenant> tenants) {
		this.tenants = tenants;
	}

	public Map<String, String> getRabbitmq() {
		return rabbitmq;
	}

	public void setRabbitmq(Map<String, String> rabbitmq) {
		this.rabbitmq = rabbitmq;
	}

	public static class Tenant {
		private String id;
		private String asyncConsumers;
		private String syncConsumers;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getAsyncConsumers() {
			return asyncConsumers;
		}

		public void setAsyncConsumers(String asyncConsumers) {
			this.asyncConsumers = asyncConsumers;
		}

		public String getSyncConsumers() {
			return syncConsumers;
		}

		public void setSyncConsumers(String syncConsumers) {
			this.syncConsumers = syncConsumers;
		}

		@Override
		public String toString() {
			return "Tenant [id=" + id + "]";
		}

	}

}
