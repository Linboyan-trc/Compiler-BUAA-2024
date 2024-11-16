package SyntaxTree;

import Lexer.Pair;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class DefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DefNode> = <DefNode> = 符号表 + isFinal + Pair:IDENFR + 维数 + 初始值
    private final SymbolTable symbolTable;
    private final boolean isFinal;
    private SyntaxType defNodeType = null;
    private Pair pair;
    private ExpNode length = null;
    private LinkedList<ExpNode> initValues = null;
    private Pair initValueForSTRCON = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DefNode(SymbolTable symbolTable, boolean isFinal,
                   SyntaxType defNodeType, Pair pair,
                   ExpNode length, LinkedList<ExpNode> initValues, Pair initValueForSTRCON) {
        this.symbolTable = symbolTable;
        this.isFinal = isFinal;
        this.defNodeType = defNodeType;
        this.pair = pair;
        this.length = length;
        this.initValues = initValues;
        this.initValueForSTRCON = initValueForSTRCON;
    }

    // 2. get
    public boolean isFinal() {
        return isFinal;
    }

    public SyntaxType getDefNodeType() {
        return defNodeType;
    }

    public Pair getPair() {
        return pair;
    }

    public ExpNode getLength() {
        return length;
    }
}
