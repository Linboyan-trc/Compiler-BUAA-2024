package midend.LabelTable;

import backend.MipsCode.MipsCode;
import midend.MidCode.MidCode.Jump;
import midend.MidCode.MidCode.MidCode;

public class Label implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. Label Id
    private static int cnt = 1;
    private int id = 0;
    // 2. Label的名字，和对应的中间代码节点
    private String name;
    private MidCode midCode;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 创建空Label
    public Label() {
        this.id = cnt++;
    }

    // 2. 创建有名字的label
    public Label(String name) {
        this.name = name;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 获取label的id
    public int getLabelId() {
        return id;
    }

    // 2. 获取label的名字，空label的名字为Label + <id>
    public String getLabelName() {
        if (name != null) {
            return name;
        } else {
            return "Label" + id;
        }
    }

    // 3. 获取Label对应的中间代码节点
    public MidCode getMidCode() {
        return midCode;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 为此标签指定关联的中间代码
    // 1. 并建立中间代码 + 标签列表的映射关系
    public void setMidCode(MidCode midCode) {
        this.midCode = midCode;
        LabelTable.getInstance().addToMidCodeToLabels(midCode, this);
    }

    // 2. 找到跳转的最终目标中间代码
    public Label getTarget() {
        if (midCode instanceof Jump) {
            if (((Jump) midCode).getLabel() == this) {
                return this;
            }
            return ((Jump) midCode).getLabel().getTarget();
        } else {
            return this;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        // 1. 生成中间代码
        // 2. 标签 + :
        if (name != null) {
            return name + ":";
        } else {
            return "Label" + id + ":";
        }
    }
}
