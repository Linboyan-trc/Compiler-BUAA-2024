package backend;

import backend.Address.*;
import backend.Block.FuncBlock;
import backend.MipsCode.IIns.IIns;
import backend.MipsCode.IIns.IInsSW;
import backend.ValueMeta.Reg;
import midend.MidCode.MidCode.Nop;
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
    // 2. 单例模式
    private static RegScheduler instance = new RegScheduler();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 寄存器到Value的映射，代表寄存器中存储的值
    // 2. 标记正在用的寄存器，标记没有被使用的寄存器
    private HashMap<Reg, Value> registerToValue = new HashMap<>();
    private LinkedList<Reg> busyRegisters = new LinkedList<>();
    private LinkedList<Reg> freeRegsisters = new LinkedList<>();

    private List<Reg> globalRegisters = new ArrayList<>();
    private List<Reg> localRegisters = new ArrayList<>();
    private HashMap<Value, Reg> valueToRegister = new HashMap<>();

    private FuncBlock funcBlock;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 构造一个寄存器分配器，只会构造一个
    private RegScheduler() {
        freeRegsisters.addAll(regs);
    }

    public static RegScheduler getInstance() {
        return instance;
    }

    // 2. 获取映射表
    public HashMap<Reg, Value> getReg2value() {
        return registerToValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 释放所有寄存器，全部寄存器都加入freeRegsisters
    public void clear() {
        for (Reg reg : localRegisters) {
            registerToValue.remove(reg);
        }
        busyRegisters.clear();
        freeRegsisters.clear();
        freeRegsisters.addAll(localRegisters);
    }

    public void clearAll() {
        registerToValue.clear();
        valueToRegister.clear();
        busyRegisters.clear();
        freeRegsisters.clear();
        freeRegsisters.addAll(localRegisters);
    }

    // 2. 根据Value寻找有没有哪个寄存器里存了这个Value，然后返回这个寄存器
    public Reg find(Value value) {
        for (Reg reg : registerToValue.keySet()) {
            if (registerToValue.get(reg).equals(value)) {
                if (busyRegisters.contains(reg)) {
                    busyRegisters.remove(reg);
                    busyRegisters.addFirst(reg);
                }
                return reg;
            }
        }
        if (valueToRegister.containsKey(value)) {
            registerToValue.put(valueToRegister.get(value), value);
        }
        return valueToRegister.get(value);
    }

    // 3. 为一个Value分配一个寄存器，分配失败的时候返回null
    public Reg alloc(Value value) {
        if (funcBlock.getRegister(value) != null) {
            registerToValue.put(funcBlock.getRegister(value), value);
            valueToRegister.put(value, funcBlock.getRegister(value));
            return funcBlock.getRegister(value);
        }
        if (!freeRegsisters.isEmpty()) {
            Reg reg = freeRegsisters.removeFirst();
            busyRegisters.addLast(reg);
            registerToValue.put(reg, value);
            return reg;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 占用一个正在使用的寄存器
    public Reg preempt(Value value) {
        Reg selectedReg = null;
        for (Reg reg : busyRegisters) {
            Value oldValue = registerToValue.get(reg);
            if (Translator.getInstance().getBasicBlock().usedUp(oldValue, new Nop())
                    && !Translator.getInstance().getSynchronizedReg().contains(reg)) {
                selectedReg = reg;
                break;
            }
        }
        if (selectedReg == null) {
            for (Reg reg : busyRegisters) {
                Value oldValue = registerToValue.get(reg);
                if (!Translator.getInstance().getBasicBlock().isLive(oldValue, Translator.getInstance().getNowMidCode())
                        && !Translator.getInstance().getSynchronizedReg().contains(reg)) {
                    selectedReg = reg;
                    break;
                }
            }
        }
        if (selectedReg == null) {
            for (Reg reg : busyRegisters) {
                if (!Translator.getInstance().getSynchronizedReg().contains(reg)) {
                    selectedReg = reg;
                }
            }
        }
        if (selectedReg != null) {
            Value oldValue = registerToValue.get(selectedReg);
            if ((oldValue instanceof Word || (value instanceof Addr && ((Addr) oldValue).isTemp())) &&
                    Translator.getInstance().getBasicBlock().isLive(oldValue, Translator.getInstance().getNowMidCode())) {
                Translator.getInstance().getMipsCodeList().add(
                        new IInsSW(selectedReg, Translator.getInstance().getValueToAddress().get(oldValue)));
            }
            registerToValue.put(selectedReg, value);
            busyRegisters.remove(selectedReg);
            busyRegisters.addLast(selectedReg);
            return selectedReg;
        }
        return null;
    }

    // 2. 写回到内存 + 解除寄存器分配状态
    public void flush(boolean isReturn) {
        for (Reg reg : busyRegisters) {
            Value value = registerToValue.get(reg);
            Address address = Translator.getInstance().getValueToAddress().get(value);
            if (address instanceof AbsoluteAddress && value instanceof Word) {
                Translator.getInstance().getMipsCodeList().add(new IInsSW(reg, address));
            } else if (!isReturn && address instanceof RelativeAddress && !(value instanceof Addr && !((Addr) value).isTemp()) &&
                    Translator.getInstance().getBasicBlock().getliveOutUnitTable().contains(value)) {
                Translator.getInstance().getMipsCodeList().add(new IInsSW(reg, address));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<Reg> getGlobalRegisters() {
        return globalRegisters;
    }

    public void dividing(HashSet<Reg> usedRegs) {
        globalRegisters.addAll(usedRegs);
        localRegisters.clear();
        for (Reg reg : regs) {
            if (!globalRegisters.contains(reg)) {
                localRegisters.add(reg);
            }
        }
    }

    public void setFuncBlock(FuncBlock curFuncBlock) {
        this.funcBlock = curFuncBlock;
    }

    public void dismissMapping(Value useVal) {
        for (Reg reg : busyRegisters) {
            if (registerToValue.get(reg).equals(useVal)) {
                freeRegsisters.add(reg);
                busyRegisters.remove(reg);
                registerToValue.remove(reg);
                break;
            }
        }
    }
}
