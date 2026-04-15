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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenTableCompressed;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Builds the {@link CodeGenTableCompressed}.
 */
public class CodeGenTableCompressedBuilder extends CodeGenModelBuilder {

	private static final int COMPRESSION_LEVEL = 9;

	private String tableName;
	private int uncompressedLength;
	private int index;
	private byte[] compressedContent;

	public CodeGenTableCompressedBuilder(String tableName, byte[] uncompressedContent, int index, CodeGenData data,
			IProblemReporter problemReporter, PackageInformation packageInformation) {
		super(data, problemReporter, packageInformation);
		this.tableName = tableName;
		this.uncompressedLength = uncompressedContent.length;
		this.index = index;
		this.compressedContent = deflate(uncompressedContent);
	}

	private byte[] deflate(byte[] data) {
		Deflater deflater = new Deflater(COMPRESSION_LEVEL);
		deflater.reset();
		deflater.setInput(data);
		deflater.finish();
		byte[] buf = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		while (!deflater.finished()) {
			int i = deflater.deflate(buf);
			bos.write(buf, 0, i);
		}
		deflater.end();
		try {
			bos.close();
		} catch (IOException e) {
			// This is never reached because ByteArrayOutputStream#close() never throws (see its javadoc)
		}
		return bos.toByteArray();
	}

	public String getTableName() {
		return tableName;
	}

	public int getUncompressedLength() {
		return uncompressedLength;
	}

	public int getIndex() {
		return index;
	}

	public byte[] getCompressedContent() {
		return compressedContent;
	}

	public int getCompressedLength() {
		return compressedContent.length;
	}

	@Override
	public CodeGenTableCompressed build() {
		return new CodeGenTableCompressed(this);
	}
}
