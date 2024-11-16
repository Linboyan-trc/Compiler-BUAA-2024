package SyntaxTable;

public enum SyntaxType {
    ConstChar,ConstInt,ConstCharArray,ConstIntArray,
    Char,Int,CharArray,IntArray,
    VoidFunc,CharFunc,IntFunc,
    Bool, // <Cond>
    Void; // VoidFunc

    public boolean isArray() {
        return this == ConstCharArray || this == ConstIntArray || this == CharArray || this == IntArray;
    }

    public boolean isVariable() {
        return this == ConstChar || this == ConstInt || this == Char || this == Int;
    }

    public boolean isIntArray() {
        return this == ConstIntArray || this == IntArray;
    }

    public boolean isCharArray() {
        return this == ConstCharArray || this == CharArray;
    }

    public SyntaxType toArray() {
        switch (this) {
            case ConstChar:
                return ConstCharArray;
            case ConstInt:
                return ConstIntArray;
            case Char:
                return CharArray;
            case Int:
                return IntArray;
            default:
                return this;
        }
    }

    public String toString() {
        return name();
    }
}
