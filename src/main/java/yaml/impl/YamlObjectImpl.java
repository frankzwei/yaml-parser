package yaml.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import yaml.type.YamlObject;
import yaml.type.YamlValue;

public class YamlObjectImpl implements YamlObject {

    private final Map<String, YamlValue> values;

    private YamlObjectImpl() {
        this.values = new HashMap<>();
    }

    private YamlObjectImpl(Map<String, YamlValue> values) {
        this.values = values;
    }

    public static YamlObject getYamlObject() {
        return new YamlObjectImpl();
    }

    public static YamlObject getYamlObject(Map<String, YamlValue> values) {
        return new YamlObjectImpl(values);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public void clear() {
        this.values.clear();
    }

    @Override
    public YamlValue get(String name) {
        return this.values.getOrDefault(name, null);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof YamlObjectImpl)) {
            return false;
        }

        YamlObjectImpl other = (YamlObjectImpl) obj;
        return this.values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.values);
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

    @Override
    public Set<Entry<String, YamlValue>> entrySet() {
        return this.values.entrySet();
    }

    @Override
    public Map<String, YamlValue> asMap() {
        return Collections.unmodifiableMap(this.values);
    }
}