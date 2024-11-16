package SyntaxTree;

import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class NumberNode implements ExpNode {
    // 1. num
    private long number;

    // 2.
    public NumberNode(long number) {
        this.number = number;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        return SyntaxType.Int;
    }
}