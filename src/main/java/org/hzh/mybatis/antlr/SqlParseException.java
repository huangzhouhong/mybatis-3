package org.hzh.mybatis.antlr;

public class SqlParseException extends RuntimeException {
	private static final long serialVersionUID = 2361646890802391426L;

	public SqlParseException() {
		super();
	}

	public SqlParseException(String message) {
		super(message);
	}

	public SqlParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlParseException(Throwable cause) {
		super(cause);
	}
}
