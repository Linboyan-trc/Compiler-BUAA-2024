package frontend.SyntaxTree;

public class NopNode implements StmtNode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public NopNode simplify() {
        return this;
    }
}
