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

package com.mgmtp.a12.kernel.core.parser.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.FeldOperationUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAllAst;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAnyAst;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.IndexArt;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;

/**
 * Die Klasse implementiert verschiedene Analysen einer IBedingung.
 */
public class AnalyseService {

	private AnalyseService() {
		throw new IllegalStateException("Utility class");
	}

	public static Set<IField> getIndexFields(IEntity entity, IEC ec) {
		Set<IField> indexFields = new HashSet<>();
		Set<IGroup> indexGroups = new HashSet<>();
		while (entity != null) {
			if (entity.isGroup() && !((IGroup) entity).getIndexFieldNames().isEmpty()) {
				indexGroups.add((IGroup) entity);
			}
			// we iterate backwards
			entity = ec.get(EntityUtils.getParentPath(entity.getFullName()));
		}
		for (IGroup indexGroup : indexGroups) {
			Set<String> indexFieldNames = indexGroup.getIndexFieldNames();
			for (String indexFieldName : indexFieldNames) {
				IEntity e = EntityUtils.getEntityInGroup(ec, indexFieldName, indexGroup.getFullName());
				if (e != null && e.isField()) {
					indexFields.add((IField) e);
				}
			}
		}
		return indexFields;
	}

	/**
	 * Methode gibt wahr zurück, wenn einer der Teilbedingungen AnzahlAngegebenerUSB ist.
	 */
	public static boolean beinhaltetAnzahlAngegebenerUsb(ICondition bedingung) {
		return FindAnyAst.findeInBedingung(bedingung, INumberOfUsbOperation.class::isInstance, false).isPresent();
	}

	/**
	 * @param condition
	 * 		the condition to be searched.
	 * @return true, iff one or more occurrences of the construct RepetitionNotUnique are contained
	 * 		in the given condition.
	 */
	public static boolean beinhaltetWiederholungNichtEindeutig(ICondition condition) {
		return FindAnyAst.findeInBedingung(condition, AnalyseService::isRepetitionNotUnique, true).isPresent();
	}

	/**
	 * @param condition
	 * 		the condition to be searched.
	 * @return true, iff multiple occurrences of the construct RepetitionNotUnique are contained in
	 * 		the given condition.
	 */
	public static boolean containsMultipleRepetitionNotUnique(ICondition condition) {
		return FindAllAst.findAll(condition, AnalyseService::isRepetitionNotUnique, true).size() > 1;
	}

	public static boolean isRepetitionNotUnique(IParseTreeEntity p) {
		return isEntityListConditionWithName(p, EntityListConditionName.RepetitionNotUnique);
	}

	public static boolean containsMultipleZeileNichtEindeutig(ICondition condition) {
		return FindAllAst.findAll(condition, AnalyseService::isZeileNichtEindeutig, true).size() > 1;
	}

	private static boolean isZeileNichtEindeutig(IParseTreeEntity p) {
		return isEntityListConditionWithName(p, EntityListConditionName.ZeileNichtEindeutig);
	}

	private static boolean isEntityListConditionWithName(IParseTreeEntity p, EntityListConditionName name) {
		return (p instanceof IEntityListCondition) && ((IEntityListCondition) p).getConditionName() == name;
	}

	/**
	 * Methode gibt wahr zurück, wenn einer der Teilbedingungen AnzahlAngegebenerVordrucke ist.
	 */
	public static boolean beinhaltetAnzahlAngegebenerVordrucke(ICondition bedingung) {
		return FindAnyAst.findeInBedingung(bedingung, INumberOfRootGroupsOperation.class::isInstance, false).isPresent();
	}

	public static boolean beinhaltetAnzahlAngegebenerFelderOrKontexteOrVerschiedenerWerteMitKleinerPruefung(ICondition b) {
		final Boolean[] result = new Boolean[1];
		result[0] = false;

		class Visitor extends ASTVisitor {
			@Override
			public boolean visit(IComparisonCondition ib) {
				IOperation op1 = ib.getOperation1();
				IOperation op2 = ib.getOperation2();

				ComparisonOperation v = ib.getCmpOp();
				switch (v) {
					case GE:
					case GT:
						if (FeldOperationUtil.enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(op2)) {
							result[0] = true;
						}
						break;
					case LE:
					case LT:
						if (FeldOperationUtil.enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(op1)) {
							result[0] = true;
						}
						break;
					case EQ:
						// Bei Gleichheit muss im Fehlerfall ein Auslassungfehler
						// geworfen werden.  Wenn alle Felder belegt sind, und es
						// zu dem Fehlerfall kommt wird sowieso kein Auslassungfehler
						// geworfen. D.h. es wird nur für den Fall, dass nicht alle
						// Felder angegeben sind, UND der Fehlerfall auftritt, ein
						// Auslassungsfehler geworfen. Daher ist die hier vorgenommene
						// Kategorisierung korrekt.
					case NE:
					case NE2:
					case NE5:
					case NE1:
					case NE10:
						if (FeldOperationUtil.enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(op1)
								|| FeldOperationUtil.enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(op2)) {
							result[0] = true;
						}
						break;
					default:
				}
				return false;
			}
		}

		b.traverse(new Visitor());
		return result[0];
	}

	/**
	 * Gibt true zurück, wenn die Bedingung mehrere Vordrucke referenziert. Dazu zählen auch
	 * verschiedene Wiederholungen des gleichen Vordrucks.
	 */
	public static boolean referenziertMehrereVordrucke(ICondition bedingung) {
		final String[] vordruckName = new String[1];
		final Boolean[] result = new Boolean[1];
		result[0] = false;

		class Visitor extends ASTVisitor {

			@Override
			public boolean visit(IEntityId id) {
				String aktVordruckName = FeldIdUtilities.getVordruckName(id);
				if (vordruckName[0] == null) {
					vordruckName[0] = aktVordruckName;
				}
				if (!aktVordruckName.equals(vordruckName[0])) {
					result[0] = true;
					return false; // jetzt kann man aufhören
				}
				if (!FeldIdUtilities.isLIteration(id) && FeldIdUtilities.getLfdNr(id) != 1) {
					result[0] = true;
					return false; // jetzt kann man aufhören
				}
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
				return true;
			}

			@Override
			public boolean visit(IRootGroupCondition b) {
				result[0] = true;
				return false;
			}

			@Override
			public void endVisit(IApplicationCondition b) {
				result[0] = true;
			}

		}
		bedingung.traverse(new Visitor());
		return result[0];
	}

	/**
	 * Returns <code>true</code> if the condition references different root groups (directly or by
	 * referencing a field or group on the root group). "Different" meaning root groups with
	 * different names not only different instances of one root group.
	 */
	public static boolean referencesDifferentRootGroups(ICondition condition) {
		if (condition == null) {
			return false;
		}

		ReferencedRootGroupVisitor v = new ReferencedRootGroupVisitor();
		condition.traverse(v);
		return v.getRootGroups().size() > 1;
	}

	public static Set<String> getUsbIterationRootGroups(IParseTreeEntity e) {
		if (e == null) {
			return new HashSet<>();
		}
		UsbIterationRootGroupVisitor v = new UsbIterationRootGroupVisitor();
		e.traverse(v);
		return Collections.unmodifiableSet(v.getRootGroups());
	}

	private static class UsbIterationRootGroupVisitor extends ASTVisitor {

		private Set<String> usbIterationRootGroups = new HashSet<>();

		@Override
		public boolean visit(IEntityId id) {
			if (id.getUsb() == IParseTreeEntity.ITERATION) {
				usbIterationRootGroups.add(FeldIdUtilities.getVordruckName(id));
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
			}
			return false;
		}

		@Override
		public boolean visit(IRootGroupCondition c) {
			if (c.getUsb() == IParseTreeEntity.ITERATION) {
				usbIterationRootGroups.add(c.getRootGroupName());
			}
			return false;
		}

		@Override
		public void endVisit(INumberOfRootGroupsOperation op) {
			if (op.getUsb() == IParseTreeEntity.ITERATION) {
				usbIterationRootGroups.add(op.getRootGroupName());
			}
		}

		Set<String> getRootGroups() {
			return usbIterationRootGroups;
		}
	}

	private static class ReferencedRootGroupVisitor extends ASTVisitor {

		private Set<String> rootGroups = new HashSet<>();

		@Override
		public boolean visit(IEntityId id) {
			rootGroups.add(FeldIdUtilities.getVordruckName(id));
			if (id.getFilterCondition() != null) {
				id.getFilterCondition().traverse(this);
			}
			return false;
		}

		@Override
		public boolean visit(IRootGroupCondition c) {
			rootGroups.add(c.getRootGroupName());
			return false;
		}

		@Override
		public void endVisit(INumberOfRootGroupsOperation op) {
			rootGroups.add(op.getRootGroupName());
		}

		Set<String> getRootGroups() {
			return rootGroups;
		}
	}

	/**
	 * Gibt true zurück, wenn in mindestens einer Teilbedingung ein Feld bzw. eine Vordruckbedingung
	 * (nur für LfdNr / USB) die angegebene IndexArt als Muster referenziert.
	 *
	 * @param b
	 * 		- zu prüfende Bedingung
	 */
	public static boolean referenziertIteration(ICondition b, final IndexArt indexArt) {
		assert !VirtualEntityTester.containsVirtualEntities(b) : "Iteration cannot be given for leniently parsed parse trees";
		switch (indexArt) {
			case ZEILE:
				boolean isRowRelevant = FindAnyAst.findeInBedingung(b, p -> p instanceof IEntityId && ((IEntityId) p).isHmodel(),
						false).isPresent();
				return isRowRelevant && isIteration(b, 1, true);
			case LFDNUMMER:
				return isIteration(b, 0, true);
			case USB:
				return isUIteration(b);
			default:
				return false;
		}
	}

	/**
	 * Checks if a condition references iteration for a level (level 0 = root group and so on - USB
	 * is not treated here).
	 */
	public static boolean isIteration(IParseTreeEntity b, int level, boolean useFilterCondition) {
		assert !VirtualEntityTester.containsVirtualEntities(b) : "Iteration cannot be given for leniently parsed parse trees";
		if (b == null) {
			return false;
		}
		final Boolean[] result = new Boolean[1];
		result[0] = false;
		class Visitor extends ASTVisitor {
			private boolean isInFilterCondition;

			@Override
			public boolean visit(IEntityId id) {
				if ((!isInFilterCondition || id.isOuter()) &&
						FeldIdUtilities.isIteration(id, level)) {
					result[0] = true;
				}
				if (useFilterCondition && id.getFilterCondition() != null) {
					this.isInFilterCondition = true;
					id.getFilterCondition().traverse(this);
					this.isInFilterCondition = false;
				}
				return true;
			}

			@Override
			public boolean visit(IRootGroupCondition ifb) {
				if (isInFilterCondition && ifb.getEntityId() != null && !ifb.getEntityId().isOuter()) {
					return true;
				}
				if (level == 0 && ifb.getRootGroupIndex() == IParseTreeEntity.ITERATION) {
					result[0] = true;
				}
				return true;
			}
		}
		b.traverse(new Visitor());
		return result[0];
	}

	/**
	 * Checks if a condition references parallel iteration.
	 *
	 * @param p
	 * 		- parse tree to be checked, this may be a condition or a part of a condition
	 * @return true, if parallel iteration is specified, false otherwise
	 */
	public static boolean isParallelIteration(IParseTreeEntity p, boolean useFilterCondition) {
		if (p == null) {
			return false;
		}
		assert !VirtualEntityTester.containsVirtualEntities(p) : "Iteration cannot be given for leniently parsed parse trees";
		boolean[] result = { false };
		class Visitor extends ASTVisitor {
			@Override public boolean visitGeneral(IParseTreeEntity e) {
				// If we already know the result end traversal
				return !result[0];
			}

			@Override
			public boolean visit(IEntityId id) {
				if (result[0]) {
					return false;
				}
				if (isParallelIterationId(id)) {
					result[0] = true;
					return false;
				}
				if (id.getFilterCondition() != null && useFilterCondition) {
					id.getFilterCondition().traverse(this);
				}
				return !result[0];
			}

			@Override
			public boolean visit(IRootGroupCondition cond) {
				if (cond.getRootGroupIndex() == IParseTreeEntity.PARALLEL_ITERATION) {
					result[0] = true;
				}
				return !result[0];
			}
		}
		p.traverse(new Visitor());
		return result[0];
	}

	/**
	 * @param id
	 * 		the {@link IEntityId} to test.
	 * @return
	 */
	public static boolean isParallelIterationId(IEntityId id) {
		return ArrayUtils.contains(id.getIndices(), IParseTreeEntity.PARALLEL_ITERATION);
	}

	/**
	 * returns true, if the condition references a USB Iteration. ExtMP World only, H models only.
	 * A USB iteration is referenced if
	 * <ul>
	 * <li>a field,
	 * <li>a 'VordruckBedingung', or
	 * <li>an 'AnzahlVordruckeFeldOperation'
	 * </ul>
	 * references an usb iteration
	 *
	 * @param p
	 * 		- parse tree to be checked, this may be a condition or a part of a condition
	 * @return true, if usb iteration is specified, false otherwise
	 */
	public static boolean isUIteration(IParseTreeEntity p) {
		assert !VirtualEntityTester.containsVirtualEntities(p) : "Iteration cannot be given for leniently parsed parse trees";
		if (p == null) {
			return false;
		}

		class Visitor extends ASTVisitor {
			private boolean result = false;

			@Override
			public boolean visit(IEntityId iid) {
				if (iid.getUsb() == IParseTreeEntity.ITERATION) {
					result = true;
				}
				return false;
			}

			@Override
			public boolean visit(IRootGroupCondition bed) {
				if (bed.getUsb() == IParseTreeEntity.ITERATION) {
					result = true;
				}
				return false;
			}

			@Override
			public void endVisit(INumberOfRootGroupsOperation op) {
				if (op.getUsb() == IParseTreeEntity.ITERATION) {
					result = true;
				}
			}

		}

		Visitor t = new Visitor();
		p.traverse(t);
		return t.result;
	}

	/**
	 * returns <code>true</code>, if the parts in their totality reference multiple root groups or
	 * multiple repetitions of root groups with USB pattern.
	 * <p>
	 * ExtMP World only, H models only.
	 */
	public static boolean isUSBPatternForRootGroups(List<? extends IParseTreeEntity> parts) {
		final Set<String> referenzierteVordrucke = new HashSet<>();
		final Set<Long> referenzierteLfdNummern = new HashSet<>();

		class Visitor extends ASTVisitor {

			@Override
			public boolean visit(IEntityId id) {
				if (FeldIdUtilities.isUIteration(id)) {
					referenzierteVordrucke.add(FeldIdUtilities.getRootGroupName(id));
					referenzierteLfdNummern.add(FeldIdUtilities.getLfdNr(id));
				}
				return false;
			}

			@Override
			public boolean visit(IRootGroupCondition fBed) {

				if (fBed.getUsb() == IParseTreeEntity.ITERATION) {
					referenzierteVordrucke.add(fBed.getRootGroupName());
					referenzierteLfdNummern.add(fBed.getRootGroupIndex());
				}
				return false;
			}

			@Override
			public void endVisit(INumberOfRootGroupsOperation avFop) {
				if (avFop.getUsb() == IParseTreeEntity.ITERATION) {
					referenzierteVordrucke.add(avFop.getRootGroupName());
					referenzierteLfdNummern.add(0L);
				}
			}

		}

		Visitor visitor = new Visitor();
		parts.forEach(p -> p.traverse(visitor));

		return referenzierteVordrucke.size() > 1 // multiple different root groups
				|| referenzierteLfdNummern.contains(0L) // wild card for root group index, i.e. multiple root group repetitions
				|| referenzierteLfdNummern.size() > 1; // multiple concrete root group indexes.

	}

	/**
	 * returns <code>true</code>, if the condition references multiple root groups or multiple
	 * repetitions of root groups with USB pattern.
	 * <p>
	 * ExtMP World only, H models only.
	 */
	public static boolean isUSBPatternForRootGroups(ICondition bedingung) {
		return isUSBPatternForRootGroups(Collections.singletonList(bedingung));
	}

	/**
	 * @return <code>true</code> if the given condition uses a semantic index. Else
	 * 		<code>false</code>.
	 */
	public static boolean usesSemanticIndex(ICondition condition) {
		if (condition == null) {
			return false;
		}

		boolean[] semIdxUsed = new boolean[] { false };

		class Visitor extends ASTVisitor {
			@Override
			public boolean visitGeneral(IParseTreeEntity e) {
				// stop criterion
				return !semIdxUsed[0];
			}

			@Override
			public boolean visit(IEntityId id) {
				if (!id.getSemanticIndex().isEmpty()) {
					semIdxUsed[0] = true;
				}
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
				return false;
			}
		}

		Visitor visitor = new Visitor();
		condition.traverse(visitor);
		return semIdxUsed[0];
	}

	/**
	 * @return <code>true</code> if the error field is referenced in a construct using categories or
	 * 		in a semantic index construct either as value field or as semantic index. Else
	 * 		<code>false</code>.
	 */
	public static boolean usesSemanticIndexOrCategoriesForErrorField(ICondition condition, String errorFieldFullName) {
		boolean[] badConstructUsed = new boolean[] { false };

		class Visitor extends ASTVisitor {
			@Override
			public boolean visitGeneral(IParseTreeEntity e) {
				// stop criterion
				return !badConstructUsed[0];
			}

			@Override
			public boolean visit(IEntityId id) {
				if (badConstructUsed[0]) {
					return false;
				}
				if (id.getSemanticIndex().isEmpty() && StringUtils.isEmpty(id.getCategoryName())) {
					return false;
				}
				if (id.getFullName().equals(errorFieldFullName)) {
					badConstructUsed[0] = true;
					return false;
				}
				for (Object semIdxObj : id.getSemanticIndex()) {
					if (semIdxObj instanceof String) {
						continue;
					}
					IEntityId semIdx = (IEntityId) semIdxObj;
					if (semIdx.getFullName().equals(errorFieldFullName)) {
						badConstructUsed[0] = true;
						return false;
					}
				}
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
				return false;
			}
		}

		Visitor visitor = new Visitor();
		condition.traverse(visitor);
		return badConstructUsed[0];
	}

	/**
	 * @return all groups referenced via parallel iteration by the given {@link IEntityId}. Ignores filter conditions.
	 */
	public static List<String> getParallelIterationGroupsOfId(IEntityId id) {
		List<String> parallelIterationGroups = new ArrayList<>();
		long[] indices = id.getIndices();
		for (int level = 0; level < indices.length; level++) {
			if (indices[level] == IParseTreeEntity.PARALLEL_ITERATION) {
				parallelIterationGroups.add(EntityUtils.getPathUpToLevel(level, id.getFullName()));
			}
		}
		return parallelIterationGroups;
	}

	/**
	 * @return all groups referenced via parallel iteration in the given condition
	 */
	public static List<String> getParallelIterationGroups(IParseTreeEntity p) {
		assert !VirtualEntityTester.containsVirtualEntities(p) : "Iteration cannot be given for leniently parsed parse trees";
		Set<String> parallelIterationGroups = new TreeSet<>();

		class Visitor extends ASTVisitor {
			@Override
			public boolean visit(IEntityId id) {
				parallelIterationGroups.addAll(getParallelIterationGroupsOfId(id));
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
				return true;
			}

			@Override
			public boolean visit(IRootGroupCondition cond) {
				if (cond.getRootGroupIndex() == IParseTreeEntity.PARALLEL_ITERATION) {
					parallelIterationGroups.add(EntityUtils.createRootGroupPath(cond.getRootGroupName()));
				}
				return true;
			}
		}

		Visitor visitor = new Visitor();
		p.traverse(visitor);

		return new ArrayList<>(parallelIterationGroups);
	}

	public static boolean hasParallelIteratedFilterConditionId(ICondition cond) {
		return FindAnyAst.findeInBedingung(cond, AnalyseService::isParallelIteratedFilterConditionId, false).isPresent();
	}

	private static boolean isParallelIteratedFilterConditionId(IParseTreeEntity p) {
		if (!(p instanceof IEntityId)) {
			return false;
		}
		IEntityId id = (IEntityId) p;
		return id.getFilterCondition() != null && isParallelIterationId(id);
	}

	public static int getParallelIterationLevel(IParseTreeEntity p, IEntity errorField, IEC ec) {
		if (!isParallelIteration(p, true)) {
			return -1;
		}
		return EntityUtils.getIndexFieldLevel(errorField.getFullName(), ec);
	}

	public static List<ICondition> getFilterConditions(List<? extends IParseTreeEntity> parseTrees) {
		final List<ICondition> result = new ArrayList<>();
		class Visitor extends ASTVisitor {
			@Override
			public void endVisit(IEntityId eId) {
				if (eId.getFilterCondition() != null) {
					result.add(eId.getFilterCondition());
				}
			}
		}
		Visitor visitor = new Visitor();
		for (IParseTreeEntity parseTree : parseTrees) {
			parseTree.traverse(visitor);
		}
		return result;
	}

	public static boolean containsInvalidLenghOfNoValueValidation(ICondition condition, IEC ec) {
		return containsLengthOfNoValueValidation(condition, ec) &&
				getNameAndLengthOfAllowedValueValidation(condition, ec).isEmpty();
	}

	public record NameAndLengthOfAllowedValueValidation(String fieldFullName, int length) {
	}

	public static Optional<NameAndLengthOfAllowedValueValidation> getNameAndLengthOfAllowedValueValidation(
			ICondition condition,
			IEC ec
	) {
		if (!(condition instanceof IComparisonCondition compCond)) {
			return Optional.empty();
		}

		return switch (compCond.getCmpOp()) {
			case GT -> {
				if (isLengthOfNoValueValidation(compCond.getOperation1(), ec)
						&& compCond.getOperation2() instanceof IConstantOperation constOp) {
					// e.g.: Length(FieldWithNoValueValidation) > 100
					yield Optional.of(new NameAndLengthOfAllowedValueValidation(
							extractFieldInLength((IFieldIdOperation) compCond.getOperation1()),
							extractConstant(constOp)));
				} else {
					yield Optional.empty();
				}
			}
			case LT -> {
				if (compCond.getOperation1() instanceof IConstantOperation constOp
						&& isLengthOfNoValueValidation(compCond.getOperation2(), ec)) {
					// e.g.: 100 < Length(FieldWithNoValueValidation)
					yield Optional.of(new NameAndLengthOfAllowedValueValidation(
							extractFieldInLength((IFieldIdOperation) compCond.getOperation2()),
							extractConstant(constOp)));
				} else {
					yield Optional.empty();
				}
			}
			default -> Optional.empty();
		};
	}

	private static String extractFieldInLength(IFieldIdOperation operation) {
		return operation.getField().getFullName();
	}

	private static int extractConstant(IConstantOperation constOp) {
		return Integer.parseInt(constOp.getValue());
	}

	private static boolean containsLengthOfNoValueValidation(ICondition condition, IEC ec) {
		class Visitor extends ASTVisitor {
			boolean containsLengthOfNoValueValidation = false;

			@Override
			public boolean visit(IFieldIdOperation fop) {
				if (containsLengthOfNoValueValidation) {
					return false;
				}
				containsLengthOfNoValueValidation = isLengthOfNoValueValidation(fop, ec);
				return false;
			}

			@Override
			public boolean visit(IEntityId id) {
				if (containsLengthOfNoValueValidation) {
					return false;
				}
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
				return false;
			}

			@Override
			public boolean visitGeneral(IParseTreeEntity e) {
				return !containsLengthOfNoValueValidation;
			}
		}

		Visitor visitor = new Visitor();
		condition.traverse(visitor);
		return visitor.containsLengthOfNoValueValidation;
	}

	private static boolean isLengthOfNoValueValidation(IParseTreeEntity p, IEC ec) {
		if (!(p instanceof IFieldIdOperation fop)) {
			return false;
		}

		if (fop.getOperatorName() != FieldIdOperationName.Length) {
			return false;
		}
		IField entity = (IField) ec.get(fop.getField().getFullName());
		IFieldType type = entity.getType();
		return type instanceof IStringType stringType && stringType.isNoValueValidation();
	}

	public static boolean containsValueNotConsistent(ICondition condition) {
		class Visitor extends com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor {
			private boolean valueNotConsistentFound = false;

			public boolean isValueNotConsistentFound() {
				return valueNotConsistentFound;
			}

			@Override
			public boolean visit(IEntityListAndValueListCondition cond) {
				if (cond.getConditionName() == EntityListAndValueListConditionName.ValueNotConsistent) {
					valueNotConsistentFound = true;
				}
				return false;
			}
		}

		Visitor visitor = new Visitor();
		condition.traverse(visitor);
		return visitor.isValueNotConsistentFound();
	}
}
