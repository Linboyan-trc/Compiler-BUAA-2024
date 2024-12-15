package midend.MidCode.Optimize;

import midend.MidCode.Value.Value;

import java.util.LinkedList;

public interface UseUnit {
    LinkedList<Value> getUseUnit();

    void changeToAnotherUnit(Value oldValue, Value newValue);
}
