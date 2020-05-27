package dev;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import core.Application;
import util.MathUtil;

class Command {
	
	private static boolean invertInput = false;
	private String args;
	private final String name;
	private final String value;
	private Class<?> locationClass = null;
	//private Object locationObject = null;
	private float valueDef = Float.NaN;
	private float valueMin;
	private float valueMax;
	public boolean requiresCheats = false;
	private CommandType type = CommandType.METHOD;
	private Method method;
	
	public Command(String name, boolean requiresCheats) {
		this(name, name, CommandMethods.class, CommandType.METHOD, requiresCheats, null);
	}
	
	public Command(String name, boolean requiresCheats, String ... paramNames) {
		this(name, name, CommandMethods.class, CommandType.METHOD, requiresCheats, paramNames);
	}
	
	public Command(String name, Class<?> locationClass, boolean requiresCheats, String ... paramNames) {
		this(name, name, locationClass, CommandType.METHOD, requiresCheats, paramNames);
	}

	public Command(String name, String value, Class<?> locationClass, CommandType type, boolean requiresCheats, String[] args) {
		this.name = name;
		this.requiresCheats = requiresCheats;
		this.locationClass = locationClass;
		this.type = type;
		this.value = value;
		this.args = "";

		if (type == CommandType.SETTER) {
			try {
				final Field field = locationClass.getField(value);
				determineArgs(field);
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		else if (type == CommandType.METHOD) {
			if (args != null) {
				determineArgs(args);
			} else {
				for(Method method : this.locationClass.getMethods()) {
					if (method.getName().equals(name)) {
						determineArgs(method);
						this.method = method;
						return;
					}
				}
			}
		}
	}

	/*public Command(String name, String value, Object object, CommandType type, boolean requiresCheats) {
		this.name = name;
		this.requiresCheats = requiresCheats;
		this.locationObject = object;
		this.type = type;
		this.value = value;
		
	}*/

	/*(private static Method getMethod(Class<?> A, String methodName, Object... args)
			throws NoSuchMethodException, SecurityException {
		Method m = null;

		final Class<?>[] types = new Class[args.length];

		for (int i = 0; i < types.length; i++) {
			types[i] = args[i].getClass();
		}

		m = A.getMethod(methodName, types);

		return m;
	}*/

	public static String getVariable(Class<?> A, String name) {
		try {
			try {
				return A.getField(name).get(null).toString();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final NoSuchFieldException e) {
			Console.log("Console error: No such var " + A.toString() + "." + name);
		} catch (final SecurityException e) {
			e.printStackTrace();
		}

		return "ERR";
	}

	public static String invokeMethod(Class<?> A, Method m, String... args) {
		try {
			Class<?>[] types = m.getParameterTypes();
			Object[] inArgs = new Object[types.length];
			
			if (args.length > types.length) {
				return "Too many Parameters";
			}
			
			if (args.length == 0 && types.length > 0) {
				return "Command requires parameters";
			}
			
			for(int i = 0; i < args.length; i++) {
				try {
					switch(types[i].getName()) {
					case "java.lang.String":
						inArgs[i] = args[i];
						break;
					case "int":
					case "long":
					case "short":
					case "byte":
						inArgs[i] = Integer.parseInt(args[i]);
						break;
					case "float":
					case "double":
						inArgs[i] = Float.parseFloat(args[i]);
						break;
					case "char":
						inArgs[i] = args[i].charAt(0);
						break;
					case "boolean":
						inArgs[i] = inArgs[i] = Boolean.parseBoolean(args[i]);
						break;
					default:
						return "Incorrect/Bad Parameters";
					}
				}
				catch(NumberFormatException e) {
					return "Incorrect/Bad Parameters";
				}
			}
			
			for(int i = args.length; i < types.length; i++) {
				switch(types[i].getName()) {
				case "java.lang.String":
					inArgs[i] = "";
					break;
				case "int":
				case "long":
				case "short":
				case "byte":
					inArgs[i] = 0;
					break;
				case "float":
				case "double":
					inArgs[i] = 0f;
					break;
				case "char":
					inArgs[i] = ' ';
					break;
				case "boolean":
					inArgs[i] = false;
					break;
				default:
					inArgs[i] = null;
				}
			}
			
			final Object s = m.invoke(null, inArgs);
			if (s == null) {
				return null;
			} else {
				return s.toString();
			}
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	/*public static String invokeMethod(Object o, String methodName, Object... args) {
		try {
			final Method m = getMethod(o.getClass(), methodName, args);

			final Object s = m.invoke(o, args);

			if (s == null) {
				return null;
			} else {
				return s.toString();
			}
		} catch (NoSuchMethodException | SecurityException e) {
			Console.log("Console error: No such method " + o.getClass().toString() + "." + methodName);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}*/

	public static void log(String data) {
		Console.log(data);
	}

	public static String setVariable(Class<?> A, String name, String value, float valueDef, float valueMin,
			float valueMax) {
		try {
			try {
				final Field field = A.getField(name);

				if (field.getType().isAssignableFrom(Float.TYPE)) {

					if (!Float.isNaN(valueDef) && value.toLowerCase().equals("default")
							|| value.toLowerCase().equals("d")) {
						field.set(null, valueDef);
					} else {
						float v = Float.parseFloat(value);
						if (!Float.isNaN(valueDef)) {
							v = MathUtil.clamp(v, valueMin, valueMax);
						}

						field.set(null, invertInput ? 1f / v : v);
					}
				}
				if (field.getType().isAssignableFrom(Boolean.TYPE)) {
					if (value.equals("")) {
						final Boolean v = (Boolean) field.get(new Boolean(false));
						field.set(null, !v.booleanValue());
					} else if (value.equals("1")) {
						field.set(null, invertInput ? false : true);
					} else if (value.equals("0")) {
						field.set(null, invertInput ? true : false);
					} else {
						final boolean b = Boolean.parseBoolean(value);
						field.set(null, invertInput ? !b : b);
					}
				}
				if (field.getType().isAssignableFrom(String.class)) {
					field.set(null, value);
				}
				if (field.getType().isAssignableFrom(Integer.TYPE)) {
					if (!Float.isNaN(valueDef) && value.toLowerCase().equals("default")
							|| value.toLowerCase().equals("d")) {
						field.set(null, (int) valueDef);
					} else {
						int v = Integer.parseInt(value);
						if (!Float.isNaN(valueDef)) {
							v = (int) MathUtil.clamp(v, valueMin, valueMax);
						}
						field.set(null, invertInput ? 1 / v : v);
					}
				}

				return A.getField(name).get(null).toString();
			} catch (final IllegalAccessException e) {
				Console.printStackTrace(e);
			} catch (final NumberFormatException e) {
				Console.log("Incorrect parameters for " + A.toString() + "." + name);
			}
		} catch (final NumberFormatException e) {
			Console.printStackTrace(e);
		} catch (final NoSuchFieldException e) {
			Console.log("No such var " + A.toString() + "." + name);
		} catch (final SecurityException e) {
			Console.printStackTrace(e);
		}

		return "ERR";
	}
	
	
	
	private void determineArgs(Field field) {
		Class<?> type = field.getType();
		
		if (type.isAssignableFrom(String.class)) {
			args = "<str>";
		}
		else if (type.isAssignableFrom(Float.TYPE)) {
			args = "<float>";
		}
		else if (type.isAssignableFrom(Integer.TYPE)) {
			args = "<int>";
		}
		else if (type.isAssignableFrom(Boolean.TYPE)) {
			args = "<0/1>";
		}
	}
	
	private void determineArgs(Method method) {
		Parameter[] params = method.getParameters();
		
		for(Parameter param : params) {
			args += "<" + param.getType().getSimpleName() + "> ";
		}
	}
	
	private void determineArgs(String[] params) {
		args = "";
		for(String param : params) {
			args += "<" + param + "> ";
		}
	}

	public Command clampInput(float defaultValue, float minValue, float maxValue) {
		valueDef = defaultValue;
		valueMin = minValue;
		valueMax = maxValue;
		return this;
	}

	@SuppressWarnings("all")
	public void execute(String[] args) {
		/*if (locationClass == Console.class) {
			locationObject = Application.scene;
		}*/

		if (type == CommandType.GETTER) {
			Console.log(getVariable(locationClass, getValue()));
		}

		if (type == CommandType.SETTER) {
			if (args.length != 0) {
				setVariable(locationClass, getValue(), args[0], valueDef, valueMin, valueMax);
			} else {
				setVariable(locationClass, getValue(), "", valueDef, valueMin, valueMax);
			}
		}

		if (type == CommandType.METHOD) {
			String output = null;

			/*if (locationClass != null && locationClass != Console.class) {
				output = invokeMethod(locationClass, getValue(), args);
			} else {
				output = invokeMethod(locationObject, getValue(), args);
			}*/
			
			output = invokeMethod(locationClass, method, args);

			if (output != null) {
				Console.log(output);
			}
		}

		if (name.equals("quit") || name.equals("exit")) {
			Application.close();
		}

	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	/**
	 * For Setter Commands: If the input is boolean, this swaps the flag If the
	 * input is an int/float, this will set the variable to 1/input
	 * 
	 * @return this object
	 */
	public Command invertInput() {
		invertInput = true;
		return this;
	}

	public String getArgs() {
		return args == null ? "" : args;
	}
}
