package com.philips.rabbitmqconsumertest.core;

public class Tenant {
	
	private String tenantId;
	private Integer asyncConsumers;
	private Integer syncConsumers;

	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public Integer getAsyncConsumers() {
		return asyncConsumers;
	}
	public void setAsyncConsumers(Integer asyncConsumers) {
		this.asyncConsumers = asyncConsumers;
	}
	public Integer getSyncConsumers() {
		return syncConsumers;
	}
	public void setSyncConsumers(Integer syncConsumers) {
		this.syncConsumers = syncConsumers;
	}
	

}
