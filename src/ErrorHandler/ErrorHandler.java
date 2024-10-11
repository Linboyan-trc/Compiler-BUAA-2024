package ErrorHandler;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static ErrorHandler errorHandler = new ErrorHandler();
    private List<ErrorRecord> errors = new ArrayList<>();

    private ErrorHandler() {}

    public static ErrorHandler getInstance() {
        return errorHandler;
    }

    public void addError(ErrorRecord error) {
        errors.add(error);
    }

    public List<ErrorRecord> getErrors() {
        return errors;
    }
}
