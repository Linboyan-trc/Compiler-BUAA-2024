package midend.MidCode.Value;

import midend.MidCode.MidCode.*;
import midend.MidCode.Optimize.*;

import java.util.LinkedList;

public abstract class Value {
    //////////////////////////////////////////////////////////////////////
    public static int tempCnt = 0;

    public abstract String getName();

    public abstract Void truncTo8();

    //////////////////////////////////////////////////////////////////////
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            return toString().equals(obj.toString());
        } else {
            return false;
        }
    }

    @Override public int hashCode()
    {
        return toString().hashCode();
    }

    //////////////////////////////////////////////////////////////////////
    public boolean isGlobal() {
        return this.toString().endsWith("@0");
    }

    public boolean isReturn() {
        return this.toString().equals("$?");
    }

    public boolean isRunTimeInvariant(MidCode defMidCode, MidCode midCode) {
        if (defMidCode instanceof Load) {
            return false;
        }

        LinkedList<Value> useUnits = new LinkedList<>();
        if (defMidCode instanceof UseUnit) {
            useUnits.addAll(((UseUnit) defMidCode).getUseUnit());
        }

        MidCode tempCode = defMidCode.getNext();
        while (tempCode != midCode) {
            if (tempCode instanceof DefUnit && useUnits.contains(((DefUnit) tempCode).getDefUnit())) {
                return false;
            }
            tempCode = tempCode.getNext();
        }
        return true;
    }
}
