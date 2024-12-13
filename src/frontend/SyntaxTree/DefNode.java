package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.ExpNode.CharacterNode;
import frontend.SyntaxTree.ExpNode.ExpNode;
import frontend.SyntaxTree.ExpNode.NumberNode;
import midend.MidCode.MidCode.Declare;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Imm;
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
        // 1. 字符串转初值
        if(this.initValueForSTRCON != null) {
            if(this.initValues == null) {
              this.initValues = new LinkedList<>();
            }
            for(int index = 1; index < (this.initValueForSTRCON.getWord().length() - 1); index++){
                // 1. 处理转义字符
                char ch = this.initValueForSTRCON.getWord().charAt(index);
                if(ch == '\\'){
                    if(index < this.initValueForSTRCON.getWord().length() - 1){
                        char nextCh = this.initValueForSTRCON.getWord().charAt(index + 1);
                        if(nextCh == 'a' || nextCh == 'b' || nextCh == 't' || nextCh == 'n'
                        || nextCh == 'v' || nextCh == 'f' || nextCh == '\"' || nextCh == '\''
                        || nextCh == '\\' || nextCh == '0'){
                            NumberNode temp;
                            switch (nextCh){
                                case 'a':
                                    temp = new NumberNode(7);
                                    break;
                                case 'b':
                                    temp = new NumberNode(8);
                                    break;
                                case 't':
                                    temp = new NumberNode(9);
                                    break;
                                case 'n':
                                    temp = new NumberNode(10);
                                    break;
                                case 'v':
                                    temp = new NumberNode(11);
                                    break;
                                case 'f':
                                    temp = new NumberNode(12);
                                    break;
                                case '\"':
                                    temp = new NumberNode(34);
                                    break;
                                case '\'':
                                    temp = new NumberNode(39);
                                    break;
                                case '\\':
                                    temp = new NumberNode(92);
                                    break;
                                case '0':
                                    temp = new NumberNode(0);
                                    break;
                                default:
                                    temp = new NumberNode(0);
                                    break;
                            }
                            this.initValues.add(temp);
                            index++;
                            continue;
                        }
                    }
                }
                NumberNode temp = new NumberNode(this.initValueForSTRCON.getWord().charAt(index));
                this.initValues.add(temp);
            }
        }
        // 2. 如果length是Number，补齐不足的值
        if(length instanceof NumberNode) {
            while(initValues.size() < ((NumberNode)length).getValue()){
                initValues.add(new NumberNode(0));
            }
        }
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

        // 3. 对初始值化简
        LinkedList<ExpNode> newInitValues = new LinkedList<>();
        for (ExpNode node : initValues) {
            newInitValues.add(node.simplify());
        }
        initValues = newInitValues;

        // 4. 返回化简后的结果
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

        // 2. 对每个initValues或initValueForSTRCON生成中间代码
        LinkedList<Value> values = new LinkedList<>();
        for (ExpNode initValue : initValues) {
            values.add(initValue.generateMidCode());
        }

        // 3. 计算此<DefNode>长度
        int size =  (int) (length == null ? 1 : ((NumberNode) length).getValue());

        // 4.1 如果不是数组，就返回一个Word
        if (defNodeType.isVariable()) {
            Word value = new Word(pair.getWord() + "@" + symbolTable.getId());
            MidCodeTable.getInstance().addToMidCodes(
                new Declare(isGlobal, isFinal, value, 1, values)
            );
            MidCodeTable.getInstance().addToVarInfo(value, size);
        }

        // 4.2 如果是数组，就返回一个地址
        else {
            Addr value = new Addr(pair.getWord() + "@" + symbolTable.getId());
            MidCodeTable.getInstance().addToMidCodes(
                new Declare(isGlobal, isFinal, value, size, values)
            );
            MidCodeTable.getInstance().addToVarInfo(value, size);
        }

        // 5. 不需要返回
        return null;
    }
}
