package com.philips.rabbitmqconsumertest.services;

import org.springframework.stereotype.Service;

import com.philips.rabbitmqconsumertest.dtos.RequestAsyncProcessorVO;
import com.rabbitmq.client.Channel;

@Service
public class MessageListenerProcessor {

	public void process(final Channel channel, final long deliveryTag, final RequestAsyncProcessorVO message)
			throws Exception {
		Thread.sleep(1000);
		int messageAction = 1 + (int) (Math.random() * 5);
		switch (messageAction) {
		case 1:
			System.out.println("rejected to dlx: ");
			channel.basicReject(deliveryTag, true);
			break;
		case 2:
			System.out.println("no ack with requeu");
			break;
		case 3:
			channel.basicAck(deliveryTag, false);
			System.out.println("ACK OK ");
			break;
		case 4:
			channel.basicNack(deliveryTag, false, false);
			System.out.println("NO ACK without requeue ");
			break;
		default:
			channel.basicAck(deliveryTag, false);
		}
	}

}
