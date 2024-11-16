package SyntaxTree;

import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class DeclNode implements BlockItemNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DeclNode> = 符号表 + isFinal + <DefNode>
    private final SymbolTable symbolTable;
    private final boolean isFinal;
    private LinkedList<DefNode> defNodes = new LinkedList<>();

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DeclNode(SymbolTable symbolTable, boolean isFinal, LinkedList<DefNode> defNodes) {
        this.symbolTable = symbolTable;
        this.isFinal = isFinal;
        this.defNodes = defNodes;
    }
}