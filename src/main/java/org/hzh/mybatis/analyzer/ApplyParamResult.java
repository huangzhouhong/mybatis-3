package org.hzh.mybatis.analyzer;

import java.util.List;

public class ApplyParamResult {
	String executaleSql;
//	List<String> paramNameList;
	List<Object> paramList;

	public String getExecutaleSql() {
		return executaleSql;
	}

	public List<Object> getParamList() {
		return paramList;
	}

	@Override
	public String toString() {
		return executaleSql + "\n" + paramList;
	}
}
