package com.philips.rabbitmqconsumertest.dtos;

import java.io.Serializable;

public class RequestAsyncProcessorVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String url;
	private String body;
	private String oneTimeToken;
	private String messageID;
	private EnumProcessMode processMode;

	public RequestAsyncProcessorVO() {
	}

	/**
	 * Constructor with params.
	 * 
	 * @param url          The request URL.
	 * @param body         The message body.
	 * @param oneTimeToken The OTT token to generate the access token.
	 * @param messageID    The message identification to traceability.
	 * @param processMode  The process mode of the message.
	 */
	public RequestAsyncProcessorVO(String url, String body, String oneTimeToken, String messageID,
			EnumProcessMode processMode) {
		this.url = url;
		this.body = body;
		this.oneTimeToken = oneTimeToken;
		this.messageID = messageID;
		this.processMode = processMode;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getOneTimeToken() {
		return oneTimeToken;
	}

	public void setOneTimeToken(String oneTimeToken) {
		this.oneTimeToken = oneTimeToken;
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public EnumProcessMode getProcessMode() {
		return processMode;
	}

	public void setProcessMode(EnumProcessMode processMode) {
		this.processMode = processMode;
	}

	@Override
	public String toString() {
		return "RequestAsyncProcessorVO{" + "url='" + url + '\'' + ", body='" + body + '\'' + ", oneTimeToken='"
				+ oneTimeToken + '\'' + ", messageID='" + messageID + '\'' + ", processMode=" + processMode + '}';
	}

}
