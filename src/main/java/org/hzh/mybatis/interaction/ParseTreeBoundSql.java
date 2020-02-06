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
