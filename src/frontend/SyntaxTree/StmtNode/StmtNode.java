package frontend.SyntaxTree.StmtNode;

import frontend.SyntaxTree.BlockItemNode;

public interface StmtNode extends BlockItemNode {
    @Override
    StmtNode simplify();

    boolean hasContinue(AssignNode assignNode);
}
