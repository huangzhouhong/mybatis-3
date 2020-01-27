package org.hzh.mybatis.analyzer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.hzh.mybatis.antlr.CaseChangingCharStream;
import org.hzh.mybatis.antlr.ErrorListener;
import org.hzh.mybatis.antlr.SafeTokenStream;
import org.hzh.mybatis.parser.MySqlLexer;
import org.hzh.mybatis.parser.MySqlParser;

public class ParseExecutor {
	public ParseResult parse(String originalSql) {
		ErrorListener lexerListener = new ErrorListener("lexer");
		ErrorListener parserListener = new ErrorListener("parser");

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

		ParseResult parseResult = new ParseResult();
		parseResult.tokens = SafeTokenStream.CopyFrom(tokens);
		parseResult.tree = tree;
		parseResult.originalSql = originalSql;
		parseResult.parser = parser;

		return parseResult;
	}
}
