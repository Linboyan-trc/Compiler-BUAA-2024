package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class DefNode implements SyntaxNode {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public DefNode simplify() {
        // 1. 对<ExpNode>化简
        if (length != null) {
            length = length.simplify();
        }

        // 2. 对初始值化简
        initValues = initValues.stream().map(ExpNode::simplify).collect(Collectors.toCollection(LinkedList::new));

        // 3. 返回化简后的结果
        return this;
    }

    // 2. 当defNode是单变量，或者是数组，但是传入了指定的下标，则可以获取具体的数值，返回为NumberNode或CharacterNode
    public ExpNode getValue(ExpNode length){
        // 1. 对DefNode自己化简
        simplify();

        // 2. 没有传入下标，就是单变量
        if(length == null) {
            if (initValues.isEmpty()) {
                return new NumberNode(0);
            } else {
                return initValues.get(0);
            }
        }

        // 3. 否则是数组
        else{
            // 1. 数组没有初始化
            int index = (int)(((NumberNode)length).getValue());
            if(initValues.isEmpty() && initValueForSTRCON == null) {
                return new NumberNode(0);
            }
            // 2. 数组有初始化
            else if (!initValues.isEmpty()) {
                return initValues.get(index);
            } else {
                CharacterNode temp = new CharacterNode(String.valueOf(initValueForSTRCON.getWord().charAt(index)));
                return temp;
            }
        }
    }
}
