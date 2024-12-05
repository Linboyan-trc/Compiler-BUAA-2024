package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.ExpNode.CharacterNode;
import frontend.SyntaxTree.ExpNode.ExpNode;
import frontend.SyntaxTree.ExpNode.NumberNode;
import midend.MidCode.MidCode.Declare;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.Word;

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
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

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

    public int getScopeId() {
        return symbolTable.getId();
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

    @Override
    public Value generateMidCode() {
        // 1. 判断此<DefNode>:Pair是否是全局变量
        // 1. 通过此<DefNode>所在的符号表是否有上层符号表来判断
        boolean isGlobal = symbolTable.getParent() == null;

        // 2. 对每个initValues生成中间代码
        LinkedList<Value> values = new LinkedList<>();
        for (ExpNode initValue : initValues) {
            values.add(initValue.generateMidCode());
        }


        // 3. 计算此<DefNode>长度
        int size =  (int) (length == null ? 1 : ((NumberNode) length).getValue());

        // 4.1 如果不是数组，就返回一个Word
        if (length == null) {
            Word value = new Word(pair.getWord() + "@" + symbolTable.getId());
            new Declare(isGlobal, isFinal, value, size, values);
        }

        // 4.2 如果是数组，就返回一个地址
        else {
            Addr value = new Addr(pair.getWord() + "@" + symbolTable.getId());
            new Declare(isGlobal, isFinal, value, size, values);
        }

        // 5. 不需要返回
        return null;
    }
}
