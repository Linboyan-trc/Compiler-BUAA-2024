package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.StmtNode.*;
import midend.MidCode.Value.Value;

import java.util.LinkedList;

public class BlockNode implements StmtNode {
    // 1. 一个<Block>有多个<BlockItem>
    private final SymbolTable symbolTable;
    private LinkedList<BlockItemNode> blockItemNodes;
    private int endLineNumber;
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造 + 添加<BlockItem> + 设置endLineNumber
    public BlockNode(SymbolTable symbolTable, LinkedList<BlockItemNode> blockItemNodes, int endLineNumber) {
        this.symbolTable = symbolTable;
        this.blockItemNodes = blockItemNodes;
        this.endLineNumber = endLineNumber;
    }

    // 3. 检查
    public void checkForError(SyntaxType funcDefSyntaxType) {
        // 1. 对于一个自定义函数的<Block>
        // 1. 如果是IntFunc或CharFunc，就要检查<Block>的<BlockItem>数量
        // 1. 如果为0就是缺少返回值，g类错误
        // 1. 或者最后一个<BlockItem>不是<ReturnNode>，也是缺少返回值，g类错误
        if (funcDefSyntaxType == SyntaxType.IntFunc || funcDefSyntaxType == SyntaxType.CharFunc) {
            if(blockItemNodes.size() == 0) {
                errorHandler.addError(new ErrorRecord(endLineNumber, 'g'));
            } else if (!(blockItemNodes.getLast() instanceof ReturnNode)) {
                errorHandler.addError(new ErrorRecord(endLineNumber, 'g'));
            }
        }
        // 2. 对于VoidFunc
        // 2. 就要检查每一个<BlockItem>，包括<BlockNode>,<BranchNode>, <ForNode>, <ReturnNode>的返回值类型
        else {
            for(BlockItemNode blockItemNode : blockItemNodes) {
                if(blockItemNode instanceof BlockNode) {
                    ((BlockNode) blockItemNode).checkForError(funcDefSyntaxType);
                } else if (blockItemNode instanceof BranchNode) {
                    ((BranchNode) blockItemNode).checkForError(funcDefSyntaxType);
                } else if (blockItemNode instanceof ForNode) {
                    ((ForNode) blockItemNode).checkForError(funcDefSyntaxType);
                } else if (blockItemNode instanceof ReturnNode && ((ReturnNode) blockItemNode).hasExpNode()) {
                    errorHandler.addError(new ErrorRecord(((ReturnNode) blockItemNode).getLineNumber(), 'f'));
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public BlockNode simplify() {
        LinkedList<BlockItemNode> simplifiedBlockItems = new LinkedList<>();
        for (BlockItemNode blockItem : blockItemNodes) {
            BlockItemNode simplified = blockItem.simplify();
            simplifiedBlockItems.add(simplified);
        }
        blockItemNodes = simplifiedBlockItems;

        return this;
    }

    // 2. 给funcDef的void类型加上一个return
    public void complete() {
        if (blockItemNodes.size() == 0 || !(blockItemNodes.getLast() instanceof ReturnNode)) {
            blockItemNodes.add(new ReturnNode(symbolTable, null, null));
        }
    }

    @Override
    public boolean hasContinue(AssignNode assignNode){
        for (int i = 0; i < blockItemNodes.size(); i++) {
            BlockItemNode blockItemNode = blockItemNodes.get(i);
            if (blockItemNode instanceof StmtNode) {
                if (((StmtNode) blockItemNode).hasContinue(assignNode)) {
                    // 在当前位置前插入 assignNode
                    if(blockItemNode instanceof ContinueNode) {
                        blockItemNodes.add(i, assignNode);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Value generateMidCode() {
        for(BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.generateMidCode();
        }
        return null;
    }
}
