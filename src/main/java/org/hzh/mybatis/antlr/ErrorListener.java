package org.hzh.mybatis.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ErrorListener extends BaseErrorListener {
	private String prefix;

	public ErrorListener(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		String errorMsg = "input: line " + line + ":" + charPositionInLine + " " + msg;
		if (prefix != null) {
			errorMsg = prefix + " error. " + errorMsg;
		}
		throw new SqlParseException(errorMsg, e);
	}
}
