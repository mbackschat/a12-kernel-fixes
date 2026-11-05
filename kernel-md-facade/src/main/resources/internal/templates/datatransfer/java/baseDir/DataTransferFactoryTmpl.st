DataTransferFactoryTmpl(t) ::= <<
package <t.packageName>;

import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.datatransfer.mappingmodel.PrecompModelDataMapper;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.datatransfer.DataTransferServiceImpl;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.IDataTransferService;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.execution.StructuralMapperVK;

public class <t.factoryName> implements DataTransferServiceImpl.IDataTransferServiceParams {
    private static final IDataTransferService TRANSFER_SERVICE_INSTANCE = new DataTransferServiceImpl(new <t.factoryName>());
    /**
     * A IDataTransferService for this data transfer.
     */
    public static IDataTransferService getInstance() {
        return TRANSFER_SERVICE_INSTANCE;
    }

    private static final Map\<String, IMetaDataValidierung> metaObjects = Map.ofEntries(
            <t.metaValidierungMapEntries>
    );

    private static final Map\<String, IValidator> validators = Map.ofEntries(
            <t.validatorMapEntries>
    );

    private static final Set\<String> noSourceValidationSources = Set.of(
            <t.noSourceValidationSources>
    );

    @Override public ICalculator precompCalculator() {
        return <t.precompCalculatorFqn>.getInstance();
    }

    @Override public IMetaDataValidierung precompMetaObject() {
        return <t.precompMetaFqn>.getInstance().getMetaDataValidierung();
    }

    @Override public StructuralMapperVK getStructuralMapper() {
        return <t.structural_mapper_Fqn>.getStructuralMapper();
    }

    @Override public PrecompModelDataMapper getPrecompModelDataMapper() {
        // NOTE: we must currently return a fresh instance each time,
        // because the map-back state is kept directly in the mapper
        // and could get inconsistent if instances are reused
        // see javadoc of this method in the interface
        return PrecompModelDataMapper.loadFromClasspath(
            "<t.serializedPrecompModelDataMapperResourcePath>",
            this.getClass().getClassLoader());
    }

    @Override public Set\<String> supportedSourceNames() {
        return validators.keySet();
    }

    @Override public IMetaDataValidierung getMetaObjectForSource(String sourceName) {
        return metaObjects.get(sourceName);
    }

    @Override public IValidator getValidatorForSource(String sourceName) {
        return validators.get(sourceName);
    }

    @Override public boolean isNoSourceValidation(String sourceName) {
        return noSourceValidationSources.contains(sourceName);
    }
}>>