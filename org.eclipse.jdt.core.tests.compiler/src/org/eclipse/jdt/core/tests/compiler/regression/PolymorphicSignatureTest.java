/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class PolymorphicSignatureTest extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "testBug515863" };
	}
	public PolymorphicSignatureTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}
	public static Class testClass() {
		return PolymorphicSignatureTest.class;
	}

	public void test0001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.lang.invoke.*;
					public class X {
					   public static void main(String[] args) throws Throwable{
					      MethodType mt; MethodHandle mh;\s
					      MethodHandles.Lookup lookup = MethodHandles.lookup();
					      mt = MethodType.methodType(String.class, char.class, char.class);
					      mh = lookup.findVirtual(String.class, "replace", mt);
					      String s = (String) mh.invokeExact("daddy",'d','n');
					      System.out.println(s);
					   }
					}
					"""
			},
			"nanny");
	}
	public void test0002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import static java.lang.invoke.MethodHandles.*;\s
					import java.lang.invoke.MethodHandle;
					public class X {
						public static void main(String[] args) throws Throwable {
							MethodHandle mh = dropArguments(insertArguments(identity(int.class), 0, 42), 0, Object[].class);
							int value = (int)mh.invokeExact(new Object[0]);
							System.out.println(value);
						}
					}"""
			},
			"42");
	}
	public void testBug515863() {
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.util.ArrayList;
					import java.util.Collections;
					
					public class Test {
					\t
						public void foo() throws Throwable {
						\t
							MethodHandle mh = null;
							mh.invoke(null);                           // works, no issues.
							mh.invoke(null, new ArrayList<>());        // Bug 501457 fixed this
							mh.invoke(null, Collections.emptyList());  // This triggers UOE
						\t
						}
					}
					"""
			});
	}
	public void testBug475996() {
		if (!isJRE9Plus)
			return; // VarHandle is @since 9
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.VarHandle;\n" +
				"public class X<T> {\n" +
				"	static class Token {}\n" +
				"	Token NIL = new Token();\n" +
				"	VarHandle RESULT;\n" +
				"	void call(T t) {\n" +
				"		RESULT.compareAndSet(this, null, (t==null) ? NIL : t);\n" +
				"	}\n" +
				"" +
				"}\n"
			});
	}
}
