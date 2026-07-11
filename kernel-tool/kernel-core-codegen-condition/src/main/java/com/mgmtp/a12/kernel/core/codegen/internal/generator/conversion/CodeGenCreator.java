/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */

package com.mgmtp.a12.kernel.core.codegen.internal.generator.conversion;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.ApplicationCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.ComparisonCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CompositeCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.DateValidityCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListAndValueConditionFieldList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListAndValueConditionMixedList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListAndValueConditionStringList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.PredefinedTypeValidityCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.RootGroupCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.ValueListCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.BoolConstantOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CompositeOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.ConstantOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.EntityListOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.EntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.FieldIdOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.FieldIdOperationWithParameters;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.GroupOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.NumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.NumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.OpListOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.OpListOperationWithParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.PointInTimeOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.SetOpOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateAddOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateConstructionOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateDiffOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateExtractionOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateRangeConstructionOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateRangeExtractionOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateRangeExtractionOperationBaseYear;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.DateTimeConstructionOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date.TimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IDateValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IPredefinedTypeValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IBoolConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperationWithParameters;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperationWithParameter;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IPointInTimeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateDiffOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateTimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.ITimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.DateRangeExtractionOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

/**
 * Kopiert eine IBedingung in die einfachen Strukturen, die die ParseBaum Interfaces Implementieren.
 * Die Klasse realisiert einen Visitor. Alle Knoten des Baums werden traversiert und in den
 * visit-Methoden auf den Stack (bedingungStack für Bedingungen, opStack für FeldOperationen)
 * gelegt. In den endVisit Methode für die zusammengesetzen Knoten des Baums werden die Teile vom
 * Stack geholt und in den neuen Objekten gesetzt.
 * <p>
 * Die Implementierung basiert auf der Implementierung der Traversierungsmethoden. Wenn diese sich
 * ändern, und sich dadurch die Reihenfolge ändert, in der die Objekte auf den Stack gelegt werden,
 * muss auch diese Implementierung angepasst werden.
 * </p>
 */
public class CodeGenCreator extends ASTVisitor {

	private final Deque<CodeGenCondition> bedingungsStack = new ArrayDeque<>();
	private final Deque<CodeGenOperation> opStack = new ArrayDeque<>();
	// Use LinkedList with addLast, removeLast to ease ordering of entities
	private final LinkedList<EntityId> entityIdDeq = new LinkedList<>();
	private final LinkedList<EntityIdList> entityIdListDeq = new LinkedList<>();
	private final Function<IEntityId, String> uniqueFieldNames;
	private final String uniqueErrorFieldName;
	private final String baseYear;
	private final IEC ec;
	private final boolean expandGroups;

	/**
	 * @param uniqueFieldNames
	 * 		the mapping from entity id to the used name in the code.
	 * @param uniqueErrorFieldName
	 * 		the unique name of the associated field for
	 * 		<ul>
	 * 		<li>the rule (error field),</li>
	 * 		<li>the calculation (calculated field)</li>
	 * 		</ul>
	 * 		This parameter is used for application conditions.
	 * @param baseYear
	 * 		the base year. Used for date constructions, where the base year has to be added.
	 * @param ec
	 * 		the {@link IEC} to ask fields and there types from.
	 */
	private CodeGenCreator(Function<IEntityId, String> uniqueFieldNames,
			String uniqueErrorFieldName,
			String baseYear,
			IEC ec,
			boolean expandGroups) {
		this.uniqueFieldNames = uniqueFieldNames;
		this.uniqueErrorFieldName = uniqueErrorFieldName;
		this.baseYear = baseYear;
		this.ec = ec;
		this.expandGroups = expandGroups;
	}

	/**
	 * Convert a condition to a code generator version to emit code.
	 * <p>
	 * In the process, small transformations are performed:
	 * <ul>
	 * <li>comparisons of [Yes-fields] != TRUE is transformed to FieldNotFilled(Yes-field)</li>
	 * <li>comparison of [Yes-field1] != [Yes-field2] is transformed to
	 * FieldsNotCollectivelyFilled(Yes-field1, Yes-field2)</li>
	 * </ul>
	 *
	 * @param condition
	 * 		the condition as parsed from a rule.
	 * @param uniqueFieldNames
	 * 		the mapping from entity id to the used name in the code.
	 * @param uniqueErrorFieldName
	 * 		the unique name of the associated field for
	 * 		<ul>
	 * 		<li>the rule (error field), or</li>
	 * 		<li>the calculation (calculated field).</li>
	 * 		</ul>
	 * 		This parameter is used for application conditions.
	 * @param baseYear
	 * 		the base year. Used for date constructions, where the base year has to be
	 * 		added.
	 * @param entityIdsCollector
	 * 		will collect entityIds and entityIdLists <em>and also, as a side-effect, in-place-assign unique
	 * 		varName to the converted EntityIds</em> (and lists?).
	 * @return a code generator condition able to emit code.
	 */
	public static CodeGenCondition convertCondition(ICondition condition, Function<IEntityId, String> uniqueFieldNames,
			String uniqueErrorFieldName, String baseYear, EntityIdsCollector entityIdsCollector, IEC ec, boolean expandGroups) {
		CodeGenCreator creator = new CodeGenCreator(uniqueFieldNames, uniqueErrorFieldName, baseYear, ec, expandGroups);
		condition.traverse(creator);
		CodeGenCondition codeGenCondition = creator.getBedingung();
		codeGenCondition.traverse(entityIdsCollector);
		return codeGenCondition;
	}

	/**
	 * Convert an operation to a code generator version to emit code.
	 *
	 * @param operation
	 * 		the operation as parsed from a rule.
	 * @param uniqueFieldNames
	 * 		the mapping from entity id to the used name in the code.
	 * @param uniqueErrorFieldName
	 * 		the unique name of the associated field for
	 * 		<ul>
	 * 		<li>the rule (error field), or</li>
	 * 		<li>the calculation (calculated field).</li>
	 * 		</ul>
	 * 		This parameter is used for application conditions.
	 * @param baseYear
	 * 		the base year. Used for date constructions, where the base year has to be added.
	 * @return a code generator operation able to emit code.
	 */
	public static CodeGenOperation convertOperation(IOperation operation,
			Function<IEntityId, String> uniqueFieldNames,
			String uniqueErrorFieldName,
			String baseYear,
			EntityIdsCollector entityIdsCollector,
			IEC ec,
			boolean expandGroups) {
		CodeGenCreator creator = new CodeGenCreator(uniqueFieldNames, uniqueErrorFieldName, baseYear, ec, expandGroups);
		operation.traverse(creator);
		CodeGenOperation op = creator.getOperation();
		op.traverse(entityIdsCollector);
		return op;
	}

	/**
	 * Convert an entity to a code generator version to emit code.
	 * <p>
	 * It appears that this method does not support filter conditions, i.e., if the origin entity has a filter condition that
	 * includes entity references, those will probably not be converted or collected.
	 *
	 * @param origin
	 * 		the original entity.
	 * @param uniqueFieldNames
	 * 		a function that creates the uniqueFieldName from the entity's name.
	 * @param entityIdsCollector
	 * 		will collect entityIds and entityIdLists <em>and also, as a side-effect, in-place-assign unique
	 * 		varName to the converted EntityIds</em> (and lists?).
	 * @return a code generator entity able to emit code.
	 */
	public static EntityId convertEntity(IEntityId origin, Function<IEntityId, String> uniqueFieldNames,
			EntityIdsCollector entityIdsCollector) {
		EntityId entityId = EntityId.build(origin, uniqueFieldNames);
		entityId.traverse(entityIdsCollector);
		return entityId;
	}

	/**
	 * Nach der Traversierung einer FeldOperation ist das Resultat als letzte Operation vorhanden.
	 * Wenn alles ohne Fehler bearbeitet wurde, ist der bedingungsStack leer und der Operationsstack
	 * enthält genau das Resultat.
	 *
	 * @return das Resultat der Traversierung einer Feldoperation.
	 */
	public CodeGenOperation getOperation() {
		assert bedingungsStack.isEmpty();
		assert opStack.size() == 1;
		assert entityIdListDeq.isEmpty();
		assert entityIdDeq.isEmpty();
		return opStack.pop();
	}

	/**
	 * Nach der Traversierung einer Bedingung ist das Resultat als letzte Bedingung vorhanden. Wenn
	 * alles ohne Fehler bearbeitet wurde, ist der opStack leer und der Bedingung-Stack hat genau
	 * das Resultat Objekt.
	 *
	 * @return gib das Resultat der Traversierung zurück.
	 */
	public CodeGenCondition getBedingung() {
		assert bedingungsStack.size() == 1;
		assert opStack.isEmpty();
		assert entityIdListDeq.isEmpty();
		assert entityIdDeq.isEmpty();
		return bedingungsStack.pop();
	}

	@Override
	public void endVisit(IApplicationCondition b) {
		bedingungsStack.push(new ApplicationCondition(b, uniqueErrorFieldName));
	}

	@Override
	public void endVisit(IEntityListCondition b) {
		EntityIdList entityIdList = entityIdListDeq.removeLast();
		EntityId additionalId = b.getAdditionalGroupReference()
				.map(e -> EntityId.build(e, uniqueFieldNames))
				.orElse(null);
		// Note: additionalId will be collected by the entityIdsCollector later, in the separate traversal
		// (via ASTVisitorr.endVisit(IEntityListCondition))
		bedingungsStack.push(new EntityListCondition(b.getConditionName(), entityIdList, additionalId));
	}

	@Override
	public void endVisit(IEntityListAndValueListCondition b) {
		EntityListAndValueListCondition cb;

		//we have different implementations for the different kind of value list
		if (!b.getValueListAsStringList().isEmpty()) {
			EntityIdList entityList = entityIdListDeq.removeLast();
			cb = new EntityListAndValueConditionStringList(b, entityList);
		} else if (!b.getValueListAsMixedList().isEmpty()) {
			EntityIdList entityList = entityIdListDeq.removeLast();
			List<Object> mixedList = new ArrayList<>(b.getValueListAsMixedList());
			// The entities are popped in reverse order
			Collections.reverse(mixedList);
			mixedList = mixedList.stream()
					.map(this::popMixedValue)
					.collect(Collectors.toList());
			Collections.reverse(mixedList);
			cb = new EntityListAndValueConditionMixedList(b, entityList, mixedList);
		} else {
			//values are fields
			EntityIdList valueList = entityIdListDeq.removeLast();
			EntityIdList entityList = entityIdListDeq.removeLast();
			cb = new EntityListAndValueConditionFieldList(b, entityList, valueList);
		}
		bedingungsStack.push(cb);
	}

	private Object popMixedValue(Object value) {
		if (value instanceof String) {
			return value;
		} else if (value instanceof IEntityId) {
			return entityIdDeq.removeLast();
		} else {
			throw new IllegalArgumentException(
					"Mixed value list with a value " + value + " of class " + (value == null ? "null" : value.getClass()));
		}
	}

	@Override
	public void endVisit(IEntityIdList p) {
		int firstElement = entityIdDeq.size() - p.getEntityIdList().size();
		// firstElement might not be 0. E.g. in SumOfTerms(G*, [Z] + Sum(G/Z*)), it would be 1 for the EntityIdList in the Sum
		// because G* would already be on the stack
		List<EntityId> entities = new ArrayList<>(entityIdDeq.subList(firstElement, entityIdDeq.size()));
		// Remove elements, that are now in entities. RemoveLastN would be nice.
		for (int i = 0; i < p.getEntityIdList().size(); i++) {
			entityIdDeq.removeLast();
		}
		entityIdListDeq.addLast(new EntityIdList(entities));
	}

	@Override
	public boolean visit(IEntityId p) {
		entityIdDeq.addLast(EntityId.build(p, uniqueFieldNames));
		// Semantic index is handled in EntityId.build
		return false;
	}

	/**
	 * Hole die rechte und die linke Seite der Vergleichsbedingung vom Stack und setze die Objekte
	 * im Vergleichs-Objekt.
	 */
	@Override
	public void endVisit(IComparisonCondition b) {
		CodeGenOperation op2 = opStack.pop();
		CodeGenOperation op1 = opStack.pop();

		if (b.getCmpOp() == ComparisonOperation.NE &&
				(isYesFieldOperation(op1) || isYesFieldOperation(op2))) {
			CodeGenCondition result = convertYesFieldComparison(op1, op2);
			bedingungsStack.push(result);
		} else {
			ComparisonCondition v = new ComparisonCondition(b, op1, op2);
			bedingungsStack.push(v);
		}
	}

	private CodeGenCondition convertYesFieldComparison(CodeGenOperation op1, CodeGenOperation op2) {
		if (!isYesFieldOperation(op1)) {
			assert op1 instanceof IBoolConstantOperation;
			EntityId field2 = ((FieldIdOperation) op2).getField();
			return createFieldListConditionForYesCmpConversion(EntityListConditionName.NoFieldFilled, field2);
		} else if (!isYesFieldOperation(op2)) {
			assert op2 instanceof IBoolConstantOperation;
			EntityId field1 = ((FieldIdOperation) op1).getField();
			return createFieldListConditionForYesCmpConversion(EntityListConditionName.NoFieldFilled, field1);
		} else {
			EntityId field1 = ((FieldIdOperation) op1).getField();
			EntityId field2 = ((FieldIdOperation) op2).getField();
			return createFieldListConditionForYesCmpConversion(EntityListConditionName.FieldsNotCollectivelyFilled, field1, field2);
		}
	}

	/**
	 * (doesn't support additionalGroupReference)
	 */
	private CodeGenCondition createFieldListConditionForYesCmpConversion(EntityListConditionName name, EntityId... entityIds) {
		EntityIdList fields = new EntityIdList(Arrays.asList(entityIds));
		return new EntityListCondition(name, fields, null);
	}

	private boolean isYesFieldOperation(IOperation operation) {
		if (!(operation instanceof IFieldIdOperation fieldIdOp)) {
			return false;
		}
		if (fieldIdOp.getOperatorName() != FieldIdOperationName.FieldValue) {
			return false;
		}
		IField f = (IField) ec.get(fieldIdOp.getField().getFullName());
		return FieldUtils.isYes(f);
	}

	@Override
	public void endVisit(IRootGroupCondition b) {
		IEntityId entity = null;
		if (b.getEntityId() != null) {
			entity = entityIdDeq.removeLast();
		}
		bedingungsStack.push(new RootGroupCondition(b, entity));
	}

	@Override
	public void endVisit(INumberOfUsbOperation op) {
		opStack.push(new NumberOfUsbOperation(op));
	}

	@Override
	public void endVisit(IDateRangeExtractionOperation op) {
		CodeGenOperation fop = opStack.pop();

		if (fop instanceof IPointInTimeOperation) { // BaseYear
			// the parser ensures that this language construct is only used for models with a base year
			String monatTag = op.getOperatorName() == DateRangeExtractionOperationName.StartOfDateRange ? "01.01." : "31.12.";
			opStack.push(new DateRangeExtractionOperationBaseYear(monatTag, baseYear));
		} else {
			assert fop instanceof IFieldIdOperation;
			FieldIdOperation fieldOp = (FieldIdOperation) fop;
			opStack.push(new DateRangeExtractionOperation(op, fieldOp));
		}
	}

	@Override
	public void endVisit(IEntityListOperation op) {
		EntityIdList entityIdList = entityIdListDeq.removeLast();
		opStack.push(new EntityListOperation(op, entityIdList, expandGroups, ec));
	}

	@Override
	public void endVisit(IEntityListOperationWithValue op) {
		EntityIdList entityIdList = entityIdListDeq.removeLast();
		CodeGenOperation feldoperation = opStack.pop();
		opStack.push(new EntityListOperationWithValue(op, entityIdList, feldoperation, expandGroups, ec));
	}

	@Override
	public void endVisit(IFieldIdOperation op) {
		EntityId entityId = entityIdDeq.removeLast();
		opStack.push(new FieldIdOperation(op, entityId));
	}

	@Override
	public void endVisit(IFieldIdOperationWithParameters op) {
		EntityId entityId = entityIdDeq.removeLast();
		String parameter = getParameterCode(op);
		opStack.push(new FieldIdOperationWithParameters(op, parameter, entityId));
	}

	private String getParameterCode(IFieldIdOperationWithParameters op) {
		return switch (op.getOperatorName()) {
			case ValueAsDate ->
				// The usage of the template StringParameter at construction time is not really nice.
				// Only for this usage, we need to initialize the CodeGenContext in
				// {@link ValidationCodeGenerator#fillBackingBeans()} instead of
				// {@link ValidationCodeGenerator#createEntries()}
					String.format("%s, %s", op.isFirstDay(), new StringParameter(op.getFormat()).toCode());
			case RangeAsNumber, RangeAsString -> String.format("%s, %s", op.getRangeStart(), op.getRangeEnd());
			case RoundAccountingValue, RoundUpValue, RoundDownValue -> Integer.toString(Math.max(0, op.getNumberOfDecimalPlaces()));
			default -> throw new IllegalArgumentException(
					"Field operation with parameters of type " + op.getOperatorName() + " not allowed.");
		};
	}

	@Override
	public void endVisit(IOpListOperation op) {
		List<CodeGenOperation> ops = popNOps(op.getOperations().size());
		opStack.push(new OpListOperation(op, ops));
	}

	@Override
	public void endVisit(ISetOpOperation p) {
		CodeGenOperation operation = opStack.pop();
		EntityId iterationEntity = entityIdDeq.removeLast();
		opStack.push(new SetOpOperation(p, iterationEntity, operation));
	}

	@Override
	public void endVisit(IOpListOperationWithParameter op) {
		List<CodeGenOperation> ops = popNOps(op.getOperations().size());
		opStack.push(new OpListOperationWithParameter(op, ops));
	}

	private List<CodeGenOperation> popNOps(int op) {
		List<CodeGenOperation> ops = new ArrayList<>();
		for (int i = 0; i < op; i++) {
			ops.addFirst(opStack.pop()); // preserve order
		}
		return ops;
	}

	@Override
	public void endVisit(IPointInTimeOperation op) {
		opStack.push(new PointInTimeOperation(op));
	}

	/**
	 * Hole die Parameter für die DatumAngabe-Konstrukt in der richtigen Reihenfolge vom Stack und
	 * setze die Objekte in das neue DatumAngabeKonstrukt.
	 */
	@Override
	public void endVisit(IDateConstructionOperation op) {
		CodeGenOperation jahr2 = (op.getYear2() != null) ? opStack.pop() : null;
		CodeGenOperation jahr1 = (op.getYear1() != null) ? opStack.pop() : null;

		CodeGenOperation monat = opStack.pop();
		CodeGenOperation tag = opStack.pop();

		opStack.push(new DateConstructionOperation(op,
				baseYear,
				createDatumParameter(jahr1),
				createDatumParameter(jahr2),
				createDatumParameter(monat),
				createDatumParameter(tag)));
	}

	@Override
	public void endVisit(IDateTimeConstructionOperation op) {
		CodeGenOperation time = opStack.pop();
		CodeGenOperation date = opStack.pop();

		opStack.push(new DateTimeConstructionOperation(op, date, time));
	}

	@Override
	public void endVisit(ITimeConstructionOperation op) {
		CodeGenOperation second = (op.getSecond() != null) ? opStack.pop() : null;
		CodeGenOperation minute = (op.getMinute() != null) ? opStack.pop() : null;
		CodeGenOperation hour = (op.getHour() != null) ? opStack.pop() : null;

		opStack.push(new TimeConstructionOperation(op,
				createDatumParameter(hour),
				createDatumParameter(minute),
				createDatumParameter(second)));
	}

	@Override
	public void endVisit(IDateRangeConstructionOperation op) {
		EntityId date2 = entityIdDeq.removeLast();
		EntityId date1 = entityIdDeq.removeLast();

		opStack.push(new DateRangeConstructionOperation(op, date1, date2));
	}

	private CodeGenOperation createDatumParameter(CodeGenOperation fop) {
		return switch (fop) {
			case null -> null;
			case FieldIdOperation _fieldIdOperation -> {
				fop.setType(OperationType.NUMBER);
				yield fop;
			}
			case ConstantOperation _constantOperation -> {
				fop.setType(OperationType.NUMBER);
				yield fop;
			}
			case DateExtractionOperation _dateExtractionOperation -> {
				fop.setType(OperationType.NUMBER);
				yield fop;
			}
			default -> throw new IllegalArgumentException("InternalError: unexpected class " + fop.getClass().getName());
		};
	}

	@Override
	public void endVisit(IConstantOperation op) {
		opStack.push(new ConstantOperation(op));
	}

	@Override
	public void endVisit(IDateExtractionOperation op) {
		CodeGenOperation da = opStack.pop();
		assert da.getType() == OperationType.DATE;
		opStack.push(new DateExtractionOperation(op, da));
	}

	@Override
	public void endVisit(IDateAddOperation op) {
		CodeGenOperation amountSpec = opStack.pop();
		CodeGenOperation dateSpec = opStack.pop();
		opStack.push(new DateAddOperation(op, dateSpec, amountSpec));
	}

	@Override
	public void endVisit(IDateDiffOperation op) {
		CodeGenOperation datumAngabe2 = opStack.pop();
		CodeGenOperation datumAngabe1 = opStack.pop();
		opStack.push(new DateDiffOperation(op, datumAngabe1, datumAngabe2));
	}

	@Override
	public void endVisit(ICompositeOperation op) {
		List<CodeGenOperation> ops = popNOps(op.getOperations().size());
		opStack.push(new CompositeOperation(op, ops));
	}

	@Override
	public void endVisit(IValueListCondition b) {
		EntityId entityId = entityIdDeq.removeLast();

		List<CodeGenOperation> ops = popNOps(b.getValueList().size());

		bedingungsStack.push(new ValueListCondition(b, entityId, ops));
	}

	@Override
	public void endVisit(IDateValidityCondition c) {
		CodeGenOperation dateConstruct = opStack.pop();

		bedingungsStack.push(new DateValidityCondition(c, dateConstruct));
	}

	@Override
	public void endVisit(IPredefinedTypeValidityCondition c) {
		EntityId field = entityIdDeq.removeLast();
		String predefinedTypeConstant = c.getPredefinedTypeConstant();

		bedingungsStack.push(new PredefinedTypeValidityCondition(c, field, predefinedTypeConstant));
	}

	@Override
	public void endVisit(INumberOfRootGroupsOperation b) {
		opStack.push(new NumberOfRootGroupsOperation(b));
	}

	@Override
	public void endVisit(IBoolConstantOperation b) {
		opStack.push(new BoolConstantOperation(b));
	}

	@Override
	public void endVisit(ICompositeCondition b) {
		List<CodeGenCondition> bedingungen = new ArrayList<>();
		for (int i = 0; i < b.getConditionList().size(); i++) {
			bedingungen.addFirst(bedingungsStack.pop()); // preserve order
		}

		bedingungsStack.push(new CompositeCondition(b.getLogOp(), b.hasBrackets(), bedingungen));
	}

	@Override
	public void endVisit(IGroupOperation op) {
		EntityId entityId = entityIdDeq.removeLast();
		opStack.push(new GroupOperation(op, entityId));
	}
}
