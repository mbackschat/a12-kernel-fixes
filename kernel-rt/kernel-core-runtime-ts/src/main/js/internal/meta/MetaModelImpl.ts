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
import type { IMetaField, IMetaGroup, IMetaModel, IMetaRule } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { HashSet, IIdentifier, IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IDynEnumsIntern } from "../core/IDynEnumsIntern.js";
import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { Container_t } from "../formatdef/Container_t.js";
import type { Feld_t } from "../formatdef/Feld_t.js";
import type { Identifier_t } from "../formatdef/Identifier_t.js";
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import type { Model_t } from "../formatdef/Model_t.js";
import type { Regel_t } from "../formatdef/Regel_t.js";
import { EntityUtil } from "../util/EntityUtil.js";
import { FeldUtil } from "../util/FeldUtil.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";
import { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";
import type { RtIdentifierTemplate } from "../util/RtIdentifierTemplate.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import { SemanticIndexSpec } from "../util/SemanticIndexSpec.js";
import type { TrieNode } from "../util/TrieNode.js";

import { MetaContainer } from "./MetaContainer.js";
import { MetaDataValidierungIntern } from "./MetaDataValidierungIntern.js";
import { MetaEntity } from "./MetaEntity.js";
import { MetaFeld } from "./MetaFeld.js";
import type { MetaLokalePflichtInfo } from "./MetaLokalePflichtInfo.js";
import { MetaRegel } from "./MetaRegel.js";

export interface MetaModelProxy {
	getVordruckName(id: IIdentifier): string;
}

interface IAdditivInfo {
	alleFelderAufEinemVordruck(): boolean;

	getVordrucke(): Set<string>;

	getLfdNr(): number;
}

export class MetaModelImpl
	extends MetaEntity
	implements IMetaKeysInternal, IMetaModel, MetaModelProxy {

	private _metaDataValidierung: IMetaDataValidierung;
	private static _schluessel: Set<string> = new Set<string>();
	private static _internalKeys: Set<string> = new Set<string>();

	private _pflichtFelderFSList: IIdentifier[] = new Array<IIdentifier>();
	private _pflichtFelderMFList: IMetaField[] = new Array<IMetaField>();
	private _vordruckPflichtFelderMap: Map<string, IMetaField[]> = new Map<string, IMetaField[]>();

	private _felderMap: Map<string, IMetaField> = new Map<string, IMetaField>();
	private _felderVollerPfadMap: Map<string, IMetaField> = new Map<string, IMetaField>();

	private _regelnMap: Map<string, IMetaRule> = new Map<string, IMetaRule>();
	private _regelnVollerPfadMap: Map<string, IMetaRule> = new Map<string, IMetaRule>();

	private _containerAlleMap: Map<string, IMetaGroup> = new Map<string, IMetaGroup>();
	private _rootContainer: IMetaGroup[] = new Array<IMetaGroup>();
	private _vordruckMap: Map<string, MetaContainer> = new Map<string, MetaContainer>();
	private _vordruckNameFelderMap: Map<string, IMetaField[]> = new Map<string, IMetaField[]>();

	private _indexFieldFullNames: string[] = [];
	private _hasIndexFields: boolean;

	private _uniqueFeldNameToFormatDefinition: Map<string, IFormatDefinition> = new Map<
		string,
		IFormatDefinition
	>();

	private _globalFields: Set<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>() as any;

	private _berechneteFelderServer: HashSet<IMetaField> = new HashSet<IMetaField>();
	private _dynamicEnumerationFields: HashSet<IMetaField> = new HashSet<IMetaField>();

	/**
	 * (annotation-key) -> (entity-fullName -> annotation-value)
	 * For each annotation key (key of the "outer" map): for each entity to which the annotation key is assigend the key of the
	 * "inner" map is the full name of the entity and the value of the "inner" map is the respective value of the annotation.
	 */
	private _annotation2EntityValues: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();

	/**
	 * Speichert zu einer Menge von IIdentifiern die referenzierten Vordrucke und laufende Nummern
	 * wobei für die laufenden Nummern 0 gesetzt wird, wenn verschiedene laufende Nummern
	 * referenziert werden.<br>
	 */
	private AdditivInfo = class implements IAdditivInfo {
		private _vordrucke: Set<string> = new Set<string>();
		private _lfdNr = 0;

		constructor(private _parent: MetaModelProxy, additiv: HashSet<RtInternalIdentifier>) {
			let first = true;
			for (const id of additiv) {
				this._vordrucke.add(this._parent.getVordruckName(id));
				if (first) {
					this._lfdNr = id.getIndices()[0];
					first = false;
				} else {
					if (this._lfdNr !== 0 && this._lfdNr !== id.getIndices()[0]) {
						this._lfdNr = 0;
					}
				}
			}
		}

		/**
		 * Gibt <code>true</code> zurück, wenn alle Felder in dieser AdditivInfo auf dem gleichen
		 * Vordruck liegen - ohne die laufenden Nummern zu berücksichtigen.
		 */
		public alleFelderAufEinemVordruck(): boolean {
			return this._vordrucke.size === 1;
		}

		public getVordrucke(): Set<string> {
			return this._vordrucke;
		}

		public getLfdNr(): number {
			return this._lfdNr;
		}
	};

	/**
	 * Don't use this. This is just for a static initialization.
	 */
	static _constructor = (() => {
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_GROUP_LIST);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_META_RULE);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_META_GROUP);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_META_FIELD);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_APPLICATION_CONDITION);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_CUSTOM_FIELD_TYPES_TO_BE_PROVIDED);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_INDEX_FIELDS);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_INDEX_FIELD_FULL_NAMES);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_CALC_FIELDS);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_MANDATORY_FIELDS);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_LANGUAGES);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_DYNAMIC_ENUM_FIELDS);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_ANNOTATION);
		MetaModelImpl._schluessel.add(IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES);

		MetaModelImpl._internalKeys.add(IMetaKeysInternal.MODEL_GLOBAL_FIELDS);
		MetaModelImpl._internalKeys.add(IMetaKeysInternal.MODEL_TIME_ZONE);
		MetaModelImpl._internalKeys.add(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD);
	})();

	/**
	 * Konstruktor liest alle Daten aus den _t Strukturen aus und speichert sie in den
	 * Laufzeitstrukturen.
	 * Beim lesen werden auch die entsprechenden Containment-Beziehungen aufgebaut.
	 */
	constructor(
		da: Model_t,
		felder: Feld_t[],
		regeln: Regel_t[],
		container: Container_t[],
		legalCodepoints: number[][],
		legalGraphemeTrie: TrieNode,
		dynEnumCalc?: { (): IDynEnumsIntern }
	) {
		super(MetaModelImpl._schluessel, MetaModelImpl._internalKeys);

		// ////////////////////////////////////////////////////////////
		// create the group tree from the flat structure of Container_t
		this.createContainerTreeAndDetermineIndexFields(container);

		// and determine if there are index fields in the model at all.
		this._hasIndexFields = this._indexFieldFullNames.length > 0;

		// Das Befüllen der folgenden Maps dient dem performanten Zugriff auf Metainformationen
		// Erzeuge alle Felder.  Diese werden sowohl in die Container Struktur
		// als auch in die Vordruck-Struktur eingeordnet.

		for (const f of felder) {
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const vordruckName: string = EntityUtil.getRootGroupName(f.pfadName)!;

			const feld: MetaFeld = new MetaFeld(this, f, da.dezimalTrenner);

			// füge das Feld in die alte Struktur ein
			this._felderMap.set(f.uniqueName, feld);
			this._felderVollerPfadMap.set(f.pfadName, feld);
			this._uniqueFeldNameToFormatDefinition.set(f.uniqueName, f.formatDefintion);
			let vordruckFelder: IMetaField[] | undefined = this._vordruckNameFelderMap.get(vordruckName);
			if (vordruckFelder == undefined) {
				vordruckFelder = new Array<IMetaField>();
				this._vordruckNameFelderMap.set(vordruckName, vordruckFelder);
			}
			vordruckFelder.push(feld);

			// füge das Feld in die ContainerStruktur ein.
			const containerPfad: string = EntityUtil.getParentPath(f.pfadName);
			const cont: MetaContainer = <MetaContainer>this._containerAlleMap.get(containerPfad);
			cont.addFeld(feld);

			// Falls das Feld ein Pflichtfeld ist, füge es in die Liste der IIdentifier und
			// in die MetaFeldListe hinzu
			if (f.isPflichtFeld) {
				this._pflichtFelderFSList.push(new RtExternalIdentifier(f.uniqueName, [0, 0]));
				this._pflichtFelderMFList.push(feld);
			}
			feld.setMaxIndices(this);

			// if the field is global it is added to the set of global fields
			if (FeldUtil.isGlobal(f)) {
				const length: number = EntityUtil.countHierarchyLevels(f.pfadName);
				const indices: number[] = new Array<number>(length);
				for (let i = 0; i < length; i++) {
					indices[i] = 0;
				}
				this._globalFields.add(new RtExternalIdentifier(f.pfadName, indices));
			}
		}

		// Regeln können erst nach den Feldern erzeugt werden,  da hier eine Verknüpfung zu
		// den Felder verwendet wird. Regeln werden in Container Struktur und in die
		// Vordruckstruktur eingehängt.
		for (const r of regeln) {
			const regel: MetaRegel = this._createRegel(r, felder);

			this._regelnMap.set(r.name, regel);
			this._regelnVollerPfadMap.set(r.pfadName, regel);

			const containerPfad: string = EntityUtil.getParentPath(r.pfadName);
			const cont: MetaContainer = <MetaContainer>this._containerAlleMap.get(containerPfad);
			cont.addRegel(regel);
		}

		// Information für Pflichtfelder wird in den internen Strukturen initialisiert.
		for (const [key, value] of this._vordruckNameFelderMap) {
			const vordruckPflichtfelderList: IMetaField[] = new Array<IMetaField>();
			this._vordruckPflichtFelderMap.set(key, vordruckPflichtfelderList);
			for (const iFeld of value) {
				const feld: MetaFeld = <MetaFeld>iFeld;
				let liste: number[] = feld.getPflichtfeldIntern();
				if (liste.length > 0) {
					this._pflichtFelderMFList.push(feld);
				}
				liste = feld.getVordruckPflichtfeldIntern();
				if (liste.length > 0) {
					vordruckPflichtfelderList.push(feld);
				}
			}
		}

		this.addWert(IMetaKeys.MODEL_GROUP_LIST, this._rootContainer);
		this.addWert(IMetaKeys.MODEL_APPLICATION_CONDITION, da.applBedingungen);
		this.addWert(IMetaKeys.MODEL_CUSTOM_FIELD_TYPES_TO_BE_PROVIDED, da.customFieldTypes);
		this.addWert(IMetaKeys.MODEL_INDEX_FIELDS, this._hasIndexFields);
		this.addWert(IMetaKeys.MODEL_INDEX_FIELD_FULL_NAMES, this._indexFieldFullNames);
		this.addWert(
			IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD,
			new Array<MetaLokalePflichtInfo>()
		);

		this.addWert(IMetaKeys.MODEL_LANGUAGES, da.unterstuetzteSprachen);

		this.addWert(IMetaKeysInternal.MODEL_GLOBAL_FIELDS, this._globalFields);

		this.addWert(
			IMetaKeys.MODEL_DYNAMIC_ENUM_FIELDS,
			this._dynamicEnumerationFields
		);

		this.addWert(IMetaKeys.MODEL_ANNOTATION, undefined); // will be overwritten by additional values if any annotation is provided.
		this.addWert(IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES, undefined); // Register the key.

		const internalKeys: Set<string> = da.internalAdditionalKeys
		for (const [key, value] of da.additionalKeyValues) {
			if (internalKeys.has(key)) {
				this.addAdditionalInternalKey(key);
			} else {
				this.addAdditionalKey(key);
			}
			this.addWert(key, value);
		}

		this._metaDataValidierung = new MetaDataValidierungIntern(
			this,
			da.dezimalTrenner,
			legalCodepoints,
			legalGraphemeTrie,
			dynEnumCalc
		);

		// Collect annotations map
		this._addToAnnotationsMap(da.getAdditionalValue("Annotation"), "");
		for (const f of felder) {
			this._addToAnnotationsMap(f.getAdditionalValue("Annotation"), f.pfadName);
		}
		for (const ct of container) {
			this._addToAnnotationsMap(ct.getAdditionalValue("Annotation"), ct.fullName);
		}
		for (const r of regeln) {
			this._addToAnnotationsMap(r.getAdditionalValue("Annotation"), r.pfadName);
		}
	}


	private _addToAnnotationsMap(annotations: any, fullName: string): void {
		if (annotations == null || !(annotations instanceof Map)) {
			return;
		}
		annotations.forEach((value: any, key: string) => {
			if (this._annotation2EntityValues.has(key)) {
				this._annotation2EntityValues.get(key)?.set(fullName, value);
			} else {
				this._annotation2EntityValues.set(key, new Map().set(fullName, value));
			}
		})
		return;
	}


	public createContainerTreeAndDetermineIndexFields(container: Container_t[]): void {
		for (const ct of container) {
			const pfad: string = ct.fullName;

			const mc: MetaContainer = new MetaContainer(ct);
			this._containerAlleMap.set(pfad, mc);

			const parentPfad: string = EntityUtil.getParentPath(pfad);
			// add to parent container and add indices; Container_t for parents are always generated before their children.
			if (parentPfad.length !== 0) {
				const parent: MetaContainer = <MetaContainer>this._containerAlleMap.get(parentPfad);
				parent.addIMetaGroup(mc);
				mc.setMaxIndices(parent, ct.maxIndex);
			} else {
				// for root groups:
				// container is added to root container list
				this._rootContainer.push(mc);
				this._vordruckMap.set(EntityUtil.getEntityName(ct.fullName), mc);
				mc.setMaxIndices(undefined, ct.maxIndex);
			}
			ct.indexFields?.forEach(indexFieldShortName =>
				this._indexFieldFullNames.push(EntityUtil.createPath(pfad, indexFieldShortName)));
		}
	}

	/**
	 * Erzeuge eine Regel aus den Regelstrukturen.
	 */
	private _createRegel(r: Regel_t, f: Feld_t[]): MetaRegel {
		const refIdentifierAuslassungIntern: HashSet<RtInternalIdentifier> =
			new HashSet<RtInternalIdentifier>();
		if (r.refAuslassungsIdentifier != undefined) {
			for (const id of r.refAuslassungsIdentifier) {
				refIdentifierAuslassungIntern.add(this._createInternalIdentifierWithUniqueName(f, id));
			}
		}

		// Speichere die referenzierten FeldIdentifier
		const refIdentifierIntern: HashSet<RtInternalIdentifier> | undefined =
			new HashSet<RtInternalIdentifier>();
		if (r.refIdentifier) {
			for (const id of r.refIdentifier) {
				refIdentifierIntern.add(this._createInternalIdentifierWithUniqueName(f, id));
			}
		}

		if (r.isServerBerechnungsRegel) {
			// das berechnete Feld ist für serverseitige Berechnungen immer das Fehlerfeld
			const berFeldServer: MetaFeld = <MetaFeld>this._felderVollerPfadMap.get(r.fehlerFeld);
			this._berechneteFelderServer.add(berFeldServer);

			const operanden: HashSet<RtExternalIdentifier> = berFeldServer._operandenMengeServer;
			let berFeldServerId: RtExternalIdentifier;

			// hole das Fehlerfeld aus den referenzierten Feldern, das immer das berechnete Feld ist.
			for (const id of r.refIdentifier) {
				const fieldIdentifier: RtExternalIdentifier = this._createExternalIdentifierWithPathName(
					f,
					id,
					IIdentifier.ALL_INDICES
				);
				if (fieldIdentifier.getName() === r.fehlerFeld) {
					berFeldServerId = fieldIdentifier;
				} else {
					operanden.add(fieldIdentifier);
				}
			}

			for (const operand of operanden) {
				// Wandle jedes referenzierte Feld,  das nicht das Fehlerfeld ist in ein Metafeld um, um die
				// inverse Beziehung zu speichern.
				const operandFeld: MetaFeld = <MetaFeld>this._felderVollerPfadMap.get(operand.getName());
				const zielMengeFuerBerechnung: HashSet<RtExternalIdentifier> =
					operandFeld._zielMengeFuerBerechnungServer;
				// TODO: Fix this the next time the file is edited.
				// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
				zielMengeFuerBerechnung.add(berFeldServerId!);
			}
		}

		if (r.isDynamicEnumerationRule) {
			const dynamicEnumerationField: MetaFeld = <MetaFeld>(
				this._felderVollerPfadMap.get(r.fehlerFeld)
			);
			this._dynamicEnumerationFields.add(dynamicEnumerationField);
			dynamicEnumerationField.addWert(IMetaKeys.FIELD_DYNAMIC_ENUMERATION, true);

			const operanden: HashSet<RtExternalIdentifier> =
				dynamicEnumerationField.operandSetDynamicEnum;
			for (const id of r.refIdentifier) {
				const fieldIdentifier: RtExternalIdentifier = this._createExternalIdentifierWithPathName(
					f,
					id,
					IIdentifier.ALL_INDICES
				);
				if (fieldIdentifier.getName() !== r.fehlerFeld) {
					operanden.add(fieldIdentifier);
				}
			}
		}
		// Referenced entities (may contain groups) for the V2 possibly-causing-entities API. These are
		// already RtIdentifierTemplates produced by the code generator, so no unique-name resolution is
		// needed.
		const refEntity: HashSet<RtIdentifierTemplate> = new HashSet<RtIdentifierTemplate>(r.refEntity);
		const refOmissionEntity: HashSet<RtIdentifierTemplate> = new HashSet<RtIdentifierTemplate>(
			r.refOmissionEntity
		);

		return new MetaRegel(
			this,
			r,
			refIdentifierIntern,
			refIdentifierAuslassungIntern,
			refEntity,
			refOmissionEntity
		);
	}

	private _createExternalIdentifierWithPathName(
		f: Feld_t[],
		id: Identifier_t,
		semIdxOrParallelIterationValue: number
	): RtExternalIdentifier {
		const idx: number[] = new Array<number>(id.indices.length);
		for (let i = 0; i < id.indices.length; i++) {
			idx[i] =
				id.indices[i] !== IIdentifier.SEMANTIC_INDEX &&
					id.indices[i] !== IIdentifier.PARALLEL_ITERATION
					? id.indices[i]
					: semIdxOrParallelIterationValue;
		}
		return new RtExternalIdentifier(f[id.feldIndex].pfadName, idx);
	}

	private _createInternalIdentifierWithUniqueName(
		f: Feld_t[],
		id: Identifier_t
	): RtInternalIdentifier {
		let semIdx: SemanticIndexSpec | undefined;
		if (id.semanticIndexValue) {
			semIdx = new SemanticIndexSpec(id.semanticIndexValue);
		} else if (id.semanticIndex) {
			semIdx = new SemanticIndexSpec(
				this._createInternalIdentifierWithUniqueName(f, id.semanticIndex)
			);
		}
		return new RtInternalIdentifier(
			f[id.feldIndex].uniqueName,
			id.indices,
			undefined,
			semIdx
		);
	}

	/**
	 * Allgemeine getter-Methode zum Zugriff auf die verschiedenen Werte.
	 *
	 */
	public getValue(key: string, ...parameter: any[]): any {
		if (IMetaKeys.MODEL_ANNOTATION === key) {
			return this._getAnnotation(key, parameter);
		}
		if (IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES === key) {
			return this._getAllAnnotatedEntities(parameter);
		}
		if (IMetaKeys.MODEL_CALC_FIELDS === key) {
			return this._berechneteFelderServer;
		}

		if (IMetaKeys.MODEL_MANDATORY_FIELDS === key) {
			return this._getPflichtFelderWert(parameter);
		}

		if (IMetaKeys.MODEL_META_FIELD === key) {
			return this._getMetaFeldByParameter(parameter);
		}
		if (IMetaKeys.MODEL_META_RULE === key) {
			return this._getMetaRegel(parameter);
		}
		if (IMetaKeys.MODEL_META_GROUP === key) {
			return this._getMetaContainer(parameter);
		}
		if (IMetaKeysInternal.MODEL_METAFELDUNIQUE === key) {
			return this._getMetaFeldUnique(parameter);
		}

		if (IMetaKeys.MODEL_APPLICATION_CONDITION === key) {
			// kopiere Werte:
			const werte: Set<string> = super.getValue(IMetaKeys.MODEL_APPLICATION_CONDITION);
			let result: Set<string> | undefined;
			if (werte) {
				result = new Set<string>();
				for (const s of werte) {
					result.add(s);
				}
			}
			return result;
		}

		if (IMetaKeys.MODEL_GROUP_LIST === key) {
			// kopiere Werte:
			const werte: IMetaGroup[] = super.getValue(IMetaKeys.MODEL_GROUP_LIST);
			let result: IMetaGroup[] | undefined;
			if (werte) {
				result = new Array<IMetaGroup>();
				for (const m of werte) {
					result.push(m);
				}
			}
			return result;
		}
		return super.getValue(key);
	}


	private _getAllAnnotatedEntities(parameter: any[]): Map<string, string> | undefined {
		if (parameter == null || parameter.length !== 1 || typeof parameter[0] !== "string") {
			throw new Error(`For lookups with key '${IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES}', a single parameter of type string must be given.`);
		}
		return this._annotation2EntityValues.get(parameter[0]);
	}

	private _getMetaFeldUnique(parameter: any[]): any {
		const name: string | undefined = this._getNameFromParams(
			IMetaKeysInternal.MODEL_METAFELDUNIQUE,
			parameter
		);
		if (!name) {
			return undefined;
		}
		return this._felderMap.get(name);
	}

	private _getPflichtFelderWert(parameter: any[]): any {
		if (
			!this.checkParameterAngegeben(
				IMetaKeys.MODEL_MANDATORY_FIELDS,
				1,
				parameter
			)
		) {
			return undefined;
		}
		try {
			const additiv: Set<IIdentifier> = parameter[0];
			const aIntern: HashSet<RtInternalIdentifier> = MetaModelImpl.transformToIdsWithUniqueName(
				additiv,
				this._metaDataValidierung
			);
			if (aIntern.size <= 0) {
				return undefined;
			}
			return MetaModelImpl.transformToIdsWithFullName(
				this._getPflichtFelderInternal(aIntern) as any,
				this._metaDataValidierung
			);
		} catch (_error) {
			return undefined;
			// bei falsch uebergebenem Parameter wird immer undefined zurueckgegeben
		}
	}

	private _getPflichtFelderInternal(
		additiv: HashSet<RtInternalIdentifier>
	): HashSet<RtInternalIdentifier> {
		const result: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();
		const addInfo: IAdditivInfo = new this.AdditivInfo(this, additiv);

		// Wenn es eine lokale Pflichtinfo zu dem übergebenen Additiv gibt,  gebe diese zurück.
		const infos: MetaLokalePflichtInfo[] = this.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD);

		for (const metaInfo of infos) {
			for (const field of metaInfo.getPMengeWennMetaAdditivUmfasstMenge(
				additiv
			)) {
				result.add(field);
			}
		}

		if (addInfo.alleFelderAufEinemVordruck()) {
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const vordruckName: string = Array.from(addInfo.getVordrucke()).find(
				(s: string) => s != undefined
			)!;
			for (const field of this._convertVordruckPflichtfelder(
				vordruckName,
				addInfo.getLfdNr()
			)) {
				result.add(field);
			}
		} else {
			for (const field of this._convertPflichtfelder()) {
				result.add(field);
			}
		}
		return result;
	}

	/**
	 * Diese Methode liefert zum Feldnamen das MetaFeld.
	 *
	 * @param parameter	es wird ein String-Parameter erwartet
	 * @return MetaFeld oder undefined, falls kein Feld mit dem angegebenen Namen gefunden wurde.
	 */
	private _getMetaFeldByParameter(parameter: any[]): IMetaField | undefined {
		const name: string | undefined = this._getNameFromParams(
			IMetaKeys.MODEL_META_FIELD,
			parameter
		);
		if (!name) {
			return undefined;
		}
		let result: IMetaField | undefined = this._felderVollerPfadMap.get(name);

		if (!result) {
			result = this._felderMap.get(name);
		}
		return result;
	}

	/**
	 * Diese Methode liefert zum Regelnamen das MetaRegel Objekt.
	 *
	 * @param parameter	es wird ein String-Parameter erwartet
	 * @return MetaFeld oder undefined, falls keine Regel mit dem angegebenen Namen gefunden wurde.
	 */
	private _getMetaRegel(parameter: any[]): IMetaRule | undefined {
		const name: string | undefined = this._getNameFromParams(
			IMetaKeys.MODEL_META_RULE,
			parameter
		);
		if (!name) {
			return undefined;
		}
		let result: IMetaRule | undefined = this._regelnVollerPfadMap.get(name);
		if (!result) {
			result = this._regelnMap.get(name);
		}
		return result;
	}

	private _getMetaContainer(parameter: any[]): IMetaGroup | undefined {
		const name: string | undefined = this._getNameFromParams(
			IMetaKeys.MODEL_META_GROUP,
			parameter
		);
		if (!name) {
			return undefined;
		}
		return this._containerAlleMap.get(name);
	}

	private _getNameFromParams(key: string, parameter: any[]): string | undefined {
		if (!this.checkParameterAngegeben(key, 1, parameter)) {
			return undefined;
		}
		if (typeof parameter[0] === "string") {
			return parameter[0];
		}
		return undefined;
	}

	private _convertPflichtfelder(): HashSet<RtInternalIdentifier> {
		const result: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();
		for (const iFeld of this._pflichtFelderMFList) {
			const feld: MetaFeld = <MetaFeld>iFeld;
			const liste: number[] = feld.getPflichtfeldIntern();
			const name: string = feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			const anzahlEbenen = this._metaDataValidierung.getMaxFieldIndices(name).length;
			for (const i of liste) {
				const idx: number[] = new Array<number>(anzahlEbenen);
				idx[0] = i;
				for (let j = 1; j < anzahlEbenen; j++) {
					idx[j] = 0;
				}
				result.add(new RtInternalIdentifier(name, idx));
			}
		}
		return result;
	}

	private _convertVordruckPflichtfelder(
		vordruckName: string,
		lfdNr: number
	): HashSet<RtInternalIdentifier> {
		const result: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		for (const iFeld of this._vordruckPflichtFelderMap.get(vordruckName)!) {
			const feld: MetaFeld = <MetaFeld>iFeld;
			const liste: number[] = feld.getVordruckPflichtfeldIntern();
			const name: string = feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			const anzahlEbenen = this._metaDataValidierung.getMaxFieldIndices(name).length;
			if (lfdNr === 0) {
				for (const i of liste) {
					const idx: number[] = new Array<number>(anzahlEbenen);
					idx[0] = i;
					for (let j = 1; j < anzahlEbenen; j++) {
						idx[j] = 0;
					}
					result.add(new RtInternalIdentifier(name, idx));
				}
			} else {
				if (liste.indexOf(0) > -1 || liste.indexOf(lfdNr) > -1) {
					const idx: number[] = new Array<number>(anzahlEbenen);
					idx[0] = lfdNr;
					for (let j = 1; j < anzahlEbenen; j++) {
						idx[j] = 0;
					}
					result.add(new RtInternalIdentifier(name, idx));
				}
			}
		}
		return result;
	}

	/**
	 * Gebe den Vordrucknamen zu dem Identifier zurück
	 *
	 * @param id	ein Feld
	 * @return Name des Vordrucks.
	 */
	getVordruckName(id: RtInternalIdentifier): string {
		const parameter: any[] = [];
		parameter.push(id.getName());
		const feld: MetaFeld = <MetaFeld>this._getMetaFeldByParameter(parameter);
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const rootContainer: IMetaGroup = feld.getTopContainer()!;
		return rootContainer.getValue(IMetaKeys.GROUP_SHORT_NAME);
	}

	public getMetaDataValidierung(): IMetaDataValidierung {
		if (this._metaDataValidierung == undefined) {
			throw new Error("The 'IMetaDataValidierung' object was not created yet.");
		}
		return this._metaDataValidierung;
	}

	/**
	 * @return zugehörige {@link IFormatDefinition}
	 */
	public getIFormatDefinition(uniqueName: string): IFormatDefinition | undefined {
		return this._uniqueFeldNameToFormatDefinition.get(uniqueName);
	}

	public static transformToIdsWithUniqueName(
		ids: Set<IIdentifier>,
		metaData: IMetaDataValidierung
	): HashSet<RtInternalIdentifier> {
		const idsIntern: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();
		for (const id of ids) {
			idsIntern.add(
				new RtInternalIdentifier(metaData.getUniqueName(id.getName()), id.getIndices())
			);
		}
		return idsIntern;
	}

	public static transformToIdsWithFullName(
		internIds: Set<RtInternalIdentifier>,
		metaData: IMetaDataValidierung
	): HashSet<RtExternalIdentifier> {
		const idsExtern: HashSet<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>();
		for (const id of internIds) {
			idsExtern.add(
				new RtExternalIdentifier(metaData.getPfadName(id.getName()), id.getIndices())
			);
		}
		return idsExtern;
	}
}
