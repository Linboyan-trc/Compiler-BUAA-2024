package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolItem;
import frontend.SyntaxTable.SymbolTable;

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

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for(SymbolItem<DefNode> item:symbolTable.getVariables()){
            str.append(Integer.toString(symbolTable.getId() + 1) + " " + item.getName() + " " + item.getNode().getDefNodeType().toString() + "\n");
        }
        for(SymbolItem<FuncDefNode> item:symbolTable.getFunctions()){
            if (item.getName().equals("main")) {
                continue;
            }
            str.append(Integer.toString(symbolTable.getId() + 1) + " " + item.getName() + " " + item.getNode().getFuncDefType().toString() + "\n");
        }
        for(SymbolTable child:symbolTable.getChildren()){
            str.append(child.toString());
        }
        return str.toString();
    }
}
