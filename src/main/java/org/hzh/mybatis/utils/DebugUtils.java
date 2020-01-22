package org.hzh.mybatis.utils;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.hzh.mybatis.analyzer.ParseExecutor;
import org.hzh.mybatis.analyzer.ParseResult;
import org.hzh.mybatis.antlr.CaseChangingCharStream;
import org.hzh.mybatis.antlr.ErrorStateListener;
import org.hzh.mybatis.parser.MySqlLexer;

public class DebugUtils {

	public static void inspect(String sql) {
		ParseExecutor executor = new ParseExecutor();
		ParseResult parseResult= executor.parse(sql);

		Trees.inspect(parseResult.getTree(), parseResult.getParser());
	}

	public static void printToken(String sql){
		ErrorStateListener lexerListener = new ErrorStateListener();
		lexerListener.setPrefix("lexer");

		CharStream is = null;
		is = CharStreams.fromString(sql);

		CaseChangingCharStream caseChangingCharStream = new CaseChangingCharStream(is, true);
		MySqlLexer lexer = new MySqlLexer(caseChangingCharStream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);

		for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
			System.out.println(((CommonToken) token).toString(lexer));
//			System.out.println(token.toString());
		}

	}
	
	public static int countForChar(String s, char ch) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ch) {
				count++;
			}
		}
		return count;
	}

}
