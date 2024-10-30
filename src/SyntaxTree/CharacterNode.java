package SyntaxTree;

import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class CharacterNode implements ExpNode {
    // 1. num
    private String word;

    // 2.
    public CharacterNode(String word) {
        this.word = word;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        return SyntaxType.Char;
    }
}
