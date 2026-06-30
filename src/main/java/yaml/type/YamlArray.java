package yaml.type;

import java.util.List;

import yaml.exception.YamlInvalidValueException;

public interface YamlArray extends YamlValue, Iterable<YamlValue> {

    default boolean getBoolean(int index) {
        YamlValue value = this.get(index);
        if (value != null && (value.getValueType() == ValueType.TRUE || value.getValueType() == ValueType.FALSE)) {
            return value.getValueType() == ValueType.TRUE ? true : false;
        }

        throw new YamlInvalidValueException("Failed to read in boolean");
    }

    default boolean getBoolean(int index, boolean defaultValue) {
        YamlValue value = this.get(index);
        if (value != null && (value.getValueType() == ValueType.TRUE || value.getValueType() == ValueType.FALSE)) {
            return value.getValueType() == ValueType.TRUE ? true : false;
        }

        return defaultValue;
    }

    default int getInt(int index) {
        YamlNumber value = this.getYamlNumber(index);
        if (value != null) {
            return value.intValue();
        }

        throw new YamlInvalidValueException("Failed to read in integer");
    }

    default int getInt(int index, int defaultValue) {
        YamlNumber value = this.getYamlNumber(index);
        return value == null ? defaultValue : value.intValue();
    }

    default long getLong(int index) {
        YamlNumber value = this.getYamlNumber(index);
        if (value != null) {
            return value.longValue();
        }

        throw new YamlInvalidValueException("Failed to read in long");
    }

    default long getLong(int index, long defaultValue) {
        YamlNumber value = this.getYamlNumber(index);
        return value == null ? defaultValue : value.longValue();
    }

    default double getDouble(int index) {
        YamlNumber value = this.getYamlNumber(index);
        if (value != null) {
            return value.doubleValue();
        }

        throw new YamlInvalidValueException("Failed to read in double");
    }

    default double getDouble(int index, double defaultValue) {
        YamlNumber value = this.getYamlNumber(index);
        return value == null ? defaultValue : value.doubleValue();
    }

    default YamlObject getYamlObject(int index) {
        return (YamlObject) this.get(index);
    }

    default YamlArray getYamlArray(int index) {
        return (YamlArray) this.get(index);
    }

    default YamlNumber getYamlNumber(int index) {
        return (YamlNumber) this.get(index);
    }

    default YamlString getYamlString(int index) {
        return (YamlString) this.get(index);
    }

    default String getString(int index) {
        return this.getString(index, null);
    }

    default String getString(int index, String defaultValue) {
        YamlString value = this.getYamlString(index);
        return value == null ? defaultValue : value.getString();
    }

    int size();
    YamlValue get(int index);
    <T extends YamlValue> List<T> getValuesAs(Class<T> clazz);
    List<YamlValue> getValues();
}