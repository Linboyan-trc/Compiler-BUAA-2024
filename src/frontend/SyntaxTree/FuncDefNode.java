package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

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

}