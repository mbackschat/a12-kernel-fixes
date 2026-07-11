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

package com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.util.SMMNotificationSource;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.FieldOrGroup;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SMMConstants;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SourceOrTarget;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.Type;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_structuralmappingmodel__0031.Factory_codegen_mm_structuralmappingmodel__0031;

public class StructuralMappingModelNotifier {
	private static final String BUNDLE_NAME = "com.mgmtp.a12.kernel.md.structuralmapping.internal.i18n.messages";

	private final Consumer<? super RankedNotification> notificationReceiver;
	private final Locale locale;
	private final ResourceBundle bundle;

	private int errorCount = 0;
	private int warningCount = 0;
	private int infoCount = 0;

	public StructuralMappingModelNotifier(Consumer<? super RankedNotification> notificationReceiver, Locale locale) {
		this.notificationReceiver = notificationReceiver;
		this.locale = locale;
		this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.forLanguageTag(locale.getLanguage()));
	}

	public Locale locale() {
		return locale;
	}

	public void forwardMetaModelValidationMessage(IMessage message, MM_StructuralMappingModel_1 smm) {
		forwardValidationMessage(message, null, () -> message.getErrorFieldPointer().toDocumentPointer()
				.map(pointer -> {
					String ancestorFullName = repeatableAncestor(pointer.fullName(),
							Factory_codegen_mm_structuralmappingmodel__0031.getInstance().getMetaObject());
					if (ancestorFullName == null) {
						return null;
					}
					List<String> newPath = ElementPathUtils.splitAbsPath(ancestorFullName);
					DocumentPointer newPointer = DocumentPointer.of(newPath,
							pointer.repetitionIndexes().subList(0, newPath.size()));
					GroupInstanceV2 groupInstance = smm._unwrap().group(newPointer);
					return new SMMNotificationSource<>(newPointer, typedGroupView(ancestorFullName, groupInstance));
				}).orElse(null));
	}

	public void forwardValidationMessage(IMessage message, NotificationCode errorCodeOrNull,
			Supplier<SMMNotificationSource<?>> sourceSupplier) {
		notify(StructuralMappingModelNotification.builder()
				.severity(Severity.valueOf(message.getSeverity().name()))
				.rawMessage(message.getErrorText())
				.errorCode(errorCodeOrNull == null ? message.getErrorCode() : errorCodeOrNull.key())
				.source(sourceSupplier.get())
				.build());
	}

	private static TypedGroupView<?> typedGroupView(String groupFullName, GroupInstanceV2 groupInstance) {
		if (groupFullName.equals(SMMConstants.FULL_NAME_MAPPING_BLOCKS)) {
			return MappingBlocks._viewOf(groupInstance);
		} else if (groupFullName.equals(SMMConstants.FULL_NAME_GROUPS_TO_CLEAR)) {
			return GroupsToClearOnFirstFill._viewOf(groupInstance);
		} else if (groupFullName.equals(SMMConstants.FULL_NAME_FIELD_MAPPINGS)) {
			return FieldMappings._viewOf(groupInstance);
		} else if (groupFullName.equals(SMMConstants.FULL_NAME_RESOLUTION_STRATEGIES)) {
			return ResolutionStrategies._viewOf(groupInstance);
		} else {
			throw new IllegalStateException("Cannot determine typed group view for groupFullName: " + groupFullName);
		}
	}

	private static String repeatableAncestor(String fullName, IMetaModel metaModel) {
		List<String> ancestorPaths = ElementPathUtils.getAncestorPaths(fullName);
		ancestorPaths.add(fullName);
		Collections.reverse(ancestorPaths);
		for (String ancestorPath : ancestorPaths) {
			if (isRepeatable(ancestorPath, metaModel)) {
				return ancestorPath;
			}
		}
		return null;
	}

	private static boolean isRepeatable(String fullName, IMetaModel metaModel) {
		IMetaGroup metaGroup = (IMetaGroup) metaModel.getValue(IMetaKeys.MODEL_META_GROUP, fullName);
		if (metaGroup != null) {
			Long repeatability = (Long) metaGroup.getValue(IMetaKeys.GROUP_MAX_INDEX);
			return repeatability > 1;
		}
		return false;
	}

	public void error(SMMNotificationSource<?> source, NotificationCode code, Object... arguments) {
		notify(Severity.ERROR, source, code, arguments);
	}

	public void warning(SMMNotificationSource<?> source, NotificationCode code, Object... arguments) {
		notify(Severity.WARNING, source, code, arguments);
	}

	public void info(SMMNotificationSource<?> source, NotificationCode code, Object... arguments) {
		notify(Severity.INFO, source, code, arguments);
	}

	private void notify(Severity severity, SMMNotificationSource<?> source, NotificationCode code,
			Object... arguments) {
		notify(StructuralMappingModelNotification.builder()
				.severity(severity)
				.source(source)
				.errorCode(code.key())
				.rawMessage(getMessage(code, arguments))
				.build());
	}

	private void notify(StructuralMappingModelNotification notification) {
		switch (notification.getSeverity()) {
			case ERROR -> errorCount++;
			case WARNING -> warningCount++;
			case INFO -> infoCount++;
		}
		notificationReceiver.accept(notification);
	}

	private String getMessage(NotificationCode code, Object... args) {
		return MessageFormat.format(getString(code), args);
	}

	private String getString(NotificationCode code) {
		return bundle.getString(code.key());
	}

	public String getString(SourceOrTarget sourceOrTarget) {
		return getString(switch (sourceOrTarget) {
			case SOURCE -> NotificationCode.FRAGMENT_SOURCE;
			case TARGET -> NotificationCode.FRAGMENT_TARGET;
		});
	}

	public String getString(FieldOrGroup fieldOrGroup) {
		return getString(switch (fieldOrGroup) {
			case FIELD -> NotificationCode.FRAGMENT_FIELD;
			case GROUP -> NotificationCode.FRAGMENT_GROUP;
		});
	}

	public String getString(Type type) {
		if (type == null) {
			return getString(NotificationCode.FRAGMENT_RS_UNKNOWN);
		}
		return getString(switch (type) {
			case Fold -> NotificationCode.FRAGMENT_RS_FOLD;
			case Slice -> NotificationCode.FRAGMENT_RS_SLICE;
		});
	}

	public String getString(FieldMappings fm) {
		return getMessage(NotificationCode.FRAGMENT_FM, fm.sourceFieldFullName(), fm.targetFieldFullName());
	}

	public String getString(ResolutionStrategies rs) {
		return getMessage(NotificationCode.FRAGMENT_RS, getString(rs.type()), rs.sourceGroupFullName(), rs.targetGroupFullName());
	}

	public String getString(Element element) {
		return switch (element) {
			case Field field -> getString(NotificationCode.FRAGMENT_FIELD);
			case Group group -> getString(NotificationCode.FRAGMENT_GROUP);
			case Computation computation -> getString(NotificationCode.FRAGMENT_COMPUTATION);
			case Rule rule -> getString(NotificationCode.FRAGMENT_RULE);
			case null, default -> getString(NotificationCode.FRAGMENT_UNKNOWN);
		};
	}

	public boolean hasError() {
		return errorCount > 0;
	}

	public boolean hasNoError() {
		return errorCount == 0;
	}

	public boolean hasNotifications() {
		return errorCount + warningCount + infoCount > 0;
	}
}
