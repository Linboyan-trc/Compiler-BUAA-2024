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

    public LinkedList<Label> getLabelList(MidCode midCode) {
        return midCodeToLabels.getOrDefault(midCode, new LinkedList<>());
    }

    public void removeUnusedLabels(HashSet<Label> usedLabels) {
        for (LinkedList<Label> labelList : midCodeToLabels.values()) {
            labelList.removeIf(label -> !usedLabels.contains(label) && label.getLabelId() != 0);
        }
    }
}
