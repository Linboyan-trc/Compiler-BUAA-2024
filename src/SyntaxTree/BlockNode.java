package SyntaxTree;

import java.util.LinkedList;

public class BlockNode implements StmtNode{
    // 1. 一个<Block>有多个<BlockItem>
    private LinkedList<BlockItemNode> blockItemNodes = new LinkedList<>();
    private int endLineNumber;

    // 2. 构造 + 添加<BlockItem> + 设置endLineNumber
    public BlockNode() { }

    public void addBlockItemNode(BlockItemNode blockItemNode) {
        blockItemNodes.add(blockItemNode);
    }

    public void setEndLineNumber(int endLineNumber) {
        this.endLineNumber = endLineNumber;
    }

    // 3. 检查
    // TODO: 错误处理
}
