package com.philips.rabbitmqconsumertest.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.rabbitmqconsumertest.dtos.Order;
import com.rabbitmq.client.Channel;

public class OrderConsumerService implements ChannelAwareMessageListener {
	
	private final String tenantId;
	public OrderConsumerService(String tenantId) {
		this.tenantId = tenantId;
	}
	
	@Override
	public void onMessage(Message message,Channel channel) throws IOException, InterruptedException, TimeoutException  {
		System.out.println("MESSAGE RECEIVED >>> " + new String(message.getBody(), StandardCharsets.UTF_8));
		ObjectMapper om = new ObjectMapper();
		Order status = om.readValue(message.getBody(), Order.class);
		Thread.sleep(1000);
		int messageAction = 1+(int) (Math.random()*5);
		switch(messageAction) {
		case 1:
			System.out.println("rejected to dlx: "+status);
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
			break;
		case 2:
			System.out.println("no ack with requeu"+status);
			break;
		case 3:
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			System.out.println("ACK OK "+status);				
			break;
		case 4:
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
			System.out.println("NO ACK without requeue "+status);				
			break;				
		default:
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);	
		}
		channel.close();
	}
}
