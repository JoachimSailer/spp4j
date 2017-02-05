package org.spp4j.core;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spp4j.core.Performance.Level;
import org.spp4j.core.entity.MethodDescriptor;

class MethodDescriptorFactory {

	private static final Logger LOG = LoggerFactory.getLogger(MethodDescriptorFactory.class);
	private static final Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>();

	static {
		PRIMITIVES.put("boolean", boolean.class);
		PRIMITIVES.put("int", int.class);
		PRIMITIVES.put("long", long.class);
		PRIMITIVES.put("float", float.class);
		PRIMITIVES.put("double", double.class);
		PRIMITIVES.put("char", char.class);
	}

	static MethodDescriptor createMethodDescriptor(Signature signature) {
		String qualifiedMethodName = signature.toString();
		int bracketStart = qualifiedMethodName.indexOf("(");
		int packageNameStart = qualifiedMethodName.lastIndexOf(" ", bracketStart);
		int classNameEnd = qualifiedMethodName.lastIndexOf(".", bracketStart);
		int packageNameEnd = qualifiedMethodName.lastIndexOf(".", classNameEnd - 1);
		String packageName = getName(qualifiedMethodName, packageNameStart, packageNameEnd + 1);
		String className = getName(qualifiedMethodName, packageNameEnd + 1, classNameEnd);
		String methodName = qualifiedMethodName.substring(classNameEnd + 1);
		Method methodSignature = getMethod(signature.getDeclaringType(), signature.toLongString(), methodName);
		Level level = getLevel(methodSignature);
		return new MethodDescriptor(packageName, className, methodName, level);
	}

	private static String getName(String qualifiedMethodName, int start, int end) {
		int realStart = start;
		if (realStart == -1) {
			realStart = 0;
		}
		String ret = null;
		if (end == -1) {
			ret = "";
		} else {
			ret = qualifiedMethodName.substring(realStart, end - 1).trim();
		}
		return ret;
	}

	private static Level getLevel(Method method) {
		Level level = Level.OFF;
		if (method != null) {
			Performance perf = method.getAnnotation(Performance.class);
			level = perf.value();
		}
		return level;
	}

	private static Method getMethod(Class<?> declaringClass, String method, String simpleMethodName) {
		Method ret = null;
		try {
			int bracket = simpleMethodName.indexOf("(");
			String methodName = simpleMethodName.substring(0, bracket);
			Class<?>[] argClasses = getParameterList(method);
			ret = declaringClass.getDeclaredMethod(methodName, argClasses);
		} catch (NoSuchMethodException e) {
			LOG.error(String.format("Could not find Method %s (from class %s)", method,
					declaringClass != null ? declaringClass.toString() : null, e.getMessage()), e);
		} catch (ClassNotFoundException e) {
			LOG.error(String.format("Could not find Class of parameter list for method %s. Reason: %s", method,
					e.getMessage()));
		}
		return ret;
	}

	private static Class<?>[] getParameterList(String method) throws ClassNotFoundException {
		int bracket = method.indexOf("(");
		String arguments = method.substring(bracket + 1, method.length() - 1);
		String[] params = arguments.split("\\s*,\\s*");
		Class<?>[] argClasses = new Class<?>[params.length];
		for (int i = 0; i < argClasses.length; i++) {
			String className = params[i];
			int arrayDimension = 0;
			if (className.endsWith("]")) {
				int arrayStart = className.indexOf("[");
				String arraySuffix = className.substring(arrayStart, className.length());
				className = className.substring(0, arrayStart);
				int position = arraySuffix.indexOf("[");
				while (position != -1) {
					arrayDimension++;
					position = arraySuffix.indexOf("[", position + 1);
				}
			}
			Class<?> clazz = PRIMITIVES.get(className);
			if (clazz == null) {
				clazz = Class.forName(className);
			}
			for (int j=0;j<arrayDimension;j++) {
				clazz = Array.newInstance(clazz, 0).getClass();
			}
			argClasses[i] = clazz;
		}
		return argClasses;
	}

}
