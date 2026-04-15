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

package com.mgmtp.a12.kernel.core.utils.internal;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing functionality based on an MD5 checksum.
 */
public class HashingHelper {
	private HashingHelper() {
		throw new UnsupportedOperationException("Helper class should not be instantiated!");
	}

	/**
	 * Calculate an MD5 checksum for the given string.
	 *
	 * @param string
	 * 		the string to be hashed.
	 * @return the MD5 checksum.
	 */
	public static String calculateHash(final String string) {
		// nosemgrep: java.lang.security.audit.crypto.use-of-md5.use-of-md5
		MessageDigest messageDigest = createMessageDigest("MD5");
		messageDigest.update(string.getBytes());
		final byte[] digest = messageDigest.digest();
		final BigInteger bigInt = new BigInteger(1, digest);
		return bigInt.toString(16);
	}

	public static String calculateHashSha256(final String string) {
		MessageDigest messageDigest = createMessageDigest("SHA-256");
		messageDigest.update(string.getBytes());
		final byte[] digest = messageDigest.digest();
		final BigInteger bigInt = new BigInteger(1, digest);
		return bigInt.toString(16);
	}

	private static MessageDigest createMessageDigest(String algorithName) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithName);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Unable to retrieve " + algorithName + " message digest", e);
		}
		messageDigest.reset();
		return messageDigest;
	}
}
