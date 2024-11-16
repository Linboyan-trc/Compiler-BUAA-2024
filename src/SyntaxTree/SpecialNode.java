package SyntaxTree;

import Lexer.Pair;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class SpecialNode extends DefNode {
    public SpecialNode(SymbolTable symbolTable, boolean isFinal,
                       SyntaxType defNodeType, Pair pair,
                       ExpNode length, LinkedList<ExpNode> initValues, Pair initValueForSTRCON) {
        super(symbolTable, isFinal,
                defNodeType, pair,
                length, initValues, initValueForSTRCON);
    }
}
