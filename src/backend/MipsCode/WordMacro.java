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
        // 1. String label是变量名: $a1@0, &a2@0
        // 1. 剔除'@0'
        // 1. 保留$a1, &a2作为WordMacro:label
        if(label.indexOf('@') >= 0){
            this.label = label.substring(0, label.indexOf('@'));
        } else {
            this.label = label;
        }

        // 2. 就是这个变量需要多少个字
        this.size = size;

        // 3. 每个字的初始int值
        this.intValues = intValues;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        // 1. 对于没有初始值的遍历，直接按照尺寸声明
        if (intValues.isEmpty()) {
            return label + ": .word 0:" + size;
        }
        // 2. 对于有初始值，需要指定初始值
        // 2. 对于不足的位置，要补0
        StringBuilder intValuesString = new StringBuilder();
        int i = 0;
        for (; i < intValues.size(); i++) {
            intValuesString.append(intValues.get(i)); // 添加当前值
            if (i < size - 1) { // 如果不是最后一个元素，添加逗号和空格
                intValuesString.append(", ");
            }
        }
        if(i < size){
            // 3. 对于不足的位置，补0
            for(;i < size;i++){
                intValuesString.append("0");
                if(i < size - 1){
                    intValuesString.append(", ");
                }
            }
        }
        return label + ": .word " + intValuesString.toString();
    }
}
