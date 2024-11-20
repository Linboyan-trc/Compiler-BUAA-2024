package frontend.SyntaxTree;

import frontend.SyntaxTable.SymbolTable;
import midend.MidCode.Value.Value;

import java.util.LinkedList;
import java.util.stream.Collectors;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public DeclNode simplify() {
        // 1. 对每个<DefNode>化简
        LinkedList<DefNode> newDefNodes = new LinkedList<>();
        for(DefNode defNode : defNodes) {
            newDefNodes.add(defNode.simplify());
        }
        defNodes = newDefNodes;

        // 2. 返回化简后的结果
        return this;
    }

    @Override
    public Value generateMidCode() {
        for(DefNode defNode : defNodes) {
            defNode.generateMidCode();
        }
        return null;
    }
}