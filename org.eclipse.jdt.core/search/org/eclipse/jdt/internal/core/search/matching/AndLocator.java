/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class AndLocator extends PatternLocator {

final PatternLocator[] patternLocators;
final int[] levels;

public AndLocator(AndPattern pattern) {
	super(pattern);

	SearchPattern[] patterns = pattern.patterns;
	PatternLocator[] locators = new PatternLocator[patterns.length];
	this.levels = new int[patterns.length];
	for (int i=0, l=patterns.length; i<l; i++) {
		locators[i] = PatternLocator.patternLocator(patterns[i]);
		this.levels[i] = IMPOSSIBLE_MATCH;
	}
	this.patternLocators = locators;
}
@Override
public void initializePolymorphicSearch(MatchLocator locator) {
	for (PatternLocator patternLocator : this.patternLocators) {
		patternLocator.initializePolymorphicSearch(locator);
	}
}
@Override
public int match(Annotation node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(ASTNode node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(Expression node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(FieldDeclaration node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(LambdaExpression node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(LocalDeclaration node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(MethodDeclaration node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(MemberValuePair node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(MessageSend node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(Reference node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(ReferenceExpression node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(TypeDeclaration node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(TypeParameter node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
public int match(TypeReference node, MatchingNodeSet nodeSet) {
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.match(node, nodeSet);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
@Override
protected int matchContainer() {
	int result = ALL_CONTAINER;
	for (PatternLocator patternLocator : this.patternLocators) {
		result &= patternLocator.matchContainer();
	}
	return result;
}
@Override
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	PatternLocator weakestPattern = null;
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		int newLevel = patternLocator.matchLevel(importRef);
		if (newLevel == IMPOSSIBLE_MATCH) return;
		if (weakestPattern == null || newLevel < level) {
			weakestPattern = patternLocator;
			level = newLevel;
		}
	}
	weakestPattern.matchReportImportRef(importRef, binding, element, accuracy, locator);
}
@Override
protected void matchReportReference(ASTNode reference, IJavaElement element, IJavaElement localElement, IJavaElement[] otherElements, Binding elementBinding, int accuracy, MatchLocator locator) throws CoreException {
	PatternLocator weakestPattern = null;
	int level = IMPOSSIBLE_MATCH;
	for (PatternLocator patternLocator : this.patternLocators) {
		if (patternLocator.referenceType() == 0) return; // impossible match
		int newLevel = patternLocator.resolveLevel(reference);
		if (newLevel == IMPOSSIBLE_MATCH) return;
		if (weakestPattern == null || newLevel < level) {
			weakestPattern = patternLocator;
			level = newLevel;
		}
	}
	weakestPattern.matchReportReference(reference, element, localElement, otherElements, elementBinding, accuracy, locator);
}
@Override
protected void matchReportReference(ASTNode reference, IJavaElement element, Binding elementBinding, int accuracy, MatchLocator locator) throws CoreException {
	matchReportReference(reference, element, null, null, elementBinding, accuracy, locator);
}
@Override
public int resolveLevel(ASTNode node) {
	int level = ACCURATE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].resolveLevel(node);
		if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
		this.levels[i] = newLevel;
		if (newLevel < level) {
			level = newLevel; // want to answer the weaker match
		}
	}
	return level;
}
@Override
public int resolveLevel(Binding binding) {
	int level = ACCURATE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].resolveLevel(binding);
		if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
		this.levels[i] = newLevel;
		if (newLevel < level) {
			level = newLevel; // want to answer the weaker match
		}
	}
	return level;
}

@Override
void setFlavors(int flavors) {
	for (PatternLocator patternLocator : this.patternLocators) {
		patternLocator.setFlavors(flavors);
	}
}
@Override
public void recordResolution(QualifiedTypeReference typeReference, TypeBinding resolution) {
	for (PatternLocator patternLocator : this.patternLocators) {
		patternLocator.recordResolution(typeReference, resolution);
	}
}
}
