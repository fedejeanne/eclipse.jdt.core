package org.eclipse.jdt.internal.compiler.lookup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class can walk the whole hierarchy of some classes and print the necessary code to declare the whole hierarchy
 * as internal classes/interfaces.
 */
@SuppressWarnings("nls")
class HierarchyWalker {

	/**
	 * The classes from which the hierarchy will be collected.
	 */
	private static final Set<Class<?>> CLASSES = Set.of(String.class);

	private static final boolean OBFUSCATE = true;

	private static final List<String> NATIVE_TYPES = List.of("void", "boolean", "byte", "short", "char", "int", "long",
			"double", "float");

	private static final String[] WORDS = Set.copyOf(List.of("ability", "able", "about", "above", "accept", "access",
			"act", "active", "actor", "actual", "adding", "address", "advice", "afford", "again", "agency", "agenda",
			"agreed", "agreement", "ahead", "aid", "aim", "air", "alert", "alien", "alive", "alley", "allow", "alone",
			"alpha", "alter", "among", "amount", "analyst", "and", "angry", "animal", "annual", "answer", "ant",
			"antique", "anxiety", "any", "anybody", "ape", "appeal", "appear", "apple", "appointment", "arc", "are",
			"area", "argue", "arise", "army", "around", "array", "arrive", "arrow", "art", "artist", "aside", "ask",
			"aspect", "assess", "assignment", "assist", "assume", "assumption", "ate", "attack", "attend", "audience",
			"audio", "author", "autumn", "avenue", "average", "award", "aware", "baby", "back", "bad", "baker", "ball",
			"band", "bar", "base", "basic", "bat", "bay", "beach", "bear", "beat", "become", "bed", "bee", "before",
			"begin", "below", "bend", "better", "beyond", "big", "bike", "bin", "bird", "bit", "bizarre", "blood",
			"blue", "boat", "body", "bonus", "book", "border", "born", "both", "bottle", "bottom", "box", "boy",
			"brain", "branch", "brave", "break", "breath", "bridge", "brief", "bright", "bring", "broad", "broke",
			"bronze", "budget", "buffer", "build", "burden", "burn", "burst", "bus", "busy", "button", "buy", "buyer",
			"cable", "call", "calm", "camera", "cancer", "cannot", "capture", "car", "carbon", "card", "care", "career",
			"carry", "castle", "casual", "cat", "catch", "cats", "cause", "celebration", "cell", "center", "chain",
			"chair", "championship", "chance", "change", "chapter", "charge", "chart", "chase", "chat", "cheap",
			"check", "cheek", "child", "childhood", "choir", "chop", "church", "cigarette", "city", "class", "clay",
			"clear", "click", "client", "clock", "close", "cloud", "coach", "coast", "cold", "combination",
			"competition", "connection", "consequence", "contribution", "control", "cook", "cookie", "cool", "core",
			"cost", "count", "cover", "create", "cross", "crush", "cup", "curl", "currency", "curve", "customer", "cut",
			"cute", "dance", "dark", "data", "date", "dawn", "day", "deal", "dealer", "death", "debt", "deep", "delay",
			"departure", "depression", "depth", "desk", "development", "die", "diet", "dig", "dirt", "disk", "dog",
			"dot", "drop", "dry", "dust", "ear", "easy", "eat", "economics", "edit", "egg", "election", "elevator",
			"employment", "end", "ends", "engineering", "entertainment", "enthusiasm", "equipment", "exam",
			"examination", "expression", "extent", "eye", "face", "fact", "fail", "failure", "fair", "fall", "far",
			"farm", "fast", "fat", "fate", "fear", "few", "file", "film", "find", "fine", "fire", "fishing", "flight",
			"fog", "for", "fox", "friendship", "fun", "fur", "garbage", "gas", "hair", "hat", "height", "historian",
			"homework", "independence", "indication", "information", "instance", "insurance", "interaction",
			"knowledge", "lab", "lady", "language", "magazine", "maintenance", "marriage", "math", "measurement",
			"meat", "mixture", "moment", "music", "office", "payment", "penalty", "performance", "philosophy",
			"physics", "pizza", "platform", "poet", "policy", "possibility", "power", "preparation", "profession",
			"professor", "quantity", "queen", "ratio", "recipe", "relationship", "requirement", "revenue", "salad",
			"sample", "selection", "session", "shopping", "signature", "significance", "singer", "sister", "skill",
			"society", "song", "stranger", "studio", "supermarket", "surgery", "system", "teaching", "temperature",
			"theory", "tongue", "topic", "uncle", "union", "unit", "user", "video", "virus", "wealth", "week", "wife",
			"woman", "writer")).toArray(new String[] {});

	private static LinkedHashSet<Class<?>> toVisit = new LinkedHashSet<>();

	private static PrintStream outStream = System.out;

	private static final Map<Class<?>, PrintStream> outStreamMap = new HashMap<>();

	private static final String OUT_DIR = "output";

	// FIXME: ideally, there would be no banned superclasses
	private static final Set<Class<?>> BANNED_SUPERCLASSES = Set
			.of(java.util.EventObject.class/*
											 * , org.eclipse.core.runtime.MultiStatus.class,
											 * org.eclipse.core.runtime.Status.class
											 */);

	// FIXME: ideally, there would be no banned methods
	private static final Set<String> BANNED_METHODS = Set.of("values", "valueOf", "compareTo", "internalClone");

	private static Map<String, String> obfuscationDictionaryClasses = new HashMap<>();

	private static Map<String, String> obfuscationDictionaryMethods = new HashMap<>();

	private static int obfuscatedCounter;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (OBFUSCATE) {
			System.out.println("There are " + WORDS.length + " words in the obfuscation dictionary");
		}

		Files.createDirectories(Path.of(OUT_DIR));
		deleteAllFiles(OUT_DIR);

		for (Class<?> c : CLASSES) {
			collectClassesToVisitFromHierarchy(c);
		}

		visitClasses();

		if (OBFUSCATE) {
			printDictionary();
		}

		printVisitedClasses();
	}

	private static void deleteAllFiles(String folderPath) {
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		// TODO (visjee): REMOVE!
		System.out.println("Deleting all files in " + folderPath);
		int count = 0;
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					count++;
					file.delete();
				}
			}
		}
		// TODO (visjee): REMOVE!
		System.out.println("Deleted " + count + " files");
	}

	private static PrintStream createOrGetOutputStream(Class<?> c) throws FileNotFoundException {
		outStreamMap.putIfAbsent(c, new PrintStream(
				new FileOutputStream(OUT_DIR + "/" + simpleName(translateClassName(c)) + ".java", false)));

		return outStreamMap.get(c);
		// return System.out;
	}

	// private static void collectClassesToVisit(Class<?> c) throws ClassNotFoundException {
	//
	// collectClassesToVisitFromHierarchy(c);
	//
	// if (c == null //
	// || !toVisit.add(c)// skip if it was already visited
	// || !isOurs(c) // Skip if it's a "known" class
	// || isNativeType(c.getName())) {
	// return;
	// }
	// }

	private static void collectClassesToVisitFromHierarchy(Class<?> c) throws ClassNotFoundException {
		if (c == null || Object.class.equals(c) || c.isPrimitive() || !isOurs(c) || toVisit.contains(c)) {
			return;
		}

		// Don't work with arrays, work with the type they contain
		if (c.isArray()) {
			String className = c.getCanonicalName().replace("[]", "");
			if (isNativeType(className)) {
				return;
			}

			try {
				c = Class.forName(className);
			} catch (ClassNotFoundException e) {
				System.err.println("Couldn't find class '" + className + "', ignoring it");
				return;
			}
		}

		// System.out.println("Collecting from: " + c);

		// collect from the super class
		collectClassesToVisitFromHierarchy(c.getSuperclass());

		// collect from the implemented interfaces
		for (Class<?> i : c.getInterfaces()) {
			collectClassesToVisitFromHierarchy(i);
		}

		// add to visit at the end to have better chances that all superclasses have already been collected
		if (!addToVisit(c)) {
			return;
		}

		// collect from the methods
		for (Method m : c.getDeclaredMethods()) {
			// first from the return type
			collectClassesToVisitFromHierarchy(m.getReturnType());

			// and then from the parameters
			for (Class<?> pt : m.getParameterTypes()) {
				collectClassesToVisitFromHierarchy(pt);
			}
		}

	}

	private static void visitClasses() throws FileNotFoundException {
		// TODO (visjee): REMOVE!
		System.out.println("There are " + toVisit.size() + " classes to visit");
		for (Class<?> c : toVisit) {

			// System.out.println("visiting: " + c);

			outStream = createOrGetOutputStream(c);

			appendClassModifiers(c);

			append(c.isInterface() ? "interface " : c.isEnum() ? "enum " : "class ");

			appendClassName(c);
			append(" ");

			// Add the extended classes
			appendExtendedClasses(c);

			// add the extended/implemented interfaces
			appendImplementedInterfaces(c);

			// Add the body of the class/interface
			appendClassBody(c);

			append(System.lineSeparator());

			outStream.flush();
			outStream.close();

		}
	}

	private static void appendClassName(Class<?> c) {
		append(translateClassName(c));
		append(" ");
	}

	private static void appendClassModifiers(Class<?> c) {
		int modifiers = c.getModifiers();

		if ((modifiers & Modifier.PUBLIC) != 0) {
			append("public ");
		}

		// Skip the rest for enums, only "public" is allowed anyway
		if (c.isEnum()) {
			return;
		}

		// FIXME: some modifiers are not supported
		// if ((modifiers & Modifier.PROTECTED) != 0) {
		// append("protected ");
		// } else if ((modifiers & Modifier.PRIVATE) != 0) {
		// append("private ");
		// }
		// if ((modifiers & Modifier.STATIC) != 0) {
		// append("static ");
		// }
		if ((modifiers & Modifier.ABSTRACT) != 0) {
			append("abstract ");
		}
		if ((modifiers & Modifier.FINAL) != 0) {
			append("final ");
		}
	}

	private static void appendImplementedInterfaces(Class<?> c) {
		Class<?>[] interfaces = c.getInterfaces();

		if (interfaces.length > 0) {
			if (c.isInterface()) {
				append("extends ");
			} else {
				append("implements ");
			}

			append(//
					Arrays.stream(interfaces)//
							.map(i -> translateClassName(i))//
							.collect(Collectors.joining(", ")));

			append(" ");
			// visit the implemented/extended interfaces too
			for (Class<?> i : interfaces) {
				// addToVisit(i);
			}
		}
	}

	private static void appendExtendedClasses(Class<?> c) {
		if (c.isInterface() || c.isEnum()) {
			return;
		}

		Class<?> superclass = c.getSuperclass();
		if (superclass != null) {
			if (!BANNED_SUPERCLASSES.contains(superclass)) {
				append("extends ");
				appendClassName(superclass);
				append(" ");
			}

			// Visit the superclass even if it's not allowed to be used as a superclass
			// addToVisit(superclass);
		}
	}

	private static boolean addToVisit(String c) {
		// Don't use arrays, use the type of the array.
		String className = c.replace("[]", "");

		if (isNativeType(className)) {
			return false;
		}

		try {
			Class<?> klass = Class.forName(className);
			if (!klass.isPrimitive()) {
				return addToVisit(klass);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Couldn't add type '" + className + "'");
		}

		return false;
	}

	private static boolean isNativeType(String className) {
		return NATIVE_TYPES.contains(className.replace("[]", "")); // If it's an array, look at the type of the elements
	}

	private static boolean addToVisit(Class<?> c) {
		if (isNativeType(c.getCanonicalName())) {
			return false;
		}

		if (c.isArray()) {
			String arrayType = c.getCanonicalName().replace("[]", "");
			try {
				c = Class.forName(arrayType);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (Object.class.equals(c) // Ignore Object
				|| toVisit.contains(c) //
		) {
			return false;
		}

		return toVisit.add(c);
	}

	private static boolean isOurs(Class<?> c) {
		return isOurs(c.getCanonicalName());
	}

	private static boolean isOurs(String c) {
		return c.startsWith("aquintos.") //
				|| c.startsWith("com.itiv.") //
				|| c.startsWith("javax.jmi.")//
				|| c.startsWith("ru.") //
				|| c.startsWith("vi.") //
		;
	}

	private static void appendClassBody(Class<?> c) {
		append(" {");

		if (c.isEnum()) {
			append(System.lineSeparator());
			append("A;");
		}

		Method[] methods = filterMethods(c);
		if (methods.length > 0) {
			append(System.lineSeparator());

			Set<String> addedMethods = new HashSet<>();
			for (Method m : methods) {
				if (addedMethods.add(m.getName())) { // gets rid of method overloading but hey, what the heck
					appendMethod(c, m);
				}
			}

			append(System.lineSeparator());
		}

		append("}");
	}

	private static Method[] filterMethods(Class<?> c) {
		Method[] methods = c.getDeclaredMethods();
		// if (!c.isEnum()) {
		// return methods;
		// }

		return Arrays.stream(methods)//
				.filter(m -> !BANNED_METHODS.contains(m.getName()))// some methods only bring trouble in enums
				.collect(Collectors.toList())//
				.toArray(new Method[] {});
	}

	private static void appendMethod(Class<?> c, Method m) {
		boolean isInterface = c.isInterface();
		// Do not override final methods
		if ((m.getModifiers() & Modifier.FINAL) != 0 && isOverridingMethod(c, m)) {
			return;
		}

		// Visit the return type too
		// addToVisit(m.getReturnType());

		String line = m.toString();

		String methodPrefixAndName = line.substring(0, line.indexOf("("));

		Class<?> declaringClass = m.getDeclaringClass();

		String methodPrefix = methodPrefixAndName
				.substring(0, methodPrefixAndName.lastIndexOf(declaringClass.getName())).trim();
		String methodModifiers = methodPrefix.substring(0, Math.max(methodPrefix.lastIndexOf(" "), 0));
		String methodName = translateMethodName(simpleName(methodPrefixAndName.substring(methodPrefix.length())));

		Class<?> returnTypeClassName = getReturnType(c, m);

		// addToVisit(returnTypeClassName);

		String newParameters = "";

		if (m.getParameterCount() > 0) {
			String parameterTypes = line.substring(line.indexOf("("), line.indexOf(")") + 1);
			String[] parameterTypeClassNames = parameterTypes.substring(1, parameterTypes.length() - 1).split(",");

			for (int i = 0; i < parameterTypeClassNames.length; i++) {

				String className = parameterTypeClassNames[i];

				// Visit the parameter types too
				// addToVisit(className);

				String translatedClassName = translateClassName(className);
				parameterTypeClassNames[i] = translatedClassName + " " + (char) ('a' + i);
			}

			newParameters = Arrays.stream(parameterTypeClassNames)//
					.collect(Collectors.joining(", "));
		}

		// FIXME: "default" is not supported
		append(methodModifiers.replace("default", ""));
		append(" ");

		String returnType = translateClassName(returnTypeClassName);
		append(returnType);

		append(" ");
		append(methodName);
		append("(");
		append(newParameters);
		append(")");

		appendMethodBody(m, isInterface);
	}

	private static Class<?> getReturnType(Class<?> c, Method m) {
		Class<?> returnTypeInSuperclass = getReturnTypeInSuperclass(c.getSuperclass(), m);
		return (returnTypeInSuperclass != null) ? returnTypeInSuperclass : m.getReturnType();
	}

	private static boolean isOverridingMethod(Class<?> c, Method m) {
		// FIXME (visjee) I think this returns always false
		return !m.getDeclaringClass().equals(c);
	}

	private static Class<?> getReturnTypeInSuperclass(Class<?> c, Method m) {
		if (c == null) {
			return null;
		}

		try {
			Method found = c.getDeclaredMethod(m.getName(), m.getParameterTypes());

			if (found != null) {
				return found.getReturnType();
			}

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// method does not exist in this class. Keep searching
			return getReturnTypeInSuperclass(c.getSuperclass(), m);
		}

		return null;
	}

	private static String translateMethodName(String originalName) {
		if (!OBFUSCATE) {
			return originalName;
		}

		if (obfuscationDictionaryMethods.containsKey(originalName)) {
			return obfuscationDictionaryMethods.get(originalName);
		}

		int idxUpper = indexOfFirstUppercase(originalName);

		// is, get, has...
		String prefix = originalName.substring(0, Math.max(idxUpper, 0));

		String obfuscated = prefix + getObfuscatedString();

		obfuscationDictionaryMethods.put(originalName, obfuscated);

		return obfuscated;
	}

	public static int indexOfFirstUppercase(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				return i;
			}
		}
		return -1; // No uppercase letter found
	}

	private static String translateClassName(Class<?> c) {
		return translateClassName(c.getCanonicalName());
	}

	private static String translateClassName(String c) {

		boolean isArray = c.endsWith("[]");
		String className = c.replace("[]", "").replace("$", ".");

		final String translated;

		if (isNativeType(className) || !isOurs(className)) {
			translated = className;
		} else if (OBFUSCATE) {
			// If there is already a dictionary, the words in it do not use FQNs so return them as they are
			String obfuscatedName = getObfuscatedString();
			obfuscationDictionaryClasses.putIfAbsent(className, obfuscatedName);
			translated = obfuscationDictionaryClasses.get(className);
		} else {
			// No obfuscation, no JDK class
			translated = simpleName(className);
		}

		return isArray ? translated + "[]" : translated;
	}

	private static String getObfuscatedString() {
		String obfuscatedName = WORDS[obfuscatedCounter % WORDS.length] + (obfuscatedCounter / WORDS.length + 1);
		obfuscatedName = obfuscatedName.substring(0, 1).toUpperCase() + obfuscatedName.substring(1);
		obfuscatedCounter++;
		return obfuscatedName;
	}

	private static String simpleName(String fqClassName) {
		String[] split = fqClassName.split("\\.");
		if (split.length == 0) {
			// it is not a FQN
			return fqClassName;
		}

		return split[split.length - 1];
	}

	private static void appendMethodBody(Method m, boolean isInterface) {
		if (isInterface && ((m.getModifiers() & Modifier.STATIC) == 0)//
				|| ((m.getModifiers() & Modifier.ABSTRACT) != 0)) {
			append(";");
		} else if ((m.getModifiers() & Modifier.NATIVE) != 0) {
			append(";");
		} else {
			append("{");
			appendReturn(m.getReturnType());
			append("}");
		}
		append(System.lineSeparator());
	}

	private static void appendReturn(Class<?> returnType) {
		if (returnType.getName().contains("void")) {
			return;
		}

		if (returnType.isPrimitive()) {
			if (returnType.getName().contains("boolean")) {
				append("return false;");
			} else {
				append("return 0;");
			}
		} else {
			append("return null;");
		}
	}

	private static void append(String s) {
		outStream.append(s);
	}

	private static void printDictionary() {
		printDictionaryInterestingClasses();
		printDictionaryAllClasses();
	}

	private static void printDictionaryInterestingClasses() {
		System.out.println("============================================================");
		System.out.println("Dictionary (interesting classes)");
		System.out.println("============================================================");

		for (Class<?> c : CLASSES) {
			String className = c.getName();
			String obfuscated = obfuscationDictionaryClasses.getOrDefault(className, className);

			System.out.println(className + "=" + obfuscated);
		}
		System.out.println("============================================================");
	}

	private static void printDictionaryAllClasses() {
		System.out.println("============================================================");
		System.out.println("Dictionary");
		System.out.println("============================================================");

		for (Entry<String, String> e : obfuscationDictionaryClasses.entrySet()) {
			System.out.println(e.getKey() + "=" + e.getValue());
		}
		System.out.println("============================================================");
	}

	private static void printVisitedClasses() {
		System.out.println("============================================================");
		System.out.println("Visited classes");
		System.out.println("============================================================");

		for (Class<?> c : toVisit) {
			System.out.println(c.getName());
		}

		System.out.println("============================================================");
	}
}
