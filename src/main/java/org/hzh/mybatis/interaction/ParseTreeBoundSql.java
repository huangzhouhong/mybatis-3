package org.hzh.mybatis.interaction;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.hzh.mybatis.analyzer.ApplyParamResult;

public class ParseTreeBoundSql extends BoundSql {

	ApplyParamResult applyParamResult;

	public ParseTreeBoundSql(Configuration configuration, ApplyParamResult applyParamResult, Object parameterObject) {
		super(configuration, applyParamResult.getExecutaleSql(), null, parameterObject);
		this.applyParamResult = applyParamResult;
	}

	public List<Object> getParamList() {
		return applyParamResult.getParamList();
	}

	@Override
	public List<ParameterMapping> getParameterMappings() {
		throw new RuntimeException();
	}

//	@Override
//	public Object getParameterObject() {
//		throw new RuntimeException();
//	}

	@Override
	public boolean hasAdditionalParameter(String name) {
		throw new RuntimeException();
	}

	@Override
	public void setAdditionalParameter(String name, Object value) {
		throw new RuntimeException();
	}

	@Override
	public Object getAdditionalParameter(String name) {
		throw new RuntimeException();
	}

}
