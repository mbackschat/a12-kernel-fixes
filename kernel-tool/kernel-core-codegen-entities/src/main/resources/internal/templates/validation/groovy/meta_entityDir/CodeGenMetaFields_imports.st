CodeGenMetaFields_imports(t) ::= <<

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode.RuleType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.CustomConditionException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.CalculationCache.CalculationFieldException;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.CalculationCommand;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.ValidationCommand;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.FormatDefinitionDatumBereich.Jahresinterpretation;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.IFormatDefinition.FeldFormatEnumType;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Identifier_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Identifier_t.ReferenzTyp;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Regel_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MetaFeldUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.MetaIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.MetaLokalePflichtInfo;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.MetaModelImpl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatumTeilbekanntArt;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.ArrayUtils;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.ListBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.LinkedHashMapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.EbenenIterator;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.FilterPredicate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.LongArray;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

>>