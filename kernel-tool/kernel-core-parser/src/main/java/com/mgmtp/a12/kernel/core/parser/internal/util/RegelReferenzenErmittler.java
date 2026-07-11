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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.GroupOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.FeldOperationUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;

/**
 * Klasse wird verwendet, um die referenzierten Felder bzw. Vordrucke für die Auslassungsfehler zu
 * ermitteln.
 * Die Methoden wird bei der Codegenerierung verwendet, um die Information für die Schlüssel
 * <ul>
 * <li>RefFeldIdentifierAuslassungsFehler</li>
 * <li>RefVordruckeAuslassungsFehler</li>
 * </ul>
 * zu ermitteln.
 */
public class RegelReferenzenErmittler {
	private final ICondition bedingung;
	private final ICondition unexpandedCondition;
	private final int levelToStartDuplicateSearch;
	private IEC ec;

	public RegelReferenzenErmittler(ICondition bedingung, ICondition unexpandedCondition, IEC ec, int levelToStartDuplicateSearch) {
		assert bedingung != null || unexpandedCondition != null;
		this.bedingung = bedingung;
		this.unexpandedCondition = unexpandedCondition;
		this.ec = ec;
		// relevant for ids referenced in RepetitionNotUnique (= WiederholungNichtEindeutig)
		this.levelToStartDuplicateSearch = levelToStartDuplicateSearch;
	}

	/**
	 * like {@link #getReferenzierteEntityIdentifierFuerAuslassungsfehler}, but for *-referenced enitityIds that have filter
	 * conditions, the filter conditions will be removed (because filter evaluation is not supported in the Meta_Regel* generated
	 * code.
	 * <br>
	 * Note: entityIds that are referenced <em>within</em> filterConditions will nonetheless be included (the removal of filter
	 * conditions only happens in post-processing and affects only the effective repetitions of the filtered entities, not the
	 * set of referenced entities).
	 * <br>
	 * See also {@link #getReferencedEntityIds4RefEntity(boolean)}
	 */
	public Set<IEntityId> getReferencedEntityIds4RefOmissionEntity(
			boolean nonRootGroupsAsUnexpanded,
			boolean useRootGroups) {
		return getReferenzierteEntityIdentifierFuerAuslassungsfehler(nonRootGroupsAsUnexpanded, useRootGroups).stream()
				.map(RegelReferenzenErmittler::removeFilterCondition)
				.collect(Collectors.toSet());
	}

	/**
	 * Creates an entityId that is like the given entityId, except with any filtercondition removed.
	 * Note: not to be confused with referenced entities *from* filter condtitions (those should be included in refEntities.)
	 * <br>
	 * The removal of filter conditions is done because they are not evaluated anyway for addMessageV2 (neither for isGiven, nor
	 * for the EntityIds that are passed in possiblyCausingEntities (theoretically we could pass individual references for
	 * all repetitions for which the filter is true, but currently we simply pass *-references).
	 * Also, codegen would need some adaptations to support filter conditions in refEntities.
	 */
	private static IEntityId removeFilterCondition(IEntityId eId) {
		return eId.getFilterCondition() == null ? eId : eId.withFilterCondition(null);
	}

	/**
	 * Ermittle alle referenzierten Felder, die einen Auslassungsfehler erzeugen können.
	 * Der Ermittlungsalgorithmus verwendet dazu eine einfache Heuristik: Wenn ein Feld nur
	 * durch die Sprachkonstrukte AlleFelderAngegeben, FeldAngegeben oder MindestensEinFeldangegeben
	 * referenziert wird, kann es nie einen Auslassungsfehler erzeugen und ist daher nicht in der
	 * Rückgabemenge enthalten. Das gleiche gilt für ein Feld, das durch
	 * <code>AnzahlAngegebenerFelder &gt; Konstante</code> geprüft wurde.
	 *
	 * @return Menge der referenzierten Felder, die einen Auslassungsfehler erzeugen können.
	 */
	public Set<IEntityId> getReferenzierteEntityIdentifierFuerAuslassungsfehler(
			boolean nonRootGroupsAsUnexpanded,
			boolean useRootGroups) {
		final Set<IEntityId> result = new HashSet<>();

		class Visitor extends ASTVisitor {
			private boolean inRepetitionNotUnique = false;

			@Override
			//Falls die Feldlistenbedingung keinen Auslassungsfehler erzeugen kann, wird nicht weiter iteriert.
			public boolean visit(IEntityListCondition b) {
				if (kannKeinenAuslassungFehlerErzeugen(b)) {
					return false; //es wird nicht weiter traversiert
				}
				if (b.getConditionName() == EntityListConditionName.RepetitionNotUnique) {
					inRepetitionNotUnique = true;
				}
				return true;
			}

			@Override
			public boolean visit(IGroupOperation gop) { // copy & paste & adapt from ReferencedIdsUtil
				if (gop.getOperatorName() == GroupOperationName.CurrentRepetition) {
					// Only the index field can be an error-causing entity for AktuelleWiederholung,
					// the group itself (or any other fields inside it) is not a referencedEntity that
					// could cause a validation error.
					// But even the index field cannot be an *omission*error-causing entity because of MVK_PARALLEL_ITERATION_INVALID
					// -> we don't descend into the groupOperation (return false), and also don't add any indexfield.
					return false;
				} else {
					throw new IllegalArgumentException("Unknown IGroupOperation '" + gop.getOperatorName() + "'.");
				}
			}

			@Override
			public void endVisit(IEntityListCondition c) {
				inRepetitionNotUnique = false;
			}

			@Override
			public boolean visit(IRootGroupCondition cond) {
				if (RootGroupConditionName.KeinVordruckAngegeben != cond.getConditionName()) {
					return false; // Only KeinVordruckAngegeben can create an omission error
				}
				if (useRootGroups && cond.getEntityId() == null) {
					result.add(ReferencedIdsUtil.createEntityForRootGroupCondition(cond, ec));
					return false;
				}
				return true;
			}

			@Override public void endVisit(INumberOfRootGroupsOperation p) {
				if (useRootGroups) {
					result.add(ReferencedIdsUtil.createEntityForNumberOfRootGroupCondition(p, ec));
				}
			}

			@Override
			//Füge ein referenziertes Feld hinzu, durch den Traversierungsalgorithmus wurde sichergestellt,
			//dass nur bis zu Feldern traversiert wird, die Auslassungsfehler erzeugen können.
			public boolean visit(IEntityId fid) {
				Set<IEntityId> tmpResult = new HashSet<>();
				// Die Kontexte zu ignorieren ist hier auch bei bereits expandierter Bedingung nötig (falls keine Kontexte gewünscht
				// sind), da im Fall von AnzahlAngegebenerKontexte die expandierten Bedingungen neben den expandierten Feldern
				// zusätzlich auch noch die Kontexte enthalten, siehe JavaDoc von ExpandMode.EXPAND
				if (ReferencedIdsUtil.mkEntityFilter(nonRootGroupsAsUnexpanded, useRootGroups).test(fid)) {
					tmpResult.add(fid);
				}
				// Notes about adding the index-field of semantic-index / parallel iteration reference as an omission entity:
				// - If the original reference is an omission entity, then the index-field is an omission entity too, because it
				//   can cause the entity to be treated as-if-not-given, when the index-field is not given.
				// - If the original reference is not an omission entity, no omission-entity-ness of the index-field should be
				//   derived: omission of the index field will make the original reference equivalent to not-given, and
				//   because the original reference is not an omission entity, this cannot cause a validation error.
				// Assumption: an entity that is referenced via the index-field (semantic index or parallel iteration) is
				// treated as not-given whenever there is no repetition where the index-field has the matching value, or when
				// the required index-field-value is specified via a field reference and that field is not given.
				tmpResult.addAll(ReferencedIdsUtil.getEntitiyReferencesIfSemanticIndex(
						fid, ec, true).all());
				ReferencedIdsUtil.getIndexFieldIfParallelIteration(
								fid, ec, true)
						.ifPresent(tmpResult::add);

				// Note: filter conditions are handled separately in FilterConditionForOmissionErrorVisitor, because
				// the references from filter conditions might be omission entities even if the primary (filtered) entity
				// is not an omission entity (the pruning with kannKeinenAuslassungFehlerErzeugen() doesn't apply there)

				tmpResult.stream()
						.map(e -> ReferencedIdsUtil.replaceIndices4RepNotUnqIfRequired(
								e, inRepetitionNotUnique, levelToStartDuplicateSearch))
						.map(ReferencedIdsUtil::replaceInternalIndicesByWildcard)
						.forEach(result::add);
				return true;
			}

			/**
			 * Es werden für die Ermittlung der FeldIds für Auslassungsfehler nur die FeldIds
			 * ausgeschlossen, die in Vergleichsbedingungen vorkommen, bei denen ein Sprachkonstrukt
			 * der Form
			 * <code>Operand mit AnzahlAngegebenerFelder > Operand ohne AnzahlAngegebenerFelder</code>
			 * ausgeschlossen, da dadurch üblicherweise keine Auslassungsfehler erzeugt werden.
			 */
			@Override
			public boolean visit(IComparisonCondition b) {
				boolean op1MitAAF = FeldOperationUtil.enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(
						b.getOperation1());
				boolean op2MitAAF = FeldOperationUtil.enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(
						b.getOperation2());
				// Wenn auf beiden Seiten oder auf keiner AnzahlAngegebenerFelder vorkommt, kann ein Auslassungsfehler entstehen.
				if ((op1MitAAF && op2MitAAF) || (!op1MitAAF && !op2MitAAF)) {
					return true;
				}
				// Wenn bei einem &gt;-Vergleich links AAF vorkommt führt das üblicherweise nicht zu einem Auslassungsfehler
				if (isGroesserVergleich(b) && op1MitAAF) {
					return false;
				}
				// der Spiegelsymmetrische Fall
				if (isKleinerVergleich(b) && op2MitAAF) {
					return false;
				}
				return true;
			}
		}

		// We want to have the fields of ALL filter conditions (even if the whole subtree is pruned in the primary visitor due to
		// the IComparisonCondition optimizations
		// -> second Visitor
		class FilterConditionForOmissionErrorVisitor extends ASTVisitor {
			@Override public boolean visit(IEntityId e) {
				if (e.getFilterCondition() != null) {
					result.addAll(
							ReferencedIdsUtil.getReferencedIdsInFilterCondition(e, ec, nonRootGroupsAsUnexpanded, useRootGroups));
				}
				return true;
			}
		}

		if (nonRootGroupsAsUnexpanded) { // root groups don't get expanded anyway
			unexpandedCondition.traverse(new Visitor());
			unexpandedCondition.traverse(new FilterConditionForOmissionErrorVisitor());
		} else {
			bedingung.traverse(new Visitor());
			bedingung.traverse(new FilterConditionForOmissionErrorVisitor());
		}

		return result;
	}

	private boolean isKleinerVergleich(IComparisonCondition b) {
		return (b.getCmpOp() == ComparisonOperation.LE || b.getCmpOp() == ComparisonOperation.LT);
	}

	private boolean isGroesserVergleich(IComparisonCondition b) {
		return (b.getCmpOp() == ComparisonOperation.GE || b.getCmpOp() == ComparisonOperation.GT);
	}

	private boolean isUngleichVergleich(IComparisonCondition b) {
		return (b.getCmpOp() == ComparisonOperation.NE
				|| b.getCmpOp() == ComparisonOperation.NE1 || b.getCmpOp() == ComparisonOperation.NE2
				|| b.getCmpOp() == ComparisonOperation.NE5 || b.getCmpOp() == ComparisonOperation.NE10);
	}

	/**
	 * @return gibt true zurück, falls die Feldlistenbedingung Auslassungfehler erzeugen kann.
	 */
	private boolean kannKeinenAuslassungFehlerErzeugen(IEntityListCondition b) {
		return (b.getConditionName() == EntityListConditionName.AllFieldsFilled ||
				b.getConditionName() == EntityListConditionName.AtLeastOneFieldFilled ||
				b.getConditionName() == EntityListConditionName.MoreThanOneFieldFilled ||
				b.getConditionName() == EntityListConditionName.AllGroupsFilled ||
				b.getConditionName() == EntityListConditionName.AtLeastOneGroupFilled);
	}

	public Set<IEntityId> getReferenzierteFeldIds() {
		return ReferencedIdsUtil.getReferencedIds4MetaReferences(bedingung, ec, levelToStartDuplicateSearch, false, null);
	}

	/**
	 * See also {@link #getReferencedEntityIds4RefOmissionEntity(boolean, boolean)}
	 */
	public Set<IEntityId> getReferencedEntityIds4RefEntity(boolean isA12Model) {
		return getReferencedEntityIds(isA12Model).stream()
				.map(RegelReferenzenErmittler::removeFilterCondition)
				.collect(Collectors.toSet());
	}

	public Set<IEntityId> getReferencedEntityIds(boolean isA12Model) {
		if (isA12Model) {
			return getReferencedEntityIdsA12DV();
		} else {
			return getReferencedEntityIdsHDV();
		}
	}

	private Set<IEntityId> getReferencedEntityIdsA12DV() {
		return ReferencedIdsUtil.getReferencedIds4MetaReferences(unexpandedCondition, ec,
				levelToStartDuplicateSearch, true, null);
	}

	private Set<IEntityId> getReferencedEntityIdsHDV() {
		return ReferencedIdsUtil.getReferencedIds4MetaReferences(bedingung, ec,
				levelToStartDuplicateSearch, false, true, null);
	}

	/**
	 * Gibt die Menge von Vordrucknamen zurück, die von Vordrucksprachkonstrukte referenziert
	 * werden, die Auslassungsfehler erzeugen können.
	 *
	 * @return Menge der referenzierten Vordrucknamen, oder leere Menge.
	 */
	public Set<String> getReferenzierteVordruckNamenFuerAuslassungsfehler() {
		final Set<String> resultSet = new HashSet<>();

		class Visitor extends ASTVisitor {
			@Override
			//Falls eine relevante Formularbedingung,  wird der Formularname in die Menge eingefügt.
			public boolean visit(IRootGroupCondition b) {
				if (b.getConditionName() == RootGroupConditionName.KeinVordruckAngegeben) {
					resultSet.add(b.getRootGroupName());
				}
				return false;
			}

			/**
			 * Überprüft, ob die Operation AnzahlAngegebenerVordrucke so geprüft wird, dass ein
			 * Auslassungsfehler erzeugt werden kann.
			 */
			@Override
			public boolean visit(IComparisonCondition b) {
				if (isKleinerVergleich(b)) {
					addVordruckNameIfRelevant(b.getOperation1(), resultSet);
				}
				if (isGroesserVergleich(b)) {
					addVordruckNameIfRelevant(b.getOperation2(), resultSet);
				}
				if (isUngleichVergleich(b)) {
					addVordruckNameIfRelevant(b.getOperation1(), resultSet);
					addVordruckNameIfRelevant(b.getOperation2(), resultSet);
				}
				return false;
			}
		}

		bedingung.traverse(new Visitor());
		return resultSet;
	}

	private void addVordruckNameIfRelevant(IOperation op, Set<String> vordruckNamen) {
		if (op instanceof INumberOfRootGroupsOperation) {
			INumberOfRootGroupsOperation vop = (INumberOfRootGroupsOperation) op;
			vordruckNamen.add(vop.getRootGroupName());
		}
	}

}
