package backend.MipsCode;

import java.util.LinkedList;
import java.util.Objects;

public class WordMacro implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 全局变量: label + : + .word + 值/需要的字
    private String label;
    private int size;
    private LinkedList<Long> intValues;

    public WordMacro(String label, int size, LinkedList<Long> intValues) {
        if(label.indexOf('@') >= 0){
            this.label = label.substring(0, label.indexOf('@'));
        } else {
            this.label = label;
        }
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
