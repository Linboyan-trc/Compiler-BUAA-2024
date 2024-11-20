package backend.MipsCode;

import java.util.LinkedList;
import java.util.Objects;

public class WordMacro implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 全局变量: label + : + .word + 值/需要的字
    private final String label;
    private final int size;
    private final LinkedList<Long> intValues;

    public WordMacro(String label, int size, LinkedList<Long> intValues) {
        this.label = label.substring(0, label.indexOf('@') >= 0 ? label.indexOf('@') : label.length());
        this.size = size;
        this.intValues = intValues;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        if (intValues.isEmpty()) {
            return label + ": .word 0:" + size;
        }
        return label + ": .word " + intValues.stream().map(Objects::toString).
                reduce((sum, item) -> sum + ", " + item).orElse("");
    }
}
