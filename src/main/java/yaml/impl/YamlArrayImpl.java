package yaml.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import yaml.type.YamlArray;
import yaml.type.YamlValue;

public class YamlArrayImpl implements YamlArray {

    private final List<YamlValue> values;

    private YamlArrayImpl() {
        this.values = new ArrayList<>();
    }

    private YamlArrayImpl(List<YamlValue> values) {
        this.values = values;
    }

    public static YamlArray getYamlArray() {
        return new YamlArrayImpl();
    }

    public static YamlArray getYamlArray(List<YamlValue> values) {
        return new YamlArrayImpl(values);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public Iterator<YamlValue> iterator() {
        return this.values.iterator();
    }

    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public YamlValue get(int index) {
        return this.values.get(index);
    }

    @Override
    public List<YamlValue> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    @Override
    public <T extends YamlValue> List<T> getValuesAs(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (YamlValue value: this.values) {
            if (!clazz.isInstance(value)) {
                throw new IllegalStateException("Failed to cast " + value.getClass().getSimpleName() + " into " + clazz.getSimpleName());
            }

            list.add(clazz.cast(value));
        }

        return List.copyOf(list);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof YamlArrayImpl)) {
            return false;
        }

        YamlArrayImpl other = (YamlArrayImpl) obj;
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
}