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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.datatransfer.mappingmodel;

import static com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IdentifierUtil.extractRootGroup;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.datatransfer.SourceIdentifierImpl;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.datatransfer.util.Util;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.DatenImpl;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IdentifierUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.InternalFallbackErrorLookup;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceData;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

/**
 * An implementation of {@link IPrecompModelDataMapper} that supports precomputation models that follow a simple fixed schema
 * (rather than e.g. a flexible modeler-defined schema):
 * <ul>
 *     <li>All sources are mounted via an include at {@code /<sourceName>}</li>
 *     <li>The source models are expected to already contain the appropriate validation rules for include
 *       filtering. (i.e. If the sources have been modeled with different validation rules, a combination
 *       model that removes the rules and adds appropriate ones could be defined externally, and that
 *       combination model may be referenced as a source model in the mapping model; but the mapping model
 *       does not itself support do specify a combination <em>within</em> the mapping model)</li>
 *     <li>The target is currently not <em>included</em>, but added. i.e. the target rootgroups become
 *       root groups of the precomputation model. A future version may possibly introduce an optional mountpoint
 *       for the target, where the target would be included with a MODEL_ROOT include.
 *       This seems like it would be useful for "Datenübernahme aus dem Vorjahr"</li>
 * </ul>
 */
public class FixedShapePrecompModelDataMapper implements IPrecompModelDataMapper {
	/**
	 * error code
	 */
	public static final String TOO_MANY_SOURCE_INSTANCES = "TOO_MANY_SOURCE_INSTANCES";
	// WARNING: changes in this version affect the compatibility between the runtime and the generated code
	// -> version must _not_ be changed in a runtime- or codegenerator- patch-update (because we typically assume that such
	//    a patch preserves compatibility of new generated code with the unpatched runtime, and vice versa - as reflected
	//    by the version infix in the runtime packages, which only contains major and minor, but not the patch version.
	public static final String SERIALIZATION_FORMAT_VERSION_STRING = "FixedShapePrecompModelDataMapper serialization format v9";
	public static final String FIELD_FULLNAMES_FROM_PRECOMP_FRAGMENT_START_MARKER = "fieldFullNamesInPrecompFragment";

	private final Set<SourceSpec4Runtime> sourceSpecs;
	private final Set<String> fieldFullNamesInPrecompFragment;

	private final Map<String, SourceSpec4Runtime> sourceSpecsByName;
	private final Map<String, SourceSpec4Runtime> sourceSpecsByMountpointPath;

	private Map<String, Map<Long, String>> mapBackInfo_SourceNameToSourceRepToSourceId = null;

	public static FixedShapePrecompModelDataMapper of(List<SourceSpec4Runtime> inputSpecs,
			Set<String> fieldFullNamesInPrecompFragment) {
		Set<SourceSpec4Runtime> inputSpecsSet = new HashSet<>(inputSpecs);
		if (inputSpecsSet.size() != inputSpecs.size()) { // Note: a metamodel rule already prevents this, so it should not happen
			// during normal use.
			throw new RuntimeException("inputSpecs must not contain duplicates, but got " + inputSpecs);
		}
		return FixedShapePrecompModelDataMapper.of(inputSpecsSet, fieldFullNamesInPrecompFragment);
	}

	public static FixedShapePrecompModelDataMapper of(Set<SourceSpec4Runtime> inputSpecs,
			Set<String> fieldFullNamesInPrecompFragment) {
		return new FixedShapePrecompModelDataMapper(inputSpecs, fieldFullNamesInPrecompFragment);
	}

	private FixedShapePrecompModelDataMapper(
			Set<SourceSpec4Runtime> sourceSpecs,
			Set<String> fieldFullNamesInPrecompFragment
	) {
		if (sourceSpecs.stream().map(is -> is.inputName).distinct().count() != sourceSpecs.size()) {
			// note: this check is currently redundant, because toMap would otherwise throw an exception when sourceSpecsByName
			// is initialized - but making this explicit here ensures that the check won't get accidentally lost during refactoring
			throw new IllegalArgumentException("no two inputSpecs may have the same source name, but got " + sourceSpecs);
		}
		this.sourceSpecs = sourceSpecs;
		this.fieldFullNamesInPrecompFragment = fieldFullNamesInPrecompFragment;
		this.sourceSpecsByName = this.sourceSpecs.stream().collect(Collectors.toMap(SourceSpec4Runtime::inputName, e -> e));
		this.sourceSpecsByMountpointPath = this.sourceSpecs.stream().collect(Collectors
				.toMap(FixedShapePrecompModelDataMapper::mountPointRgPathForInput, e -> e));
	}

	@Override public MapToPrecompResult mapDataIntoPrecompDM(IData targetData, List<SourceData> sourceData,
			Locale localeForErrors) {
		validateSourceIdsAreUnique(sourceData);
		assert mapBackInfo_SourceNameToSourceRepToSourceId == null :
				"did not expect to map forward twice - mapBack info for previous forward map will get lost unless it happens to be equal";
		this.mapBackInfo_SourceNameToSourceRepToSourceId = new HashMap<>();
		IData mappedData = new DatenImpl();
		// target currently has no mountpoint -> we simply copy the data over
		for (IIdentifier id : targetData) {
			mappedData.addValue(id, targetData.getValue(id));
		}
		var sourcesByName = sourceData.stream().collect(Collectors.groupingBy(SourceData::sourceName));

		Set<String> includedSourceIds = new HashSet<>();
		Set<String> excludedExcessiveSourceIds = new HashSet<>();
		for (String sourceName : sourcesByName.keySet()) {
			if (!this.sourceSpecsByName.containsKey(sourceName)) {
				throw new RuntimeException(
						"got source name " + sourceName + " but expected one of " + this.sourceSpecsByName.keySet());
			}
			Map<Long, String> mapBackInfo_RepetitionToSourceId = new HashMap<>();
			SourceSpec4Runtime sourceSpec = this.sourceSpecsByName.get(sourceName);
			var sourceInstances = sourcesByName.get(sourceName);
			// we keep the order of the instances with the same sourceName as we encounter them in the overall
			// list. If sorting of source repetitions is later added, the list could therefore simply be sorted before it gets
			// passed into mapDataIntoPrecompDM
			long iOneBased = 0;
			for (SourceData source : sourceInstances) {
				iOneBased++;
				if (iOneBased > sourceSpec.maxInstances()) {
					excludedExcessiveSourceIds.add(source.sourceId());
					reportErrorForExcessiveSourceInstance(source, sourceSpec.maxInstances(), localeForErrors);
				} else {
					includedSourceIds.add(source.sourceId());
					String mountPointRgPathForInput = mountPointRgPathForInput(sourceSpec);
					for (IIdentifier id : source.data()) {
						mappedData.addValue(
								sourceSpec.isModelRootInclude()
										? IdentifierUtil.insertRoot(id, mountPointRgPathForInput, iOneBased)
										: IdentifierUtil.changeRoot(id, mountPointRgPathForInput, iOneBased),
								source.data().getValue(id));
					}
					mapBackInfo_RepetitionToSourceId.put(iOneBased, source.sourceId());
				}
			}
			assert !this.mapBackInfo_SourceNameToSourceRepToSourceId.containsKey(sourceName);
			this.mapBackInfo_SourceNameToSourceRepToSourceId.put(sourceName, mapBackInfo_RepetitionToSourceId);
		}
		return new MapToPrecompResult(mappedData, includedSourceIds, excludedExcessiveSourceIds);
	}

	private void validateSourceIdsAreUnique(List<SourceData> sourceData) {
		Set<String> encounteredIds = new HashSet<>();
		Set<String> duplicateIds = new HashSet<>();
		sourceData.stream().map(SourceData::sourceId).forEach(id -> {
			if (!encounteredIds.add(id)) {
				duplicateIds.add(id);
			}
		});
		if (!duplicateIds.isEmpty()) {
			throw new IllegalArgumentException("source IDs must be unique, but the following source IDs occured more than once: "
					+ String.join(", ", duplicateIds));
		}
	}

	private void reportErrorForExcessiveSourceInstance(SourceData source, long maxAllowedInstances, Locale localeForErrors) {
		String ruleName = "tooManySourceInstances";
		String errCode = TOO_MANY_SOURCE_INSTANCES;
		List<IIdentifier> rootGroups = allPresentRootGroupInstancesInNormalizedOrder(source.data());
		IIdentifier errField = rootGroups.stream().findFirst()
				// we need a fallback for the edge case where the source data is completely empty:
				// (this is not expected to be ever shown to users: 1) projAlpha has already switched to
				// addMessageV2(), where the errField is only kernel-internal for test assertions and 2) A12 does still use the old
				// addError(), but doesn't currently forward server-side errors to the client anyway (I think). And
				// A12 should probably switch to addMessageV2 at some point in the not too distant future.
				.orElse(RtExternalIdentifier.of("/placeholderForRootOfEmptyData", new long[] { 1 }));
		String errMsg = InternalFallbackErrorLookup.getBareErrorMessage(
				errCode,
				Constants.DATATRANSFER_ERROR_MESSAGES_MESSAGES_RESOURCE_NAME,
				localeForErrors,
				source.sourceName(), maxAllowedInstances) + " [" + errCode + "]";
		IResult.ErrorType errType = IResult.ErrorType.VALUE_ERROR;
		source.result().addError(ruleName, errCode, errField,
				errMsg, errType, Set.of(), // we leave the referencedFields empty, because the only sensible content
				// would probably be all fields from the source IData, and a) this seems not useful in practice (too
				// many fields) and b) I'm not sure how we can filter the identifiers in the IData to exclude groups and
				// only keep fields
				Set.of(), Set.of());
		source.result().addMessageV2(ruleName, errCode, errMsg, errType, SeverityType.ERROR,
				new ErrorCause(errField /* use errField for iterationGroup - I think this is ok, especially because the
				iterationGroup is only used internally for testing, I think? */,
						List.of(new HashSet<>(rootGroups))));
	}

	private List<IIdentifier> allPresentRootGroupInstancesInNormalizedOrder(IData data) {
		return Util.iterableAsStream(data)
				.map(IdentifierUtil::extractRootGroup)
				.distinct()
				.sorted(Comparator.comparing(IIdentifier::getName).thenComparing(id -> id.getIndices()[0]))
				.toList();
	}

	@Override public SourceIdentifier mapIdentifierBack(IIdentifier identifierInPrecomputationDM) {
		if (this.mapBackInfo_SourceNameToSourceRepToSourceId == null) {
			throw new IllegalStateException("cannot map back before mapDataIntoPrecompDM has been called");
		}
		if (this.fieldFullNamesInPrecompFragment.contains(identifierInPrecomputationDM.getName())) {
			// the field does not originate from a source or the target model, but directly from the precomputation fragment
			return new SourceIdentifierImpl(identifierInPrecomputationDM, SourceIdentifier.ID_FOR_PRECOMPUTATION_DM);
		}
		IIdentifier rootGroup = extractRootGroup(identifierInPrecomputationDM);
		long repetition = rootGroup.getIndices()[0];
		if (sourceSpecsByMountpointPath.containsKey(rootGroup.getName())) {
			SourceSpec4Runtime inputSpec = sourceSpecsByMountpointPath.get(rootGroup.getName());
			IIdentifier idInSource = inputSpec.isModelRootInclude()
					? IdentifierUtil.removeRoot(identifierInPrecomputationDM)
					: IdentifierUtil.changeRoot(identifierInPrecomputationDM, inputSpec.sourceRgPathToRemove(), 1);
			String originalSourceId = this.mapBackInfo_SourceNameToSourceRepToSourceId.get(inputSpec.inputName()).get(repetition);
			return new SourceIdentifierImpl(idInSource, originalSourceId);
		}
		// if it is neither from the precomp fragment nor from a source, it must be from the target model
		return new SourceIdentifierImpl(identifierInPrecomputationDM, SourceIdentifierImpl.ID_FOR_TARGET);
	}

	public static String mountPointRgPathForInput(SourceSpec4Runtime input) {
		return mountPointRgPathForInput(input.inputName());
	}

	public static String mountPointRgPathForInput(String inputName) {
		return "/" + inputName;
	}

	@Override public String serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append(SERIALIZATION_FORMAT_VERSION_STRING).append("\n");
		for (var is : this.sourceSpecs
				.stream().sorted(Comparator.comparing(ss -> ss.inputName)).toList()
		) {
			serializeInputTo(sb, is);
		}
		sb.append(FIELD_FULLNAMES_FROM_PRECOMP_FRAGMENT_START_MARKER).append("\n");
		for (var fullName : this.fieldFullNamesInPrecompFragment) {
			sb.append(fullName).append("\n");
		}
		return sb.toString();
	}

	private static void serializeInputTo(StringBuilder sb, SourceSpec4Runtime is) {
		sb
				.append("inputName\t").append(is.inputName()).append("\t")
				.append("maxInstances\t").append(is.maxInstances()).append("\t")
				.append("sourceRgPathToRemove\t").append(is.sourceRgPathToRemove())
				.append("\tendOfLine\n"); // the endOfLine marker prevents the split() method from stripping empty trailing sourceRgPathToRemove values
	}

	public static FixedShapePrecompModelDataMapper deserialize(String serialized) {
		String[] lines = serialized.strip().lines().toArray(String[]::new);
		if (lines.length == 0 || !SERIALIZATION_FORMAT_VERSION_STRING.equals(lines[0])) {
			throw new IllegalArgumentException("serialization format version mismatch: expected \""
					+ SERIALIZATION_FORMAT_VERSION_STRING
					+ "\" but got: \"" + (lines.length == 0 ? "" : lines[0]) + "\"");
		}
		Set<SourceSpec4Runtime> inputSpecs = new HashSet<>();
		Set<String> fullNamesFromPrecompFragment = new HashSet<>();
		int i = 1; // skip header line
		for (; i < lines.length; i++) {
			if (lines[i].startsWith(FIELD_FULLNAMES_FROM_PRECOMP_FRAGMENT_START_MARKER)) {
				i++;
				break;
			}
			String[] parts = lines[i].split("\t");
			assert "inputName".equals(parts[0]);
			assert "maxInstances".equals(parts[2]);
			assert "sourceRgPathToRemove".equals(parts[4]);
			assert "endOfLine".equals(parts[6]);
			// note: parts[5], the sourceRgPathToRemove value, can be the empty string
			inputSpecs.add(SourceSpec4Runtime.of(parts[1], Long.parseLong(parts[3]), parts[5]));
		}
		for (; i < lines.length; i++) {
			String fullName = lines[i].trim();
			assert fullName.startsWith("/");
			fullNamesFromPrecompFragment.add(fullName);
		}
		return FixedShapePrecompModelDataMapper.of(inputSpecs, fullNamesFromPrecompFragment);
	}

	/**
	 * @param sourceRgPathToRemove
	 * 		either the path of the single non-repeatable root group of the source DM (if the source uses
	 * 		non-root-preserving includes), or the empty string (if the source uses root-preserving includes,
	 * 		and therefore also may have multiple and repeatable root groups, see also
	 *        {@link #isModelRootInclude()}).
	 */
	public record SourceSpec4Runtime(String inputName, long maxInstances, String sourceRgPathToRemove) {
		public SourceSpec4Runtime {
			Objects.requireNonNull(inputName);
			Objects.requireNonNull(sourceRgPathToRemove);
			if (maxInstances < 1) {
				throw new IllegalArgumentException("maxInstances must be 1 or greater");
			}
			if (!sourceRgPathToRemove.isEmpty() && !(sourceRgPathToRemove.startsWith("/") && sourceRgPathToRemove.length() >= 2)) {
				throw new IllegalArgumentException("sourceRgPathToRemove must either be the empty string or " +
						"be the path of a root group, but was: " + sourceRgPathToRemove);
			}
		}

		public static SourceSpec4Runtime of(String inputName, long maxInstances, String sourceRgPathToRemove) {
			return new SourceSpec4Runtime(inputName, maxInstances, sourceRgPathToRemove);
		}

		/**
		 * @return true if the corresponding source dm uses {@code includeLevel=MODEL_ROOT}, false if it uses
		 *        {@code includeLevel=SINGLE_RG}
		 */
		public boolean isModelRootInclude() {
			return "".equals(sourceRgPathToRemove);
		}
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FixedShapePrecompModelDataMapper that = (FixedShapePrecompModelDataMapper) o;
		return Objects.equals(sourceSpecs, that.sourceSpecs) && Objects.equals(fieldFullNamesInPrecompFragment,
				that.fieldFullNamesInPrecompFragment) && Objects.equals(mapBackInfo_SourceNameToSourceRepToSourceId,
				that.mapBackInfo_SourceNameToSourceRepToSourceId);
	}

	@Override public int hashCode() {
		return Objects.hash(sourceSpecs, fieldFullNamesInPrecompFragment, mapBackInfo_SourceNameToSourceRepToSourceId);
	}
}
