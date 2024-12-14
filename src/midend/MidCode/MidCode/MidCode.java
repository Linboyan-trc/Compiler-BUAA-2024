package midend.MidCode.MidCode;

import midend.LabelTable.Label;
import midend.LabelTable.LabelTable;

import java.util.LinkedList;

public class MidCode {
    private MidCode previous;
    private MidCode next;

    // 1. 链表连接到下一个
    public MidCode linkToNext(MidCode next) {
        this.next = next;
        if (next != null) {
            next.previous = this;
        }
        return next;
    }

    // 2. 删除链表中的一个
    public void removeFromMidCodeList() {
        // 1. 上一个节点连接到下一个
        previous.next = next;

        // 2. 下一个节点连接回上一个
        if (next != null) {
            next.previous = previous;
        }

        // 3. 获取与当前MidCode相关联的标签列表
        LinkedList<Label> labels = LabelTable.getInstance().getLabelList(this);

        // 4. 将当前MidCode相关联的标签列表关联到下一个节点
        for (Label label : labels) {
            label.setMidCode(next);
        }

        // 5. 从LabelTable中删除与当前MidCode相关联的标签
        LabelTable.getInstance().deleteFromMidCodeToLabels(this);
    }

    // 3. 更换当前中间代码
    public void changeToAnother(MidCode... midCodes) {
        // 3.1 把新的中间代码连接上上一个中间代码
        for (MidCode midCode : midCodes) {
            previous = previous.linkToNext(midCode);
        }

        // 3.2 最后next再连接回来
        if (next != null) {
            previous.linkToNext(next);
        }

        // 3.3 获取这个中间代码的所有标签
        LinkedList<Label> labels = LabelTable.getInstance().getLabelList(this);

        // 3.4 把每个标签关联的中间代码改成上一个中间代码的下一个
        for (Label label : labels) {
            label.setMidCode(previous.next);
        }
    }

    // 4. 获取上一个节点
    public MidCode getPrevious() {
        return previous;
    }

    // 5. 获取下一个节点
    public MidCode getNext() {
        return next;
    }
}
