package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;

public class NumberNode implements ExpNode {
    // 1. num
    private long number;

    // 2.
    public NumberNode(long number) {
        this.number = number;
    }

    public long getValue() {
        return number;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        return SyntaxType.Int;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public NumberNode simplify() {
        return this;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode) {
        return false;
    }

    @Override
    public Value generateMidCode() {
        return new Imm(number);
    }
}
