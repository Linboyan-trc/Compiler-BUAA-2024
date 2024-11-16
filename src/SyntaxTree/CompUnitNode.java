package SyntaxTree;

import SyntaxTable.SymbolTable;

import java.util.LinkedList;

public class CompUnitNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <CompUnit> = 符号表 + <DeclNode> + <FuncDefNode> + <FuncDefNode:mainFuncDefNode>
    private final SymbolTable symbolTable;
    private LinkedList<DeclNode> declNodes = new LinkedList<>();
    private LinkedList<FuncDefNode> funcDefNodes = new LinkedList<>();
    private FuncDefNode mainFuncDefNode;

////////////////////////////////////////////////////////////////////////////////////////////////////
    public CompUnitNode(SymbolTable symbolTable, LinkedList<DeclNode> declNodes, LinkedList<FuncDefNode> funcDefNodes, FuncDefNode mainFuncDefNode) {
        this.symbolTable = symbolTable;
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }
}
