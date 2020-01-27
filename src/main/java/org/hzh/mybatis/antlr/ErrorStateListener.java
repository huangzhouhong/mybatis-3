package org.hzh.mybatis.antlr;

import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ErrorStateListener extends ConsoleErrorListener {
	private String prefix;
	private boolean hasError;
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public boolean getHasError() {
		return hasError;
	}
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		hasError=true;
		if (prefix!=null) {
			System.err.print(prefix+":");
		}
		super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
	}
}
