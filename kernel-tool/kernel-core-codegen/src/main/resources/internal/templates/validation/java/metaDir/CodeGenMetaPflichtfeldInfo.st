CodeGenMetaPflichtfeldInfo(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.MetaIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.MetaLokalePflichtInfo;

/*
 * Enthält Metadaten über die Pflichtfeldinformationen.
 *
 */
public class  Meta_PfFeld_<t.name> {
	
    private final IMetaDataValidierung metaDataValidierung;

    Meta_PfFeld_<t.name>(IMetaDataValidierung metaDataValidierung) {
		this.metaDataValidierung = metaDataValidierung;
	}
	
    void addLokalePflichtFeldInfos(Set\<MetaLokalePflichtInfo\> additivUndPflichtfelder) {
    	
    	String[][] infos = new String[<t.anzahlLokalePflichtfeldInfosCode>][];
    	<t.lokalePflichtFeldInfoMethodenAufrufCode>
    	
        for (String [] info : infos) {
            addLokalePflichtFeldInfo(
            		additivUndPflichtfelder,
                    info[0],
                    info[1],
                    Meta_<t.name>.getMetaFSMenge(info[2]),
                    Meta_<t.name>.getMetaFSMenge(info[3]));
        }

    }
    
    <t.lokalePflichtFeldInfoMethodenCode>
	
	private void addLokalePflichtFeldInfo(Set\<MetaLokalePflichtInfo\> additivUndPflichtfelder, String lfdNrMuster, String zeileMuster,
			Set\<MetaIdentifier\> additiv,
			Set\<MetaIdentifier\> pflichtfelder) {
		MetaLokalePflichtInfo info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this.metaDataValidierung, additiv, pflichtfelder, lfdNrMuster, zeileMuster);
		additivUndPflichtfelder.add(info);
	}

}
>>