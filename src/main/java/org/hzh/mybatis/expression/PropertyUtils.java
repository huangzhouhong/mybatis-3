package org.hzh.mybatis.expression;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.compiler.STParser.mapExpr_return;

public class PropertyUtils {
	private static final Object[] NULL_ARGUMENTS = {};
	private static final char INDEXED_START = '[';
	private static final char INDEXED_END = ']';

	// set `ExpressionTests`
	public static Object getExpression(Object obj, String expression) throws NoSuchMethodException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
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

	public static Object getByPath(Object obj, String path) throws NoSuchMethodException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IntrospectionException {
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
		} else {
			return getProperty(obj, path);
		}
	}

	public static Object getByIndex(Object obj, int index) {
		if (obj instanceof List) {
			return ((List<?>) obj).get(index);
		} else if (obj instanceof Collection) {
			List<Object> list = new ArrayList<Object>();
			list.addAll((Collection<?>) obj);
			return list.add(index);
		}
		return null;
	}

	public static Object getProperty(Object bean, String propertyName) throws NoSuchMethodException,
			IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (bean == null) {
			return null;
		} else {
			Method method = getReadMethod(bean.getClass(), propertyName);
			return method.invoke(bean, NULL_ARGUMENTS);
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

	public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName)
			throws IntrospectionException {

		final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		if (propertyDescriptors != null) {
			for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor != null) {
					final String name = propertyDescriptor.getName();
					if (name.equals(propertyName)) {
						return propertyDescriptor;
					}
				}
			}
		}

		return null;
	}
}
