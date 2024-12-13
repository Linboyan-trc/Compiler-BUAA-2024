package frontend.SyntaxTree.StmtNode;

import midend.LabelTable.Label;
import midend.MidCode.MidCode.Jump;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Value;

public class JumpNode implements StmtNode {
    private Label label;

    public JumpNode(Label label) {
        this.label = label;
    }

    @Override
    public StmtNode simplify() {
        return this;
    }

    @Override
    public boolean hasContinue(AssignNode assignNode){
        return false;
    }

    @Override
    public Value generateMidCode() {
        MidCodeTable.getInstance().addToMidCodes(new Jump(label));
        return null;
    }
}
