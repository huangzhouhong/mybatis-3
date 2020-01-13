package org.hzh.mybatis.utils;

import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hzh.mybatis.analyzer.CaseChangingCharStream;
import org.hzh.mybatis.analyzer.ErrorStateListener;
import org.hzh.mybatis.analyzer.ParseExecutor;
import org.hzh.mybatis.analyzer.ParseResult;
import org.hzh.mybatis.parser.MySqlLexer;
import org.hzh.mybatis.parser.MySqlParser;

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

}
