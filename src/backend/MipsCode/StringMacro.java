package backend.MipsCode;

public class StringMacro implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 表示一个字符串的全局变量: label + : + .asciiz + "..."
    private String label;
    private String stringValue;

    public StringMacro(String label, String stringValue) {
        this.label = label;
        this.stringValue = stringValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return label + ": .asciiz \"" + stringValue + "\"";
    }
}
