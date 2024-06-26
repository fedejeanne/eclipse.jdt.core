/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
*
https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

import junit.framework.Test;

public class ResolveTests21 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	 TESTS_NAMES = new String[] { "testBug577508_4" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests21.class);
}
public ResolveTests21(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("Resolve", "21", false);
	waitUntilIndexesReady();
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	super.tearDownSuite();
}

@Override
protected void tearDown() throws Exception {
	if (this.wc != null) {
		this.wc.discardWorkingCopy();
	}
	super.tearDown();
}

public void test001() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				public class X {
				  private String abc = "abc"; // unused
				  public void main(String[] args) {
				    String s = STR."A simple String \\{clone(abc)}";
				    System.out.println(s);
				  }
				  public String clone(String s) {
				    return "clone";
				  }
				}""");
	String str = this.wc.getSource();
	String selection = "clone";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"clone(String) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test002() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				public class X {
				  private String abc = "abc"; // unused
				  public void main(String[] args) {
				    String s = STR."A simple String \\{clone(abc)}";
				    System.out.println(s);
				  }
				  public String clone(String s) {
				    return "clone";
				  }
				}""");
	String str = this.wc.getSource();
	String selection = "abc";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abc [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test003() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				public class X {
				  static int CONST = 0;
				    private static int foo() {
				    return CONST;
				  }
				  public static void main(String argv[]) {
				    String str = STR."{\\{new Object() { class Test { int i; Test() { i = foo();}}}.new Test().i\\u007d}";
				    System.out.println(str.equals("{0}"));
				  }
				}""");
	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test004() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				public class X {
				    private final static int LF  = (char) 0x000A;
				    private static boolean compare(String s) {
				        char[] chars = new char[] {LF,'a','b','c','d'};
				        if (chars.length != s.length())
				            return false;
				        for (int i = 0; i < s.length(); i++) {
				            if(chars[i] != s.charAt(i)) {
				                return false;
				            }
				        }
				        return true;
				    }
				    public static void main(String argv[]) {
				        String abcd = "abcd"; //$NON-NLS-1$
				        String textBlock = STR.\"""
				  \s
				\\{abcd}\""";//$NON-NLS-1$
				        System.out.println(compare(textBlock));
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "abcd";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abcd [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
}
