/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

/**
 * Import declaration AST node type.
 *
 * <pre>
 * ImportDeclaration:
 *    <b>import</b> [ <b>static</b> ] Name [ <b>.</b> <b>*</b> ] <b>;</b>
 * </pre>
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class ImportDeclaration extends ASTNode {

	/**
	 * The "name" structural property of this node type (child type: {@link Name}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(ImportDeclaration.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "onDemand" structural property of this node type (type: {@link Boolean}).
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor ON_DEMAND_PROPERTY =
		new SimplePropertyDescriptor(ImportDeclaration.class, "onDemand", boolean.class, MANDATORY); //$NON-NLS-1$

	/**
	 * The "static" structural property of this node type (type: {@link Boolean}) (added in JLS3 API).
	 * @since 3.1
	 */
	public static final SimplePropertyDescriptor STATIC_PROPERTY =
		new SimplePropertyDescriptor(ImportDeclaration.class, "static", boolean.class, MANDATORY); //$NON-NLS-1$

	/**
	 * The "modifiers" structural property of this node type (element type: {@link IExtendedModifier}) (added in JLS23 API).
	 * @since 3.40
	 * @noreference preview feature
	 */
	public static final ChildListPropertyDescriptor MODIFIERS_PROPERTY =
			new ChildListPropertyDescriptor(ImportDeclaration.class, "modifiers", IExtendedModifier.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.1
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.39
	 */
	private static final List PROPERTY_DESCRIPTORS_23;

	static {
		List properyList = new ArrayList(3);
		createPropertyList(ImportDeclaration.class, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(ON_DEMAND_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(properyList);

		properyList = new ArrayList(4);
		createPropertyList(ImportDeclaration.class, properyList);
		addProperty(STATIC_PROPERTY, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(ON_DEMAND_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(properyList);

		properyList = new ArrayList(5);
		createPropertyList(ImportDeclaration.class, properyList);
		addProperty(STATIC_PROPERTY, properyList);
		addProperty(MODIFIERS_PROPERTY, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(ON_DEMAND_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_23 = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel >= AST.JLS23_INTERNAL) {
			return PROPERTY_DESCRIPTORS_23;
		} else if (apiLevel >= AST.JLS3_INTERNAL) {
			return PROPERTY_DESCRIPTORS_3_0;
		} else {
			return PROPERTY_DESCRIPTORS_2_0;
		}
	}

	/**
	 * The import name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private volatile Name importName;

	/**
	 * On demand versus single type import; defaults to single type import.
	 */
	private boolean onDemand = false;

	/**
	 * Static versus regular; defaults to regular import.
	 * Added in JLS3; not used in JLS2.
	 * @since 3.1
	 */
	private boolean isStatic = false;

	/**
	 * The extended modifiers (element type: {@link IExtendedModifier}).
	 * Added in JLS23; defaults to an empty list
	 * (see constructor).
	 * @since 3.39
	 */
	private ASTNode.NodeList modifiers = null;

	/**
	 * Creates a new AST node for an import declaration owned by the
	 * given AST. The import declaration initially is a regular (non-static)
	 * single type import for an unspecified, but legal, Java type name.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	ImportDeclaration(AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.JLS23_INTERNAL) {
			this.modifiers = new ASTNode.NodeList(MODIFIERS_PROPERTY);
		}
	}

	/**
	 * Returns the live ordered list of modifiers of this declaration (added in JLS23 API).
	 *
	 * @return the live list of modifiers (element type: {@link IExtendedModifier})
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST below JLS23
	 * @since 3.39
	 * @noreference preview feature
	 */
	public List modifiers() {
		if (this.ast.apiLevel < AST.JLS23_INTERNAL)
			throw new UnsupportedOperationException("Operation not supported in AST below JLS23"); //$NON-NLS-1$
		return this.modifiers;
	}

	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * This method is a convenience method that computes these flags based on availability:
	 * </p>
	 * <ul>
	 * <li>At JLS23 it is computed from from {@link #modifiers()}.</li>
	 * <li>At JLS3 only the information from {@link #isStatic()} is available.</li>
	 * <li>At lower JLS {@code 0} is constantly returned.
	 * </ul>
	 *
	 * @return the bit-wise "or" of <code>Modifier</code> constants
	 * @see Modifier
	 * @since 3.39
	 * @noreference preview feature
	 */
	public int getModifiers() {
		if (this.modifiers == null) {
			// JLS3 behavior (for JLS2 this is constantly 0)
			return this.isStatic ? Modifier.STATIC : Modifier.NONE;
		}
		// JLS23 behavior - convenience method
		// performance could be improved by caching computed flags
		// but this would require tracking changes to this.modifiers
		int computedmodifierFlags = Modifier.NONE;
		for (Object x : modifiers()) {
			if (x instanceof Modifier modifier) {
				computedmodifierFlags |= modifier.getKeyword().toFlagValue();
			}
		}
		return computedmodifierFlags;
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == ON_DEMAND_PROPERTY) {
			if (get) {
				return isOnDemand();
			} else {
				setOnDemand(value);
				return false;
			}
		}
		if (property == STATIC_PROPERTY) {
			if (get) {
				return isStatic();
			} else {
				setStatic(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Name) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == MODIFIERS_PROPERTY) {
			return modifiers();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final int getNodeType0() {
		return IMPORT_DECLARATION;
	}

	@SuppressWarnings("unchecked")
	@Override
	ASTNode clone0(AST target) {
		ImportDeclaration result = new ImportDeclaration(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setOnDemand(isOnDemand());
		if (this.ast.apiLevel >= AST.JLS3_INTERNAL) {
			result.setStatic(isStatic());
		}
		if (this.ast.apiLevel >= AST.JLS23_INTERNAL) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		}
		result.setName((Name) getName().clone(target));
		return result;
	}

	@Override
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getName());
			if (this.ast.apiLevel >= AST.JLS23_INTERNAL) {
				acceptChildren(visitor, this.modifiers);
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the name imported by this declaration.
	 * <p>
	 * For a regular on-demand import, this is the name of a package.
	 * For a static on-demand import, this is the qualified name of
	 * a type. For a regular single-type import, this is the qualified name
	 * of a type. For a static single-type import, this is the qualified name
	 * of a static member of a type.
	 * </p>
	 *
	 * @return the imported name node
	 */
	public Name getName()  {
		if (this.importName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.importName == null) {
					preLazyInit();
					this.importName = postLazyInit(
							this.ast.newQualifiedName(new SimpleName(this.ast), new SimpleName(this.ast)),
							NAME_PROPERTY);
				}
			}
		}
		return this.importName;
	}

	/**
	 * Sets the name of this import declaration to the given name.
	 * <p>
	 * For a regular on-demand import, this is the name of a package.
	 * For a static on-demand import, this is the qualified name of
	 * a type. For a regular single-type import, this is the qualified name
	 * of a type. For a static single-type import, this is the qualified name
	 * of a static member of a type.
	 * </p>
	 *
	 * @param name the new import name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.importName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.importName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/**
	 * Returns whether this import declaration is an on-demand or a
	 * single-type import.
	 *
	 * @return <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */
	public boolean isOnDemand() {
		return this.onDemand;
	}

	/**
	 * Sets whether this import declaration is an on-demand or a
	 * single-type import.
	 *
	 * @param onDemand <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */
	public void setOnDemand(boolean onDemand) {
		preValueChange(ON_DEMAND_PROPERTY);
		this.onDemand = onDemand;
		postValueChange(ON_DEMAND_PROPERTY);
	}

	/**
	 * Returns whether this import declaration is a static import (added in JLS3 API).
	 *
	 * @return <code>true</code> if this is a static import,
	 *    and <code>false</code> if this is a regular import
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public boolean isStatic() {
		if (this.modifiers != null) {
			// JLS23 behavior: extract from the list of Modifier
			for (Object x : modifiers()) {
				if (x instanceof Modifier modifier && modifier.isStatic()) {
					return true;
				}
			}
			return false;
		}
		unsupportedIn2();
		return this.isStatic;
	}

	/**
	 * Sets whether this import declaration is a static import (added in JLS3 API).
	 *
	 * Note, that in JLS23 API this method creates a {@link Modifier} without source positions
	 * (or removes, if {@code isStatic == false}), so it should not be invoked in situations where
	 * valid source positions are required.
	 *
	 * @param isStatic <code>true</code> if this is a static import,
	 *    and <code>false</code> if this is a regular import
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 *
	 * @see #modifiers()
	 */
	@SuppressWarnings("unchecked")
	public void setStatic(boolean isStatic) {
		if (this.ast.apiLevel >= AST.JLS23_INTERNAL) {
			List<Modifier> mods = modifiers();
			for (Modifier mod : mods) {
				if (mod.isStatic()) {
					if (!isStatic)
						this.modifiers.remove(mod);
					return;
				}
			}
			if (isStatic) {
				Modifier newMod = this.ast.newModifier(ModifierKeyword.STATIC_KEYWORD);
				this.modifiers.add(newMod);
			}
			return;
		}
		unsupportedIn2();
		preValueChange(STATIC_PROPERTY);
		this.isStatic = isStatic;
		postValueChange(STATIC_PROPERTY);
	}

	/**
	 * Resolves and returns the binding for the package, type, field, or
	 * method named in this import declaration.
	 * <p>
	 * The name specified in a non-static single-type import can resolve
	 * to a type (only). The name specified in a non-static on-demand
	 * import can itself resolve to either a package or a type.
	 * For static imports (introduced in JLS3), the name specified in a
	 * static on-demand import can itself resolve to a type (only).
	 * The name specified in a static single import can resolve to a
	 * type, field, or method; in cases where the name could be resolved
	 * to more than one element with that name (for example, two
	 * methods both named "max", or a method and a field), this method
	 * returns one of the plausible bindings.
	 * </p>
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return a package, type, field, or method binding, or <code>null</code>
	 * if the binding cannot be resolved
	 */
	public IBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveImport(this);
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.importName == null ? 0 : getName().treeSize());
	}
}

