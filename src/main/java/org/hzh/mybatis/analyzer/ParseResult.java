package org.hzh.mybatis.analyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hzh.mybatis.listener.ParamListener;
import org.hzh.mybatis.listener.ParamListener.ParamInfo;
import org.hzh.mybatis.parser.MySqlParser;

public class ParseResult {
	String originalSql;
	ParseTree tree;
	CommonTokenStream tokens;
	MySqlParser parser;
	// to do: add dynamic property for performance

	// parameter appear in sql
	private Set<ParamInfo> sqlParamInfos;

	public MySqlParser getParser() {
		return parser;
	}

	public ParseTree getTree() {
		return tree;
	}

	public Set<ParamInfo> getSqlParamInfos() {
		if (sqlParamInfos == null) {
			ParamListener listener = new ParamListener();
			ParseTreeWalker walker = new ParseTreeWalker();
			walker.walk(listener, tree);

			sqlParamInfos=listener.getParamInfos();
		}

		return sqlParamInfos;
	}

//	public ApplyParamResult apply(Map<String, Object> params) {
//		CollectionUtils.removeMapNulls(params);
//		FieldNames fieldNames = new FieldNames(params.keySet());
//		TokenStreamRewriter rewriter = new TokenStreamRewriter(tokens);
//
//		SqlProcessorListener listener = new SqlProcessorListener(rewriter, fieldNames);
//		ParseTreeWalker walker = new ParseTreeWalker();
//		walker.walk(listener, tree);
//
//		ApplyParamResult applyParamResult = new ApplyParamResult();
//		applyParamResult.executaleSql = rewriter.getText();
//		applyParamResult.paramNameList = listener.paramNameList;
//		
//		return applyParamResult;
//	}
}
