DataTransferFactoryTmpl(t) ::= <<

public class <t.groovyFactoryName> implements DataTransferServiceImpl.IDataTransferServiceParams {
    private static final IDataTransferService TRANSFER_SERVICE_INSTANCE = new DataTransferServiceImpl(new <t.groovyFactoryName>());
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
        return <t.precompCalculatorFqn_Groovy>.getInstance();
    }

    @Override public IMetaDataValidierung precompMetaObject() {
        return <t.precompMetaFqn_Groovy>.getInstance().getMetaDataValidierung();
    }

    @Override public StructuralMapperVK getStructuralMapper() {
        return <t.structural_mapper_Fqn_Groovy>.getStructuralMapper();
    }

    @Override public PrecompModelDataMapper getPrecompModelDataMapper() {
        // NOTE: we must currently return a fresh instance each time,
        // because the map-back state is kept directly in the mapper
        // and could get inconsistent if instances are reused
        // see javadoc of this method in the interface
        return PrecompModelDataMapper.deserialize(<t.serializedPrecompModelClassGroovy>.serializedModel);
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