package midend.MidCode.MidCode;

import midend.MidCode.MidCodeTable;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.Word;

public class CharGet extends MidCode implements DefUnit {
    public CharGet() { }

    @Override
    public String toString() {
        return "CALL GETCHAR";
    }

    @Override
    public Value getDefUnit() {
        return new Word("?");
    }
}
