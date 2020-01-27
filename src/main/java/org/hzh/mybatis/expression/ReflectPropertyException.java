package org.hzh.mybatis.expression;

public class ReflectPropertyException extends RuntimeException {
	private static final long serialVersionUID = 2361646890802391426L;

	public ReflectPropertyException() {
		super();
	}

	public ReflectPropertyException(String message) {
		super(message);
	}

	public ReflectPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectPropertyException(Throwable cause) {
		super(cause);
	}
}
