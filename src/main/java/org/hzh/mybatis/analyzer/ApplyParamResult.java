package org.hzh.mybatis.analyzer;

import java.util.List;

public class ApplyParamResult {
	String executaleSql;
	List<String> paramNameList;

	@Override
	public String toString() {
		return executaleSql + "\n" + paramNameList;
	}
}
