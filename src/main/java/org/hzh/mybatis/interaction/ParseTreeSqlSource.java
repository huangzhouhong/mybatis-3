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
package org.hzh.mybatis.interaction;

import java.util.Locale;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.hzh.mybatis.analyzer.ApplyParamResult;
import org.hzh.mybatis.analyzer.ParseExecutor;
import org.hzh.mybatis.analyzer.ParseResult;

public class ParseTreeSqlSource implements SqlSource {
	private final ParseResult parseResult;
	private final SqlCommandType sqlCommandType;
	private final Configuration configuration;

	public ParseTreeSqlSource(Configuration configuration, String originalSql) {
		ParseExecutor executor = new ParseExecutor();
		parseResult = executor.parse(originalSql);
		String firstTextInSql = parseResult.getFirstText();
		sqlCommandType = SqlCommandType.valueOf(firstTextInSql.toUpperCase(Locale.ENGLISH));
		this.configuration = configuration;
	}

	public SqlCommandType getSqlCommandType() {
		return sqlCommandType;
	}

	@Override
	public BoundSql getBoundSql(Object parameterObject) {
		ApplyParamResult applyParamResult = parseResult.apply(parameterObject);
		ParseTreeBoundSql boundSql = new ParseTreeBoundSql(configuration, applyParamResult, parameterObject);
		return boundSql;
	}

}
