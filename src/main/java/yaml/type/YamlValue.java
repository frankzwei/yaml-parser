package yaml.type;

import java.util.Objects;

public interface YamlValue {

    YamlValue FALSE = new YamlValue() {

        @Override
        public ValueType getValueType() {
            return ValueType.FALSE;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof YamlValue)) {
                return false;
            }

            YamlValue other = (YamlValue) obj;
            return this.getValueType() == other.getValueType();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getValueType());
        }
        
        @Override
        public String toString() {
            return "false";
        }
    };

    YamlValue TRUE = new YamlValue() {

        @Override
        public ValueType getValueType() {
            return ValueType.TRUE;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof YamlValue)) {
                return false;
            }

            YamlValue other = (YamlValue) obj;
            return this.getValueType() == other.getValueType();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getValueType());
        }
        
        @Override
        public String toString() {
            return "true";
        }
    };

    ValueType getValueType();

    default boolean isNull() {
        return false;
    }
    
    enum ValueType {
        OBJECT,
        ARRAY,
        NUMBER,
        STRING,
        TRUE,
        FALSE
    }
}