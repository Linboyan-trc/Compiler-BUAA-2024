package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;

public class CharacterNode implements ExpNode {
    // 1. num
    private String word;
    private long value;

    // 2. 直接构造法 + 赋值转换构造
    public CharacterNode(String word) {
        this.word = word;
        this.value = (long) word.charAt(0);
    }

    public CharacterNode(long value) {
        this.value = value & 0xFF;
        char character = (char) this.value;
        this.word = String.valueOf(character);
    }

    public long getValue() {
        return value;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        return SyntaxType.Char;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public CharacterNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        return new Imm(value);
    }
}
