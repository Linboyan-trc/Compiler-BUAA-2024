package backend.Address;

public class AbsoluteAddress implements Address {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 表示一个绝对地址，只保留字符串label@1中的label
    private final String label;

    public AbsoluteAddress(String label) {
        this.label = label.substring(0, label.indexOf('@') >= 0 ? label.indexOf('@') : label.length());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return label;
    }
}
