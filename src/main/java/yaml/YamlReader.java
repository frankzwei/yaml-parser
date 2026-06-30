package yaml;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import yaml.exception.YamlParseException;
import yaml.parser.YamlParser;
import yaml.parser.YamlParser.YamlLine;
import yaml.type.YamlArray;
import yaml.type.YamlNumber;
import yaml.type.YamlObject;
import yaml.type.YamlString;
import yaml.type.YamlValue;

public class YamlReader implements Closeable {

    private final Reader reader;
    private boolean consumed = false;

    private YamlReader(Reader reader) {
        this.reader = reader;
    }

    public static YamlReader createReader(InputStream in) {
        return YamlReader.createReader(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
    }

    public static YamlReader createReader(Reader reader) {
        return new YamlReader(reader);
    }

    public YamlString readString() {
        return (YamlString) this.read();
    }

    public YamlNumber readNumber() {
        return (YamlNumber) this.read();
    }

    public YamlArray readArray() {
        return (YamlArray) this.read();
    }

    public YamlObject readObject() {
        return (YamlObject) this.read();
    }

    public YamlValue read() {
        if (this.consumed) {
            throw new IllegalStateException("Attempted to read an already consumed parser");
        }

        this.consumed = true;
        try {
            List<YamlLine> lines = YamlReader.loadLines(this.reader);
            if (lines.isEmpty()) {
                return null;
            }

            YamlParser parser = new YamlParser(lines);
            return parser.parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<YamlLine> loadLines(Reader reader) throws IOException {
        List<YamlLine> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = YamlReader.stripComments(line);
                if (line.trim().isEmpty()) {
                    continue;
                }

                int indent = YamlReader.countIndents(line, lineNumber);
                String content = line.substring(indent).stripTrailing();
                if (content.equals("---")) {
                    continue;
                }

                if (content.equals("...")) {
                    break;
                }

                lines.add(new YamlLine(content, indent, lineNumber));
            }
        }

        return lines;
    }

    private static int countIndents(String content, int lineNumber) {
        int i = 0;
        for (; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\t') {
                throw new YamlParseException("Line " + lineNumber + ": Encountered tab used for indentation");
            }

            if (c != ' ') {
                break;
            }

        }

        return i;
    }

    private static String stripComments(String content) {
        boolean inSingle = false;
        boolean inDouble = false;
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

            if (c == '#' && !inSingle && !inDouble) {
                if (i == 0 || Character.isWhitespace(content.charAt(i - 1))) {
                    return content.substring(0, i).stripTrailing();
                }
            }
        }

        return content.stripTrailing();
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}