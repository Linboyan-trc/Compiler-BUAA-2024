package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;

public class ReturnNode implements StmtNode {
    // 1. Pair + <Exp>
    private final SymbolTable symbolTable;
    private Pair pair;
    private ExpNode expNode;

    // 2.
    public ReturnNode(SymbolTable symbolTable, Pair pair, ExpNode expNode) {
        this.symbolTable = symbolTable;
        this.pair = pair;
        this.expNode = expNode;
    }

    // 1. 获取return行号，以及是否具有返回值
    public int getLineNumber() {
        return pair.getLineNumber();
    }

    public boolean hasExpNode() {
        return expNode != null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public ReturnNode simplify() {
        if (expNode != null) {
            expNode = expNode.simplify();
        }
        return this;
    }
}
