package ErrorHandler;

import java.util.LinkedList;
import java.util.List;

public class ErrorHandler {
    private static ErrorHandler errorHandler = new ErrorHandler();
    private List<ErrorRecord> errors = new LinkedList<>();
    private boolean inUse = true;

    private ErrorHandler() {}

    public static ErrorHandler getInstance() {
        return errorHandler;
    }

    public void addError(ErrorRecord error) {
        if (inUse) {
            errors.add(error);
        }
    }

    public List<ErrorRecord> getErrors() {
        return errors;
    }

    public void turnOff() {
        inUse = false;
    }

    public void turnOn() {
        inUse = true;
    }
}
