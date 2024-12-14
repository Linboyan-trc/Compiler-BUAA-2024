package midend.LabelTable;

import midend.MidCode.MidCode.MidCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class LabelTable {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 单例模式 + 中间代码对应的标签表
    private static final LabelTable instance = new LabelTable();
    private static final HashMap<MidCode, LinkedList<Label>> midCodeToLabels = new HashMap<>();

    public static LabelTable getInstance() {
        return instance;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 建立中间代码和标签的关联，标签加入列表
    public void addToMidCodeToLabels(MidCode midCode, Label label) {
        if (!midCodeToLabels.containsKey(midCode)) {
            midCodeToLabels.put(midCode, new LinkedList<>());
        }
        midCodeToLabels.get(midCode).add(label);
    }

    // 2. 通过中间代码获取相关的标签
    public LinkedList<Label> getLabelList(MidCode midCode) {
        return midCodeToLabels.getOrDefault(midCode, new LinkedList<>());
    }

    // 3. 删除中间代码对应的标签
    public void deleteFromMidCodeToLabels(MidCode midCode) {
        midCodeToLabels.remove(midCode);
    }

    // 4. 删除掉没有用到标签，比如自定义但未使用的函数
    public void removeUnusedLabels(HashSet<Label> usedLabels) {
        // 3.1 遍历每个中间代码的标签列表
        for (LinkedList<Label> labelList : midCodeToLabels.values()) {
            // 3.2 每个标签列表中，没有使用的标签，并且不是顶层标签，那么删除
            for (var iterator = labelList.iterator(); iterator.hasNext();) {
                // 3.2 每个标签列表中的标签
                Label label = iterator.next();
                // 3.3 没有使用的标签，并且不是顶层标签，那么删除
                if (!usedLabels.contains(label) && label.getLabelId() != 0) {
                    iterator.remove();
                }
            }
        }
    }
}
