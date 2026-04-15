CodeGenMetaModel(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IDynEnumsIntern;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VersionCompatibilityCheck;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.TrieNode;
<if(t.withMappingInfos)>
import <t.internalUXmlMapperPackageName>.MapInformation_<t.name>;
<endif>
<if (t.extmp)>
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.MetaModelValueAccessorExt;
<endif>

public class Meta_<t.name> implements IMetaModel, IMetaEntityInternal {

<! WARNING: Updated by versionupdate script. Don't change this manually! !>
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	private static final String RUNTIME_VERSION = new String("<t.GencodeJavaGroovyRuntimeVersion>");

    private final MetaModelImpl meta;
    private Meta_Felder_<t.name> metaFelder = new Meta_Felder_<t.name>();
    <if(!t.mapperOnly)>private Meta_Regel_<t.name> metaRegeln = new Meta_Regel_<t.name>();
    <endif>

    private final List\<Locale\> unterstuetzteSprachen = Arrays.asList( <t.unterstuetzteSprachenCode> );
    
    private static final Map\<Locale, PresentationInformationValues\> DEFAULT_PRESENTATION_INFO = <t.defaultPresentationInfo>;

    <if(t.EingeschraenkteZeichen)>
	public static final int[] LEGAL_CHARACTERS_START = {
		<t.erlaubterZeichensatzCodeStart>
 	};
 	public static final int[] LEGAL_CHARACTERS_END = {
 		<t.erlaubterZeichensatzCodeEnd>
 	};
 	public static final int[][] LEGAL_CHARACTERS = new int[][] {
 		LEGAL_CHARACTERS_START, LEGAL_CHARACTERS_END
 	};
    public static final TrieNode LEGAL_GRAPHEME_TRIE = new TrieNode(new HashMap\<\>(), false);
	<else>
	public static final int[] LEGAL_CHARACTERS_START = null;
	public static final int[] LEGAL_CHARACTERS_END = null;
	public static final int[][] LEGAL_CHARACTERS = null;
	public static final TrieNode LEGAL_GRAPHEME_TRIE = new TrieNode(new HashMap\<\>(), false);
	<endif>

	// Der Konstruktor braucht den bereits sortierten LEGAL_CHARACTERS-Array. 
    private static final Meta_<t.name> instance = new Meta_<t.name>();

	static {
	  	<t.initLegalGraphemeTrie>
	}

    public static Meta_<t.name> getInstance() {
        return instance;
    }
    
    public Object getValue(String key, Object... params) {
        <if (t.extmp)>
        return new MetaModelValueAccessorExt(
            meta,
            <if(t.withMappingInfos)>MapInformation_<t.name>.getFieldMapInfosByUXMLName()<else>null<endif>)
            .getValue(key, params);
		<else>
		return meta.getValue(key, params);
        <endif>
    }

    public String[] getValidKeys() {
        return meta.getValidKeys();
    }

    public Set\<String\> getValidKeysInternal() {
        return meta.getValidKeysInternal();
    }

	<t.metaContainerSplitFunctionCode>

	static Container_t[] initContainer() {
		Container_t[] result = new Container_t[<t.numContainers>];
		<t.metaContainerSplitFunctionCalls>
		return result;
	};

	<t.metaCode>
	 
	static Set\<MetaIdentifier\> getMetaFSMenge(String beschreibung) {
        Set\<MetaIdentifier\> result = new HashSet\<MetaIdentifier\>();
        String[] felder = beschreibung.split(";");
        for (int i=0; i \< felder.length; i++) {
            if (felder[i].length() \> 0) {
                result.add(MetaIdentifier.parse(felder[i]));
            }
        }
        return result;
    }

	@SuppressWarnings("unchecked")
	private Meta_<t.name>() {
		Container_t[] container = initContainer();

	    meta = new MetaModelImpl(
					da,
					metaFelder.getFelder().toArray(new Feld_t[metaFelder.getFelder().size()]), 
					<if(!t.mapperOnly)>metaRegeln.getRegeln().toArray(new Regel_t[metaRegeln.getRegeln().size()])<else>null<endif>,
					container,
					LEGAL_CHARACTERS,
					LEGAL_GRAPHEME_TRIE,
					DEFAULT_PRESENTATION_INFO,
					<t.extmp>,
					<if(!t.mapperOnly)>E_<t.name>::getInstance<else>null<endif>,
					<if(!t.mapperOnly)>ERValidator_<t.name>::getInstance<else>null<endif>);
		<if (t.extmp)>
    	// Der XML-Writer/-Reader kann nicht bereits dem Konstruktor von MetaModelImpl uebergeben werden,
    	// denn dessen Konstruktor benoetigt die MetaModelImpl-Instanz.
    	meta.addWert(IExtmpMetaKeysBase.MODEL_XMLWRITER, <t.xmlWriterCode>);
        meta.addWert(IExtmpMetaKeysBase.MODEL_UXMLWRITER, <t.uXmlWriterCode>);
        meta.addWert(IExtmpMetaKeysBase.MODEL_XMLREADER, <t.xmlReaderCode>);
    	<else>
    	meta.addWert(IExtmpMetaKeysBase.MODEL_XMLWRITER, null);
        meta.addWert(IExtmpMetaKeysBase.MODEL_UXMLWRITER, null);
        meta.addWert(IExtmpMetaKeysBase.MODEL_XMLREADER, null);
		<endif>
    	<if(!t.mapperOnly)>
		Meta_PfFeld_<t.name> metaPlfFeldInfo = new Meta_PfFeld_<t.name> (getMetaDataValidierung());
		metaPlfFeldInfo.addLokalePflichtFeldInfos((Set\<MetaLokalePflichtInfo\>) meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
		<endif>
    }
	
	public IMetaDataValidierung getMetaDataValidierung() {
		return meta.getMetaDataValidierung();
	}
	
	public boolean isSpracheUnterstuetzt(Locale sprache) {
		return unterstuetzteSprachen.contains(sprache);
	}
}

>>