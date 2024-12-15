package midend.MidCode.Optimize;

import midend.MidCode.Value.Value;

import java.util.LinkedList;

public interface Copy {
    LinkedList<Value> getSource();

    Value getTarget();
}
