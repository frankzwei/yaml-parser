package yaml.exception;

public class YamlParseException extends RuntimeException {

    public YamlParseException(String s) {
        super(s);
    }

    public YamlParseException(String s, Throwable e) {
        super(s, e);
    }
}
