package yaml.exception;

public class YamlInvalidValueException extends RuntimeException {

    public YamlInvalidValueException(String s) {
        super(s);
    }

    public YamlInvalidValueException(String s, Throwable e) {
        super(s, e);
    }
}
