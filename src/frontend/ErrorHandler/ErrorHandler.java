package frontend.ErrorHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ErrorHandler {
    private static ErrorHandler errorHandler = new ErrorHandler();
    private List<ErrorRecord> errors = new ArrayList<>();
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

    public void sortErrorsByLineNumber() {
        Collections.sort(errors, Comparator.comparingInt(ErrorRecord::getLineNumber));
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

    public void print(BufferedWriter errorHandlerFile) throws IOException {
        errorHandler.sortErrorsByLineNumber();
        for (ErrorRecord errorRecord : errorHandler.getErrors()) {
            errorHandlerFile.write(errorRecord + "\n");
        }
        errorHandlerFile.close();
    }
}
