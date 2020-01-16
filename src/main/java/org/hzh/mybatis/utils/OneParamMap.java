package org.hzh.mybatis.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.binding.BindingException;

/**
 * 模拟mybatis对单一参数的处理过程，且可以获取到List类型参数原本的名称,并知道只有一个参数。
 * 参考ParamNameResolver.getNamedParams. 当方法只有一个参数，mybatis会抛弃掉参数名。
 * 参考DefaultSqlSession.wrapCollection. 类型为List,Collection,Array将参数包装成Map，key为固定值
 * @author huangzhouhong
 *
 * @param <V>
 */
public class OneParamMap<V> extends HashMap<String, V> {

	private static final long serialVersionUID = 1739553259238637257L;

	public OneParamMap(String key,V value){
		super();
		super.put(key, value);
	}

	@Override
	public V get(Object key) {
		if (!super.containsKey(key)) {
			V value = getFirstValue();
			if (value instanceof Collection) {
				if (key.equals("collection")) {
					return value;
				}
				if (value instanceof List && key.equals("list")) {
					return value;
				}
			} else if (value != null && value.getClass().isArray() && key.equals("array")) {
				return value;
			}
			throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
		}
		return super.get(key);
	}

	@Override
	public V put(String key, V value) {
		throw new RuntimeException(this.getClass() + " only allow one entry");
	}

	private V getFirstValue() {
		return super.values().iterator().next();
	}
}
