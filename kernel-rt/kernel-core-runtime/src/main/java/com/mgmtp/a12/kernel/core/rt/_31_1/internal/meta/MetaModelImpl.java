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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.IDynEnumsIntern;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Container_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Feld_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Identifier_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Model_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Regel_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.sorter.ISorter;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.sorter.StandardSorter;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.FeldUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.MetaDataUtilValidierung;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.SemanticIndexSpec;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.TrieNode;
import com.mgmtp.a12.kernel.core.rt.a12internal.InvalidKeyException;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaRule;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;

import lombok.NonNull;

/**
 * Klasse modelliert alle Metadaten für die Schnittstelle IMetaModel.
 * Im Konstruktor werden alle in den Literatal-Strukturen gespeicherten Daten in die Laufzeit-
 * strukturen eingelesen.
 */
public class MetaModelImpl extends MetaEntity implements IMetaModel {
	private final List<IIdentifier> pflichtFelderFSList = new ArrayList<>();
	private final List<IMetaField> pflichtFelderMFList = new ArrayList<>();
	private final Map<String, List<IMetaField>> vordruckPflichtFelderMap = new HashMap<>();

	private final Map<String, IMetaField> felderMap = new HashMap<>();
	private final Map<String, IMetaField> felderVollerPfadMap = new HashMap<>();

	private final Map<String, IMetaRule> regelnMap = new HashMap<>();
	private final Map<String, IMetaRule> regelnVollerPfadMap = new HashMap<>();

	private final Map<String, IMetaGroup> containerAlleMap = new HashMap<>();
	private final List<IMetaGroup> rootContainer = new ArrayList<>();
	private final LinkedHashMap<String, MetaContainer> vordruckMap = new LinkedHashMap<>();
	private final Map<String, List<IMetaField>> vordruckNameFelderMap = new HashMap<>();

	private final Map<String, List<Pair<String, SortOrder>>> sortInfo4Mapper;

	private static final LinkedHashSet<String> schluessel = new LinkedHashSet<>();
	private static final LinkedHashSet<String> internalKeys = new LinkedHashSet<>();

	private final Map<String, IFormatDefinition> uniqueFeldNameToFormatDefinition = new HashMap<>();

	private final Set<RtExternalIdentifier> globalFields = new HashSet<>();

	private final Set<IMetaField> calculatedFields = new HashSet<>();
	private final Set<IMetaField> dynamicEnumerationFields = new HashSet<>();

	/**
	 * (annotation-key) -> (entity-fullName -> annotation-value)
	 * For each annotation key (key of the "outer" map): for each entity to which the annotation key is assigend the key of the
	 * "inner" map is the full name of the entity and the value of the "inner" map is the respective value of the annotation.
	 */
	private final Map<String, Map<String, String>> annotation2EntityValues = new HashMap<>();
	private final IMetaDataValidierung metaDataValidierung;

	private final List<String> indexFieldFullNames;

	private Supplier<IValidator> validator;

	static {
		schluessel.add(IExtmpMetaKeysBase.MODEL_ERVERSION);
		schluessel.add(IMetaKeys.MODEL_GROUP_LIST);
		schluessel.add(IMetaKeys.MODEL_META_RULE);
		schluessel.add(IMetaKeys.MODEL_META_GROUP);
		schluessel.add(IMetaKeys.MODEL_META_FIELD);
		schluessel.add(IMetaKeys.MODEL_APPLICATION_CONDITION);
		schluessel.add(IMetaKeys.MODEL_CUSTOM_FIELD_TYPES_TO_BE_PROVIDED);
		schluessel.add(IMetaKeys.MODEL_INDEX_FIELDS);
		schluessel.add(IMetaKeys.MODEL_INDEX_FIELD_FULL_NAMES);
		schluessel.add(IMetaKeys.MODEL_CALC_FIELDS);
		schluessel.add(IMetaKeys.MODEL_SORTED_DATA);

		schluessel.add(IExtmpMetaKeysBase.MODEL_XMLWRITER);
		schluessel.add(IExtmpMetaKeysBase.MODEL_UXMLWRITER);
		schluessel.add(IExtmpMetaKeysBase.MODEL_XMLREADER);

		schluessel.add(IMetaKeys.MODEL_MANDATORY_FIELDS);

		schluessel.add(IMetaKeys.MODEL_LANGUAGES);

		schluessel.add(IMetaKeys.MODEL_DYNAMIC_ENUM_FIELDS);

		schluessel.add(IMetaKeys.MODEL_ANNOTATION);
		schluessel.add(IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES);

		internalKeys.add(IMetaKeysInternal.MODEL_SORT_INFO);

		internalKeys.add(IMetaKeysInternal.MODEL_GLOBAL_FIELDS);
		internalKeys.add(IMetaKeysInternal.MODEL_TIME_ZONE);
		internalKeys.add(IMetaKeysInternal.MODEL_METADATAVALIDIERUNG);

		internalKeys.add(IMetaKeysInternal.MODEL_SORTIER_INFO_4_MAPPER);
		internalKeys.add(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD);

		internalKeys.add(IMetaKeysInternal.MODEL_CONVERTED_DATA_TO_EXML);
	}

	/**
	 * Konstruktor liest alle Daten aus den _t Strukturen aus und speichert sie in den
	 * Laufzeitstrukturen.
	 * Beim lesen werden auch die entsprechenden Containment-Beziehungen aufgebaut.
	 */
	@SuppressWarnings({ "unchecked" })
	public MetaModelImpl(
			Model_t da,
			Feld_t[] felder,
			Regel_t[] regeln,
			Container_t[] container,
			int[][] legalCodepoints,
			TrieNode legalGraphemeTrie,
			Map<Locale, PresentationInformationValues> defaultPresentationInfo,
			boolean isExtmp,
			Supplier<IDynEnumsIntern> dynEnumCalc,
			Supplier<IValidator> validator) {
		super(schluessel, internalKeys);

		this.sortInfo4Mapper = new HashMap<>();

		Map<String, Long> entityUnqNameXmaxUsb = new HashMap<>();

		this.validator = validator;

		//////////////////////////////////////////////////////////////
		// create the group tree from the flat structure of Container_t
		// and determine the index fields in the model
		this.indexFieldFullNames = Collections.unmodifiableList(createContainerTreeAndDetermineIndexFields(container));

		for (Container_t ct : container) {
			String path = ct.getFullName();
			String rootGroupName = EntityUtil.getRootGroupName(path);
			MetaContainer rootGroup = vordruckMap.get(rootGroupName);
			long maxUsbIntern = MetaDataUtilValidierung.getMaxUSB(rootGroup);
			entityUnqNameXmaxUsb.put(path, maxUsbIntern);
		}

		// Das Befüllen der folgenden Maps dient dem performanten Zugriff auf Metainformationen
		Map<String, Long> felderUnqNameMaxZeile = new HashMap<>();
		// Erzeuge alle Felder. Diese werden sowohl in die Container-Struktur
		// als auch in die Vordruck-Struktur eingeordnet.

		for (Feld_t f : felder) {
			String vordruckName = EntityUtil.getRootGroupName(f.getPfadName());
			MetaContainer vordruck = vordruckMap.get(vordruckName);

			MetaField feld = new MetaField(this, f, da.getDezimalTrenner());

			// füge das Feld in die alte Struktur ein
			felderMap.put(f.getUniqueName(), feld);
			felderVollerPfadMap.put(f.getPfadName(), feld);
			uniqueFeldNameToFormatDefinition.put(f.getUniqueName(), f.getFormatDefinition());
			List<IMetaField> vordruckFelder = vordruckNameFelderMap.computeIfAbsent(vordruckName, k -> new ArrayList<>());
			vordruckFelder.add(feld);

			// füge das Feld in die Container-Struktur ein
			String containerPfad = EntityUtil.getParentPath(f.getPfadName());
			MetaContainer cont = (MetaContainer) containerAlleMap.get(containerPfad);
			cont.addFeld(feld);

			// Falls das Feld ein Pflichtfeld ist, füge es in die Liste der Identifier und
			// in die MetaFeldListe hinzu
			if (f.isPflichtFeld()) {
				pflichtFelderFSList.add(new RtExternalIdentifier(f.getUniqueName(), new long[] { 0, 0 }));
				pflichtFelderMFList.add(feld);
			}

			feld.setMaxIndices(this);

			// if the field is global it is added to the set of global fields
			if (FeldUtil.isGlobal(f)) {
				int indicesLength = EntityUtil.countHierarchyLevels(f.getPfadName());
				globalFields.add(new RtExternalIdentifier(f.getPfadName(), Arrays.copyOf(new long[0], indicesLength)));
			}

			// füge das Paar UniqueFeldName/MaxUsb in die Map ein
			String feldUniqueName = (String) feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			long maxUsbIntern = MetaDataUtilValidierung.getMaxUSB(vordruck);
			entityUnqNameXmaxUsb.put(feldUniqueName, maxUsbIntern);
			felderUnqNameMaxZeile.put(feldUniqueName, f.getMaxZeile());
		}

		Set<String> usbIndexFelderUniqueNames = new LinkedHashSet<>();
		for (IMetaGroup mc : rootContainer) {
			// Ergänze USB IndexFeld: kann erst jetzt dem Container hinzugefügt werden, weil vorher die
			// Felder erzeugt werden mussten
			MetaContainer c = (MetaContainer) mc;
			String usbIndexFeldName = c.getUSBIndexFeldName();

			IMetaField usbFeld = null;

			// falls es ein UsbIndexfeld gibt, hole das MetaField mit diesem Namen
			if (usbIndexFeldName != null) {
				usbFeld = (IMetaField) this.getValue(IMetaKeys.MODEL_META_FIELD, usbIndexFeldName);
				usbIndexFelderUniqueNames.add(EntityUtil.getEntityName(usbIndexFeldName));
				// für den Fall, dass usbIndexFeldName null ist und USBIndexFeld ein gültiger Schlüssel ist,
				// wurde bereits im Konstruktor von MetaContainer der Wert null für den Schlüssel gesetzt
				c.addWert(IExtmpMetaKeysBase.GROUP_USB_INDEX_FELD, usbFeld);
			}
		}

		if (regeln != null) {
			// Regeln können erst nach den Feldern erzeugt werden,  da hier eine Verknüpfung zu
			// den Felder verwendet wird. Regeln werden in Container-Struktur und in die
			// Vordruckstruktur eingehängt.
			for (Regel_t r : regeln) {
				MetaRegel regel = createRegel(r, felder);

				regelnMap.put(r.getName(), regel);
				regelnVollerPfadMap.put(r.getPfadName(), regel);

				String containerPfad = EntityUtil.getParentPath(r.getPfadName());
				MetaContainer cont = (MetaContainer) containerAlleMap.get(containerPfad);
				cont.addRegel(regel);
			}
		}

		// Information für Pflichtfelder wird in den internen Strukturen initialisiert.
		for (Map.Entry<String, List<IMetaField>> e : vordruckNameFelderMap.entrySet()) {
			List<IMetaField> vordruckPflichtfelderList = new ArrayList<>();
			vordruckPflichtFelderMap.put(e.getKey(), vordruckPflichtfelderList);
			for (IMetaField iFeld : e.getValue()) {
				MetaField feld = (MetaField) iFeld;
				List<Integer> liste = (List<Integer>) feld.getPflichtfeldIntern();
				if (!liste.isEmpty()) {
					pflichtFelderMFList.add(feld);
				}
				liste = (List<Integer>) feld.getVordruckPflichtfeldIntern();
				if (!liste.isEmpty()) {
					vordruckPflichtfelderList.add(feld);
				}
			}
		}

		addWert(IExtmpMetaKeysBase.MODEL_ERVERSION, da.getErVersion());
		addWert(IMetaKeys.MODEL_GROUP_LIST, rootContainer);
		addWert(IMetaKeys.MODEL_APPLICATION_CONDITION, da.getApplbedingungen());
		addWert(IMetaKeys.MODEL_CUSTOM_FIELD_TYPES_TO_BE_PROVIDED, da.getCustomFieldTypes());
		addWert(IMetaKeys.MODEL_INDEX_FIELDS, !indexFieldFullNames.isEmpty());
		addWert(IMetaKeys.MODEL_INDEX_FIELD_FULL_NAMES, indexFieldFullNames);
		addWert(IMetaKeysInternal.MODEL_SORT_INFO, da.getSortInfo());
		addWert(IMetaKeysInternal.MODEL_SORTIER_INFO_4_MAPPER, sortInfo4Mapper);
		addWert(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD, new HashSet<MetaLokalePflichtInfo>());

		addWert(IMetaKeys.MODEL_LANGUAGES, Collections.unmodifiableList(da.getUnterstuetzteSprachen()));

		addWert(IMetaKeysInternal.MODEL_GLOBAL_FIELDS, globalFields);

		addWert(IMetaKeys.MODEL_DYNAMIC_ENUM_FIELDS, dynamicEnumerationFields);

		addWert(IMetaKeys.MODEL_ANNOTATION, null); // will be overwritten by additional values if any annotation is provided.
		addWert(IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES, null); // Register the key.
		addWert(IMetaKeys.MODEL_CALC_FIELDS, calculatedFields);

		Set<String> internalAdditionalKeys = da.getInternalAdditionalKeys();
		for (String key : da.getAdditionalKeyValues().keySet()) {
			if (internalAdditionalKeys.contains(key)) {
				addAdditionalInternalKey(key);
			} else {
				addAdditionalKey(key);
			}
			addWert(key, da.getAdditionalKeyValues().get(key));
		}

		this.metaDataValidierung = new MetaDataValidierungIntern(this,
				da.getDezimalTrenner(),
				legalCodepoints,
				legalGraphemeTrie,
				usbIndexFelderUniqueNames,
				entityUnqNameXmaxUsb,
				felderUnqNameMaxZeile,
				defaultPresentationInfo,
				isExtmp,
				dynEnumCalc);

		// Collect annotations map
		addToAnnotationsMap(da::getAdditionalValue, () -> "");
		for (Feld_t f : felder) {
			addToAnnotationsMap(f::getAdditionalValue, f::getPfadName);
		}
		for (Container_t ct : container) {
			addToAnnotationsMap(ct::getAdditionalValue, ct::getFullName);
		}
		if (regeln != null) {
			for (Regel_t r : regeln) {
				addToAnnotationsMap(r::getAdditionalValue, r::getPfadName);
			}
		}
	}

	@SuppressWarnings("unchecked") private void addToAnnotationsMap(Function<String, Object> antGetter, Supplier<String> fullName) {
		Object annotations = antGetter.apply("Annotation");
		if (!(annotations instanceof Map)) {
			return;
		}
		((Map<String, Object>) annotations).forEach(
				(key, value) -> annotation2EntityValues.computeIfAbsent(key, s -> new LinkedHashMap<>())
						.put(fullName.get(), String.valueOf(value)));
	}

	private List<String> createContainerTreeAndDetermineIndexFields(Container_t[] container) {
		List<String> indexFields = new ArrayList<>();
		for (Container_t ct : container) {
			String pfad = ct.getFullName();

			MetaContainer mc = new MetaContainer(ct);
			containerAlleMap.put(pfad, mc);

			String parentPfad = EntityUtil.getParentPath(pfad);
			// add to parent container and add indices; Container_t for parents are always generated before their children.
			if (!parentPfad.isEmpty()) {
				MetaContainer parent = (MetaContainer) containerAlleMap.get(parentPfad);
				parent.addIMetaGroup(mc);
				mc.setMaxIndices(parent, ct.getMaxIndex());
			} else { // for root groups:
				// container is added to root container list
				rootContainer.add(mc);
				vordruckMap.put(EntityUtil.getEntityName(ct.getFullName()), mc);
				mc.setMaxIndices(null, ct.getMaxIndex());
			}

			if (ct.getIndexFields().length > 0) {
				List<String> indexFieldShortNames = Arrays.asList(ct.getIndexFields());
				indexFieldShortNames
						.forEach(shortName -> indexFields.add(EntityUtil.createPath(pfad, shortName)));
			}
			addSortInfo4MapperFromIndexFields(pfad, ct.getIndexFields());
		}
		return indexFields;
	}

	private void addSortInfo4MapperFromIndexFields(String path, String[] indexFields) {
		if (indexFields.length == 0) {
			return;
		}
		List<Pair<String, SortOrder>> sortInfo4Group = new ArrayList<>();
		for (String indexField : indexFields) {
			Pair<String, SortOrder> sortInfoItem = new Pair<>(indexField, SortOrder.ASCENDING);
			sortInfo4Group.add(sortInfoItem);
		}
		sortInfo4Mapper.put(path, sortInfo4Group);
	}

	/**
	 * Erzeuge eine Regel aus den Regelstrukturen.
	 */
	private MetaRegel createRegel(Regel_t r, Feld_t[] f) {
		Set<RtInternalIdentifier> refIdentifierAuslassungIntern = new HashSet<>();
		if (r.getRefAuslassungsIdentifier() != null) {
			for (Identifier_t id : r.getRefAuslassungsIdentifier()) {
				refIdentifierAuslassungIntern.add(createInternalIdentifierWithUniqueName(f, id));
			}
		}

		Set<RtIdentifierTemplate> refEntityIntern = new HashSet<>();
		if (r.getRefEntity() != null) {
			refEntityIntern.addAll(Arrays.asList(r.getRefEntity()));
		}

		Set<RtIdentifierTemplate> refEntityOmissionIntern = new HashSet<>();
		if (r.getRefOmissionEntity() != null) {
			refEntityOmissionIntern.addAll(Arrays.asList(r.getRefOmissionEntity()));
		}

		//Speichere die referenzierten Felder
		Map<String, List<IMetaField>> refFelder = new HashMap<>();
		//Speichere die referenzierten FeldIdentifier
		Set<RtInternalIdentifier> refIdentifierIntern = new HashSet<>();
		MetaField berFeld = null;
		if (r.getRefIdentifier() != null) {
			List<IMetaField> feldListe = new ArrayList<>();
			for (Identifier_t id : r.getRefIdentifier()) {
				refIdentifierIntern.add(createInternalIdentifierWithUniqueName(f, id));

				feldListe.add(felderMap.get(f[id.getFeldIndex()].getUniqueName()));

				//berechnetes Feld ?  Client-seitige Felder.
				if (id.isBerechnetesFeld()) {
					berFeld = getMetaFeld(f, id);
					berFeld.addWert(IExtmpMetaKeysBase.FIELD_BERECHNUNG_UEBERTRAG, id.isBerechnetesFeldUebertrag());
				}
			}
			refFelder.put("", feldListe);
		}

		handleCalc(r, f);

		handleDynamicEnum(r, f);

		return new MetaRegel(this, r, refFelder, refIdentifierIntern, refIdentifierAuslassungIntern, refEntityIntern,
				refEntityOmissionIntern);
	}

	@SuppressWarnings("unchecked") private void handleCalc(Regel_t r, Feld_t[] f) {
		if (r.isCalculationRule()) {
			// das berechnete Feld immer das Fehlerfeld
			MetaField calculatedField = (MetaField) felderVollerPfadMap.get(r.getFehlerFeld());
			calculatedFields.add(calculatedField);

			IIdentifier calculatedFieldId = null;
			for (Identifier_t id : r.getRefIdentifier()) {
				IIdentifier fieldIdentifier = createExternalIdentifierWithPathName(f, id, IIdentifier.ALL_INDICES);
				if (fieldIdentifier.getName().equals(r.getFehlerFeld())) {
					calculatedFieldId = fieldIdentifier;
				} else {
					calculatedField.addToOperandsSet(fieldIdentifier);
				}
			}

			for (IIdentifier operand : (Set<IIdentifier>) calculatedField.getValue(IMetaKeys.FIELD_OPERANDS_SET)) {
				// save the reverse map to the operand's MetaField.
				MetaField operandFeld = (MetaField) felderVollerPfadMap.get(operand.getName());
				operandFeld.addToTargetSetForCalc(calculatedFieldId);
			}
		}
	}

	private void handleDynamicEnum(Regel_t r, Feld_t[] f) {
		if (r.isDynamicEnumerationRule()) {
			MetaField dynamicEnumerationField = (MetaField) felderVollerPfadMap.get(r.getFehlerFeld());
			dynamicEnumerationFields.add(dynamicEnumerationField);
			dynamicEnumerationField.addWert(IMetaKeys.FIELD_DYNAMIC_ENUMERATION, true);

			Set<IIdentifier> operanden = dynamicEnumerationField.operandSetDynamicEnum;
			for (Identifier_t id : r.getRefIdentifier()) {
				IIdentifier fieldIdentifier = createExternalIdentifierWithPathName(f, id, IIdentifier.ALL_INDICES);
				if (!fieldIdentifier.getName().equals(r.getFehlerFeld())) {
					operanden.add(fieldIdentifier);
				}
			}
		}
	}

	private RtExternalIdentifier createExternalIdentifierWithPathName(Feld_t[] f, Identifier_t id,
			long semIdxOrParallelIterationValue) {
		final long[] idx = new long[id.getIndices().length];
		for (int i = 0; i < id.getIndices().length; i++) {
			idx[i] = ((id.getIndices()[i] != IIdentifier.SEMANTIC_INDEX) && (id.getIndices()[i] != IIdentifier.PARALLEL_ITERATION)) ?
					id.getIndices()[i]
					:
					semIdxOrParallelIterationValue;
		}
		String fullName = id.isGroup() ? id.getFullName() : f[id.getFeldIndex()].getPfadName();
		return new RtExternalIdentifier(fullName, idx, id.getUsb());
	}

	private RtInternalIdentifier createInternalIdentifierWithUniqueName(Feld_t[] f, Identifier_t id) {
		SemanticIndexSpec semIdx = null;
		if (id.getSemanticIndexValue() != null) {
			semIdx = new SemanticIndexSpec(id.getSemanticIndexValue());
		} else if (id.getSemanticIndex() != null) {
			semIdx = new SemanticIndexSpec(createInternalIdentifierWithUniqueName(f, id.getSemanticIndex()));
		}
		String fullName = id.isGroup() ? id.getFullName() : f[id.getFeldIndex()].getUniqueName();
		// For referenced fields category is irrelevant.
		return new RtInternalIdentifier(fullName, id.getIndices(), id.getUsb(), null, semIdx);
	}

	private MetaField getMetaFeld(Feld_t[] f, Identifier_t id) {
		String name = f[id.getFeldIndex()].getUniqueName();
		return (MetaField) felderMap.get(name);
	}

	public String toString() {
		return metaDataValidierung.getModelId();
	}

	/**
	 * Allgemeine getterMethode zum Zugriff auf die verschiedenen Werte.
	 *
	 * @throws InvalidKeyException
	 * 		if an invalid key is passed, a runtime exception is thrown
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(String key, Object... parameter) {
		return switch (key) {
			case IMetaKeys.MODEL_ANNOTATION -> getAnnotation(key, parameter);
			case IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES -> getAllAnnotatedEntities(parameter);
			case IMetaKeys.MODEL_MANDATORY_FIELDS -> getPflichtFelderWert(parameter);
			case IMetaKeys.MODEL_META_FIELD -> getMetaFeld(parameter);
			case IMetaKeys.MODEL_META_RULE -> getMetaRegel(parameter);
			case IMetaKeys.MODEL_META_GROUP -> getMetaContainer(parameter);
			case IMetaKeysInternal.MODEL_METAFELDUNIQUE -> getMetaFeldUnique(parameter);
			case IMetaKeys.MODEL_APPLICATION_CONDITION ->
					Optional.ofNullable((Set<String>) super.getValue(IMetaKeys.MODEL_APPLICATION_CONDITION))
							.map(HashSet<String>::new)
							.orElse(null);
			case IMetaKeys.MODEL_GROUP_LIST -> Optional.ofNullable(((List<IMetaGroup>) super.getValue(IMetaKeys.MODEL_GROUP_LIST)))
					.map(ArrayList<IMetaGroup>::new)
					.orElse(null);
			case IMetaKeys.MODEL_SORTED_DATA -> sortData(parameter);
			case IMetaKeysInternal.MODEL_VALIDATOR -> getValidator();
			case IMetaKeysInternal.MODEL_METADATAVALIDIERUNG -> getMetaDataValidierung();
			default -> super.getValue(key);
		};
	}

	private Map<String, String> getAllAnnotatedEntities(Object... parameter) {
		if (parameter == null || parameter.length != 1 || !(parameter[0] instanceof String key)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAMETER_NOT_SINGLE_STRING,
					IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES));
		}
		return annotation2EntityValues.get(key);
	}

	private Object getMetaFeldUnique(Object[] parameter) {
		String name = getNameFromParams(IMetaKeysInternal.MODEL_METAFELDUNIQUE, parameter);
		if (name == null) {
			return null;
		}
		return felderMap.get(name);
	}

	private Object getPflichtFelderWert(Object[] parameter) {
		if (!checkParameterAngegeben(IMetaKeys.MODEL_MANDATORY_FIELDS, 1, parameter)) {
			return null;
		}
		//Es muss genau ein Parameter übergeben werden, vom Typ Set<IIdentifier>
		assert parameter[0] instanceof Set : "The parameter for obligatory fields is not of type 'Set'.";

		try {
			@SuppressWarnings("unchecked")
			Set<IIdentifier> additiv = (Set<IIdentifier>) parameter[0];
			Set<RtInternalIdentifier> aIntern = transformToIdsWithUniqueName(additiv, this.metaDataValidierung);
			if (!checkMengeNichtLeerUndGleicheUSB(aIntern)) {
				return null;
			}
			return transformToIdsWithFullName(getPflichtFelderInternal(aIntern), this.metaDataValidierung);

		} catch (ClassCastException e) {
			return null;
			// bei falsch uebergebenem Parameter wird immer null zurueckgegeben
		}
	}

	private Set<RtInternalIdentifier> getPflichtFelderInternal(Set<RtInternalIdentifier> additiv) {

		Set<RtInternalIdentifier> result = new HashSet<>();
		AdditivInfo addInfo = new AdditivInfo(additiv);

		//Wenn es eine lokale Pflichtinfo zu dem übergebenen Additiv gibt,  gebe diese zurück.
		@SuppressWarnings("unchecked")
		Set<MetaLokalePflichtInfo> infos = (Set<MetaLokalePflichtInfo>) getValue(
				IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD);
		for (MetaLokalePflichtInfo metaInfo : infos) {
			result.addAll(metaInfo.getPMengeWennMetaAdditivUmfasstMenge(additiv, addInfo.getUsb()));
		}

		if (addInfo.alleFelderAufEinemVordruck()) {
			String vordruckName = addInfo.getVordrucke().iterator().next();
			result.addAll(convertVordruckPflichtfelder(vordruckName, addInfo.getLfdNr(), addInfo.getUsb()));
		} else {
			result.addAll(convertPflichtfelder());
		}

		return result;
	}

	private IData sortData(Object[] parameters) {
		checkSortParameters(parameters);

		IData data = (IData) parameters[0];
		IResult result = (IResult) parameters[1];
		RawParamHandler4Convert additionalParameters = RawParamHandler4Convert.of(parameters);
		ICustomFieldTypeFactory customFieldTypeFactory =
				additionalParameters.getCustomFieldTypeFactory();
		@SuppressWarnings("unchecked") Locale sprache = additionalParameters.getSprache(
				(List<Locale>) getValue(IMetaKeys.MODEL_LANGUAGES));

		ISorter sorter = getSorter(customFieldTypeFactory, sprache);
		return sorter.sort(data, result);
	}

	private void checkSortParameters(Object[] parameters) {
		if (parameters.length < 2) {
			throw new IllegalArgumentException(
					"To sort data, 2 parameters have to be specified (IData, IResult).");
		}
		if (!(parameters[0] instanceof IData)) {
			throw new IllegalArgumentException("The first parameter for sorting must be the data to sort (IData).");
		}
		if (!(parameters[1] instanceof IResult)) {
			throw new IllegalArgumentException("The second parameter for sorting must be an IResult taking possible errors.");
		}
	}

	/**
	 * Gets the {@link ISorter} to sort the data.
	 */
	private ISorter getSorter(ICustomFieldTypeFactory customFieldTypeFactory, Locale locale) {
		return new StandardSorter(metaDataValidierung, customFieldTypeFactory, locale);
	}

	/**
	 * Überprüft, ob
	 * <ul>
	 * <li>die übergebene Menge von IIdentifiern nicht leer ist und</li>
	 * <li>jeder IIdentifier den gleichen USB referenziert evtl. gemischt mit Feldern ohne USB</li>
	 * </ul>
	 *
	 * @param ids
	 * 		- Menge der zu überprüfenden Identifer
	 * @return <code>false</code>, wenn die Menge Felder mit verschiedenen USB enthält,
	 * 		<code>true</code> sonst
	 */
	private boolean checkMengeNichtLeerUndGleicheUSB(Set<RtInternalIdentifier> ids) {
		assert !ids.isEmpty() : "The given set must not be empty.";
		if (ids.isEmpty()) {
			return false;
		}
		long firstUsb = IIdentifier.USB_NOT_SPECIFIED;
		for (RtInternalIdentifier id : ids) {
			long usb = id.getUsb();
			if (usb == IIdentifier.USB_NOT_SPECIFIED) {// IIdentifier.USB_NICHT_ANGEGEBEN ist immer ok
				continue;
			}
			if (firstUsb == IIdentifier.USB_NOT_SPECIFIED) {
				// hier ist usb != IIdentifier.USB_NICHT_ANGEGEBEN -> initialisiere firstUsb
				firstUsb = usb;
				continue;
			}
			if (firstUsb != usb) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Speichert zu einer Menge von IIdentifiern die referenzierten Vordrucke, laufende Nummern
	 * und USBs, wobei für die laufenden Nummern 0 gesetzt wird, wenn verschiedene laufende Nummern
	 * referenziert werden.<br>
	 * Es wird vorausgesetzt, dass höchstens ein USB ungleich
	 * {@link IIdentifier#USB_NOT_SPECIFIED} referenziert wird
	 */
	private class AdditivInfo {
		Set<String> vordrucke = new HashSet<>();
		long lfdNr = 0;
		long usb;

		public AdditivInfo(Set<RtInternalIdentifier> additiv) {
			boolean first = true;
			for (RtInternalIdentifier id : additiv) {
				vordrucke.add(getVordruckName(id));
				if (first) {
					lfdNr = id.getIndices()[0];
					usb = id.getUsb();
					first = false;
				} else {
					if (lfdNr != 0 && lfdNr != id.getIndices()[0]) {
						lfdNr = 0;
					}
					usb = id.getUsb();
				}
			}
		}

		/**
		 * Gibt <code>true</code> zurück, wenn alle Felder in dieser AdditivInfo auf dem gleichen
		 * Vordruck liegen - ohne die laufenden Nummern zu berücksichtigen.
		 */
		public boolean alleFelderAufEinemVordruck() {
			return vordrucke.size() == 1;
		}

		public Set<String> getVordrucke() {
			return vordrucke;
		}

		public long getLfdNr() {
			return lfdNr;
		}

		public long getUsb() {
			return usb;
		}

		/**
		 * Gebe den Vordrucknamen zu dem Identifier zurück
		 *
		 * @param id
		 * 		- ein Feld
		 * @return Name des Vordrucks.
		 */
		private String getVordruckName(RtInternalIdentifier id) {
			Object[] parameter = { id.getName() };
			MetaField feld = (MetaField) getMetaFeld(parameter);
			if (feld == null) {
				return null;
			}
			IMetaGroup rootCont = feld.getTopContainer();
			return (String) rootCont.getValue(IMetaKeys.GROUP_SHORT_NAME);
		}
	}

	/**
	 * Diese Methode liefert zum Feldnamen das MetaField.
	 *
	 * @param parameter
	 * 		- es wird ein String-Parameter erwartet
	 * @return MetaField oder null, falls kein Feld mit dem angegebenen Namen gefunden wurde.
	 */
	private IMetaField getMetaFeld(Object[] parameter) {
		String name = getNameFromParams(IMetaKeys.MODEL_META_FIELD, parameter);
		if (name == null) {
			return null;
		}
		IMetaField result = felderVollerPfadMap.get(name);

		if (result == null) {
			result = felderMap.get(name);
		}
		return result;
	}

	/**
	 * Diese Methode liefert zum Regelnamen das MetaRegel Objekt.
	 *
	 * @param parameter
	 * 		- es wird ein String-Parameter erwartet
	 * @return MetaField oder null, falls keine Regel mit dem angegebenen Namen gefunden wurde.
	 */
	private IMetaRule getMetaRegel(Object[] parameter) {
		String name = getNameFromParams(IMetaKeys.MODEL_META_RULE, parameter);
		if (name == null) {
			return null;
		}
		IMetaRule result = regelnVollerPfadMap.get(name);
		if (result == null) {
			result = regelnMap.get(name);
		}
		return result;
	}

	private IMetaGroup getMetaContainer(Object[] parameter) {
		String name = getNameFromParams(IMetaKeys.MODEL_META_GROUP, parameter);
		if (name == null) {
			return null;
		}
		return containerAlleMap.get(name);
	}

	private String getNameFromParams(String key, Object[] parameter) {
		try {
			if (!checkParameterAngegeben(key, 1, parameter)) {
				return null;
			}

			assert parameter[0] instanceof String : String.format("The given parameter for key '%s' is not of type String.", key);
			return (String) parameter[0];
		} catch (ClassCastException e) {
			return null;
		}
	}

	private Set<RtInternalIdentifier> convertPflichtfelder() {
		Set<RtInternalIdentifier> result = new HashSet<>();
		for (IMetaField iFeld : pflichtFelderMFList) {
			MetaField feld = (MetaField) iFeld;
			@SuppressWarnings("unchecked")
			List<Integer> liste = (List<Integer>) feld.getPflichtfeldIntern();
			String name = (String) feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			//es kann kein Pflichtfeld auf einem USB-Vordruck liegen, daher setzen wir den USB im Pflichtfeld auf null.
			int anzahlEbenen = MetaDataUtilValidierung.getAnzahlWiederholungsEbenen(name, this.metaDataValidierung);
			for (Integer i : liste) {
				long[] idx = new long[anzahlEbenen];
				idx[0] = i;
				for (int j = 1; j < anzahlEbenen; j++) {
					idx[j] = 0;
				}
				RtInternalIdentifier pf = new RtInternalIdentifier(name, idx);
				result.add(pf);
			}
		}
		return result;
	}

	private Collection<RtInternalIdentifier> convertVordruckPflichtfelder(String vordruckName, long lfdNr, long usb) {
		Set<RtInternalIdentifier> result = new HashSet<>();

		for (IMetaField iFeld : vordruckPflichtFelderMap.get(vordruckName)) {
			MetaField feld = (MetaField) iFeld;
			@SuppressWarnings("unchecked")
			List<Integer> liste = (List<Integer>) feld.getVordruckPflichtfeldIntern();
			String name = (String) feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			int anzahlEbenen = MetaDataUtilValidierung.getAnzahlWiederholungsEbenen(name, this.metaDataValidierung);
			if (lfdNr == 0) {
				for (Integer i : liste) {
					long[] idx = new long[anzahlEbenen];
					idx[0] = i;
					for (int j = 1; j < anzahlEbenen; j++) {
						idx[j] = 0;
					}
					RtInternalIdentifier pf = new RtInternalIdentifier(name, idx, usb);
					result.add(pf);
				}
			} else {
				if (liste.contains(0) || liste.contains((int) lfdNr)) {
					long[] idx = new long[anzahlEbenen];
					idx[0] = lfdNr;
					for (int j = 1; j < anzahlEbenen; j++) {
						idx[j] = 0;
					}
					RtInternalIdentifier pf = new RtInternalIdentifier(name, idx, usb);
					result.add(pf);
				}
			}
		}
		return result;

	}

	public IMetaDataValidierung getMetaDataValidierung() {
		if (this.metaDataValidierung == null) {
			throw new IllegalStateException("The 'IMetaDataValidierung' object was not created yet.");
		}
		return this.metaDataValidierung;
	}

	/**
	 * @return zugehörige IFormatDefinition
	 */
	public IFormatDefinition getIFormatDefinition(String uniqueName) {
		return uniqueFeldNameToFormatDefinition.get(uniqueName);
	}

	public @NonNull Set<String> getFieldFullNames() {
		return felderVollerPfadMap.keySet();
	}

	private Set<RtInternalIdentifier> transformToIdsWithUniqueName(Collection<IIdentifier> ids, IMetaDataValidierung metaData) {
		Set<RtInternalIdentifier> idsIntern = new HashSet<>();
		for (IIdentifier id : ids) {
			String uniqueName = metaData.getUniqueName(id.getName());
			idsIntern.add(new RtInternalIdentifier(uniqueName, id.getIndices(), id.getUsb()));
		}
		return idsIntern;
	}

	private Set<IIdentifier> transformToIdsWithFullName(Collection<RtInternalIdentifier> internIds, IMetaDataValidierung metaData) {
		Set<IIdentifier> idsExtern = new HashSet<>();
		for (RtInternalIdentifier id : internIds) {
			String pfadName = metaData.getPfadName(id.getName());
			idsExtern.add(new RtExternalIdentifier(pfadName, id.getIndices(), id.getUsb()));
		}
		return idsExtern;
	}

	public IValidator getValidator() {
		return validator == null ? null : validator.get();
	}
}
