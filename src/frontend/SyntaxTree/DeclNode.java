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
        defNodes = defNodes.stream().map(DefNode::simplify).collect(Collectors.toCollection(LinkedList::new));

        // 2. 返回化简后的结果
        return this;
    }

    @Override
    public Value generateMidCode() {
        defNodes.forEach(DefNode::generateMidCode);
        return null;
    }
}