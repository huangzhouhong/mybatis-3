package org.hzh.mybatis.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;
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
		// DefaultParameterHandler.setParameters
		if (theValue == null) {
			return null;
		}
		//simple type
		boolean canDirectMapToDbType = typeHandlerRegistry.hasTypeHandler(theValue.getClass());
		if (canDirectMapToDbType) {
			return theValue;
		}

		// map or bean
		if (size() == 1) {
			return PropertyUtils.getByPath(theValue, (String)key);
		}
		
		// list,collection or array
		if (!super.containsKey(key)) {
			// only one param, can access property or key of value directly
			return PropertyUtils.getByPath(theValue, (String)key);
		}
		return super.get(key);
	}
}
