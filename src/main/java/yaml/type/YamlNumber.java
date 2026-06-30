package yaml.type;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface YamlNumber extends YamlValue {

    BigDecimal bigDecimalValue();
    BigInteger bigIntegerValue();
    BigInteger bigIntegerValueExact();
    double doubleValue();
    int intValue();
    int intValueExact();
    boolean isIntegral();
    long longValue();
    long longValueExact();
}