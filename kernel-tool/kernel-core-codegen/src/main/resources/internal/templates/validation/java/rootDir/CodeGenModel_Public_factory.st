CodeGenModel_Public_factory(t) ::= <<
<t.generatedFileInformation>

package <t.publicPackageName>;

import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;

<if(t.extmp)>
import java.util.Set;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.extmp.ExtmpDataTransferFactoryImpl;
import com.mgmtp.a12.kernel.core.rt.extmp.a12internal.datatransfer.IExtmpDataTransferFactory;
import com.mgmtp.a12.kernel.core.rt.extmp.a12internal.datatransfer.IExtmpDataTransferService;
<endif>

public class Factory_<t.name> implements IGeneratedCodeAccessFactory<if(t.extmp)>, IExtmpDataTransferFactory<endif> {

    private static final Factory_<t.name> INSTANCE = new Factory_<t.name>();

    private final <t.packageName>.Factory_<t.name> internalFactory =
        <t.packageName>.Factory_<t.name>.getInstance();

    public static Factory_<t.name> getInstance() {
        return INSTANCE;
    }

    private Factory_<t.name>() { }

	public IMetaModel getMetaObject() {
		return internalFactory.getMetaObject();
	}

	public IValidator getValidator() {
		return internalFactory.getValidator();
	}

	public ICalculator getCalculator() {
		return internalFactory.getCalculator();
	}

    <if(t.extmp)>
	private static final IExtmpDataTransferFactory dataTransferFactory = ExtmpDataTransferFactoryImpl.defaultsForTarget(getInstance().getMetaObject());

	@Override public Set\<String\> supportedTransferNames() {
		return dataTransferFactory.supportedTransferNames();
	}

	@Override public IExtmpDataTransferService getExtmpDataTransferService(String transferName) {
		return dataTransferFactory.getExtmpDataTransferService(transferName);
	}
	<endif>
}

>>