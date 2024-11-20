package backend;

import backend.Address.*;
import backend.MipsCode.IIns.IIns;
import backend.MipsCode.IIns.IInsSW;
import backend.ValueMeta.Reg;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.Word;

import java.util.*;

public class RegScheduler {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 寄存器堆，只包括了10个临时变量寄存器和15个全局变量寄存器
    static List<Reg> regs = Arrays.asList(
            Reg.T0, Reg.T1, Reg.T2, Reg.T3, Reg.T4,
            Reg.T5, Reg.T6, Reg.T7, Reg.T8, Reg.T9,
            Reg.S0, Reg.S1, Reg.S2, Reg.S3, Reg.S4,
            Reg.S5, Reg.S6, Reg.S7, Reg.S8, Reg.S9,
            Reg.S10, Reg.S11, Reg.S12, Reg.S13, Reg.S14
    );
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 寄存器到Value的映射，代表寄存器中存储的值
    // 2. 标记正在用的寄存器，标记没有被使用的寄存器
    private HashMap<Reg, Value> reg2value = new HashMap<>();
    private LinkedList<Reg> busyRegs = new LinkedList<>();
    private LinkedList<Reg> freeRegs = new LinkedList<>();
    // 3. 单例模式
    private static RegScheduler instance = new RegScheduler();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 构造一个寄存器分配器，只会构造一个
    private RegScheduler() {
        freeRegs.addAll(regs);
    }

    public static RegScheduler getInstance() {
        return instance;
    }

    // 2. 获取映射表
    public HashMap<Reg, Value> getReg2value() {
        return reg2value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 释放所有寄存器，全部寄存器都加入freeRegs
    public void clear() {
        reg2value.clear();
        busyRegs.clear();
        freeRegs.clear();
        freeRegs.addAll(regs);
    }

    // 2. 根据Value寻找有没有哪个寄存器里存了这个Value，然后返回这个寄存器
    public Reg find(Value value) {
        for (Reg reg : busyRegs) {
            if (reg2value.get(reg).equals(value)) {
                return reg;
            }
        }
        return null;
    }

    // 3. 为一个Value分配一个寄存器，分配失败的时候返回null
    public Reg alloc(Value value) {
        if (!freeRegs.isEmpty()) {
            Reg reg = freeRegs.getFirst();
            freeRegs.removeFirst();
            busyRegs.addLast(reg);
            reg2value.put(reg, value);
            return reg;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 占用一个正在使用的寄存器
    public Reg preempt(Value value) {
        // 1. 当所有寄存器都是使用时，找一个寄存器不处于同步占用
        for (Reg reg : busyRegs) {
            if (!Translator.getInstance().getSynchronizedReg().contains(reg)) {
                // 2. 取出寄存器的值
                Value oldValue = reg2value.get(reg);
                // 3. 如果寄存器是Word，或者寄存器是Addr但是是临时变量
                // 3. 这两种情况都需要将此时寄存器的值存储到内存中
                if (oldValue instanceof Word || (value instanceof Addr && ((Addr) oldValue).isTemp())) {
                    // 4. 需要制作一个sw指令，将此时reg的值存储到值对应的地址中
                    Translator.getInstance()
                            .getMipsCodeList()
                            .add(
                                    new IInsSW(
                                            reg,
                                            Translator.getInstance().getValueToAddress().get(oldValue)));
                }
                // 5. 更新寄存器的值
                reg2value.put(reg, value);
                // 6. 寄存器加入到栈尾
                busyRegs.remove(reg);
                busyRegs.addLast(reg);
                // 7. 返回寄存器
                return reg;
            }
        }
        // 2. 如果所有寄存器都在被同步占用，由返回空，表示分配失败
        return null;
    }

    // 2. 写回到内存 + 解除寄存器分配状态
    public void flush() {
        // 1. 获取所有寄存器和值的映射
        for (Map.Entry<Reg, Value> entry : reg2value.entrySet()) {
            // 2. 获取变量地址
            Address address = Translator.getInstance().getValueToAddress().get(entry.getValue());
            // 3. 如果是绝对地址 + Word，相对地址 + 不是Addr，就需要写回内存
            if (address instanceof AbsoluteAddress && entry.getValue() instanceof Word
                    || address instanceof RelativeAddress && !(entry.getValue() instanceof Addr)) {
                Translator.getInstance().getMipsCodeList().add(new IInsSW(entry.getKey(), address));
            }
        }
        // 4. 释放所有寄存器
        clear();
    }
}
