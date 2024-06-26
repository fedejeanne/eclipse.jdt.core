/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

public class EnumSelectionTest extends AbstractSelectionTest {
public EnumSelectionTest(String testName) {
	super(testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85379
public void test0001() {

	String str =
		"""
		public class X {	\t
		  void foo() {							\t
		    switch(e) {  						\t
		      case A:							\t
		        break;							\t
		    }        							\t
		  }        							\t
		}										\t
		""";

	String selectionStartBehind = "case ";
	String selectionEndBehind = "A";

	String expectedCompletionNodeToString = "<SelectOnName:A>";
	String completionIdentifier = "A";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      switch (e) {
		      case <SelectOnName:A> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "A";
	String testName = "<select test>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
