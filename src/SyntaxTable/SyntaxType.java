package SyntaxTable;

public enum SyntaxType {
    ConstChar,ConstInt,ConstCharArray,ConstIntArray,
    Char,Int,CharArray,IntArray,
    VoidFunc,CharFunc,IntFunc;

    public String toString() {
        return name();
    }
}
