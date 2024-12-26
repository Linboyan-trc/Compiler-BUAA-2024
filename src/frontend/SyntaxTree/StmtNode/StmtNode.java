package frontend.SyntaxTree.StmtNode;

import frontend.SyntaxTree.BlockItemNode;
import frontend.SyntaxTree.DeclNode;

public interface StmtNode extends BlockItemNode {
    @Override
    StmtNode simplify();

    boolean hasContinue(BlockItemNode declNode, AssignNode assignNode);
}
