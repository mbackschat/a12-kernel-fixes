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


import type { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { IData, IMetaField } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
	ArrayMap,
	HashMap,
	HashSet,
	IIdentifier,
	IMetaKeys,
	SortedSet
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { IterationState } from "../util/IterationState.js";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { ArrayUtils } from "../util/ArrayUtils.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { EntityUtil } from "../util/EntityUtil.js";
import { GesetzteWiederholungen } from "../util/GesetzteWiederholungen.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";
import { MetaDataUtilValidierung } from "../util/MetaDataUtilValidierung.js";
import { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";
import { RtExternalIdentifierWithSemanticIndex } from "../util/RtExternalIdentifierWithSemanticIndex.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import type { SemanticIndexSpec } from "../util/SemanticIndexSpec.js";

import { GroupFillCache } from "./GroupFillCache.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import { IndexFieldCache } from "./IndexFieldCache.js";
import { ResultBaseImpl } from "./ResultBaseImpl.js";

export class WerteContainer {
	constructor(private readonly _feldWertOberflaeche: string, private readonly _feldWertXML: string) { }

	get feldWertOberflaeche(): string {
		return this._feldWertOberflaeche;
	}

	get feldWertXML(): string {
		return this._feldWertXML;
	}
}

/**
 * Hier werden Daten zur Laufzeitoptimierung gecached.
 */
export class ValidationCache {
	/**
	 * This value is currently used to substitute the value of an entity whose actual value we do not
	 * want to add to the cache. This currently can have different reasons:
	 * - the value was not validated because the field has the property 'noValueValidation'. Usually,
	 * such fields are assigned huge values which might otherwise cause performance issues.
	 * - the value was invalid but should cause containing groups to be treated as filled (only in calculation)
	 * - the value belonged to a group
	 */
	private static readonly PSEUDO_VALUE: string = "X";

	private _data: IData | undefined;

	/**
	 * Hier werden zum Feld der XML-Wert und der Wert in Oberflächendarstellung
	 * gecached. Diese Klasse stellt Methoden bereit um den Cache zu füllen und
	 * um die relevanten Werte auszulesen.
	 */
	protected _datenCache: HashMap<RtInternalIdentifier, WerteContainer> = new HashMap();

	private readonly _gesetzteWiederholungen: GesetzteWiederholungen = new GesetzteWiederholungen();

	/**
	 * Nur Felder in der Menge sind prüfungsrelevant (für Teilvalidierung).
	 */
	private readonly _pruefungsrelevanteEntitiesIntern: HashSet<RtInternalIdentifier> =
		new HashSet<RtInternalIdentifier>();

	/**
	 * Zu jedem (eindeutigen) Feldnamen, für den ein Identifier prüfungsrelevant ist,
	 * werden die prüfungsrelevanten Identifier in der Map gespeichert.
	 *
	 * A group counts as relevant, if at least one identifier in the group is relevant.
	 */
	private readonly _pruefungsrelevanteFelderInternProName: Map<
		string,
		HashSet<RtInternalIdentifier>
	> = new Map<string, HashSet<RtInternalIdentifier>>();

	/**
	 * For every group name, for which a contained identifier is relevant, the fully relevant group
	 * iterations are stored. For a fully relevant group, all descendant identifiers need to be
	 * relevant, too.
	 */
	private _fullyRelevantGroupsIntern: Map<string, Set<RtInternalIdentifier>> = new Map();

	protected readonly _erroneousGroups: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();

	/**
	 * Menge der prüfungsrelevanten Entitäten (Gruppen und/oder Felder), wie von der Anwendung übergeben.
	 */
	private _pruefungsrelevanteEntitiesExtern: Set<IIdentifier> = new Set<IIdentifier>();

	/**
	 * Felder, die bei der formalen Prüfung zu Fehlern führten, werden in dieser Menge gesammelt.
	 */
	private readonly _fehlerhafteFelderExtern: HashSet<RtExternalIdentifier> =
		new HashSet<RtExternalIdentifier>();

	/**
	 * Wie fehlerhafteFelderExtern, aber mit den intern verwendeten eindeutigen Feldnamen.
	 */
	protected _fehlerhafteFelderIntern: HashSet<RtInternalIdentifier> = new HashSet();

	/**
	 * Unbekannte Feldnamen von Feldern, die zur Validierung als Daten oder prüfungsrelevante Felder
	 * übergeben wurden, werden in dieser Menge gesammelt. Diese wird dazu benutzt, um
	 * pro unbekanntem Feldnamen nur einen formalen Fehler der Ergebnismenge hinzuzufügen
	 */
	private readonly _unbekannteFelderExtern: Map<string, RtExternalIdentifier> = new Map<
		string,
		RtExternalIdentifier
	>();

	/**
	 * For each index group (outer key) and the indices of its parent (key 2)
	 * there is a map from value to specific iteration index.
	 * Used to look up semantic indexes.
	 */
	protected _indexFieldCache: IndexFieldCache;

	/**
	 * Remembers the fill status of groups.
	 */
	protected groupFillCache: GroupFillCache;

	private _isValidationPartWith3ValueLogic = false;

	constructor(
		protected readonly _metaData: IMetaDataValidierung,
		protected readonly _presentationInfo: DarstellungsInformationIntern,
		protected readonly _mode: ValidationMode,
	) {
		this._indexFieldCache = new IndexFieldCache(_metaData);
		this.groupFillCache = new GroupFillCache(_metaData);
	}

	/**
	 * Liefert interne Metadaten Schnittstelle.
	 */
	public getMetaData(): IMetaDataValidierung {
		return this._metaData;
	}

	public addGroupIfAbsent(id: RtInternalIdentifier): void {
		if (!this._datenCache.has(id)) {
			this._datenCache.set(id, new WerteContainer(ValidationCache.PSEUDO_VALUE, ValidationCache.PSEUDO_VALUE));
			this.setGroupFilled(id);
		}
	}

	public addValidFieldValue(id: RtInternalIdentifier, displayValue: string, internalXmlValue: string): void {
		this._datenCache.set(id, new WerteContainer(displayValue, internalXmlValue));

		if (this._isIndexFieldInstance(id)) {
			// For index fields we cache all the values in the IndexFieldCache to be able to easily lookup the correct iteration
			// for a given value in a semantic index construct. See class comment of IndexFieldCache for an example.
			this._indexFieldCache.setIndexFieldValue(id, internalXmlValue);
		}
		this.setFieldFilled(id);
	}

	public addValidFieldValueIfAbsent(id: RtInternalIdentifier, displayValue: string, internalXmlValue: string): void {
		if (!this._datenCache.has(id)) {
			this.addValidFieldValue(id, displayValue, internalXmlValue);
		}
	}

	public addPseudoFieldValueIfAbsent(id: RtInternalIdentifier): void {
		if (!this._datenCache.has(id)) {
			this._datenCache.set(id, new WerteContainer(ValidationCache.PSEUDO_VALUE, ValidationCache.PSEUDO_VALUE));
			// do not add the field value to the index field cache
			this.setFieldFilled(id);
		}
	}

	/**
	 * Adds fields for which the value was non-empty but formally invalid to the cache.
	 */
	public addInvalidFieldValue(id: RtInternalIdentifier, idExtern?: RtExternalIdentifier): void {
		this.markFieldAsInvalidForValidation(id, idExtern);
	}

	/**
	 * @returns the XML value of the field or undefined if the field is not in the cache
	 */
	public getXMLValue(f: RtInternalIdentifier): string | undefined {
		return this._getXMLValue(this.resolveSemanticIndex(f))
	}

	protected _getXMLValue(resolvedField: RtInternalIdentifier): string | undefined {
		const result = this._datenCache.get(resolvedField)?.feldWertXML;
		return result && this._resolveCategory(resolvedField, result);
	}

	protected _resolveCategory(f: RtInternalIdentifier, value: string): string | undefined {
		const category: string | undefined = f.getCategory();
		if (!category) {
			return value;
		}
		return MetaDataUtilValidierung.getValue4Category(this._metaData, f.getName(), value, category);
	}

	/**
	 * Liefert den Oberflächen-Wert des Feldes oder undefined falls es das Feld im
	 * Cache nicht gibt.
	 */
	public getWertOberflaeche(f: RtInternalIdentifier): string | undefined {
		f = this.resolveSemanticIndex(f);
		return this._datenCache.get(f)?.feldWertOberflaeche;
	}

	/**
	 * Checks whether the given entity is filled.
	 */
	public isEntityFilled(entity: RtInternalIdentifier): boolean {
		const resolvedEntity = this.resolveSemanticIndex(entity);
		return this._datenCache.has(resolvedEntity)
			|| this.groupFillCache.isGroupFilled(resolvedEntity);
	}

	public isGroupFilled(group: RtInternalIdentifier): boolean {
		const resolvedGroup = this.resolveSemanticIndex(group);
		return this.groupFillCache.isGroupFilled(resolvedGroup);
	}

	/**
	 * If a semantic index was defined, this method returns a new instance of {@link RtInternalIdentifier}
	 * in which the semantic index (see {@link IIdentifier.SEMANTIC_INDEX}) is replaced with the specific index.
	 */
	public resolveSemanticIndex(id: RtInternalIdentifier): RtInternalIdentifier {
		const semIdx = id.getSemanticIndex();
		return semIdx == undefined
			? id
			: this._actuallyResolveSemanticIndex(id, semIdx);
	}

	protected _actuallyResolveSemanticIndex(id: RtInternalIdentifier, semIdx: SemanticIndexSpec): RtInternalIdentifier {
		const indexValue = this.getSemanticIndexValue(semIdx);
		return this._replaceSemanticIndex(id, indexValue);
	}

	/**
	 * Get the index value for the given semantic index specification. If the semantic index is
	 * represented by a field, the value will be looked up in the data.
	 *
	 * @param semIdx 	the semantic index specification. Must not be null.
	 * @return the value for the semantic index.
	 */
	public getSemanticIndexValue(semIdx: SemanticIndexSpec): string {
		if (semIdx.isEntity()) {
			const semIdxId: RtInternalIdentifier = semIdx.getAsEntity();
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			return this.getXMLValue(semIdxId)!;
		} else {
			return semIdx.getAsConstant();
		}
	}

	private _replaceSemanticIndex(
		semIdxId: RtInternalIdentifier,
		indexValue: string
	): RtInternalIdentifier {
		const fullName: string = this._metaData.getPfadName(semIdxId.getName());
		const lvl: number = ArrayUtils.indexOf(semIdxId.getIndices(), IIdentifier.SEMANTIC_INDEX);
		// The name of the group containing the index fields
		const groupPath: string = EntityUtil.getPathOfHierarchyLevel(fullName, lvl);
		const indices = semIdxId.getIndices().slice(0, lvl);

		const index: number = this._indexFieldCache.lookup(groupPath, indices, indexValue);

		// Replace the placeholder
		return semIdxId.cloneWithSemanticIndexReplaced(lvl, index);
	}

	/**
	 * @return the specific index for the given root group and the given index value.
	 */
	public getSpecificIndexForSemanticIndex(rootGroupName: string, indexValue: string): number {
		return this._indexFieldCache.lookup("/" + rootGroupName, new Array<number>(), indexValue);
	}

	public replaceSemanticIndexRepetition(
		origIdx: IterationState,
		indexedGroupFullName: string,
		levelOfIndexFieldGroup: number,
		indexValue: string
	): IterationState {
		const newIndexes: number[] = origIdx.getIndexes().slice();
		const indexedGroupParentIndices = origIdx.getIndexes().slice(0, levelOfIndexFieldGroup);
		newIndexes[levelOfIndexFieldGroup] = this._indexFieldCache.lookup(
			indexedGroupFullName,
			indexedGroupParentIndices,
			indexValue
		);
		return new IterationState(newIndexes, indexValue);
	}

	/**
	 * Fügt das Feld zur Menge der unbekannten Felder hinzu. Diese werden dabei wird nur nach
	 * Feldname unterschieden.
	 */
	public addUnbekanntesFeld(id: IIdentifier): void {
		this._unbekannteFelderExtern.set(id.getName(), new RtExternalIdentifier(id));
	}

	/**
	 * Liefert die unbekannten Felder. Pro unbekanntem Feldnamen wird nur ein IdentifierImpl als
	 * Vertreter gespeichert.
	 *
	 * @return die unbekannten Felder
	 */
	public getUnbekannteFelder(): Map<string, RtExternalIdentifier> {
		return this._unbekannteFelderExtern;
	}

	public setFieldFilled(id: RtInternalIdentifier): void {
		this.groupFillCache.setFieldFilled(id);
		this._gesetzteWiederholungen.update(id);
	}

	public setGroupFilled(id: RtInternalIdentifier): void {
		this.groupFillCache.setGroupFilled(id);
		this._gesetzteWiederholungen.update(id);
	}

	/**
	 * Liefert den Index des Kontextes mit der Verschachtelungstiefe kontextIndex,
	 * bei dem unter allen angegebenen Feldern der Index maximal ist.
	 * Laufende Nummer ist Kontext mit Schachtelungstiefe 0
	 *
	 */
	public getMaxGesetzterKontext(kontextIndex: number): number {
		return this.groupFillCache.getHighestRepetitionAtLevel(kontextIndex);
	}

	/**
	 * Liefert für die spezifizierte Wiederholungsebene die maximal gesetzte Wiederholung, wobei nur
	 * Vordruck und Kontextwiederholbarkeiten betrachtet werden, Zeilen nicht. <br>
	 * Ebene 0 die LfdNr, Ebene 1 Kontexte direkt unter dem Vordruck etc.
	 */
	public getMaxGesetzteWiederholungOhneZeile(ebene: number): number {
		return this.getMaxGesetzterKontext(ebene);
	}

	public getGesetzteWiederholungen(): GesetzteWiederholungen {
		return this._gesetzteWiederholungen;
	}

	public numberOfFilledGroupInstances(groupPath: string): number {
		return this.groupFillCache.numberOfFilledGroupInstances(groupPath);
	}

	public getFilledGroupInstances(groupPath: string): Iterable<RtInternalIdentifier> {
		return this.groupFillCache.getFilledGroupInstances(groupPath);
	}

	isNotEmpty(map: HashMap<any, any> | Map<any, any> | undefined): boolean {
		return map != undefined && map.size > 0;
	}

	/**
	 * Fügt alle übergebenen Felder dem Cache der prüfungsrelevanten Felder hinzu (Info für Teilvalidierung relevant).
	 *
	 * @param idSet 	zu setzende Menge der prüfungsrelevanten Felder
	 */
	public addAllRelevantEntitiesIntern(idSet: HashSet<RtInternalIdentifier>): void {
		for (const id of idSet) {
			this._pruefungsrelevanteEntitiesIntern.add(id);
		}
	}

	/**
	 * Liefert alle prüfungsrelevanten Felder (intern).
	 *
	 * @return Menge der prüfungsrelevanten Felder, leere Menge falls kein
	 *         Feld prüfungsrelevant.
	 */
	public getAllRelevantEntitiesIntern(): HashSet<RtInternalIdentifier> {
		return this._pruefungsrelevanteEntitiesIntern;
	}

	/**
	 * Fügt der Map mit den prüfungsrelevanten Feldern pro Feldname einen
	 * Identifier hinzu. Interne Darstellung, d.h. unique Name
	 */
	public addPruefungsrelevantesFeldInternZumNamen(id: RtInternalIdentifier): void {
		let ids: HashSet<RtInternalIdentifier> | undefined =
			this._pruefungsrelevanteFelderInternProName.get(id.getName());
		if (ids == undefined) {
			ids = new HashSet<RtInternalIdentifier>();
			this._pruefungsrelevanteFelderInternProName.set(id.getName(), ids);
		}
		ids.add(id);
	}

	public reduceRedundanciesInRelevantEntities() {
		this._pruefungsrelevanteFelderInternProName.forEach(ids => ValidationCache.reduceRedundancies(ids as any))
	}

	public static reduceRedundancies(ids: Set<RtInternalIdentifier>) {
		const idsWithStar = [...ids].filter(id => ArrayUtils.contains(id.getIndices(), IIdentifier.ALL_INDICES))
		const toRemove = [...ids].filter(id => ValidationCache.isRedundant(id, idsWithStar))
		toRemove.forEach(id => ids.delete(id))
	}

	private static isRedundant(id: RtInternalIdentifier, idsWithStar: RtInternalIdentifier[]): boolean {
		return idsWithStar.some(idWithStar => idWithStar !== id && idWithStar.umfasst(id))
	}

	/**
	 * Liefert die internen prüfungsrelevanten Felder.
	 *
	 * @return die internen prüfungsrelevanten Felder
	 */
	public getPruefungsrelevanteFelderIntern(): Map<string, HashSet<RtInternalIdentifier>> {
		return this._pruefungsrelevanteFelderInternProName;
	}

	public setFullyRelevantGroups(
		fullyRelevantGroupsByName: Map<string, Set<RtInternalIdentifier>>
	): void {
		this._fullyRelevantGroupsIntern = fullyRelevantGroupsByName;
	}

	public getFullyRelevantGroupsIntern(): Map<string, Set<RtInternalIdentifier>> {
		return this._fullyRelevantGroupsIntern;
	}

	/**
	 * Fügt alle übergebenen Entitäten dem Cache hinzu. Menge wird von den
	 * Applikationsbedingungen benötigt.
	 * Übergebene prüfungsrelevante Gruppen sind in dieser Menge nicht expandiert, dies muss von
	 * Applikationsbedingungen beachtet werden.
	 */
	public addAllPruefungsrelevanteEntitiesExtern(set: Set<IIdentifier>): void {
		this._pruefungsrelevanteEntitiesExtern = set;
	}

	/**
	 * Liefert alle prüfungsrelevanten Entitäten (extern).
	 */
	public getAllPruefungsrelevanteEntitiesExtern(): Set<IIdentifier> {
		return this._pruefungsrelevanteEntitiesExtern;
	}

	/**
	 * Returns the information, whether the given entity has been detected as erroneous.<br>
	 *
	 * The entity indices of <code>id</code> should be concrete, semantic indices and parallel
	 * iteration indices have to be resolved before calling this method. Otherwise the id will not
	 * be identified as erroneous.
	 *
	 * @param id 	{@link RtInternalIdentifier} of the entity to be checked.
	 * @param partialIsUnknown 	specifies, whether for a group, one erroneous field leads to an erroneous group.
	 * @return <code>true</code>, if the syntactic validation of the entity failed.
	 */
	public isFehlerhafteEntity(id: RtInternalIdentifier, partialIsUnknown: boolean): boolean {
		if (this._fehlerhafteFelderIntern.has(id)) {
			return true;
		}
		if (!this._erroneousGroups.has(id)) {
			return false;
		}
		// We could return partialIsUnknown directly, but documentation is easier this way.
		if (partialIsUnknown) {
			// If one erroneous field makes the group erroneous, it is erroneous already.
			return true;
		}
		// This case should never be relevant: Either we have a negative language construct,
		// then partialIsUnknown should be true, or it is a positive language construct, then
		// the case, where all fields are erroneous is irrelevant, because then, no field is
		// filled anyway (the exception are preliminary rule errors, but these are corner cases).
		return false;
	}

	/**
	 * Liefert alle fehlerhaften Felder (extern).
	 */
	public getAllFehlerhafteFelderExtern(): Set<IIdentifier> {
		return this._fehlerhafteFelderExtern as any;
	}

	public getDaten(): IData {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return this._data!;
	}

	public setDaten(data: IData): void {
		this._data = data;
	}

	/**
	 * Adds the field to the list of erroneous fields. Also marks containing groups as erroneous.
	 *
	 * @param internId 	the internal id of the field, that caused the error.
	 */
	public markFieldAsInvalidForValidation(internId: RtInternalIdentifier, externId?: RtExternalIdentifier): void {
		if (this._fehlerhafteFelderIntern.tryAdd(internId)) {
			this._fehlerhafteFelderExtern.add(externId ?? this.convert2Extern(internId));
			this._addErroneousEntityToParent(internId);
			if (this._isIndexFieldInstance(internId)) {
				this._indexFieldCache.addInvalidIndexField(internId);
			}
		}
	}

	private _addErroneousEntityToParent(internId: RtInternalIdentifier) {
		let parentId = RtInternalIdentifier.createParentId(internId, this._metaData.getPfadName(internId.getName()));
		while (parentId != null) {
			if (!this._erroneousGroups.tryAdd(parentId)) {
				break;
			}
			parentId = RtInternalIdentifier.createParentId(parentId, parentId.getName());
		}
	}

	/**
	 * For the given identifier, mark all parent groups on the path to the root as relevant for
	 * validation.
	 */
	public markAllParentGroupsRelevant(id: RtInternalIdentifier): void {
		const fullName: string = this._metaData.getPfadName(id.getName());
		const numLevels: number = EntityUtil.countHierarchyLevels(fullName);

		for (let i = 0; i < numLevels - 1; i++) {
			const groupName = EntityUtil.getPathOfHierarchyLevel(fullName, i);
			const groupIndices: number[] = id.getIndices().slice(0, i + 1);
			const groupId = new RtInternalIdentifier(groupName, groupIndices);
			this.addPruefungsrelevantesFeldInternZumNamen(groupId);
		}
	}

	/**
	 * Konvertiere einen internen Identifier in einen externen.
	 * Dazu muss der UniqueName in den Pfadnamen geändert werden.
	 *
	 * @param idIntern 	zu konvertierende Identifier.
	 * @return externer Identifier
	 */
	public convert2Extern(idIntern: RtInternalIdentifier, useSemanticIndex = false): RtExternalIdentifier {
		let externName: string = this._metaData.getPfadName(idIntern.getName());
		if (externName == null) {
			externName = idIntern.getName();
		}
		const resolvedId: RtInternalIdentifier = this.resolveSemanticIndex(idIntern);
		const semIndexSpec: SemanticIndexSpec | undefined = idIntern.getSemanticIndex();
		const indicesContainUnknown: boolean =
			ArrayUtils.indexOf(resolvedId.getIndices(), IIdentifier.UNKNOWN) >= 0;
		if (useSemanticIndex && semIndexSpec != undefined && indicesContainUnknown) {
			const indexValue: string = this.getSemanticIndexValue(semIndexSpec);
			const semIdxLevel: number = ArrayUtils.indexOf(idIntern.getIndices(), IIdentifier.SEMANTIC_INDEX);
			const indexField: RtExternalIdentifier = this._createIndexField(
				externName,
				resolvedId.getIndices(),
				semIdxLevel
			);
			let indexValueAsBasicType: SupportedType | undefined = undefined;
			if (indexValue != undefined && indexValue !== "") {
				indexValueAsBasicType = this._metaData.getMetaField(indexField.getName())
					.convertWithOnlyBasicTypeCheck(indexValue, null, new ResultBaseImpl());
			}
			return new RtExternalIdentifierWithSemanticIndex(
				externName,
				resolvedId.getIndices(),
				indexField,
				indexValue,
				indexValueAsBasicType
			);
		}
		return new RtExternalIdentifier(externName, resolvedId.getIndices());
	}

	/**
	 * Creates the (external) index field identifier for an entity whose repetition is determined by an
	 * unresolved semantic index at the given hierarchy level.
	 */
	private _createIndexField(
		fullNameOfEntityWithSemIdx: string,
		indicesOfEntityWithSemIdx: number[],
		levelOfSemIdx: number
	): RtExternalIdentifier {
		const groupPath: string = EntityUtil.getPathOfHierarchyLevel(fullNameOfEntityWithSemIdx, levelOfSemIdx);
		const indexFields: IMetaField[] = this._metaData.getIndexFields(groupPath);
		// codegen guarantees a single index field for groups referenced with a semantic index
		const indexField: IMetaField = indexFields[0];
		const indexFieldName: string = indexField.getValue(IMetaKeys.FIELD_FULLNAME);
		const indices: number[] = indicesOfEntityWithSemIdx.slice(0, levelOfSemIdx + 2);
		indices[levelOfSemIdx + 1] = 1; // Field index always 1
		return new RtExternalIdentifier(indexFieldName, indices);
	}

	/**
	 * For all index fields, set eligible instances to their default value.
	 *
	 * In the case of partial validation, such index field ids are set as erroneous fields, to
	 * prevent additional error messages in comparison to full validation. If the index field would
	 * be set, value errors could occur that won't occur for full validation. If the index field
	 * would not be set, omission errors for the index field would occur that wouldn't occur in full
	 * validation if a default value is set there.
	 *
	 * @Note The default value is the s-value given by the key {@link IMetaKeys.FIELD_S_VALUE}.
	 */
	public addDefaultValuesForIndexFieldsIfNecessary(): void {
		for (const indexFieldFullName of this._metaData.getIndexFieldFullNames()) {
			this._addDefaultValuesForIndexFieldIfNecessary(indexFieldFullName);
		}
	}

	/**
	 * @param indexFieldFullName full name of the index field to fill with default values
	 */
	private _addDefaultValuesForIndexFieldIfNecessary(indexFieldFullName: string): void {
		for (const indexFieldId of this._getIndexFieldIdsEligibleForDefaultValueHandling(indexFieldFullName)) {
			if (this._pruefungsrelevanteEntitiesExtern.size > 0) {
				// In case of partial validation we set the index fields, which should be filled with their default value,
				// as erroneous. This avoids the mandatory check, uniqueness check and evaluation of rules
				// in which these fields are referenced.
				// This is necessary to avoid having additional errors in partial validation in comparison with full validation.
				this.markFieldAsInvalidForValidation(indexFieldId);
			} else {
				this._setIndexFieldToDefaultValue(indexFieldId);
			}
		}
	}

	private _setIndexFieldToDefaultValue(indexFieldId: RtInternalIdentifier): void {
		const formatDef = this._metaData.getIFormatDefinition(indexFieldId.getName());
		// only used in `_addDefaultValuesForIndexFieldIfNecessary`, we require the svalue to be set
		/* eslint-disable-next-line @typescript-eslint/no-non-null-assertion */
		const sValue = this._getSValue(indexFieldId.getName())!;

		const uiValue = formatDef.convert(sValue, this._presentationInfo, true, this._mode.customFieldTypeFactory);
		this.addValidFieldValue(indexFieldId, uiValue, sValue);
	}

	/**
	 * Determine which instances of the given index field are eligible for default value handling.
	 *
	 * An index field instance can be filled with its default value only if:
	 * - the index field has an S-Value defined (currently only possible for enumerations)
	 * - the index field is not a mandatory field (through check box)
	 * - there is exactly 1 instance of the index group within an instance of the index group parent, and the index field in that group instance is not filled
	 * @param indexFieldFullName
	 * @returns index field ids for which a default value should be set (without duplicates)
	 */
	protected _getIndexFieldIdsEligibleForDefaultValueHandling(indexFieldFullName: string): RtInternalIdentifier[] {
		// skip if all instances of the index field are ineligible
		const indexFieldUniqueName = this._metaData.getUniqueName(indexFieldFullName);
		if (!this._getSValue(indexFieldUniqueName) || this._metaData.isExplicitlyMandatory(indexFieldUniqueName)) {
			return [];
		}

		const indexGroupFullName = EntityUtil.getParentPath(indexFieldFullName);

		const result: RtInternalIdentifier[] = [];
		for (const [indexGroupParentIndices, indexGroupIds] of this._getIndexGroupParentIndicesToIndexGroupIds(indexGroupFullName)) {
			if (indexGroupIds.length === 1
				&& this._indexFieldCache.noIndexFieldsFilled(indexGroupFullName, indexGroupParentIndices)) {
				const indexGroupIndices = indexGroupIds[0].getIndices();
				const indexFieldIndices = indexGroupIndices.concat([1]);
				result.push(new RtInternalIdentifier(indexFieldUniqueName, indexFieldIndices));
			}
		}
		return result;
	}

	/**
	 * @returns a map
	 * - from: the indices of filled instances of the given index group's parent
	 * - to: all filled index group instances within (i.e. all filled repetitions)
	 */
	private _getIndexGroupParentIndicesToIndexGroupIds(indexGroupFullName: string): ArrayMap<number[], RtInternalIdentifier[]> {
		const indexGroupParentIndicesToIndexGroupIds: ArrayMap<number[], RtInternalIdentifier[]> = new ArrayMap();
		for (const indexGroupId of this.groupFillCache.getFilledGroupInstances(indexGroupFullName)) {
			const indices = indexGroupId.getIndices();
			const parentIndices = indices.slice(0, -1);
			let indexGroupIds = indexGroupParentIndicesToIndexGroupIds.get(parentIndices);
			if (indexGroupIds == undefined) {
				indexGroupIds = [];
				indexGroupParentIndicesToIndexGroupIds.set(parentIndices, indexGroupIds);
			}
			indexGroupIds.push(indexGroupId);
		}
		return indexGroupParentIndicesToIndexGroupIds;
	}

	protected _getSValue(uniqueName: string): string | undefined {
		const formatDef: IFormatDefinition = this._metaData.getIFormatDefinition(uniqueName);
		// at the moment an s value can only be defined for enumerations
		return formatDef.getWert(IMetaKeys.FIELD_S_VALUE);
	}

	/**
	 * Get the union of the value sets of (corresponding) index fields with respect to a concrete
	 * iteration.
	 *
	 * For partial validation the returned list is empty if the index fields of the groups are not
	 * relevant for all repetitions.
	 *
	 * @param indexGroupFullNames 	full names of the groups, each containing an index field with the same name.
	 *                            each group's IRG must be on the error field path
	 * @param iterationStateIndices 	indices of the current iteration
	 * @return present index values for the current iteration or an empty list if the index fields
	 *         are not relevant for all repetitions of the current iteration
	 */
	public getIterationValuesForGroups(indexGroupFullNames: string[], iterationStateIndices: number[]): string[] {
		return this._getIterationValuesForGroups(
			indexGroupFullNames.map(indexGroupFullName => [
				indexGroupFullName, this._getParentIndices(indexGroupFullName, iterationStateIndices)]));
	}

	protected _getIterationValuesForGroups(indexGroupFullNamesWithParentIndices: [string, number[]][]): string[] {
		const usedIndexValues: SortedSet<string> = new SortedSet(); // sort w.r.t. index value
		for (const [indexGroupFullName, parentIndices] of indexGroupFullNamesWithParentIndices) {
			const iterationIndexes4Group = this._indexFieldCache.getIndexMappingForGroup(indexGroupFullName, parentIndices);
			if (iterationIndexes4Group != undefined) {
				for (const key of iterationIndexes4Group.keys()) {
					usedIndexValues.add(key);
				}
			}
		}
		return Array.from(usedIndexValues);
	}

	/**
	 *
	 * @param groupName full name of the group. Might not necessarily be on the error field path,
	 *                  but the innermost repeatable group (IRG) above the group (if it exists)
	 * 					must be on the error field path.
	 * @param iterationStateIndices 	indexes of the current iteration w.r.t. the error field path
	 * @return parent indices of the group w.r.t. the current iteration , these contain
	 *         1 for non repeatable groups and the index of <code>indexes</code> for
	 *         repeatable groups
	 *
	 * For example, given a parallel iteration over:
	 * - `/Root[5]/A[5]/B[5]/IndexGroup1[5]/IndexField1`
	 * - `/Root[5]/X[1]/Y[1]/Z[1]/IndexGroup2[5]/IndexField2`
	 *
	 * The function might receive:
	 * - `groupName` = `"/Root/X/Y/Z/IndexGroup2"`
	 * - `iterationStateIndices` = `[2, 3, 4, -6, 1]` (for an error field in `IndexGroup1`)
	 *
	 * The result would be: `[2, 1, 1, 1]`
	 */
	private _getParentIndices(groupName: string, iterationStateIndices: number[]): number[] {
		const maxIndices: number[] = this._metaData.getMaxGroupIndices(groupName);
		const parentIndices: number[] = new Array<number>();
		for (let i = 0; i < maxIndices.length - 1; i++) {
			if (maxIndices[i] === 1) { // if maxIndices === 1, then the group can potentially be not on the error field path
				// (because we are already below the groups IRG) -> here we use index 1 instead of copying the
				// iterationStaetIndices (which might be > 1, if the error field is additionally repeatable on lower levels)
				// See also the example in this method's typedoc.
				parentIndices.push(1);
			} else {
				// If the max index is > 1, the group has to be on the error field path
				// -> take the index from the current iteration
				parentIndices.push(iterationStateIndices[i]);
			}
		}

		return parentIndices;
	}

	/**
	 * Precondition: the group has to contain exactly one index field.
	 *
	 * @param groupName 	full name of group
	 * @return unique name of the index field of the group
	 */
	private _getIndexFieldUniqueName(groupName: string): string {
		const indexFields: IMetaField[] = this._metaData.getIndexFields(groupName);
		if (indexFields.length !== 1) {
			throw new Error();
		}
		const indexField: IMetaField = indexFields[0];

		return indexField.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
	}

	/**
	 * Returns an identifier for the corresponding index field of `id`, which has the
	 * same parent indices above the indexed level. The indexed level is identified by
	 * {@link IIdentifier.SEMANTIC_INDEX} in the indices of `id`.
	 *
	 * **Precondition**: the indices of the id have to contain {@link IIdentifier.SEMANTIC_INDEX}.
	 */
	getIndexFieldId(id: RtInternalIdentifier): RtInternalIdentifier {
		const fullFieldName = this._metaData.getPfadName(id.getName());
		const indexedGroupLevel = ArrayUtils.indexOf(id.getIndices(), IIdentifier.SEMANTIC_INDEX);
		const indexedGroupName = EntityUtil.getPathOfHierarchyLevel(fullFieldName, indexedGroupLevel);
		const indexFieldUniqueName = this._getIndexFieldUniqueName(indexedGroupName);
		const indices = ArrayUtils.copyOf(id.getIndices(), 0, indexedGroupLevel + 2);
		indices[indices.length - 1] = 1; // the row is not repeatable
		return new RtInternalIdentifier(
			indexFieldUniqueName,
			indices,
			undefined,
			id.getSemanticIndex(),
			id.getIterationState()
		);
	}

	protected _isIndexFieldInstance(id: RtInternalIdentifier): boolean {
		return this._metaData.isIndexField(id.getName());
	}

	isIndexFieldInvalidForOneRepetition(indexFieldId: RtInternalIdentifier): boolean {
		return this._indexFieldCache.isIndexFieldInvalidForOneRepetition(indexFieldId);
	}

	isValidationPartWith3ValueLogic(): boolean {
		return this._isValidationPartWith3ValueLogic;
	}

	setValidationPartWith3ValueLogic(): void {
		this._isValidationPartWith3ValueLogic = true;
	}
}
