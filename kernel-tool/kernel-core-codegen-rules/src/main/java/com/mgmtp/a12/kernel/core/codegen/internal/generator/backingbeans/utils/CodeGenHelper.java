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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperationWithParameters;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;

/**
 *
 */
public class CodeGenHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenHelper.class);

	private CodeGenHelper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Methode untersucht die Bedingung bezüglich der Zeilen des Fehlerfeldes. Es wird 1 bzw. die
	 * minimale angegebene Indexnummer des Fehlerfeldes mit der übergebenen lfd Nummer Vordruck
	 * zurückgegeben. D.h. neben der Überprüfung ob der Name des Fehlerfeldes gleich ist, muss auch
	 * überprüft werden, ob die laufende Nummer gleich ist. Dadurch wird erreicht, dass die
	 * Fehlermeldung sich nicht immer auf den Index der Regelbasisdaten bezieht, sondern auf den
	 * realen Index des Fehlerfeldes.
	 *
	 * @return
	 */
	public static int getFehlerFeldZeileForCodeGen(
			final String fehlerFeld,
			ICondition bedingung,
			final long lfdNr) {
		final Long[] result = new Long[1];
		result[0] = 0L;
		class Visitor extends ASTVisitor {
			@Override
			public boolean visit(IEntityId id) {
				if (id.getFullName().equals(fehlerFeld) && feldIdUmfasstLfdNr(id, lfdNr)) {
					setMinZeile(id);
				}
				return true;
			}

			private void setMinZeile(IEntityId id) {
				if (FeldIdUtilities.isZIteration(id)) {
					return;
				}

				long neuIndex = (FeldIdUtilities.isZStern(id)) ? id.getRowWildcardStart() : FeldIdUtilities.getZeile(id);
				if (result[0] == 0) {
					result[0] = neuIndex;
				} else {
					// Es wurde bereits ein Index gefunden. speichere den
					// neuen Wert nur, wenn er kleiner ist.
					result[0] = Math.min(result[0], neuIndex);
				}

			}

		}
		try {
			bedingung.traverse(new Visitor());
			return (int) Math.max(1, result[0]); // gebe mindestens 1 zurück
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
			return 1;
		}
	}

	/**
	 * Hilfsmethode überprüft, ob die Spezifikation des Feldes die angegebenen laufende Nummer
	 * enthält.
	 */
	private static boolean feldIdUmfasstLfdNr(IEntityId id, long lfdNr) {
		if (lfdNr == -1 || FeldIdUtilities.isLIteration(id)) {
			return true;
		}

		//bei Stern-Operation muss lfdNr im Stern-Intervall liegen.
		if (FeldIdUtilities.isLStern(id)) {
			return FeldIdUtilities.isInLSternIntervall(lfdNr, id);
		}

		//Spezialfall,  in der FeldId wurde ein konkreter Index angegeben.
		//Dieser muss der laufenden Nummer entsprechen.
		return lfdNr == FeldIdUtilities.getLfdNr(id);

	}

	/**
	 * Methode untersucht die Bedingung bezüglich der Indexe des Fehlerfeldes. Es wird 1 bzw. die
	 * minimale angegebene laufende Nr Vordruck zurückgegeben. Dadurch wird erreicht, dass die
	 * Fehlermeldung sich nicht immer auf den Index der Regelbasisdaten bezieht, sondern auf den
	 * realen Index des Fehlerfeldes.
	 */
	public static int getFehlerFeldLfdNrForCodeGen(final String fehlerFeld, ICondition bedingung) {

		final Long[] result = new Long[1];
		result[0] = 0L;

		class Visitor extends ASTVisitor {
			@Override
			public boolean visit(IEntityId f) {
				// Falls das Feld das Fehlerfeld ist: speichere den Index,
				// wenn dieser kleiner ist, als vorangegangen Indices
				if (f.getFullName().equals(fehlerFeld)) {
					setMinLfdNr(f);
				}
				return true;
			}

			private void setMinLfdNr(IEntityId id) {
				long l = FeldIdUtilities.getLfdNr(id);
				if (l == IParseTreeEntity.ITERATION) {
					return;
				}

				long neuIndex = (l == IParseTreeEntity.WILDCARD) ? id.getRootGroupWildcardStart() : l;
				if (l == IParseTreeEntity.SEMANTIC_INDEX) {
					neuIndex = 1;
				}

				if (result[0] == 0) {
					result[0] = neuIndex;
				} else {
					// Es wurde bereits ein Index gefunden. speichere den
					// neuen Wert nur, wenn er kleiner ist.
					result[0] = Math.min(result[0], neuIndex);
				}

			}
		}
		try {
			bedingung.traverse(new Visitor());
			return (int) Math.max(1, result[0]);
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
			return 1;
		}
	}

	/**
	 * Überprüft, ob die angegebene Bedingung ein Auslassungsfehler erzeugen kann. Dazu werden die
	 * verwendeten Sprachkonstrukte der Regelbedingung überprüft. Gibt es mindestens ein
	 * Sprachkonstrukt, das einen Auslassungsfehler erzeugen kann, so wird true zurückgegeben.
	 *
	 * @return gibt true zurück, wenn die Bedingung einen Auslassungsfehler erzeugen kann
	 */
	public static boolean canCreateOmissionErrors(ICondition b) {
		if (b == null) {
			return false;
		}

		final boolean[] auslassungsFehler = new boolean[1];

		class Visitor extends ASTVisitor {

			private void auslassungsFehlerSetzen() {
				auslassungsFehler[0] = true;
			}

			@Override
			public boolean visit(IRootGroupCondition b) {
				RootGroupConditionName bedingungName = b.getConditionName();
				if (bedingungName == RootGroupConditionName.KeinVordruckAngegeben
						|| bedingungName == RootGroupConditionName.VordruckNichtFortlaufend
						|| bedingungName == RootGroupConditionName.UsbNichtFortlaufend) {
					auslassungsFehlerSetzen();
					return false;
				}
				return true;
			}

			@Override
			public boolean visit(IEntityListOperation b) {
				auslassungsFehlerSetzen();
				return false;
			}

			@Override
			public boolean visit(IEntityListOperationWithValue b) {
				auslassungsFehlerSetzen();
				return false;
			}

			@Override
			public boolean visit(IFieldIdOperation fo) {
				auslassungsFehlerSetzen();
				return false;
			}

			@Override
			public boolean visit(IFieldIdOperationWithParameters b) {
				auslassungsFehlerSetzen();
				return false;
			}

			@Override
			public boolean visit(IEntityListCondition b) {
				if ((b.getConditionName() == EntityListConditionName.AllFieldsFilled ||
						b.getConditionName() == EntityListConditionName.MoreThanOneFieldFilled ||
						b.getConditionName() == EntityListConditionName.FieldValuesNotUnique)
						&& !containsFilterCondition(b.getEntityList())) {
					return false;
				}
				auslassungsFehlerSetzen();
				return false;
			}

			@Override
			public void endVisit(INumberOfUsbOperation op) {
				auslassungsFehlerSetzen();
			}

			@Override
			public void endVisit(INumberOfRootGroupsOperation op) {
				auslassungsFehlerSetzen();
			}

			@Override
			public boolean visit(IEntityListAndValueListCondition b) {
				switch (b.getConditionName()) {
					case NotAllFieldValuesIncludedInValueList:
					case NoFieldValueIncludedInValueList:
						auslassungsFehlerSetzen();
						break;
					case AtLeastOneFieldValueIncludedInValueList:
						if (containsFilterCondition(b.getEntityList()) || containsFilterCondition(b.getValueListAsEntityList())) {
							auslassungsFehlerSetzen();
						}
						break;
					case AtLeastOneDateRangeOverlaps:
						if (containsFilterCondition(b.getValueListAsEntityList())) {
							auslassungsFehlerSetzen();
						}
						break;
					case ValueNotConsistent:
					default:
						break;
				}
				return false;
			}

		}

		b.traverse(new Visitor());
		return auslassungsFehler[0];
	}

	private static boolean containsFilterCondition(IEntityIdList entityList) {
		return entityList.getEntityIdList().stream()
				.anyMatch(e -> e.getFilterCondition() != null);
	}

}
