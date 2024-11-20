package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.LabelTable.Label;
import midend.MidCode.*;
import midend.MidCode.MidCode.FuncEntry;
import midend.MidCode.Value.Value;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class FuncDefNode implements SyntaxNode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. <FuncDefNode> = [变量类型:需要新开一个枚举类] + Pair:IDENFR + 参数列表 + 块
    private final SymbolTable symbolTable;
    private SyntaxType funcDefType;
    private Pair pair;
    private LinkedList<FuncFParamNode> funcFParamNodes;
    private BlockNode blockNode = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. set
    public FuncDefNode(SymbolTable symbolTable,
                       SyntaxType funcDefType, Pair pair, LinkedList<FuncFParamNode> funcFParamNodes) {
        this.symbolTable = symbolTable;
        this.funcDefType = funcDefType;
        this.pair = pair;
        this.funcFParamNodes = funcFParamNodes;
    }

    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    // 2. get
    public SyntaxType getFuncDefType() {
        return funcDefType;
    }

    public Pair getPair() {
        return pair;
    }

    public LinkedList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }

    // 3. 检查
    // 3. 就是检查函数的funcDefType和<Block>的返回值是否能一致
    public void checkForError() {
        blockNode.checkForError(funcDefType);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public FuncDefNode simplify() {
        funcFParamNodes = funcFParamNodes.stream().map(FuncFParamNode::simplify).collect(Collectors.toCollection(LinkedList::new));
        blockNode = blockNode.simplify();
        if (funcDefType.isVoidFunc()) {
            blockNode.complete();
        }
        return this;
    }

    @Override
    public Value generateMidCode() {
        // 1. 创建入口标签
        Label entryLabel = new Label(pair.getWord());

        // 2. 创建入口标签的中间代码节点，自动添加到中间代码中
        FuncEntry funcEntry = new FuncEntry(entryLabel);

        // 3. 设置当前函数，以更新(函数表 = 函数名 + 变量表)
        MidCodeTable.getInstance().setFunc(entryLabel.getLabelName());

        // 4. 为每个参数生产中间代码
        for(FuncFParamNode funcFParamNode : funcFParamNodes) {
            funcFParamNode.generateMidCode();
        }

        // 5. <BlockNode>生成中间代码
        blockNode.generateMidCode();

        // 6. 为函数中间代码的入口，添加标签列表
        entryLabel.setMidCode(funcEntry);
        return null;
    }

}