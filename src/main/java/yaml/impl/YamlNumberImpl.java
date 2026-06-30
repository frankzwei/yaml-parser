package yaml.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import yaml.type.YamlNumber;

public abstract class YamlNumberImpl implements YamlNumber {

    public static YamlNumber getYamlNumber(int num) {
        return new YamlNumberInt(num);
    }

    public static YamlNumber getYamlNumber(long num) {
        return new YamlNumberLong(num);
    }

    public static YamlNumber getYamlNumber(BigDecimal num) {
        return new YamlNumberBigDecimal(num);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof YamlNumberImpl)) {
            return false;
        }

        YamlNumberImpl other = (YamlNumberImpl) obj;
        return this.bigDecimalValue().equals(other.bigDecimalValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bigDecimalValue());
    }

    @Override
    public String toString() {
        return this.bigDecimalValue().toString();
    }

    public BigInteger bigIntegerValue() {
        return this.bigDecimalValue().toBigInteger();
    }

    public BigInteger bigIntegerValueExact() {
        return this.bigDecimalValue().toBigIntegerExact();
    }

    public double doubleValue() {
        return this.bigDecimalValue().doubleValue();
    }

    public int intValue() {
        return this.bigDecimalValue().intValue();
    }

    public int intValueExact() {
        return this.bigDecimalValue().intValueExact();
    }

    public boolean isIntegral() {
        return this.bigDecimalValue().scale() == 0;
    }

    public long longValue() {
        return this.bigDecimalValue().longValue();
    }

    public long longValueExact() {
        return this.bigDecimalValue().longValueExact();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }

    private static class YamlNumberInt extends YamlNumberImpl {

        private final int num;
        private BigDecimal decimal;

        public YamlNumberInt(int num) {
            this.num = num;
        }

        @Override
        public boolean isIntegral() {
            return true;
        }

        @Override
        public double doubleValue() {
            return this.num;
        }

        @Override
        public int intValue() {
            return this.num;
        }

        @Override
        public int intValueExact() {
            return this.num;
        }

        @Override
        public long longValue() {
            return this.num;
        }

        @Override
        public long longValueExact() {
            return this.num;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.num, this.decimal);
        }

        @Override
        public BigDecimal bigDecimalValue() {
            if (this.decimal == null) {
                this.decimal = new BigDecimal(num);
            }

            return this.decimal;
        }
    }

    private static class YamlNumberLong extends YamlNumberImpl {

        private final long num;
        private BigDecimal decimal;

        public YamlNumberLong(long num) {
            this.num = num;
        }

        @Override
        public boolean isIntegral() {
            return true;
        }

        @Override
        public double doubleValue() {
            return this.num;
        }

        @Override
        public long longValue() {
            return this.num;
        }

        @Override
        public long longValueExact() {
            return this.num;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.num, this.decimal);
        }

        @Override
        public BigDecimal bigDecimalValue() {
            if (this.decimal == null) {
                this.decimal = new BigDecimal(num);
            }

            return this.decimal;
        }
    }

    private static class YamlNumberBigDecimal extends YamlNumberImpl {

        private final BigDecimal decimal;

        public YamlNumberBigDecimal(BigDecimal decimal) {
            this.decimal = decimal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.decimal);
        }

        @Override
        public BigDecimal bigDecimalValue() {
            return this.decimal;
        }
    }
}