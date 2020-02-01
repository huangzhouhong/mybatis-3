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
