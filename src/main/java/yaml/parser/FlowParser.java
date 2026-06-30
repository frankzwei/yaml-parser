package yaml.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yaml.exception.YamlParseException;
import yaml.impl.YamlArrayImpl;
import yaml.impl.YamlNumberImpl;
import yaml.impl.YamlObjectImpl;
import yaml.impl.YamlStringImpl;
import yaml.type.YamlArray;
import yaml.type.YamlObject;
import yaml.type.YamlValue;

public class FlowParser {
    
    private String content;
    private int lineNumber;
    private int pos = 0;
    private int depth = 0;

    public FlowParser(String content, int lineNumber) {
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public void reset(String content, int lineNumber) {
        this.content = content;
        this.lineNumber = lineNumber;
        this.pos = 0;
        this.depth = 0;
    }

    public YamlValue parse() {
        this.skipWhitespaces();
        YamlValue value = this.parseValue();
        this.skipWhitespaces();
        if (!this.isAtEnd()) {
            throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Unexpected trailing characters");
        }

        return value;
    }

    private YamlValue parseValue() {
        this.skipWhitespaces();
        if (this.isAtEnd()) {
            return null;
        }

        char c = this.getChar();
        if (c == '{') {
            return this.parseObject();
        }

        if (c == '[') {
            return this.parseArray();
        }

        if (c == '"' || c == '\'') {
            return YamlStringImpl.getYamlString(this.parseQuotedString());
        }

        return this.parseBareScalar();
    }

    private YamlObject parseObject() {
        this.expect('{');
        this.depth++;

        this.skipWhitespaces();
        if (this.consume('}')) {
            this.depth--;
            return YamlObjectImpl.getYamlObject();
        }

        Map<String, YamlValue> values = new HashMap<>();
        while (!this.isAtEnd()) {
            this.skipWhitespaces();

            String key;
            if (this.getChar() == '"' || this.getChar() == '\'') {
                key = this.parseQuotedString();
            } else {
                key = this.parseBareKey();
            }

            this.skipWhitespaces();
            this.expect(':');

            YamlValue value = this.parseValue();
            values.put(key, value);

            this.skipWhitespaces();
            if (this.consume(',')) {
                continue;
            }

            this.expect('}');
            this.depth--;
            break;
        }

        return YamlObjectImpl.getYamlObject(values);
    }

    private YamlArray parseArray() {
        this.expect('[');
        this.depth++;

        this.skipWhitespaces();
        if (this.consume(']')) {
            this.depth--;
            return YamlArrayImpl.getYamlArray();
        }

        List<YamlValue> array = new ArrayList<>();
        while (!this.isAtEnd()) {
            array.add(this.parseValue());
            this.skipWhitespaces();
            if (this.consume(',')) {
                this.skipWhitespaces();
                if (this.consume(']')) {
                    this.depth--;
                    break;
                }

                continue;
            }

            this.expect(']');
            this.depth--;
            break;
        }

        return YamlArrayImpl.getYamlArray(array);
    }

    private String parseBareKey() {
        int start = this.pos;
        while (!this.isAtEnd() && this.getChar() != ':') {
            this.pos++;
        }

        String key = this.content.substring(start, this.pos).trim();
        if (key.isEmpty()) {
            throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Empty keys are unsupported");
        }

        return key;
    }

    private YamlValue parseBareScalar() {
        int start = this.pos;
        while (!this.isAtEnd()) {
            char c = this.getChar();
            if (this.depth > 0 && (c == ',' || c == ']' || c == '}')) {
                break;
            }

            this.pos++;
        }

        String raw = this.content.substring(start, this.pos).trim();
        if (raw.isEmpty()) {
            return null;
        }

        return this.scalarFrom(raw);
    }

    private YamlValue scalarFrom(String raw) {
        if (raw.equalsIgnoreCase("null") || raw.equalsIgnoreCase("~")) {
            return null;
        }

        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("on") || raw.equalsIgnoreCase("yes")) {
            return YamlValue.TRUE;
        }

        if (raw.equalsIgnoreCase("false") || raw.equalsIgnoreCase("off") || raw.equalsIgnoreCase("no")) {
            return YamlValue.FALSE;
        }

        if (raw.matches("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?")) {
            try {
                return YamlNumberImpl.getYamlNumber(new BigDecimal(raw));
            } catch (NumberFormatException ignored) {}
        }

        return YamlStringImpl.getYamlString(raw);
    }

    private String parseQuotedString() {
        StringBuilder builder = new StringBuilder();

        char quote = this.next();
        while (!this.isAtEnd()) {
            char c = this.next();
            if (quote == '\'') {
                if (c == '\'' && !this.isAtEnd() && this.getChar() == '\'') {
                    this.next();
                    builder.append('\'');
                    continue;
                }

                if (c == '\'') {
                    return builder.toString();
                }

                builder.append(c);
                continue;
            }

            if (c == '"') {
                return builder.toString();
            }

            if (c == '\\') {
                if (this.isAtEnd()) {
                    throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Unfinished escape sequence");
                }

                char escaped = this.next();
                switch (escaped) {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '/':
                        builder.append('/');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'u':
                        builder.append(this.parseUnicodeEscape());
                        break;
                    default:
                        throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Unrecognized escape sequence: \\" + escaped);
                }
            } else {
                builder.append(c);
            }
        }

        throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Unterminated quoted string");
    }

    private char parseUnicodeEscape() {
        if (this.pos + 4 > this.content.length()) {
            throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Invalid unicode escape sequence");
        }

        String hex = this.content.substring(this.pos, this.pos + 4);
        this.pos += 4;

        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Invalid unicode escape sequence: \\u" + hex);
        }
    }

    private void skipWhitespaces() {
        while (!this.isAtEnd() && Character.isWhitespace(this.getChar())) {
            this.pos++;
        }
    }

    private void expect(char expected) {
        if (this.consume(expected)) {
            return;
        }

        String actual = this.isAtEnd() ? "end of input" : "'" + this.getChar() + "'";
        throw new YamlParseException("Line " + this.lineNumber + ", column " + (this.pos + 1) + ": Expected '" + expected + "', but got " + actual + " instead");
    }

    private boolean consume(char expected) {
        if (!this.isAtEnd() && this.getChar() == expected) {
            this.pos++;
            return true;
        }

        return false;
    }

    private char getChar() {
        return this.content.charAt(this.pos);
    }

    private char next() {
        return this.content.charAt(this.pos++);
    }

    private boolean isAtEnd() {
        return this.pos >= this.content.length();
    }
}
