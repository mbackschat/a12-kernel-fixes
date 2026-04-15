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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.PruefzifferVerfahren.Verfahren;

/**
 * Speicher der Bundesland-spezifischen Eigenschaften:
 * - Stelligkeit
 * - Stellen der FA-Nummer
 * - Prüfzifferverfahren
 * - Muster
 * - erlaubte Bundesfinanzamtsnummern
 */
public class EWAzProperties {

	private static final Map<Integer, Integer> LAENDERNR_STELLIGKEIT;
	private static final Map<Integer, Verfahren> LAENDERNR_VERFAHREN;
	private static final Map<Integer, Integer> LAENDERNR_FA_NR_STELLEN;
	private static final Map<Integer, Pattern> LAENDERNR_MUSTER;
	private static final Map<Integer, Set<Integer>> LAENDERNR_BUFA_NUMMERN;
	private static final Map<Integer, Integer> LAENDERNR_FORMATSCHLUESSEL;

	private static final String TR_REPLACEMENT = "[ \\\\/\\\\.-]{0,1}";
	private static final String Z_REPLACEMENT = "\\\\d";

	private static Pattern getPattern(String muster) {
		String regex = muster.replaceAll("-", TR_REPLACEMENT).replaceAll("Z", Z_REPLACEMENT);
		return Pattern.compile(regex);
	}

	private static final Pattern LAENDERMUSTER_1 = getPattern("ZZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZZ-ZZZ-9ZZ-ZZZZ-ZZZ-Z");
	private static final Pattern LAENDERMUSTER_2 = getPattern("ZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZ-ZZZ-9ZZ-ZZZZ-ZZZ-Z");
	private static final Pattern LAENDERMUSTER_3 = getPattern("ZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZ-ZZZ-7-ZZZZZ-ZZZZ-Z");
	private static final Pattern LAENDERMUSTER_4 = getPattern("ZZ-ZZZ-ZZZZZ");
	private static final Pattern LAENDERMUSTER_5 = getPattern("ZZZ-ZZZ-Z-ZZZZZ-Z");
	private static final Pattern LAENDERMUSTER_6 = getPattern("ZZ-ZZZ-9-ZZZZZZ-ZZZ-Z|ZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZ-ZZZ-7-ZZZZZ-ZZZZ-Z");

	static {
		Map<Integer, Integer> laenderStelligkeit = new HashMap<>();
		laenderStelligkeit.put(9, 18); // Bayern
		laenderStelligkeit.put(30, 18); // Brandenburg
		laenderStelligkeit.put(40, 18); // Mecklenburg-Vorpommern
		laenderStelligkeit.put(27, 18); // Rheinland-Pfalz
		laenderStelligkeit.put(10, 18); // Saarland
		laenderStelligkeit.put(32, 18); // Sachsen
		laenderStelligkeit.put(31, 18); // Sachsen-Anhalt
		laenderStelligkeit.put(41, 18); // Thüringen
		laenderStelligkeit.put(28, 18); // Baden-Württemberg
		laenderStelligkeit.put(23, 18); // Niedersachsen
		laenderStelligkeit.put(11, 12); // Berlin
		laenderStelligkeit.put(24, 12); // Bremen
		laenderStelligkeit.put(22, 12); // Hamburg
		laenderStelligkeit.put(21, 12); // Schleswig-Holstein
		laenderStelligkeit.put(26, 18); // Hessen
		laenderStelligkeit.put(5, 14); // Nordrhein-Westfalen
		LAENDERNR_STELLIGKEIT = Collections.unmodifiableMap(laenderStelligkeit);

		Map<Integer, Verfahren> laenderVerfahrenDef = new HashMap<>();
		laenderVerfahrenDef.put(9, Verfahren.A1); // Bayern
		laenderVerfahrenDef.put(30, Verfahren.A1); // Brandenburg
		laenderVerfahrenDef.put(40, Verfahren.A1); // Mecklenburg-Vorpommern
		laenderVerfahrenDef.put(27, Verfahren.A1); // Rheinland-Pfalz
		laenderVerfahrenDef.put(10, Verfahren.A1); // Saarland
		laenderVerfahrenDef.put(32, Verfahren.A1); // Sachsen
		laenderVerfahrenDef.put(31, Verfahren.A1); // Sachsen-Anhalt
		laenderVerfahrenDef.put(41, Verfahren.A1); // Thüringen
		laenderVerfahrenDef.put(28, Verfahren.A2); // Baden-Württemberg
		laenderVerfahrenDef.put(23, Verfahren.A3); // Niedersachsen
		laenderVerfahrenDef.put(11, Verfahren.STNR); // Berlin
		laenderVerfahrenDef.put(24, Verfahren.STNR); // Bremen
		laenderVerfahrenDef.put(22, Verfahren.STNR); // Hamburg
		laenderVerfahrenDef.put(21, Verfahren.STNR); // Schleswig-Holstein
		laenderVerfahrenDef.put(26, Verfahren.A2); // Hessen
		laenderVerfahrenDef.put(5, Verfahren.NRW); // Nordrhein-Westfalen
		LAENDERNR_VERFAHREN = Collections.unmodifiableMap(laenderVerfahrenDef);

		Map<Integer, Integer> laenderFANrStellen = new HashMap<>();
		laenderFANrStellen.put(9, 3); // Bayern
		laenderFANrStellen.put(30, 3); // Brandenburg
		laenderFANrStellen.put(40, 3); // Mecklenburg-Vorpommern
		laenderFANrStellen.put(27, 3); // Rheinland-Pfalz
		laenderFANrStellen.put(10, 3); // Saarland
		laenderFANrStellen.put(32, 3); // Sachsen
		laenderFANrStellen.put(31, 3); // Sachsen-Anhalt
		laenderFANrStellen.put(41, 3); // Thüringen
		laenderFANrStellen.put(28, 2); // Baden-Württemberg
		laenderFANrStellen.put(23, 2); // Niedersachsen
		laenderFANrStellen.put(11, 2); // Berlin
		laenderFANrStellen.put(24, 2); // Bremen
		laenderFANrStellen.put(22, 2); // Hamburg
		laenderFANrStellen.put(21, 2); // Schleswig-Holstein
		laenderFANrStellen.put(26, 2); // Hessen
		laenderFANrStellen.put(5, 3); // Nordrhein-Westfalen
		LAENDERNR_FA_NR_STELLEN = Collections.unmodifiableMap(laenderFANrStellen);

		Map<Integer, Pattern> laenderMuster = new HashMap<>();
		laenderMuster.put(9, LAENDERMUSTER_1); // Bayern
		laenderMuster.put(30, LAENDERMUSTER_1); // Brandenburg
		laenderMuster.put(40, LAENDERMUSTER_1); // Mecklenburg-Vorpommern
		laenderMuster.put(27, LAENDERMUSTER_1); // Rheinland-Pfalz
		laenderMuster.put(10, LAENDERMUSTER_1); // Saarland
		laenderMuster.put(32, LAENDERMUSTER_1); // Sachsen
		laenderMuster.put(31, LAENDERMUSTER_1); // Sachsen-Anhalt
		laenderMuster.put(41, LAENDERMUSTER_1); // Thüringen
		laenderMuster.put(28, LAENDERMUSTER_6); // Baden-Württemberg
		laenderMuster.put(23, LAENDERMUSTER_2); // Niedersachsen
		laenderMuster.put(11, LAENDERMUSTER_4); // Berlin
		laenderMuster.put(24, LAENDERMUSTER_4); // Bremen
		laenderMuster.put(22, LAENDERMUSTER_4); // Hamburg
		laenderMuster.put(21, LAENDERMUSTER_4); // Schleswig-Holstein
		laenderMuster.put(26, LAENDERMUSTER_3); // Hessen
		laenderMuster.put(5, LAENDERMUSTER_5); // Nordrhein-Westfalen
		LAENDERNR_MUSTER = Collections.unmodifiableMap(laenderMuster);

		Map<Integer, Set<Integer>> laenderBundesfinanzamtnummern = new HashMap<>();
		laenderBundesfinanzamtnummern.put(91, getValidBundesfinanzamtnummern(91)); // Bayern
		laenderBundesfinanzamtnummern.put(92, getValidBundesfinanzamtnummern(92)); // Bayern
		laenderBundesfinanzamtnummern.put(30, getValidBundesfinanzamtnummern(30)); // Brandenburg
		laenderBundesfinanzamtnummern.put(40, getValidBundesfinanzamtnummern(40)); // Mecklenburg-Vorpommern
		laenderBundesfinanzamtnummern.put(27, getValidBundesfinanzamtnummern(27)); // Rheinland-Pfalz
		laenderBundesfinanzamtnummern.put(10, getValidBundesfinanzamtnummern(10)); // Saarland
		laenderBundesfinanzamtnummern.put(32, getValidBundesfinanzamtnummern(32)); // Sachsen
		laenderBundesfinanzamtnummern.put(31, getValidBundesfinanzamtnummern(31)); // Sachsen-Anhalt
		laenderBundesfinanzamtnummern.put(41, getValidBundesfinanzamtnummern(41)); // Thüringen
		laenderBundesfinanzamtnummern.put(28, getValidBundesfinanzamtnummern(28)); // Baden-Württemberg
		laenderBundesfinanzamtnummern.put(23, getValidBundesfinanzamtnummern(23)); // Niedersachsen
		laenderBundesfinanzamtnummern.put(11, getValidBundesfinanzamtnummern(11)); // Berlin
		laenderBundesfinanzamtnummern.put(24, getValidBundesfinanzamtnummern(24)); // Bremen
		laenderBundesfinanzamtnummern.put(22, getValidBundesfinanzamtnummern(22)); // Hamburg
		laenderBundesfinanzamtnummern.put(21, getValidBundesfinanzamtnummern(21)); // Schleswig-Holstein
		laenderBundesfinanzamtnummern.put(26, getValidBundesfinanzamtnummern(26)); // Hessen
		laenderBundesfinanzamtnummern.put(51, getValidBundesfinanzamtnummern(51)); // Nordrhein-Westfalen
		laenderBundesfinanzamtnummern.put(52, getValidBundesfinanzamtnummern(52)); // Nordrhein-Westfalen
		laenderBundesfinanzamtnummern.put(53, getValidBundesfinanzamtnummern(53)); // Nordrhein-Westfalen
		LAENDERNR_BUFA_NUMMERN = Collections.unmodifiableMap(laenderBundesfinanzamtnummern);

		Map<Integer, Integer> laenderFormatschluessel = new HashMap<>();
		laenderFormatschluessel.put(9, 4); // Bayern
		laenderFormatschluessel.put(30, 4); // Brandenburg
		laenderFormatschluessel.put(40, 4); // Mecklenburg-Vorpommern
		laenderFormatschluessel.put(27, 4); // Rheinland-Pfalz
		laenderFormatschluessel.put(10, 4); // Saarland
		laenderFormatschluessel.put(32, 4); // Sachsen
		laenderFormatschluessel.put(31, 4); // Sachsen-Anhalt
		laenderFormatschluessel.put(41, 4); // Thüringen
		laenderFormatschluessel.put(28, 4); // Baden-Württemberg
		laenderFormatschluessel.put(23, 4); // Niedersachsen
		laenderFormatschluessel.put(11, 0); // Berlin
		laenderFormatschluessel.put(24, 0); // Bremen
		laenderFormatschluessel.put(22, 0); // Hamburg
		laenderFormatschluessel.put(21, 0); // Schleswig-Holstein
		laenderFormatschluessel.put(26, 4); // Hessen
		laenderFormatschluessel.put(5, 5); // Nordrhein-Westfalen
		LAENDERNR_FORMATSCHLUESSEL = Collections.unmodifiableMap(laenderFormatschluessel);
	}

	private EWAzProperties() {
	}

	public static int getStelligkeit(Integer buLaNr) {
		return LAENDERNR_STELLIGKEIT.get(buLaNr);
	}

	public static Verfahren getVerfahrenDef(Integer buLaNr) {
		return LAENDERNR_VERFAHREN.get(buLaNr);
	}

	public static int getFANrStellen(Integer buLaNr) {
		return LAENDERNR_FA_NR_STELLEN.get(buLaNr);
	}

	public static Pattern getMuster(Integer buLaNr) {
		return LAENDERNR_MUSTER.get(buLaNr);
	}

	public static Set<Integer> getBundesfinanzamtsnummern(Integer buLaNr) {
		return LAENDERNR_BUFA_NUMMERN.containsKey(buLaNr) ? LAENDERNR_BUFA_NUMMERN.get(buLaNr) : new HashSet<>();
	}

	public static int getFormatschluessel(Integer buLaNr) {
		return LAENDERNR_FORMATSCHLUESSEL.get(buLaNr);
	}

	private static Set<Integer> getValidBundesfinanzamtnummern(int buLaNr) {
		Set<Integer> bundesfinanzamtsnummernSet = new HashSet<>();
		switch (buLaNr) {
			case 22: // Hamburg
				// Nur FA 16
				bundesfinanzamtsnummernSet.add(2216);
				break;
			case 21: // Schleswig-Holstein
				// Nur FÄ 71 bis 92
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2171, 2192).boxed().collect(Collectors.toSet()));
				break;
			case 24: // Bremen
				// Nur FÄ 57 und 77
				bundesfinanzamtsnummernSet.add(2457);
				bundesfinanzamtsnummernSet.add(2477);
				break;
			case 11: // Berlin
				// Alle FÄ mit Ausnahme von 15, 28, 38 und 91
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(1100, 1114).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(1116, 1127).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(1129, 1137).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(1139, 1190).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(1192, 1199).boxed().collect(Collectors.toSet()));
				break;
			case 91: // Bayern
				// FA 102 - 115, 117, 119, 121, 123 - 127, 131 - 132, 134, 138 - 142, 151 - 154, 156 - 157, 159, 161 – 163, 168 – 171,
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9102, 9115).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(9117);
				bundesfinanzamtsnummernSet.add(9119);
				bundesfinanzamtsnummernSet.add(9121);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9123, 9127).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9131, 9132).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(9134);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9138, 9142).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9151, 9154).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9156, 9157).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(9159);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9161, 9163).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9168, 9171).boxed().collect(Collectors.toSet()));
				break;
			case 92: // Bayern
				// 201 – 208, 211 – 212, 216 – 218, 220 - 223, 227 - 231, 235, 241, 244, 247 – 249, 252, 254 – 255, 257 - 259
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9201, 9208).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9211, 9212).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9216, 9218).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9220, 9223).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9227, 9231).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(9235);
				bundesfinanzamtsnummernSet.add(9241);
				bundesfinanzamtsnummernSet.add(9244);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9247, 9249).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(9252);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9254, 9255).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(9257, 9259).boxed().collect(Collectors.toSet()));
				break;
			case 10: // Saarland
				// 010, 055, 060
				bundesfinanzamtsnummernSet.add(1010);
				bundesfinanzamtsnummernSet.add(1055);
				bundesfinanzamtsnummernSet.add(1060);
				break;
			case 23: // Niedersachsen
				// 313 – 323, 326 – 331, 333 – 336, 338, 340 – 341, 343 – 361, 363 – 370, 375, 376, 378, 379, 388
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2313, 2323).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2326, 2331).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2333, 2336).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2338);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2340, 2341).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2343, 2361).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2363, 2370).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2375, 2376).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2378, 2379).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2388);
				break;
			case 26: // Hessen
				// Nur FÄ 01 bis 47
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2601, 2647).boxed().collect(Collectors.toSet()));
				break;
			case 27: // Rheinland-Pfalz
				// 701 – 702, 706, 708 – 710, 719, 722 – 724, 726 – 727, 729 – 732, 735, 740 - 744
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2701, 2702).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2706);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2708, 2710).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2719);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2722, 2724).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2726, 2727).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2729, 2732).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2735);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2740, 2744).boxed().collect(Collectors.toSet()));
				break;
			case 28: // Baden-Württemberg
				// 801, 804 – 812, 814 – 816, 818 – 823, 830 – 859, 861 – 865, 869-871, 874, 876 – 891, 899
				bundesfinanzamtsnummernSet.add(2801);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2804, 2812).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2814, 2816).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2818, 2823).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2830, 2859).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2861, 2865).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2869, 2871).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2874);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(2876, 2891).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(2899);
				break;
			case 30: // Brandenburg
				// 046, 048 – 053, 056, 057, 061, 062, 064, 065
				bundesfinanzamtsnummernSet.add(3046);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3048, 3053).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3056, 3057).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3061, 3062).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3064, 3065).boxed().collect(Collectors.toSet()));
				break;
			case 31: // Sachsen-Anhalt
				// 102 – 103, 105 – 108, 110, 112, 114 - 119
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3102, 3103).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3105, 3108).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(3110);
				bundesfinanzamtsnummernSet.add(3112);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3114, 3119).boxed().collect(Collectors.toSet()));
				break;
			case 32: // Sachsen
				// 202, 204, 207 – 210, 213 – 214, 217 – 218, 220, 222 – 224, 227 – 228, 232, 236 - 239
				bundesfinanzamtsnummernSet.add(3202);
				bundesfinanzamtsnummernSet.add(3204);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3207, 3210).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3213, 3214).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3217, 3218).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(3220);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3222, 3224).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3227, 3228).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(3232);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(3236, 3239).boxed().collect(Collectors.toSet()));
				break;
			case 40: // Mecklenburg-Vorpommern
				// 072, 075, 079 – 082, 084, 086, 087, 090
				bundesfinanzamtsnummernSet.add(4072);
				bundesfinanzamtsnummernSet.add(4075);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(4079, 4082).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(4084);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(4086, 4087).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(4090);
				break;
			case 41: // Thüringen
				// 151, 154 – 157, 159, 161 - 162, 165, 166, 171
				bundesfinanzamtsnummernSet.add(4151);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(4154, 4157).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(4159);
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(4161, 4162).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(4165, 4166).boxed().collect(Collectors.toSet()));
				bundesfinanzamtsnummernSet.add(4171);
				break;
			case 51: // Nordrhein-Westfalen (OFD Düsseldorf)
				// Nur FÄ 01 bis 83
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(5101, 5183).boxed().collect(Collectors.toSet()));
				break;
			case 52: // Nordrhein-Westfalen (OFD Köln)
				// Nur FÄ 01 bis 83
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(5201, 5283).boxed().collect(Collectors.toSet()));
				break;
			case 53: // Nordrhein-Westfalen (OFD Münster)
				// Nur FÄ 01 bis 84
				bundesfinanzamtsnummernSet.addAll(IntStream.rangeClosed(5301, 5384).boxed().collect(Collectors.toSet()));
				break;
			default:
				throw new IllegalArgumentException("Die übergebene Bundeslandnummer \"" + buLaNr + "\" ist nicht gültig");
		}
		return bundesfinanzamtsnummernSet;
	}

}
