package org.hzh.mybatis.antlr;

import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

public class SafeTokenStream implements TokenStream {

	private Token[] tokens;

	public SafeTokenStream(Token[] tokens) {
		this.tokens = tokens;
	}

	public static SafeTokenStream CopyFrom(CommonTokenStream otherStream) {
		otherStream.fill();
		List<Token> tokenList = otherStream.getTokens();
		Token[] tokens = new Token[tokenList.size()];
		tokenList.toArray(tokens);
		return new SafeTokenStream(tokens);
	}

	@Override
	public int size() {
		return tokens.length;
	}

	@Override
	public Token get(int index) {
		return tokens[index];
	}

	@Override
	public String getText() {
		return getText(Interval.of(0, size() - 1));
	}

	@Override
	public String getText(Interval interval) {
		int start = interval.a;
		int stop = interval.b;
		if (start < 0 || stop < 0)
			return "";
		if (stop >= size())
			stop = size() - 1;

		StringBuilder buf = new StringBuilder();
		for (int i = start; i <= stop; i++) {
			Token t = tokens[i];
			if (t.getType() == Token.EOF)
				break;
			buf.append(t.getText());
		}
		return buf.toString();
	}

	@Override
	public void consume() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int LA(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int mark() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void release(int marker) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int index() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void seek(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSourceName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Token LT(int k) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TokenSource getTokenSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getText(RuleContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getText(Token start, Token stop) {
		throw new UnsupportedOperationException();
	}

}
