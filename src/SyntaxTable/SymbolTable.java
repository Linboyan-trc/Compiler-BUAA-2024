package SyntaxTable;

import SyntaxTree.DefNode;
import SyntaxTree.FuncDefNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class SymbolTable {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 深度，变量，函数，上层符号表
    private int depth = 1;
    private LinkedList<SymbolItem<DefNode>> variables = new LinkedList<>();
    private LinkedList<SymbolItem<FuncDefNode>> functions = new LinkedList<>();
    private SymbolTable parent = null;
    private LinkedList<SymbolTable> children = new LinkedList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 新建符号表，在建立第一个符号表的时候显示的指定深度为1
    public SymbolTable(SymbolTable parent,int depth) {
        this.parent = parent;
        this.depth = depth;
    }

    public SymbolTable(SymbolTable parent){
        this.parent = parent;
        this.depth = parent.depth + 1;
    }

    public void addChild(SymbolTable child){
        children.add(child);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 2.1 添加变量，函数
    public void addToVariables(DefNode defNode) {
        // 1. 获取变量名
        // 1.1 defNode:Pair + numOfElements + initValues
        // 1.2 defNode: a
        // 1.2 defNode: a = 1
        // 1.2 defNode: a[1] = {1}
        // 1.3 Pair:             a
        // 1.3 numOfElements:    [1] | 0
        // 1.3 initValues:       1 | null
        // 1.4 判断在本层是否存在重复
        if(containedInVariables(defNode) ||
                (parent == null && containedInFunctions(defNode))) {
            // 2.1 若重复需要添加到错误列表
            // TODO: 若重复需要添加到错误列表
        } else {
            // 2.2 否则添加到本层变量表
            variables.add(new SymbolItem<>(defNode));
        }
    }

    public void addToFunctions(FuncDefNode funcDefNode) {
        // 1. 获取变量名
        // 1.1 FuncDefNode:funcDefType三选一 + Pair + 参数列表 + 块
        // 1.2 FuncDefNode: int a() {}
        // 1.2 FuncDefNode: int a(int b) {}
        // 1.3 funcDefType三选一: VoidFunc | IntFunc | CharFunc
        // 1.3 Pair:             a
        // 1.3 参数列表:          int b | null
        // 1.3 块:               {}
        // 1.4 判断在本层是否存在重复
        if(containedInFunctions(funcDefNode) ||
                (parent == null && containedInVariables(funcDefNode))) {
            // 2.1 若重复需要添加到错误列表
            // TODO: 若重复需要添加到错误列表
        } else {
            functions.add(new SymbolItem<>(funcDefNode));
        }
    }

    // 2.2 工具:DefNode在variables，functions中是否存在
    // 2.2 工具:FuncDefNode在variables，functions中是否存在
    public boolean containedInVariables(DefNode defNode) {
        // 1. name
        String name = defNode.getPair().getWord();
        // 2. 遍历
        for(SymbolItem<DefNode> item : variables){
            if(item.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public boolean containedInVariables(FuncDefNode funcDefNode) {
        // 1. name
        String name = funcDefNode.getPair().getWord();
        // 2. 遍历
        for(SymbolItem<DefNode> item : variables){
            if(item.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public boolean containedInFunctions(DefNode defNode) {
        // 1. name
        String name = defNode.getPair().getWord();
        // 2. 遍历
        for(SymbolItem<FuncDefNode> item : functions){
            if(item.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public boolean containedInFunctions(FuncDefNode funcDefNode) {
        // 1. name
        String name = funcDefNode.getPair().getWord();
        // 2. 遍历
        for(SymbolItem<FuncDefNode> item : functions){
            if(item.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 3. get
    public SymbolTable getParent() {
        return parent;
    }

    public LinkedList<SymbolItem<DefNode>> getVariables() {
        return variables;
    }

    public LinkedList<SymbolItem<FuncDefNode>> getFunctions() {
        return functions;
    }

    public LinkedList<SymbolTable> getChildren() {
        return children;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 4. print for dfs
    public int print(FileWriter fw, int scope) throws IOException {
        for(SymbolItem<DefNode> item : variables){
            if(item.getNode().getParent() == null) {
                fw.write(scope + " " + item.getName() + " " + item.getNode().getDefNodeType().toString() + "\n");
            } else {
                fw.write(scope + " " + item.getName() + " " + item.getNode().getParent().getDeclNodeType().toString() + "\n");
            }
        }
        for(SymbolTable item : children){
            scope++;
            scope = item.print(fw,scope);
        }
        return scope;
    }
}
