package frontend.SyntaxTree;

import midend.MidCode.MidCode.Nop;
import midend.MidCode.Value.Value;

public class NopNode implements StmtNode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public NopNode simplify() {
        return this;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode){
        return false;
    }

    @Override
    public Value generateMidCode() {
        new Nop();
        return null;
    }
}
