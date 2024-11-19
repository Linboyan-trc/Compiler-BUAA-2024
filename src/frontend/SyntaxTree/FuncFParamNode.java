package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class FuncFParamNode extends DefNode {
    public FuncFParamNode(SymbolTable symbolTable, SyntaxType defNodeType, Pair pair) {
        super(symbolTable,
                false,
                defNodeType,
                pair,
                null,
                new LinkedList<>(),
                null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public FuncFParamNode simplify() {
        return this;
    }
}
