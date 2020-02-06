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
