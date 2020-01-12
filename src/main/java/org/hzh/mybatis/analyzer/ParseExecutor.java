package org.hzh.mybatis.analyzer;

import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hzh.mybatis.parser.MySqlLexer;
import org.hzh.mybatis.parser.MySqlParser;

public class ParseExecutor {
	public ParseResult parse(String originalSql) {
		ErrorStateListener lexerListener = new ErrorStateListener();
		lexerListener.setPrefix("lexer");
		ErrorStateListener parserListener = new ErrorStateListener();
		parserListener.setPrefix("parser");

		CharStream is = CharStreams.fromString(originalSql);
		CaseChangingCharStream caseChangingCharStream = new CaseChangingCharStream(is, true);
		MySqlLexer lexer = new MySqlLexer(caseChangingCharStream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MySqlParser parser = new MySqlParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(parserListener);

		ParseTree tree = parser.dmlStatement();

		if (lexerListener.getHasError() || parserListener.getHasError()) {
			throw new RuntimeException();
		}

		ParseResult parseResult = new ParseResult();
		parseResult.tokens = tokens;
		parseResult.tree = tree;
		parseResult.originalSql = originalSql;
		parseResult.parser = parser;

		return parseResult;
	}
}
