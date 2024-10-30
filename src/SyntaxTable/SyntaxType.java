package SyntaxTable;

public enum SyntaxType {
    ConstChar,ConstInt,ConstCharArray,ConstIntArray,
    Char,Int,CharArray,IntArray,
    VoidFunc,CharFunc,IntFunc,
    Bool, // <Cond>
    Void; // VoidFunc

    public String toString() {
        return name();
    }
}
