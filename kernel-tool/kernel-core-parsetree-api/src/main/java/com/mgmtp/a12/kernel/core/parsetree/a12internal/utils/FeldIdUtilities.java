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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 *
 */
@OnlyForUsage
public class FeldIdUtilities {

	public static final String ITERATIONS_EBENE_CODE = "@e";

	//definierte die Konstanten erneut,  um eine Abhängigkeit zu Validation.Api zu vermeiden.

	private FeldIdUtilities() {
		throw new IllegalStateException("Utility class");
	}

	public static long getLfdNr(IEntityId id) {
		return id.getIndices()[0];
	}

	public static long getMaxLfdNr(IEntityId id) {
		return id.getMaxValues()[0];
	}

	public static long getZeile(IEntityId id) {
		long[] e = id.getIndices();
		return e[e.length - 1];
	}

	public static boolean isEinzeilig(IEntityId id) {
		return getMaxZeile(id) == 1;
	}

	public static boolean isMehrzeilig(IEntityId id) {
		return getMaxZeile(id) > 1;
	}

	/**
	 * Gibt true zurück, wenn Zeile ein konkreter Wert ist oder das Feld nicht mehrzeilig ist.
	 */
	public static boolean isZKonkret(IEntityId id) {
		return isEinzeilig(id) || getZeile(id) > 0;
	}

	public static boolean isLKonkret(IEntityId id) {
		return getMaxLfdNr(id) == 1 || getLfdNr(id) > 0;
	}

	public static long getMaxZeile(IEntityId id) {
		return id.getMaxValues()[id.getMaxValues().length - 1];
	}

	public static boolean isZStern(IEntityId id) {
		return id.isField() && getZeile(id) == IParseTreeEntity.WILDCARD;
	}

	public static boolean isZIteration(IEntityId id) {
		return id.isHmodel() && isMehrzeilig(id) && getZeile(id) == IParseTreeEntity.ITERATION;
	}

	/**
	 * Checks if an entity id references iteration for a level (level 0 = root group and so on- USB
	 * is not treated here). If the level does not exist for the entity, false is returned.
	 */
	public static boolean isIteration(IEntityId id, int level) {
		return hasIndexOnLevel(id, level, IParseTreeEntity.ITERATION);
	}

	private static boolean hasIndexOnLevel(IEntityId id, int level, long index) {
		long[] indices = id.getIndices();
		if (level > indices.length - 1) {
			return false;
		}
		return id.getIndices()[level] == index;

	}

	public static boolean isZSternMitEinschraenkung(IEntityId id) {
		if (!isZStern(id) || isEinzeilig(id)) {
			return false;
		}
		if (id.getRowWildcardStart() > 1) {
			return true;
		}
		return (id.getRowWildcardEnd() != getMaxZeile(id));
	}

	public static boolean isLStern(IEntityId id) {
		return getLfdNr(id) == IParseTreeEntity.WILDCARD;
	}

	public static boolean isInLSternIntervall(long l, IEntityId id) {
		return l >= id.getRootGroupWildcardStart() && l <= id.getRootGroupWildcardEnd();
	}

	public static boolean isLIteration(IEntityId id) {
		return getLfdNr(id) == IParseTreeEntity.ITERATION;
	}

	public static boolean isUIteration(IEntityId id) {
		return id.getUsb() == IParseTreeEntity.ITERATION;
	}

	public static boolean isUStern(IEntityId id) {
		return id.getUsb() == IParseTreeEntity.WILDCARD;
	}

	public static String getVordruckName(IEntityId id) {
		return id.getFullName().split("/")[1];
	}

	public static boolean isLSternMitEinschraenkung(IEntityId id) {
		if (!isLStern(id) || getMaxLfdNr(id) == 1) {
			return false;
		}
		if (id.getRootGroupWildcardStart() > 1) {
			return true;
		}
		return (id.getRootGroupWildcardEnd() != getMaxLfdNr(id));
	}

	public static boolean hasKontexteMitStern(IEntityId id) {
		long[] e = id.getIndices();

		/*
		 * If field: do not take the row. For groups in the new world the row is not contained in
		 * the indices. An old world group is only repeatable for the first level, so we take only
		 * the first index.
		 */
		int maxLength;
		if (id.isField()) {
			maxLength = e.length - 1;
		} else {
			maxLength = id.isHmodel() ? 1 : e.length;
		}

		for (int i = 1; i < maxLength; i++) {
			if (e[i] == IParseTreeEntity.WILDCARD) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasWildCard(IEntityId f) {
		if (f.getUsb() == IParseTreeEntity.WILDCARD) {
			return true;
		}
		long[] indices = f.getIndices();
		/*
		 * In an old world group we may have a wildcard for the row, but this is only relevant for
		 * the fields inside the group and not for the group itself!
		 */
		int numberOfRelevantIndices = (!f.isField() && f.isHmodel()) ? 1 : indices.length;
		for (int i = 0; i < numberOfRelevantIndices; i++) {
			if (indices[i] == IParseTreeEntity.WILDCARD) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns true, if a wildcard is specified for a repetition level on the given path.
	 */
	public static boolean hasWildCardInPath(IEntityId f, String path) {
		// f may be a group and may be equal to path, and path may specify a field
		assert (f.getFullName() + "/").startsWith(path + "/");
		if (f.getUsb() == IParseTreeEntity.WILDCARD) {
			return true;
		}

		for (int i = 0; i < StringUtils.countMatches(path, "/"); i++) {
			long l = f.getIndices()[i];
			if (l == IParseTreeEntity.WILDCARD) {
				return true;
			}
		}
		return false;
	}

	public static String getRootGroupName(IEntityId f) {
		String fullName = f.getFullName();
		try {
			if (fullName.charAt(0) == '/') {
				return fullName.split("/")[1];
			}
		} catch (Exception e) {
			// ignore exception and return null
		}
		return null;
	}

	/**
	 * If the entity contains wildcards, it defines an entitylist. The method returns the overall
	 * length of the defined list and 1 if there are no wildcards in the entity.
	 */
	public static int getLengthAsList(IEntityId e) {
		int length = 1;
		for (int i = 0; i < e.getMaxValues().length; i++) {
			if (e.getIndices()[i] == IParseTreeEntity.WILDCARD) {
				length *= e.getMaxValues()[i];
			}
		}
		if (e.getUsb() == IParseTreeEntity.WILDCARD) {
			length *= e.getMaxUsb();
		}
		return length;
	}

	public static boolean isIteration(IEntityId e) {
		return ArrayUtils.contains(e.getIndices(), IParseTreeEntity.ITERATION) || e.getUsb() == IParseTreeEntity.ITERATION;
	}

	/**
	 * The number of iterations is the product of the max repeatbilites of the levels with
	 * iteration.
	 */
	public static int getNumberOfIterations(IEntityId e) {
		int result = 1;
		for (int i = 0; i < e.getMaxValues().length; i++) {
			if (e.getIndices()[i] == IParseTreeEntity.ITERATION) {
				result *= e.getMaxValues()[i];
			}
		}
		if (e.getUsb() == IParseTreeEntity.ITERATION) {
			result *= e.getMaxUsb();
		}
		return result;
	}
}
