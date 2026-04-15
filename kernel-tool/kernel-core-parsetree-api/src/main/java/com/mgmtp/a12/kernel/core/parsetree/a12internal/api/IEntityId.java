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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Schnittstelle für die Referenz einer Entität in der Bedingung.
 * <p>
 * Eine Entität ist spezifiziert durch ihren Namen (fullName) und
 * die Spezifikation des Indices für jede Wiederholungsebene. Die
 * Entität ist somit mehr als die Referenz auf ein Feld bzw. Kontext, da die
 * Indices für die Wiederholungen zusätzlich spezifiziert werden.
 * <p>
 * Eine Entität kann in einer Regelbedingung mehrfach referenziert werden.
 * <p>
 * <p>
 * Die Indices sind jeweils entweder
 * {@link IParseTreeEntity#ITERATION}, {@link IParseTreeEntity#PARALLEL_ITERATION} oder
 * {@link IParseTreeEntity#WILDCARD}.
 * <p>
 * Andere Spezifikationen für einen Index sind nur in der alten ExtMP Welt erlaubt.
 * <p>
 * Eine Entität spezifiziert eine oder mehrere Instanzen von Feldern bzw. Kontexten.
 */
@OnlyForUsage
public interface IEntityId extends IParseTreeEntity {

	/**
	 * gibt den vollen Namen der Entität (d.h. einschließlich des Pfades) zurück.
	 * fullNames sind immer eindeutig innerhalb des gesamten Modells.
	 * <p>
	 * Der Pfad ist immer absolut, d.h. die Anzahl der Slashed entspricht der
	 * Anzahl der Wiederholungsebenen (außer in der alten Welt, dort gibt es
	 * immer zwei Wiederholungsebenen - evtl. zusätzlich usb).
	 *
	 * @return the full name of the entity.
	 */
	public String getFullName();

	/**
	 * Indices für jede Wiederholungsebene.
	 * Die Größe des Arrays entspricht der Anzahl der Slahes in dem
	 * Ergebnis von {@link IEntityId#getFullName()} (außer in der alten Welt, dort gibt es
	 * immer zwei Wiederholungsebenen - laufende Nr. und Zeile, für den USB siehe {@link #getUsb()}
	 * ).
	 *
	 * @return Array von den Indices aller Wiederholungsebenen.
	 */
	public long[] getIndices();

	/**
	 * Gibt immer {@link IParseTreeEntity#UNSPECIFIED} zurück, außer in der alten ExtMP Welt.
	 *
	 * @return USB der Entität
	 */
	public long getUsb();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (!visitor.visit(this)) {
			// false return value discards semantic indexes
			return;
		}
		if (getSemanticIndex() != null) {
			for (Object semIdx : getSemanticIndex()) {
				if (semIdx instanceof IEntityId) {
					((IEntityId) semIdx).traverse(visitor);
				}
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Die size des arrays entspricht der size des return-Wertes von
	 * {@link IEntityId#getIndices()}
	 *
	 * @return maximaler Index für die einzelnen Wiederholungsebenen.
	 */
	public long[] getMaxValues();

	/**
	 * Es wird immer {@link IParseTreeEntity#UNSPECIFIED} zurückgegeben, außer in der alten ExtMP
	 * Welt.
	 *
	 * @return {@link IParseTreeEntity#UNSPECIFIED} oder maximaler USB
	 */
	public long getMaxUsb();

	/**
	 * Es wird immer false zurückgegeben, außer in der alten ExtMP Welt.
	 *
	 * @return false, außer in der alten ExtMP Welt.
	 */
	public boolean isHmodel();

	/**
	 * @return true, falls die Entität ein Feld ist.
	 */
	public boolean isField();

	/**
	 * Methode wird nur in der alten ExtMP Welt verwendet, da nur dort eine
	 * {@link IParseTreeEntity#WILDCARD} eingeschränkt werden darf.
	 *
	 * @return Start-Index, falls die Zeile mit Stern spezifiziert wurde.
	 */
	public long getRowWildcardStart();

	/**
	 * Methode wird nur in der alten ExtMP Welt verwendet, da nur dort eine
	 * {@link IParseTreeEntity#WILDCARD} eingeschränkt werden darf.
	 *
	 * @return Start-Index, falls die lfd. Nr. mit Stern spezifiziert wurde.
	 */
	public long getRootGroupWildcardStart();

	/**
	 * Methode wird nur in der alten ExtMP Welt verwendet, da nur dort eine
	 * {@link IParseTreeEntity#WILDCARD} eingeschränkt werden darf.
	 *
	 * @return Ende-Index, falls die Zeile mit Stern spezifiziert wurde.
	 */
	public long getRowWildcardEnd();

	/**
	 * Methode wird nur in der alten ExtMP Welt verwendet, da nur dort eine
	 * {@link IParseTreeEntity#WILDCARD} eingeschränkt werden darf.
	 *
	 * @return Ende-Index, falls die lfd. Nr. mit Stern spezifiziert wurde.
	 */
	public long getRootGroupWildcardEnd();

	/**
	 * If the field is not an enumeration <code>null</code> is always returned.
	 *
	 * @return name of the category or <code>null</code> if no category is specified
	 */
	public String getCategoryName();

	/**
	 * Returns an empty list, if there is no semantic index specified.
	 * Returns a list of {@link IEntityId}s or {@link String}s for the list of semantic index
	 * specifications, which can be either fields or constants.
	 *
	 * @return the list of semantic index specifications.
	 */
	public default List<Object> getSemanticIndex() {
		return Collections.emptyList();
	}

	/**
	 * Get the filter condition for filtered TODO: How do we call Mengenbildung in English?
	 *
	 * @return the filter condition.
	 */
	public default ICondition getFilterCondition() {
		return null;
	}

	/**
	 * @return a {@link PathNotationConfig} that contains additional details if the IEntityId is a
	 * 		relative path (with upward '..' parts).
	 * 		Has to be implemented in implementations. The default implementation always returns
	 * 		<code>PathNotationConfig.NO_RELATIVE_UPWARD_PATH</code>.
	 */
	public default PathNotationConfig getPathNotationConfig() {
		return PathNotationConfig.DEFAULT_MAYBELOSSY;
	}

	/**
	 * @return <code>true</code>, iff the entity is referenced inside a filter condition but uses rule iteration. The default
	 * 		implementation always returns <code>false</code>.
	 */
	public default boolean isOuter() {
		return false;
	}

	public default IEntityId withFilterCondition(ICondition filterCondition) {
		throw new NotImplementedException();
	}

	public default IEntityId withIsOuter(boolean isOuter) {
		throw new NotImplementedException();
	}

	public default IEntityId withPathNotationConfig(PathNotationConfig pathNotationConfig) {
		assert false : "withPathNotationConfig() is not implemented on " + this.getClass();
		return this; // safer than throwing a notImplementedException, because pathNotationConfig is typically a less critical detail
	}
}
