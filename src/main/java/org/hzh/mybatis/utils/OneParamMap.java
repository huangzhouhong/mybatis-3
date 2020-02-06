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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.type.TypeHandlerRegistry;
import org.hzh.mybatis.expression.PropertyUtils;

/**
 * 模拟mybatis对单一参数的处理过程，且可以获取到List类型参数原本的名称,并知道只有一个参数。
 * 参考ParamNameResolver.getNamedParams. 当方法只有一个参数，mybatis会抛弃掉参数名。
 * 参考DefaultSqlSession.wrapCollection. 类型为List,Collection,Array将参数包装成Map，key为固定值
 *
 * @author huangzhouhong
 *
 * @param <V>
 */
public class OneParamMap extends HashMap<String, Object> {

	private static final long serialVersionUID = 1739553259238637257L;

	private static TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

	private String theKey;
	private Object theValue;

	public OneParamMap(String key, Object value) {
		super();
		super.put(key, value);
		theKey = key;
		theValue = value;

		// DefaultSqlSession.wrapCollection(Object)
		if (value instanceof Collection) {
			super.put("collection", value);
			if (value instanceof List) {
				super.put("list", value);
			}
		} else if (value != null && value.getClass().isArray()) {
			super.put("array", value);
		}
	}

	public String getTheKey() {
		return theKey;
	}

	@Override
	public Object get(Object key) {
		if (super.containsKey(key)) {
			return super.get(key);
		}

		// DefaultParameterHandler.setParameters
		if (theValue == null) {
			return null;
		}
		// simple type
		boolean canDirectMapToDbType = typeHandlerRegistry.hasTypeHandler(theValue.getClass());
		if (canDirectMapToDbType) {
			return theValue;
		}

		// map,bean,list,collection or array
		return PropertyUtils.getByPath(theValue, (String) key);
	}
}
