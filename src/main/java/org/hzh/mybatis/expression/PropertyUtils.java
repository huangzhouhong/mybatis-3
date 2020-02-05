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
package org.hzh.mybatis.expression;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.hzh.mybatis.utils.OneParamNameUtil;

public class PropertyUtils {
	private static final Object[] NULL_ARGUMENTS = {};
	private static final char INDEXED_START = '[';
	private static final char INDEXED_END = ']';
	private static final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

	// see `ExpressionTests`
	public static Object getExpression(Object obj, String expression) {
		if (obj == null) {
			return null;
		}
		// simple type
		if (typeHandlerRegistry.hasTypeHandler(obj.getClass())) {
			return obj;
		}

		String[] parts = expression.split("\\.");
		Object tmpObj = obj;
		for (String part : parts) {
			int indexStart = part.indexOf(INDEXED_START);
			if (indexStart != -1) {
				tmpObj = getByPath(tmpObj, part.substring(0, indexStart));
				while (indexStart != -1) {
					int indexEnd = part.indexOf(INDEXED_END, indexStart);
					String indexString = part.substring(indexStart + 1, indexEnd);
					int index = Integer.valueOf(indexString);
					tmpObj = getByIndex(tmpObj, index);
					indexStart = part.indexOf(INDEXED_START, indexEnd);
				}
			} else {
				tmpObj = getByPath(tmpObj, part);
			}
		}

		return tmpObj;
	}

	public static Object getByPath(Object obj, String path) {
		// single parameter of array,collection or list
		if (obj instanceof StrictMap) {
			StrictMap<?> map = (StrictMap<?>) obj;
			assert map.containsKey("list") || map.containsKey("collection") || map.containsKey("array");
			boolean getSelf = "list".equals(path) || "collection".equals(path) || "array".equals(path)
					|| path.equals(OneParamNameUtil.paramName.get());
			Object value = map.values().iterator().next();
			if (getSelf) {
				return value;
			} else {
				// will get property of value
				obj = value;
			}
		}

		if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			return map.get(path);
		} else if (obj instanceof Collection<?>) {
			// `obj instanceof Collection<?>` means try to get property of item
			List<Object> itemPropertyList = new ArrayList<>();
			for (Object item : (Collection<?>) obj) {
				Object itemProperty = getByPath(item, path);
				// e.g. `list.group.users.userId`,list and users is collection
				if (itemProperty instanceof Collection<?>) {
					itemPropertyList.addAll((Collection<?>) itemProperty);
				} else {
					itemPropertyList.add(itemProperty);
				}
			}
			return itemPropertyList;
		} else if (obj != null && obj.getClass().isArray()) {
			return getByPath(Arrays.asList((Object[]) obj), path);
		}

		return getPropertyOrField(obj, path);
	}

	public static Object getByIndex(Object obj, int index) {
		if (obj instanceof List) {
			return ((List<?>) obj).get(index);
		} else if (obj instanceof Collection) {
			List<Object> list = new ArrayList<Object>();
			list.addAll((Collection<?>) obj);
			return list.add(index);
		} else if (obj != null && obj.getClass().isArray()) {
			return Array.get(obj, index);
		}
		return null;
	}

	public static void setByPath(Object obj, String path, Object value) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map) obj;
			map.put(path, value);
		} else if (obj instanceof Collection<?>) {
			for (Object item : (Collection<?>) obj) {
				setByPath(item, path, value);
			}
		} else if (obj != null && obj.getClass().isArray()) {
			setByPath(Arrays.asList((Object[]) obj), path, value);
		} else {
			setPropertyOrField(obj, path, value);
		}
	}

	public static Object getPropertyOrField(Object bean, String propertyName) {
		if (bean == null) {
			return null;
		} else {
			try {
				Method method = getReadMethod(bean.getClass(), propertyName);
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				return method.invoke(bean, NULL_ARGUMENTS);
			} catch (NoSuchMethodException noSuchMethodException) {
				try {
					return getFieldValue(bean, propertyName);
				} catch (Exception e) {
					String msg = " There is no field or getter for property named '" + propertyName + "' in '"
							+ bean.getClass() + "'";
					throw new ReflectPropertyException(msg, e);
				}
			} catch (Exception e) {
				throw new ReflectPropertyException(e);
			}
		}
	}

	public static void setPropertyOrField(Object bean, String propertyName, Object value) {
		if (bean == null) {
			throw new IllegalArgumentException("bean cannot be null");
		}

		try {
			Method method = getWriteMethod(bean.getClass(), propertyName);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			method.invoke(bean, value);
		} catch (NoSuchMethodException noSuchMethodException) {
			try {
				setFieldValue(bean, propertyName, value);
			} catch (Exception e) {
				String msg = " There is no field or setter for property named '" + propertyName + "' in '"
						+ bean.getClass() + "'";
				throw new ReflectPropertyException(msg, e);
			}
		} catch (Exception e) {
			throw new ReflectPropertyException(e);
		}
	}

	public static Object getProperty(Object bean, String propertyName) {
		if (bean == null) {
			return null;
		} else {
			try {
				Method method = getReadMethod(bean.getClass(), propertyName);
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				return method.invoke(bean, NULL_ARGUMENTS);
			} catch (Exception e) {
				String msg = " There is no getter for property named '" + propertyName + "' in '" + bean.getClass()
						+ "'";
				throw new ReflectPropertyException(msg, e);
			}
		}
	}

	public static void setProperty(Object bean, String propertyName, Object value) {
		if (bean == null) {
			throw new IllegalArgumentException("bean cannot be null");
		}

		try {
			Method method = getWriteMethod(bean.getClass(), propertyName);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			method.invoke(bean, value);
		} catch (Exception e) {
			String msg = " There is no setter for property named '" + propertyName + "' in '" + bean.getClass() + "'";
			throw new ReflectPropertyException(msg, e);
		}
	}

	public static Method getReadMethod(Class<?> beanClass, String propertyName)
			throws IntrospectionException, NoSuchMethodException {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(beanClass, propertyName);
		if (propertyDescriptor == null) {
			throw new NoSuchMethodException(propertyName);
		}
		final Method readMethod = propertyDescriptor.getReadMethod();
		if (readMethod == null) {
			throw new NoSuchMethodException(propertyName);
		}
		return readMethod;
	}

	public static Method getWriteMethod(Class<?> beanClass, String propertyName)
			throws IntrospectionException, NoSuchMethodException {
		PropertyDescriptor propertyDescriptor = getPropertyDescriptor(beanClass, propertyName);
		if (propertyDescriptor == null) {
			throw new NoSuchMethodException(propertyName);
		}
		final Method readMethod = propertyDescriptor.getWriteMethod();
		if (readMethod == null) {
			throw new NoSuchMethodException(propertyName);
		}
		return readMethod;
	}

	public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName)
			throws IntrospectionException {

		final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		if (propertyDescriptors != null) {
			for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor != null) {
					final String name = propertyDescriptor.getName();
					if (name.equalsIgnoreCase(propertyName)) {
						return propertyDescriptor;
					}
				}
			}
		}

		return null;
	}

	public static Object getFieldValue(Object bean, String propertyPath)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (bean == null)
			throw new IllegalArgumentException("bean cannot be null");
		Field field = getField(bean.getClass(), propertyPath);
		field.setAccessible(true);
		return (field.get(bean));
	}

	public static void setFieldValue(Object bean, String propertyPath, Object value)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (bean == null)
			throw new IllegalArgumentException("bean cannot be null");
		Field field = getField(bean.getClass(), propertyPath);
		field.setAccessible(true);
		field.set(bean, value);
	}

	public static Field getField(Class<?> beanClass, String property) throws NoSuchFieldException {
		if (beanClass == null)
			throw new IllegalArgumentException("beanClass cannot be null");

		Field field = null;
		try {
			field = beanClass.getDeclaredField(property);
		} catch (NoSuchFieldException e) {
			if (beanClass.getSuperclass() == null)
				throw e;
			// look for the field in the superClass
			field = getField(beanClass.getSuperclass(), property);
		}
		return field;
	}
}
