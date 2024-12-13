package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;

public class Print extends MidCode {
    private final String fmtString;

    public Print(String fmtString) {
        this.fmtString = fmtString;
    }

    public String getFmtString() {
        return fmtString;
    }

    @Override
    public String toString() {
        return "PRINT " + fmtString;
    }
}
