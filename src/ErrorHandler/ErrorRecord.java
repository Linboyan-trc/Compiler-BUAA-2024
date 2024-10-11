package ErrorHandler;

import Lexer.Token;

public class ErrorRecord {
    private int lineNumber;
    private char errorType;

    public ErrorRecord(int lineNumber, char errorType) {
        this.lineNumber = lineNumber;
        this.errorType = errorType;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public char getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return lineNumber + " " + errorType;
    }
}
