/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *							bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *							bug 292478 - Report potentially null across variable assignment
 *							bug 335093 - [compiler][null] minimal hook for future null annotation support
 *							bug 349326 - [1.7] new warning for missing try-with-resources
 *							bug 186342 - [compiler][null] Using annotations for null checking
 *							bug 358903 - Filter practically unimportant resource leak warnings
 *							bug 370639 - [compiler][resource] restore the default for resource leak warnings
 *							bug 365859 - [compiler][null] distinguish warnings based on flow analysis vs. null annotations
 *							bug 388996 - [compiler][resource] Incorrect 'potential resource leak'
 *							bug 394768 - [compiler][resource] Incorrect resource leak warning when creating stream in conditional
 *							bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *							bug 383368 - [compiler][null] syntactic null analysis for field references
 *							bug 400761 - [compiler][null] null may be return as boolean without a diagnostic
 *							Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *							Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *							Bug 427438 - [1.8][compiler] NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode(ConditionalExpression.java:280)
 *							Bug 430150 - [1.8][null] stricter checking against type variables
 *							Bug 453483 - [compiler][null][loop] Improve null analysis for loops
 *     Jesper S Moller - Contributions for
 *							Bug 378674 - "The method can be declared as static" is wrong
 *							Bug 527554 - [18.3] Compiler support for JEP 286 Local-Variable Type
 *							Bug 529556 - [18.3] Add content assist support for 'var' as a type
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *							Bug 409250 - [1.8][compiler] Various loose ends in 308 code generation
 *							Bug 426616 - [1.8][compiler] Type Annotations, multiple problems
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.VANILLA_CONTEXT;

import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationContext;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;

public class LocalDeclaration extends AbstractVariableDeclaration {

	public LocalVariableBinding binding;

	public LocalDeclaration(
		char[] name,
		int sourceStart,
		int sourceEnd) {

		this.name = name;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.declarationEnd = sourceEnd;
	}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// record variable initialization if any
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) {
		this.bits |= ASTNode.IsLocalDeclarationReachable; // only set if actually reached
	}
	if (this.initialization == null) {
		if (this.binding != null && this.binding.isPatternVariable())
			if (!this.binding.declaration.isUnnamed(currentScope))
				this.bits |= FirstAssignmentToLocal;
		return flowInfo;
	}
	this.initialization.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);

	FlowInfo preInitInfo = null;
	CompilerOptions compilerOptions = currentScope.compilerOptions();
	boolean shouldAnalyseResource = this.binding != null
			&& flowInfo.reachMode() == FlowInfo.REACHABLE
			&& compilerOptions.analyseResourceLeaks
			&& FakedTrackingVariable.isAnyCloseable(this.initialization.resolvedType);
	if (shouldAnalyseResource) {
		preInitInfo = flowInfo.unconditionalCopy();
		// analysis of resource leaks needs additional context while analyzing the RHS:
		FakedTrackingVariable.preConnectTrackerAcrossAssignment(this, this.binding, this.initialization, flowInfo,
				compilerOptions.isAnnotationBasedResourceAnalysisEnabled);
	}

	flowInfo =
		this.initialization
			.analyseCode(currentScope, flowContext, flowInfo)
			.unconditionalInits();

	if (shouldAnalyseResource)
		FakedTrackingVariable.handleResourceAssignment(currentScope, preInitInfo, flowInfo, flowContext, this, this.initialization, this.binding);
	else
		FakedTrackingVariable.cleanUpAfterAssignment(currentScope, Binding.LOCAL, this.initialization);

	int nullStatus = this.initialization.nullStatus(flowInfo, flowContext);
	if (!flowInfo.isDefinitelyAssigned(this.binding)){// for local variable debug attributes
		this.bits |= FirstAssignmentToLocal;
	} else {
		this.bits &= ~FirstAssignmentToLocal;  // int i = (i = 0);
	}
	flowInfo.markAsDefinitelyAssigned(this.binding);
	if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
		nullStatus = NullAnnotationMatching.checkAssignment(currentScope, flowContext, this.binding, flowInfo, nullStatus, this.initialization, this.initialization.resolvedType);
	}
	if ((this.binding.type.tagBits & TagBits.IsBaseType) == 0) {
		flowInfo.markNullStatus(this.binding, nullStatus);
		// no need to inform enclosing try block since its locals won't get
		// known by the finally block
	}
	return flowInfo;
}

	public void checkModifiers() {

		//only potential valid modifier is <<final>>
		if (((this.modifiers & ExtraCompilerModifiers.AccJustFlag) & ~ClassFileConstants.AccFinal) != 0)
			//AccModifierProblem -> other (non-visibility problem)
			//AccAlternateModifierProblem -> duplicate modifier
			//AccModifierProblem | AccAlternateModifierProblem -> visibility problem"

			this.modifiers &= ~ExtraCompilerModifiers.AccAlternateModifierProblem;
	}

	/**
	 * Code generation for a local declaration:
	 *	i.e.&nbsp;normal assignment to a local variable + unused variable handling
	 */
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// even if not reachable, variable must be added to visible if allocated (28298)
		if (this.binding.resolvedPosition != -1) {
			codeStream.addVisibleLocalVariable(this.binding);
		}
		if ((this.bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;

		// something to initialize?
		generateInit: {
			if (this.initialization == null && !this.binding.isPatternVariable())
				break generateInit;
			if (this.initialization != null) {
				// forget initializing unused or final locals set to constant value (final ones are inlined)
				if (this.binding.resolvedPosition < 0) {
					if (this.initialization.constant != Constant.NotAConstant)
						break generateInit;
					// if binding unused generate then discard the value
					this.initialization.generateCode(currentScope, codeStream, false);
					break generateInit;
				}
				this.initialization.generateCode(currentScope, codeStream, true);
				// 26903, need extra cast to store null in array local var
				if (this.binding.type.isArrayType()
					&& ((this.initialization instanceof CastExpression)	// arrayLoc = (type[])null
							&& (((CastExpression)this.initialization).innermostCastedExpression().resolvedType == TypeBinding.NULL))){
						codeStream.checkcast(this.binding.type);
				}
			}
			codeStream.store(this.binding, false);
			if ((this.bits & ASTNode.FirstAssignmentToLocal) != 0) {
				/* Variable may have been initialized during the code initializing it
					e.g. int i = (i = 1);
				*/
				this.binding.recordInitializationStartPC(codeStream.position);
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	@Override
	public int getKind() {
		return LOCAL_VARIABLE;
	}

    @Override
	public void getAllAnnotationContexts(int targetType, LocalVariableBinding localVariable, List<AnnotationContext> allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, localVariable, allAnnotationContexts);
		this.traverseWithoutInitializer(collector, (BlockScope) null);
	}

	// for arguments
	@Override
	public void getAllAnnotationContexts(int targetType, int parameterIndex, List<AnnotationContext> allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, parameterIndex, allAnnotationContexts);
		this.traverse(collector, (BlockScope) null);
	}

	public boolean isReceiver() {
		return false;
	}

	public TypeBinding patchType(BlockScope scope, TypeBinding newType) {
		if (TypeBinding.equalsEquals(TypeBinding.NULL, newType))
			scope.problemReporter().varLocalInitializedToNull(this);
		else if (TypeBinding.equalsEquals(TypeBinding.VOID, newType))
			scope.problemReporter().varLocalInitializedToVoid(this);
		// Perform upwards projection on type wrt mentioned type variables
		TypeBinding[] mentionedTypeVariables = newType != null ? newType.syntheticTypeVariablesMentioned() : null;
		if (mentionedTypeVariables != null && mentionedTypeVariables.length > 0) {
			newType = newType.upwardsProjection(this.binding.declaringScope, mentionedTypeVariables);
		}
		this.type.resolvedType = newType;
		if (this.binding != null) {
			this.binding.type = newType;
		}
		return this.type.resolvedType;
	}

	@Override
	public void resolve(BlockScope scope) {

		handleNonNullByDefault(scope, this.annotations, this); // prescan NNBD

		final boolean isPatternVariable = (this.bits & ASTNode.IsPatternVariable) != 0;
		final boolean isForeachElementVariable = (this.bits & ASTNode.IsForeachElementVariable) != 0;
		final boolean varTypedLocal = isVarTyped(scope);
		final boolean unnamedLocal = isUnnamed(scope);

		TypeBinding variableType = null;

		if (varTypedLocal) {
			if ((this.bits & ASTNode.IsAdditionalDeclarator) != 0)
				scope.problemReporter().varLocalMultipleDeclarators(this);
			if (isPatternVariable)
				variableType = this.type.resolvedType; // set already if this is a component pattern or is problem binding otherwise as it should be.
		} else {
			variableType = this.type == null ? null : this.type.resolveType(scope, true /* check bounds*/);
		}

		if (this.type != null) {
			this.bits |= (this.type.bits & ASTNode.HasTypeAnnotations);
			checkModifiers();
			if (variableType != null) {
				if (variableType == TypeBinding.VOID) {
					scope.problemReporter().variableTypeCannotBeVoid(this);
					return;
				}
				if (variableType.isArrayType() && ((ArrayBinding) variableType).leafComponentType == TypeBinding.VOID) {
					scope.problemReporter().variableTypeCannotBeVoidArray(this);
					return;
				}
			}

			Binding existingVariable = scope.getBinding(this.name, Binding.VARIABLE, this, false /*do not resolve hidden field*/);
			if (existingVariable != null && existingVariable.isValidBinding() && !unnamedLocal) {
				boolean localExists = existingVariable instanceof LocalVariableBinding;
				if (localExists && (this.bits & ASTNode.ShadowsOuterLocal) != 0 && scope.isLambdaSubscope() && this.hiddenVariableDepth == 0) {
					scope.problemReporter().lambdaRedeclaresLocal(this);
				} else if (localExists && this.hiddenVariableDepth == 0) {
					if (existingVariable.isPatternVariable()) {
						scope.problemReporter().illegalRedeclarationOfPatternVar((LocalVariableBinding) existingVariable, this);
					} else {
						scope.problemReporter().redefineLocal(this);
					}
				} else {
					scope.problemReporter().localVariableHiding(this, existingVariable, false);
				}
			}
		}
		if ((this.modifiers & ClassFileConstants.AccFinal) != 0 && this.initialization == null) {
			this.modifiers |= ExtraCompilerModifiers.AccBlankFinal;
		}
		// Create a binding from the specified type; In order to diagnose self-referential initializers,
		// we must create the binding with jlO as a placeholder type and patch it later
		this.binding = new LocalVariableBinding(this, varTypedLocal && variableType == null ? scope.getJavaLangObject() : variableType, this.modifiers, false /*isArgument*/);
		if (isPatternVariable)
			this.binding.tagBits |= TagBits.IsPatternBinding;
		scope.addLocalVariable(this.binding);
		this.binding.setConstant(Constant.NotAConstant);
		// allow to recursively target the binding....
		// the correct constant is harmed if correctly computed at the end of this method

		boolean resolveAnnotationsEarly = false;
		if (scope.environment().usesNullTypeAnnotations()
				&& !varTypedLocal // 'var' does not provide a target type
				&& variableType != null && variableType.isValidBinding()) {
			resolveAnnotationsEarly = this.initialization instanceof Invocation
					|| this.initialization instanceof ConditionalExpression
					|| this.initialization instanceof SwitchExpression
					|| this.initialization instanceof ArrayInitializer;
		}
		if (resolveAnnotationsEarly) {
			// these are definitely no constants, so resolving annotations early should be safe
			resolveAnnotations(scope, this.annotations, this.binding, true);
			// for type inference having null annotations upfront gives better results
			variableType = this.type.resolvedType;
		}
		if (this.initialization != null) {
			if (this.initialization instanceof ArrayInitializer) {
				if (varTypedLocal) {
					scope.problemReporter().varLocalCannotBeArrayInitalizers(this);
					variableType = scope.createArrayType(scope.getJavaLangObject(), 1); // Treat as array of anything
				}
				TypeBinding initializationType = this.initialization.resolveTypeExpecting(scope, variableType);
				if (initializationType != null) {
					((ArrayInitializer) this.initialization).binding = (ArrayBinding) initializationType;
					this.initialization.computeConversion(scope, variableType, initializationType);
				}
			} else {
				if (varTypedLocal) {
					this.binding.useFlag = LocalVariableBinding.ILLEGAL_SELF_REFERENCE_IF_USED; // hijack analysis phase flag
					if (this.initialization instanceof CastExpression castExpression)
						castExpression.setVarTypeDeclaration(true);
				}
				this.initialization.setExpressionContext(varTypedLocal ? VANILLA_CONTEXT : ASSIGNMENT_CONTEXT);
				this.initialization.setExpectedType(variableType);
				TypeBinding initializationType = this.initialization.resolveType(scope);
				if (varTypedLocal)
					this.binding.useFlag = LocalVariableBinding.UNUSED; // hand-over hijacked flag; let flow analysis do what it does with it.

				if ((variableType == null && !varTypedLocal) || // having waited until any additional errors in initializer are surfaced, bail out
						(initializationType == null && varTypedLocal)) // fubar, bail out.
					return;

				if (initializationType != null) {
					if (varTypedLocal) {
						variableType = patchType(scope, this.initialization.resolvedType);
					}
					if (TypeBinding.notEquals(variableType, initializationType)) // must call before computeConversion() and typeMismatchError()
						scope.compilationUnitScope().recordTypeConversion(variableType, initializationType);
					if (this.initialization.isConstantValueOfTypeAssignableToType(initializationType, variableType)
						|| initializationType.isCompatibleWith(variableType, scope)) {
						this.initialization.computeConversion(scope, variableType, initializationType);
						if (initializationType.needsUncheckedConversion(variableType)) {
						    scope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, variableType);
						}
						if (this.initialization instanceof CastExpression
								&& (this.initialization.bits & ASTNode.UnnecessaryCast) == 0) {
							CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression) this.initialization);
						}
					} else if (isBoxingCompatible(initializationType, variableType, this.initialization, scope)) {
						this.initialization.computeConversion(scope, variableType, initializationType);
						if (this.initialization instanceof CastExpression
								&& (this.initialization.bits & ASTNode.UnnecessaryCast) == 0) {
							CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression) this.initialization);
						}
					} else {
						if ((variableType.tagBits & TagBits.HasMissingType) == 0) {
							// if problem already got signaled on type, do not report secondary problem
							scope.problemReporter().typeMismatchError(initializationType, variableType, this.initialization, null);
						}
					}
				}
			}
			// check for assignment with no effect
			if (this.binding == Expression.getDirectBinding(this.initialization)) {
				scope.problemReporter().assignmentHasNoEffect(this, this.name);
			}
			// change the constant in the binding when it is final
			// (the optimization of the constant propagation will be done later on)
			// cast from constant actual type to variable type
			this.binding.setConstant(
				this.binding.isFinal()
					? this.initialization.constant.castTo((variableType.id << 4) + this.initialization.constant.typeID())
					: Constant.NotAConstant);
		} else if (!isPatternVariable && !isForeachElementVariable) {
			if (varTypedLocal)
				scope.problemReporter().varLocalWithoutInitizalier(this);
			if (unnamedLocal)
				scope.problemReporter().unnamedVariableMustHaveInitializer(this);
		}
		// if init could be a constant only resolve annotation at the end, for constant to be positioned before (96991)
		if (!resolveAnnotationsEarly)
			resolveAnnotations(scope, this.annotations, this.binding, true);
		Annotation.isTypeUseCompatible(this.type, scope, this.annotations);
		validateNullAnnotations(scope);
	}

	void validateNullAnnotations(BlockScope scope) {
		if (!scope.validateNullAnnotation(this.binding.tagBits, this.type, this.annotations))
			this.binding.tagBits &= ~TagBits.AnnotationNullMASK;
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			if (this.initialization != null)
				this.initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	private void traverseWithoutInitializer(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			this.type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	public boolean isRecoveredFromLoneIdentifier() { // recovered from lonely identifier or identifier cluster ?
		return this.name == RecoveryScanner.FAKE_IDENTIFIER &&
				(this.type instanceof SingleTypeReference || (this.type instanceof QualifiedTypeReference && !(this.type instanceof ArrayQualifiedTypeReference))) && this.initialization == null && !this.type.isBaseTypeReference();
	}

	@Override
	public LocalVariableBinding getBinding() {
		return this.binding;
	}

	@Override
	public void setBinding(Binding binding) {
		this.binding = (LocalVariableBinding) binding;
	}

}
