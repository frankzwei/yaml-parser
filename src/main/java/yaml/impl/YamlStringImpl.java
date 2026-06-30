package yaml.impl;

import yaml.type.YamlString;

public class YamlStringImpl implements YamlString {

    private final String content;

    private YamlStringImpl(String content) {
        this.content = content;
    }

    public static YamlString getYamlString(String content) {
        return new YamlStringImpl(content);
    }

    @Override
    public String toString() {
        return this.getString();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    public String getString() {
        return this.content;
    }
}