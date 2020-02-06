/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
