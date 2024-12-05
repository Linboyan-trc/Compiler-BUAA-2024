package backend.Address;

public class AbsoluteAddress implements Address {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 表示一个绝对地址，只保留字符串label@1中的label
    private String label;

    public AbsoluteAddress(String label) {
        // 1. 传入的是变量名: $a1@0, &a2@0
        // 1. 剔除'@0'
        int atIndex = label.indexOf('@'); // 找到 '@' 的索引
        if (atIndex >= 0) {
            this.label = label.substring(0, atIndex); // 如果有 '@'，截取到 '@' 之前
        } else {
            this.label = label; // 如果没有 '@'，直接使用整个字符串
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return label;
    }
}
