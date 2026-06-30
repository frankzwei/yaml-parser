package yaml.type;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import yaml.exception.YamlInvalidValueException;

public interface YamlObject extends YamlValue {

    default boolean getBoolean(String name) {
        YamlValue value = this.get(name);
        if (value != null && (value.getValueType() == ValueType.TRUE || value.getValueType() == ValueType.FALSE)) {
            return value.getValueType() == ValueType.TRUE ? true : false;
        }

        throw new YamlInvalidValueException("Failed to read in boolean");
    }

    default boolean getBoolean(String name, boolean defaultValue) {
        YamlValue value = this.get(name);
        if (value != null && (value.getValueType() == ValueType.TRUE || value.getValueType() == ValueType.FALSE)) {
            return value.getValueType() == ValueType.TRUE ? true : false;
        }

        return defaultValue;
    }

    default int getInt(String name) {
        YamlNumber value = this.getYamlNumber(name);
        if (value != null) {
            return value.intValue();
        }

        throw new YamlInvalidValueException("Failed to read in integer");
    }

    default int getInt(String name, int defaultValue) {
        YamlNumber value = this.getYamlNumber(name);
        return value == null ? defaultValue : value.intValue();
    }

    default long getLong(String name) {
        YamlNumber value = this.getYamlNumber(name);
        if (value != null) {
            return value.longValue();
        }

        throw new YamlInvalidValueException("Failed to read in long");
    }

    default long getLong(String name, long defaultValue) {
        YamlNumber value = this.getYamlNumber(name);
        return value == null ? defaultValue : value.longValue();
    }

    default YamlArray getYamlArray(String name) {
        return (YamlArray) this.get(name);
    }

    default double getDouble(String name) {
        YamlNumber value = this.getYamlNumber(name);
        if (value != null) {
            return value.doubleValue();
        }

        throw new YamlInvalidValueException("Failed to read in double");
    }

    default double getDouble(String name, long defaultValue) {
        YamlNumber value = this.getYamlNumber(name);
        return value == null ? defaultValue : value.doubleValue();
    }

    default YamlNumber getYamlNumber(String name) {
        return (YamlNumber) this.get(name);
    }

    default YamlObject getYamlObject(String name) {
        return (YamlObject) this.get(name);
    }

    default YamlString getYamlString(String name) {
        return (YamlString) this.get(name);
    }

    default String getString(String name) {
        return this.getString(name, null);
    }

    default String getString(String name, String defaultValue) {
        YamlString value = this.getYamlString(name);
        return value == null ? defaultValue : value.getString();
    }

    int size();
    void clear();
    YamlValue get(String name);
    Map<String, YamlValue> asMap();
    Set<Entry<String, YamlValue>> entrySet();
}