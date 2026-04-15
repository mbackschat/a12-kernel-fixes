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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV1V2Converter;
import com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.EntityInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.EntityInstanceUtils;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

import lombok.NonNull;

public class DocumentV1V2ConverterImpl implements IDocumentV1V2Converter {
	private static final DocumentV1V2ConverterImpl INSTANCE = new DocumentV1V2ConverterImpl();

	private DocumentV1V2ConverterImpl() {
	} // singleton stateless utility class. use getInstance() to obtain the instance

	public static DocumentV1V2ConverterImpl getInstance() {
		return INSTANCE;
	}

	@Override public IDocument toDocumentV1(@NonNull DocumentV2 base) {
		return toDocumentV1(base, null);
	}

	public IDocument toDocumentV1(@NonNull DocumentV2 base, IDocumentFactory documentFactory) {
		if (documentFactory == null) {
			documentFactory = new DocumentFactoryImpl();
		}

		IDocument docV1 = documentFactory.createDocument(base.getDocumentModelId());
		base.getId().ifPresent(docV1::setId);

		// has to be used, as we access it in the visitor
		IDocumentFactory finalDocumentFactory = documentFactory;

		base.traverse(new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				if (pointerRelativeToBase.size() > 0) { // don't add the base itself
					docV1.addEntityInstance(documentPointerToGroupInstance(pointerRelativeToBase, finalDocumentFactory));
				}
				return DescendType.VISIT_CHILDREN;
			}

			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				docV1.addEntityInstance(documentPointerToFieldInstance(pointerRelativeToBase, field.value(), finalDocumentFactory));
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field); // not strictly necessary - but this will
				// ensure that the assertion in the super method is reached (if assertion are enabled)
			}
		});
		return docV1;
	}

	@Override public DocumentV2 ofDocumentV1(@NonNull IDocument document) {
		List<UpdateAction> allInserts = docV1ToUpdateActions(document);
		return DocumentV2.empty(document.getDocumentModelId(), document.getId().orElse(null))
				.withBatchUpdates(allInserts);
	}

	@Override public Optional<DocumentPointer> pointerFromEntityInstance(@NonNull IEntityInstance entityInstance) {
		return pointerFromEntityInstanceInternal(entityInstance);
	}

	@Override public Optional<DocumentMultiPointer> multiPointerFromEntityInstance(@NonNull IEntityInstance entityInstance) {
		return multiPointerFromEntityInstanceInternal(entityInstance);
	}

	@Override public IEntityInstance entityInstanceFromPointer(@NonNull DocumentPointer pointer) {
		return new EntityInstanceImpl(pointer.fullName(), pointer.repetitionIndexes().stream().mapToInt(i -> i).toArray());
	}

	@Override public IEntityInstance entityInstanceFromMultiPointer(@NonNull DocumentMultiPointer pointer) {
		return new EntityInstanceImpl(pointer.fullName(), pointer.repetitionIndexes().stream().mapToInt(i -> i).toArray());
	}

	public static List<UpdateAction> docV1ToUpdateActions(@NonNull IDocument document) {
		return docV1ToUpdateActions(document.getEntityInstances());
	}

	public static List<UpdateAction> docV1ToUpdateActions(@NonNull Collection<IEntityInstance> eis) {
		return eis.stream().map(ei -> {
			Optional<DocumentPointer> pointer = pointerFromEntityInstanceInternal(ei);

			if (pointer.isEmpty()) {
				throw new IllegalArgumentException("cannot convert entity instance " + ei + " to update action");
			}

			if (ei instanceof IFieldInstance fi) {
				return UpdateAction.putField(pointer.get(), FieldInstanceV2.ofV1Value(fi.getValue().orElse(null)));
			} else if (ei instanceof IGroupInstance) {
				return UpdateAction.putGroup(pointer.get(), GroupInstanceV2.empty());
			} else {
				throw new IllegalArgumentException(
						"unexpected type of entity instance: expected IFieldInstance or IGroupInstance " +
								"but got " + ei.getClass());
			}
		}).toList();
	}

	private static Optional<DocumentPointer> pointerFromEntityInstanceInternal(@NonNull IEntityInstance ei) {
		List<String> path = Arrays.asList(EntityInstanceUtils.getPathParts(ei.getPath()));
		List<Integer> repetitions = Arrays.stream(ei.getRepetitions()).boxed().toList();

		try {
			return Optional.of(DocumentPointer.of(path, repetitions));
		} catch (IllegalArgumentException _ex) {
			return Optional.empty();
		}
	}

	private static Optional<DocumentMultiPointer> multiPointerFromEntityInstanceInternal(@NonNull IEntityInstance ei) {
		List<String> path = Arrays.asList(EntityInstanceUtils.getPathParts(ei.getPath()));
		List<Integer> repetitions = Arrays.stream(ei.getRepetitions()).boxed().toList();

		try {
			return Optional.of(DocumentMultiPointer.of(path, repetitions));
		} catch (IllegalArgumentException _ex) {
			return Optional.empty();
		}
	}

	private static IFieldInstance documentPointerToFieldInstance(@NonNull DocumentPointer ptr, Object v2Value,
			@NonNull IDocumentFactory documentFactory) {
		Object v1Value = BasicTypeV1V2Conversion.basicTypeV2ToV1(v2Value);
		return documentFactory.createFieldInstance(ptr.fullName(),
				ptr.repetitionIndexes().stream().mapToInt(i -> i).toArray(),
				v1Value);
	}

	private static IGroupInstance documentPointerToGroupInstance(@NonNull DocumentPointer ptr,
			@NonNull IDocumentFactory documentFactory) {
		return documentFactory.createGroupInstance(ptr.fullName(), ptr.repetitionIndexes().stream().mapToInt(i -> i).toArray());
	}
}
