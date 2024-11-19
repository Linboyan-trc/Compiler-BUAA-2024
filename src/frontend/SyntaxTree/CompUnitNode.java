package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolItem;
import frontend.SyntaxTable.SymbolTable;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class CompUnitNode implements SyntaxNode {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public CompUnitNode simplify() {
        // 1. 对每个<DeclNode>化简
        declNodes = declNodes.stream().map(DeclNode::simplify).collect(Collectors.toCollection(LinkedList::new));

        // 2. 对每个<FuncDefNode>化简
        funcDefNodes = funcDefNodes.stream().map(FuncDefNode::simplify).collect(Collectors.toCollection(LinkedList::new));

        // 3. 对<MainFuncDefNode>化简
        mainFuncDefNode = mainFuncDefNode.simplify();

        // 4. 返回化简后的结果
        return this;
    }
}
