package com.philips.rabbitmqconsumertest.services.exceptions;

public class ApplicationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ApplicationException(String string) {
		super(string);
	}

}
