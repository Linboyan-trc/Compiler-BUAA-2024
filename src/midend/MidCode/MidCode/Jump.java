package midend.MidCode.MidCode;

import midend.LabelTable.Label;
import midend.MidCode.MidCodeTable;

public class Jump implements MidCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private Label label;

    public Jump(Label label) {
        this.label = label;
        MidCodeTable.getInstance().addToMidCodes(this);
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "JUMP " + label.getLabelName();
    }
}
