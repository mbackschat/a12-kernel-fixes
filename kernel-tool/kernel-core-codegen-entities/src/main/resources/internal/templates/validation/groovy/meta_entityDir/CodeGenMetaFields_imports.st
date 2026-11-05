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

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.CalculationCache.CalculationFieldException;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition.FeldFormatEnumType;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.Identifier_t.ReferenzTyp;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.*;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.util.MetaFeldUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.*;

import com.mgmtp.a12.utils.conversion.InterpretationOfYear;
import com.mgmtp.a12.utils.conversion.a12internal.util.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

>>