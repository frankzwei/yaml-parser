package yaml.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import yaml.exception.YamlParseException;
import yaml.impl.YamlArrayImpl;
import yaml.impl.YamlObjectImpl;
import yaml.impl.YamlStringImpl;
import yaml.type.YamlArray;
import yaml.type.YamlObject;
import yaml.type.YamlString;
import yaml.type.YamlValue;

public class YamlParser {
    
    private final List<YamlLine> lines;
    private FlowParser parser;
    private int pos = 0;

    public YamlParser(List<YamlLine> lines) {
        this.lines = lines;
    }

    public YamlValue parse() {
        YamlValue value = this.parseBlock(this.lines.get(this.pos).getIndents());
        if (this.pos < this.lines.size()) {
            YamlLine line = this.lines.get(this.pos);
            throw new YamlParseException("Line " + line.getLineNumber() + ": Unexpected trailing content");
        }

        return value;
    }

    private YamlValue parseBlock(int indent) {
        if (this.pos >= this.lines.size()) {
            return null;
        }

        YamlLine line = this.lines.get(this.pos);
        if (line.getIndents() < indent) {
            return null;
        }

        if (line.getIndents() > indent) {
            throw new YamlParseException("Line " + line.getLineNumber() + ": Unexpected indentation");
        }

        if (this.isArrayLike(line.getContent())) {
            return this.parseArray(indent);
        }

        if (YamlParser.findMappingColon(line.getContent()) >= 0) {
            return this.parseObject(indent);
        }

        this.pos++;
        return this.parseInline(line.getContent(), line.getLineNumber());
    }

    private YamlObject parseObject(int indent) {
        Map<String, YamlValue> values = new HashMap<>();
        while (this.pos < this.lines.size()) {
            YamlLine line = this.lines.get(this.pos);
            if (line.getIndents() < indent) {
                break;
            }

            if (line.getIndents() > indent) {
                throw new YamlParseException("Line " + line.getLineNumber() + ": Unexpected indentation in object");
            }

            if (this.isArrayLike(line.getContent())) {
                break;
            }

            this.pos++;
            this.addMappingPair(values, line.getContent(), indent, line.getLineNumber());
        }

        return YamlObjectImpl.getYamlObject(values);
    }

    private YamlArray parseArray(int indent) {
        List<YamlValue> array = new ArrayList<>();
        while (this.pos < this.lines.size()) {
            YamlLine line = this.lines.get(this.pos);
            if (line.getIndents() != indent || !this.isArrayLike(line.getContent())) {
                break;
            }

            String content = line.getContent().equals("-") ? "" : line.getContent().substring(2).trim();
            this.pos++;

            YamlValue item;
            if (content.isEmpty()) {
                item = this.parseNestedOrNull(indent);
            } else {
                int colon = YamlParser.findMappingColon(content);
                if (colon >= 0) {
                    Map<String, YamlValue> values = new HashMap<>();
                    this.addMappingPair(values, content, indent, line.getLineNumber());
                    while (this.pos < this.lines.size() && this.lines.get(this.pos).getIndents() > indent) {
                        YamlLine nextLine = this.lines.get(this.pos);
                        if (this.isArrayLike(nextLine.getContent())) {
                            throw new YamlParseException("Line " + nextLine.getLineNumber() + ": Expected object continuation but found array item");
                        }

                        YamlValue continued = this.parseObject(nextLine.getIndents());
                        if (!(continued instanceof YamlObject)) {
                            throw new YamlParseException("Line " + nextLine.getLineNumber() + ": Expected object continuation");
                        }

                        YamlObject continuedObject = (YamlObject) continued;
                        values.putAll(continuedObject.asMap());
                    }

                    item = YamlObjectImpl.getYamlObject(values);
                } else {
                    item = this.parseInline(content, line.getLineNumber());
                    if (this.pos < this.lines.size() && this.lines.get(this.pos).getIndents() > indent) {
                        throw new YamlParseException("Line " + this.lines.get(this.pos).getLineNumber() + ": Unexpected indented block after scalar array item");
                    }
                }
            }

            array.add(item);
        }

        return YamlArrayImpl.getYamlArray(array);
    }

    private void addMappingPair(Map<String, YamlValue> objectMap, String content, int parentIndent, int lineNumber) {
        int colon = YamlParser.findMappingColon(content);
        if (colon < 0) {
            throw new YamlParseException("Line " + lineNumber + ": Encountered unexpected mapping entry");
        }

        String rawKey = content.substring(0, colon).trim();
        String rawValue = content.substring(colon + 1).trim();
        if (rawKey.isEmpty()) {
            throw new YamlParseException("Line " + lineNumber + ": Encountered empty key");
        }

        String key = this.parseKey(rawKey, lineNumber);
        YamlValue value;
        if (rawValue.isEmpty()) {
            value = this.parseNestedOrNull(parentIndent);
        } else if (rawValue.equals("|")) {
            value = this.parseBlockScalar(false, parentIndent);
        } else if (rawValue.equals(">")) {
            value = this.parseBlockScalar(true, parentIndent);
        } else {
            value = this.parseInline(rawValue, lineNumber);
        }

        objectMap.put(key, value);
    }

    private String parseKey(String rawKey, int lineNumber) {
        rawKey = rawKey.trim();
        if (rawKey.startsWith("\"") || rawKey.startsWith("'")) {
            YamlValue value = this.parseInline(rawKey, lineNumber);
            if (!(value instanceof YamlString)) {
                throw new YamlParseException("Line " + lineNumber + ", column " + (this.pos + 1) + ": Unable to parse quoted key into a String");
            }

            return ((YamlString) value).getString();
        }

        return rawKey;
    }

    private YamlValue parseBlockScalar(boolean folded, int parentIndent) {
        if (this.pos >= this.lines.size() || this.lines.get(this.pos).getIndents() <= parentIndent) {
            return YamlStringImpl.getYamlString("");
        }

        int blockIndent = this.lines.get(this.pos).getIndents();
        StringBuilder builder = new StringBuilder();
        while (this.pos < this.lines.size() && this.lines.get(this.pos).getIndents() >= blockIndent) {
            YamlLine line = this.lines.get(this.pos++);
            String content = " ".repeat(Math.max(0, line.getIndents() - blockIndent)) + line.getContent();
            if (folded) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }

                builder.append(content.trim());
            } else {
                builder.append(content).append('\n');
            }
        }

        return YamlStringImpl.getYamlString(builder.toString());
    }

    private YamlValue parseNestedOrNull(int parentIndent) {
        if (this.pos < this.lines.size() && this.lines.get(this.pos).getIndents() > parentIndent) {
            return this.parseBlock(this.lines.get(this.pos).getIndents());
        }

        return null;
    }

    private YamlValue parseInline(String value, int lineNumber) {
        if (this.parser == null) {
            this.parser = new FlowParser(value, lineNumber);
        } else {
            this.parser.reset(value, lineNumber);
        }

        return this.parser.parse();
    }

    private boolean isArrayLike(String content) {
        return content.startsWith("- ") || content.equals("-");
    }

    private static int findMappingColon(String content) {
        boolean inSingle = false;
        boolean inDouble = false;
        int depth = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (inDouble && c == '\\') {
                i++;
                continue;
            }

            if (!inDouble && c == '\'') {
                if (inSingle && i + 1 < content.length() && content.charAt(i + 1) == '\'') {
                    i++;
                    continue;
                }

                inSingle = !inSingle;
                continue;
            }

            if (!inSingle && c == '"') {
                inDouble = !inDouble;
                continue;
            }

            if (inSingle || inDouble) {
                continue;
            }

            if (c == '[' || c == '{') {
                depth++;
                continue;
            }

            if (c == ']' || c == '}') {
                depth--;
                continue;
            }

            if (c == ':' && depth == 0) {
                if (i + 1 == content.length() || Character.isWhitespace(content.charAt(i + 1))) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static class YamlLine {

        private final String content;
        private final int indents;
        private final int lineNumber;

        public YamlLine(String content, int indents, int lineNumber) {
            this.content = content;
            this.indents = indents;
            this.lineNumber = lineNumber;
        }

        public String getContent() {
            return this.content;
        }

        public int getIndents() {
            return this.indents;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof YamlLine)) {
                return false;
            }

            YamlLine other = (YamlLine) obj;
            return this.content.equals(other.content) && this.indents == other.indents && this.lineNumber == other.lineNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.content, this.indents, this.lineNumber);
        }

        @Override
        public String toString() {
            return "{Line=[" + this.content + ", Line Number=" + this.lineNumber + ", Indents=" + this.indents + "]}";
        }
    }
}