package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolItem;
import frontend.SyntaxTable.SymbolTable;
import midend.MidCode.*;
import midend.MidCode.MidCode.Exit;
import midend.MidCode.MidCode.FuncCall;
import midend.MidCode.Value.Value;

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
            str.append(symbolTable.getId() + 1 + " " + item.getName() + " " + item.getNode().getDefNodeType().toString() + "\n");
        }
        for(SymbolItem<FuncDefNode> item:symbolTable.getFunctions()){
            if (item.getName().equals("main")) {
                continue;
            }
            str.append(symbolTable.getId() + 1 + " " + item.getName() + " " + item.getNode().getFuncDefType().toString() + "\n");
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
        LinkedList<DeclNode> simplifiedDeclNodes = new LinkedList<>();
        for (DeclNode node : declNodes) {
            simplifiedDeclNodes.add(node.simplify());
        }
        declNodes = simplifiedDeclNodes;

        // 2. 对每个<FuncDefNode>化简
        LinkedList<FuncDefNode> simplifiedFuncDefNodes = new LinkedList<>();
        for (FuncDefNode node : funcDefNodes) {
            simplifiedFuncDefNodes.add(node.simplify());
        }
        funcDefNodes = simplifiedFuncDefNodes;

        // 3. 对<MainFuncDefNode>化简
        mainFuncDefNode = mainFuncDefNode.simplify();

        // 4. 返回化简后的结果
        return this;
    }

    @Override
    public Value generateMidCode() {
        // 1. 全局变量生成中间代码
        for(DeclNode declNode:declNodes){
            declNode.generateMidCode();
        }

        // 2. 中间代码设置当前函数:main
        // 2. 然后添加当前函数 + 程序出口
        MidCodeTable.getInstance().setFunc("main");
        new FuncCall("main");
        new Exit();

        // 3. main函数生成中间代码
        mainFuncDefNode.generateMidCode();

        // 4. 自定义函数生成中间代码
        for(FuncDefNode funcDefNode:funcDefNodes){
            funcDefNode.generateMidCode();
        }

        // 5. 不需要返回
        return null;
    }
}
