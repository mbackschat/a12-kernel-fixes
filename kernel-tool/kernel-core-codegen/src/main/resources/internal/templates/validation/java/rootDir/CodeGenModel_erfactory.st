CodeGenModel_erfactory(t) ::= <<
<t.generatedFileInformation>

package <t.legacyPublicExtmpPackageName>;

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.datatransfer.extmp.ExtmpDataTransferFactoryImpl;
import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.rt.extmp.a12internal.datatransfer.IExtmpDataTransferFactory;
import com.mgmtp.a12.kernel.core.rt.extmp.a12internal.datatransfer.IExtmpDataTransferService;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;

public class Factory_<t.name> implements IGeneratedCodeAccessFactory, IExtmpDataTransferFactory {

	private final <t.publicPackageName>.Factory_<t.name> genCodeAccessor =
		<t.publicPackageName>.Factory_<t.name>.getInstance();

	private final IExtmpDataTransferFactory dataTransferFactory = ExtmpDataTransferFactoryImpl.defaultsForTarget(genCodeAccessor.getMetaObject());

	public Factory_<t.name>() {
	}

	@Override public Set\<String\> supportedTransferNames() {
		return dataTransferFactory.supportedTransferNames();
	}

	@Override public IExtmpDataTransferService getExtmpDataTransferService(String transferName) {
		return dataTransferFactory.getExtmpDataTransferService(transferName);
	}

	public IMetaModel getMetaObject() {
		return genCodeAccessor.getMetaObject();
	}

	public IValidator getValidator() {
		return genCodeAccessor.getValidator();
	}

	public ICalculator getCalculator() {
		return genCodeAccessor.getCalculator();
	}
}

>>