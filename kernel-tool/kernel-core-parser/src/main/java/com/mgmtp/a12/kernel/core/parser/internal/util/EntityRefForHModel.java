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

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

/**
 * Specifies an entity ref in ExtMP H models.
 * <p>
 * In H models, the number of repetition layers is always 2 (and the USB)
 * To assure this, the indices for "lfd.Nr" and "Zeile" are saved while
 * constructing the object.
 */
public class EntityRefForHModel extends EntityRef {

	//In H models:  the wildcard may be restricted.
	//attributes are set,  if a wildcard is set for zeile or lfd.Nr.
	private long zeileSternStart;
	private long zeileSternEnd;
	private long lfdNrSternStart;
	private long lfdNrSternEnd;

	/**
	 * Creates a new entity reference for an H model entity. The root group index is initially set
	 * to {@link IParseTreeEntity#ITERATION}. The row index is initially set to
	 * {@link IParseTreeEntity#ITERATION} for fields and {@link IParseTreeEntity#WILDCARD} for
	 * groups. Indices may be changed later if index specifiers are parsed (which are after the
	 * entity name in H models).
	 */
	public EntityRefForHModel(IEntity e, String nameUsedInConditionText) {
		super(e, nameUsedInConditionText);

		// set LfdNr: für Wiederholbare Vordrucke ist der Default eine Iteration.
		long l = EntityUtils.getMaxLfdNr(e) > 1 ? IParseTreeEntity.ITERATION : 1L;
		addIndex(l); //LfdNr

		if (e.isField()) {
			long z = EntityUtils.getMaxZeile((IField) e) > 1 ? IParseTreeEntity.ITERATION : 1L;
			addIndex(z); //Zeile
		} else {
			/*
			 * für Kontexte bzw. Vordrucke wird der Zeilenindex auf Stern gesetzt, für den Fall dass
			 * mehrzeilige Felder enthalten sind (falls .#Zeile referenziert wird, wird später
			 * sowieso auf Iteration geändert). Ob die enthaltenen Felder wirklich mehrzeilig sind,
			 * wird bei der Expansion berücksichtigt.
			 */
			addIndex(IParseTreeEntity.WILDCARD); //Zeile
		}
	}

	/**
	 * die Methode wird nur aufgerufen, wenn für die Zeile ein Wildcard
	 * spezifiziert wird. Falls keine Einschränkung für die Zeile definiert wurde,
	 * wird immer 1 gesetzt.
	 */
	public void setZeileSternStart(long zeileSternStart) {
		this.zeileSternStart = zeileSternStart;
	}

	public long getZeileSternStart() {
		return zeileSternStart;
	}

	public long getZeileSternEnd() {
		return zeileSternEnd;
	}

	public void setZeileSternEnd(long zeileSternEnd) {
		this.zeileSternEnd = zeileSternEnd;
	}

	public long getLfdNrSternStart() {
		return lfdNrSternStart;
	}

	public void setLfdNrSternStart(long lfdNrSternStart) {
		this.lfdNrSternStart = lfdNrSternStart;
	}

	public long getLfdNrSternEnd() {
		return lfdNrSternEnd;
	}

	public void setLfdNrSternEnd(long lfdNrSternEnd) {
		this.lfdNrSternEnd = lfdNrSternEnd;
	}

	/**
	 * Attention: If this entity is a group and the method returns {@link IParseTreeEntity#WILDCARD}
	 * , it is NOT guaranteed that the group contains fields with a repeatable row, cf.
	 * {@link #EntityRefHModel(IEntity, String)}.
	 *
	 * @return row index for this entity
	 */
	public long getZeile() {
		return indices.get(1);
	}

	public void setZeile(long zeile) {
		indices.set(1, zeile);
	}

	@Override
	public void setUsb(long usb) {
		this.usb = usb;
	}

	/**
	 * Die Behandlung von Kontexten und Zeilen-Angaben ist für H-Modelle ein Hilfskonstrukt und
	 * nicht schön.
	 * <p>
	 * Für die Zeilen-Angaben wird Wildcard gesetzt, wenn im Kontext mehrzeilige Felder enthalten
	 * sind.
	 * Für eine Überprüfung ob ein Wildcard explizit gesetzt wurde, ist dies aber nicht relevant.
	 */
	@Override
	public boolean hasWildCards() {
		if (getEntity().isField()) {
			return indices.contains(IParseTreeEntity.WILDCARD) || getUsb() == IParseTreeEntity.WILDCARD;
		} else {
			return indices.get(0) == IParseTreeEntity.WILDCARD || getUsb() == IParseTreeEntity.WILDCARD;
		}
	}

	/**
	 * Überprüft, ob die Wildcard-Eingabe für die Zeile eingeschränkt ist.
	 * Falls kein Wildcard definiert wurde, wird immer true zurückgegeben.
	 *
	 * @return false, falls für die Zeile Sternoperation ohne Einschränkungen spezifiziert wurde,
	 * 		in allen anderen Fällen true.
	 */
	public boolean isZSternEingeschraenkt() {
		if (getEntity().isGroup()) {
			return false;
		}
		return getZeileSternStart() != 1 || getZeileSternEnd() != EntityUtils.getMaxZeile((IField) getEntity());
	}

	/**
	 * Überprüft, ob die Wildcard-Eingabe für die laufende Nummer eingeschränkt ist.
	 * Falls kein Wildcard definiert wurde, wird immer true zurückgegeben.
	 *
	 * @return false, falls für die laufende Nummer Sternoperation ohne Einschränkungen spezifiziert
	 * 		wurde, in allen anderen Fällen true.
	 */
	public boolean isLSternEingeschraenkt() {
		if (getEntity().isGroup()) {
			return false;
		}
		return getLfdNrSternStart() != 1 || getLfdNrSternEnd() != EntityUtils.getMaxLfdNr(getEntity());
	}
}
