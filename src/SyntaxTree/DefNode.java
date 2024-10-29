package SyntaxTree;

import Lexer.Pair;
import SyntaxTable.SyntaxType;

import java.util.LinkedList;

public class DefNode {
////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <DefNode> = Parent + Pair:IDENFR + 维数 + 初始值
    private DeclNode parent = null;
    private SyntaxType defNodeType = null;
    private Pair pair;
    private ExpNode length = null;
    private LinkedList<ExpNode> initValues = null;
    private String initValueForSTRCON = null;

////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public DefNode(DeclNode parent, Pair pair) {
        this.parent = parent;
        this.pair = pair;
    }

    public DefNode(SyntaxType defNodeType, Pair pair) {
        this.defNodeType = defNodeType;
        this.pair = pair;
    }

    public void setLength(ExpNode expNode) {
        this.length = expNode;
    }

    public void setInitValues(LinkedList<ExpNode> initValues) {
        this.initValues = initValues;
    }

    public void setInitValueForSTRCON(String initValueForSTRCON) {
        this.initValueForSTRCON = initValueForSTRCON;
    }

    public void toArray(){
        if(defNodeType == SyntaxType.Int) {
            defNodeType = SyntaxType.IntArray;
        } else if(defNodeType == SyntaxType.Char){
            defNodeType = SyntaxType.CharArray;
        }
    }

    // 2. get
    public DeclNode getParent() {
        return parent;
    }

    public Pair getPair() {
        return pair;
    }

    public ExpNode getLength() {
        return length;
    }

    public SyntaxType getDefNodeType() {
        return defNodeType;
    }
}
