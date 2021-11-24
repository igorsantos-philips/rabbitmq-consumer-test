package com.philips.rabbitmqconsumertest.dtos;

public class Order {
	private String orderId;
	private String description;
	private Double price;
	private Integer qtdItens;
	private String demandedRestaurantName;
	public Order() {
		super();
	}
	public Order(String orderId, String description, Double price, Integer qtdItens, String demandedRestaurantName) {
		super();
		this.orderId = orderId;
		this.description = description;
		this.price = price;
		this.qtdItens = qtdItens;
		this.demandedRestaurantName = demandedRestaurantName;
	}
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Integer getQtdItens() {
		return qtdItens;
	}
	public void setQtdItens(Integer qtdItens) {
		this.qtdItens = qtdItens;
	}
	public String getDemandedRestaurantName() {
		return demandedRestaurantName;
	}
	public void setDemandedRestaurantName(String demandedRestaurantName) {
		this.demandedRestaurantName = demandedRestaurantName;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (demandedRestaurantName == null) {
			if (other.demandedRestaurantName != null)
				return false;
		} else if (!demandedRestaurantName.equals(other.demandedRestaurantName))
			return false;
		return true;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((demandedRestaurantName == null) ? 0 : demandedRestaurantName.hashCode());
		return result;
	}
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", description=" + description + ", price=" + price + ", qtdItens="
				+ qtdItens + ", demandedRestaurantName=" + demandedRestaurantName + "]";
	}


}
