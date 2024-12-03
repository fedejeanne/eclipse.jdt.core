package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	private static final Set<Class<?>> CLASSES = Set.of(Integer.class, String.class);

	/**
	 * If set to <code>true</code> then classes inside the packages java.* will not be collected.
	 */
	private static final boolean IGNORE_JDK_CLASSES = false;

	private static final Set<String> WORDS = new HashSet<>();

	static {
		// Some words are repated and therefore I use addAll to get rid of them
		WORDS.addAll(List.of("society", "supermarket", "physics", "information", "elevator", "shopping", "signature",
				"virus", "video", "philosophy", "ratio", "platform", "power", "policy", "skill", "mixture", "lab",
				"cancer", "examination", "development", "failure", "office", "interaction", "union", "church", "advice",
				"uncle", "contribution", "theory", "child", "math", "surgery", "marriage", "penalty", "selection",
				"garbage", "professor", "control", "writer", "stranger", "combination", "assumption", "magazine",
				"bird", "flight", "employment", "depression", "election", "profession", "cheek", "extent", "mixture",
				"power", "indication", "lady", "cigarette", "bonus", "selection", "revenue", "requirement", "topic",
				"enthusiasm", "expression", "sample", "flight", "language", "knowledge", "customer", "possibility",
				"tongue", "blood", "entertainment", "sister", "temperature", "signature", "video", "instance", "system",
				"fishing", "maintenance", "wife", "chapter", "moment", "connection", "poet", "hair", "independence",
				"performance", "singer", "user", "insurance", "equipment", "consequence", "cancer", "engineering",
				"meat", "recipe", "anxiety", "homework", "assignment", "appointment", "debt", "agreement", "unit",
				"analyst", "measurement", "friendship", "maintenance", "economics", "dirt", "art", "woman", "childhood",
				"departure", "marriage", "historian", "knowledge", "teaching", "performance", "insurance", "height",
				"client", "currency", "cookie", "week", "preparation", "quantity", "temperature", "profession",
				"championship", "music", "competition", "society", "pizza", "dealer", "salad", "significance", "union",
				"celebration", "hat", "death", "wealth", "queen", "session", "payment", "camera", "studio", "song",
				"lab", "relationship"));
	}

	private static Set<Class<?>> visited = new HashSet<>();

	private static StringBuilder result = new StringBuilder();

	private static Map<String, String> obfuscationDictionary;

	static void collectHierarchy(Class<?> c) {
		if (c == null) {
			return;
		}

		if (!visited.add(c)) {
			return;
		}

		if (c.getName().startsWith("java.") && IGNORE_JDK_CLASSES) {
			return;
		}

		Set<Class<?>> toVisit = new HashSet<>();

		boolean isInterface = c.isInterface();

		if (isInterface) {
			result.append("interface ");
		} else {
			result.append("class ");
		}

		result.append(c.getSimpleName());
		result.append(" ");

		if (!isInterface) {
			// It's a class, add extends
			Class<?> superclass = c.getSuperclass();
			if (superclass != null) {
				result.append("extends ");
				result.append(superclass.getSimpleName());
				result.append(" ");
			}

			toVisit.add(superclass);
		}

		Class<?>[] interfaces = c.getInterfaces();

		if (interfaces.length > 0) {
			if (isInterface) {
				result.append("extends ");
			} else {
				result.append("implements ");
			}

			result.append(//
					Arrays.stream(interfaces)//
							.map(i -> i.getSimpleName())//
							.collect(Collectors.joining(", ")));

			for (Class<?> i : interfaces) {
				toVisit.add(i);
			}
		}

		result.append(" {}");
		result.append(System.lineSeparator());

		// follow up the hierarchy
		for (Class<?> t : toVisit) {
			collectHierarchy(t);
		}

	}

	private static void obfuscate() {
		createDictionary();
		printDictionary();

		String res = result.toString();

		for (Entry<String, String> e : obfuscationDictionary.entrySet()) {
			res = res.replaceAll(e.getKey(), e.getValue());
		}

		result.delete(0, result.length());
		result.append(res);
	}

	private static void printDictionary() {
		System.out.println("============================================================");
		System.out.println("Dictionary (interesting classes)");
		System.out.println("============================================================");

		Set<String> interestingClassNames = new HashSet<>();
		for (Class<?> c : CLASSES) {
			interestingClassNames.add(c.getSimpleName());
		}

		for (Entry<String, String> e : obfuscationDictionary.entrySet()) {
			if (interestingClassNames.contains(e.getKey())) {
				System.out.println(e);
			}
		}
		System.out.println("============================================================");
	}

	private static void createDictionary() {
		obfuscationDictionary = new HashMap<>();
		Iterator<String> wordsIt = WORDS.iterator();
		for (Class<?> c : visited) {
			String replacement = wordsIt.next();
			String firstLetter = replacement.substring(0, 1);
			replacement = replacement.replaceFirst(firstLetter, firstLetter.toUpperCase());

			obfuscationDictionary.put(c.getSimpleName(), replacement);
		}
	}

	public static void main(String[] args) {

		for (Class<?> c : CLASSES) {
			collectHierarchy(c);
		}

		obfuscate();

		System.out.println(result);
	}
}
