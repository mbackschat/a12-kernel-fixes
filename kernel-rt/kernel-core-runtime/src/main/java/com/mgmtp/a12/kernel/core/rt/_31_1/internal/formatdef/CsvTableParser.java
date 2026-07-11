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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parser for CSV-tables for enumerations with table reference.
 */
public class CsvTableParser {
	private static final String SEPARATOR = ";";

	private CsvTableParser() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Parses a CSV-File to a map of column name to list of column values.
	 *
	 * @param csvFile
	 * 		the name of the CSV-file for the case of an error.
	 * @param csvFileReader
	 * 		the reader to read the CSV-data from.
	 * @return a mapping from column name to the list of the values in the column.
	 *
	 * @throws IllegalArgumentException
	 * 		if there are inconsistencies in the table.
	 * @throws UncheckedIOException
	 * 		in case of errors when the reading from the csvFileReader.
	 */
	public static Map<String, List<String>> parseCsv(String csvFile, Reader csvFileReader) {
		Map<String, List<String>> result = new HashMap<>();
		try (BufferedReader br = new BufferedReader(csvFileReader)) {
			String[] headerStr = br.readLine().split(SEPARATOR, -1);
			List<String> headers = Arrays.stream(headerStr).map(String::trim).collect(Collectors.toList());
			for (String header : headers) {
				result.put(header, new ArrayList<>());
			}
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] parts = line.split(SEPARATOR, -1);
				if (parts.length != headers.size()) {
					throw new IllegalArgumentException(
							"Erroneous csv-file " + csvFile + " values " + line + " does not correspond to header " + headers);
				}
				for (int i = 0; i < parts.length; i++) {
					result.get(headers.get(i)).add(parts[i].trim());
				}
			}
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
