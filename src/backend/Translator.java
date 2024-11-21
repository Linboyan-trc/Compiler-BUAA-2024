package backend;

import backend.Address.*;
import backend.MipsCode.*;
import backend.MipsCode.IIns.*;
import backend.MipsCode.JIns.JInsJ;
import backend.MipsCode.JIns.JInsJAL;
import backend.MipsCode.RIns.RIns2Reg1Imm;
import backend.MipsCode.RIns.RIns3Reg;
import backend.MipsCode.RIns.RInsJR;
import backend.ValueMeta.Reg;
import backend.ValueMeta.ValueMeta;
import midend.LabelTable.LabelTable;
import midend.MidCode.MidCode.*;
import midend.MidCode.Operate.BinaryOperate;
import midend.MidCode.Operate.UnaryOperate;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Imm;
import midend.MidCode.Value.Value;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Word;

import java.util.HashMap;
import java.util.LinkedList;

import static backend.MipsCode.RIns.RInsOpcode.*;
import static backend.MipsCode.IIns.IInsOpcode.*;

public class Translator {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 单例模式
    private static final Translator instance = new Translator();
    // 2. 生成中间代码时的中间代码 + 标签
    private MidCodeTable midCodeTable = MidCodeTable.getInstance();
    private LabelTable labelTable = LabelTable.getInstance();
    // 3. 宏代码 + mips代码段
    // 3. 宏代码中用于记录全局变量的地址
    private LinkedList<MipsCode> macroCodeList = new LinkedList<>();
    private LinkedList<MipsCode> mipsCodeList = new LinkedList<>();
    private HashMap<Value, Address> valueToAddress = new HashMap<>();
    // 4. 寄存器使用记录
    private LinkedList<Reg> synchronizedReg = new LinkedList<>();

    private StringBuilder mipsCode = new StringBuilder();
    private RegScheduler scheduler = RegScheduler.getInstance();


    private LinkedList<Value> valueFromArg = new LinkedList<>();
    private int frameSize;
    private int pushCount = 0;
    private int strCount = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 获取单例
    public static Translator getInstance() {
        return instance;
    }

    public LinkedList<MipsCode> getMipsCodeList() {
        return mipsCodeList;
    }

    public LinkedList<Reg> getSynchronizedReg() {
        return synchronizedReg;
    }

    public HashMap<Value, Address> getValueToAddress() {
        return valueToAddress;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void translate() {
        // 1. 从中间代码中的全局变量中获取声明
        for (MidCode midCode : midCodeTable.getGlobalCodeList()) {
            // 1. 获取声明
            if (!(midCode instanceof Declare)) {
                continue;
            }
            Declare declare = (Declare) midCode;

            // 2. 获取Word, Addr, Imm
            Value value = declare.getValue();

            // 3. 获取初始值
            LinkedList<Value> initValues = declare.getInitValues();

            // 4. 所有初始值转化为Int列表
            LinkedList<Long> intValues = new LinkedList<>();
            initValues.forEach(item -> intValues.add(((Imm) item).getValue()));

            // 5. 在宏代码中添加Word宏
            // 5. WordMacro:中间代码变量名 + 中间代码变量空间 + 整数初始值
            macroCodeList.add(new WordMacro(value.getName(), declare.getSize(), intValues));

            // 6. 为变量分配地址
            valueToAddress.put(value, new AbsoluteAddress(value.getName()));
        }

        // 2. 压栈
        mipsCodeList.add(new IIns2Reg1Imm(addiu, Reg.SP, Reg.SP, new Imm(-4)));

        // 3. 从中间代码中的代码部分获取每一个中间代码节点
        for (MidCode midCode : midCodeTable.getMidCodeList()) {
            // 1. 清除寄存器
            synchronizedReg.clear();

            // 2. 添加注释，方便Debug
            mipsCodeList.add(new Comment(midCode.toString()));

            // 3. 分支，返回，跳转需要重置调度器
            if (midCode instanceof Branch || midCode instanceof Return || midCode instanceof Jump) {
                scheduler.flush();
            }

            if (labelTable.getLabelList(midCode).size() != 0) {
                scheduler.flush();
                mipsCodeList.addAll(labelTable.getLabelList(midCode));
            }
            switch (midCode.getClass().toString()) {
                case "class midend.MidCode.MidCode.ArgPush":
                    assert midCode instanceof ArgPush;
                    generateMips((ArgPush) midCode);
                    break;
                case "class midend.MidCode.MidCode.Assign":
                    assert midCode instanceof Assign;
                    generateMips((Assign) midCode);
                    break;
                case "class midend.MidCode.MidCode.Branch":
                    assert midCode instanceof Branch;
                    generateMips((Branch) midCode);
                    break;
                case "class midend.MidCode.MidCode.Declare":
                    assert midCode instanceof Declare;
                    generateMips((Declare) midCode);
                    break;
                case "class midend.MidCode.MidCode.Exit":
                    assert midCode instanceof Exit;
                    generateMips((Exit) midCode);
                    break;
                case "class midend.MidCode.MidCode.FuncCall":
                    assert midCode instanceof FuncCall;
                    generateMips((FuncCall) midCode);
                    break;
                case "class midend.MidCode.MidCode.FuncEntry":
                    assert midCode instanceof FuncEntry;
                    generateMips((FuncEntry) midCode);
                    break;
                case "class midend.MidCode.MidCode.IntGet":
                    assert midCode instanceof IntGet;
                    generateMips((IntGet) midCode);
                    break;
                case "class midend.MidCode.MidCode.CharGet":
                    assert midCode instanceof CharGet;
                    generateMips((CharGet) midCode);
                    break;
                case "class midend.MidCode.MidCode.Jump":
                    assert midCode instanceof Jump;
                    generateMips((Jump) midCode);
                    break;
                case "class midend.MidCode.MidCode.Load":
                    assert midCode instanceof Load;
                    generateMips((Load) midCode);
                    break;
                case "class midend.MidCode.MidCode.Move":
                    assert midCode instanceof Move;
                    generateMips((Move) midCode);
                    break;
                case "class midend.MidCode.MidCode.ParaGet":
                    assert midCode instanceof ParaGet;
                    generateMips((ParaGet) midCode);
                    break;
                case "class midend.MidCode.MidCode.Print":
                    assert midCode instanceof Print;
                    generateMips((Print) midCode);
                    break;
                case "class midend.MidCode.MidCode.Return":
                    assert midCode instanceof Return;
                    generateMips((Return) midCode);
                    break;
                case "class midend.MidCode.MidCode.Store":
                    assert midCode instanceof Store;
                    generateMips((Store) midCode);
                    break;
            }
        }
    }

    public ValueMeta getValueMeta(Value value, boolean load, boolean lw) {
        if (value instanceof Imm) {
            return (Imm) value;
        } else if (value instanceof Word && value.getName().equals("?")) {
            return Reg.RV;
        } else {
            Reg reg;
            if ((reg = scheduler.find(value)) == null) {
                if ((reg = scheduler.alloc(value)) == null) {
                    reg = scheduler.preempt(value);
                }
                if (load) {
                    Address address = valueToAddress.get(value);
                    if (value instanceof Word) {
                        mipsCodeList.add(new IInsLW(reg, address));
                    } else if (address instanceof AbsoluteAddress) {
                        mipsCodeList.add(new IInsLA(reg, address));
                    } else if (lw || valueFromArg.contains(value) || ((Addr) value).isTemp()) {
                        mipsCodeList.add(new IInsLW(reg, address));
                    } else {
                        mipsCodeList.add(new IInsLA(reg, address));
                    }
                }
            }
            synchronizedReg.add(reg);
            return reg;
        }
    }

    public void generateMips(ArgPush argPush) {
        pushCount++;
        ValueMeta valueMeta = getValueMeta(argPush.getValue(), true, false);
        if (valueMeta instanceof Reg) {
            mipsCodeList.add(new IInsSW((Reg) valueMeta, new RelativeAddress(Reg.SP, -pushCount * 4)));
        } else {
            mipsCodeList.add(new IInsLI(Reg.TR, (Imm) valueMeta));
            mipsCodeList.add(new IInsSW(Reg.TR, new RelativeAddress(Reg.SP, -pushCount * 4)));
        }
    }

    public void generateMips(Assign assign) {
        if (assign.getSourceValue() instanceof BinaryOperate) {
            BinaryOperate.BinaryOp binaryOp = ((BinaryOperate) assign.getSourceValue()).getBinaryOp();
            Value leftValue = ((BinaryOperate) assign.getSourceValue()).getLeftValue();
            Value rightValue = ((BinaryOperate) assign.getSourceValue()).getRightValue();
            ValueMeta leftMeta = getValueMeta(leftValue, true, false);
            ValueMeta rightMeta = getValueMeta(rightValue, true, false);
            Reg valMeta = (Reg) getValueMeta(assign.getTargetValue(), false, false);
            if (leftValue instanceof Addr) {
                if (rightMeta instanceof Imm) {
                    mipsCodeList.add(new IInsLI(Reg.TR, new Imm(((Imm) rightMeta).getValue() * 4)));
                    generateMips(binaryOp, valMeta, leftMeta, Reg.TR);
                } else {
                    mipsCodeList.add(new RIns2Reg1Imm(sll, Reg.TR, (Reg) rightMeta, new Imm(2)));
                    generateMips(binaryOp, valMeta, leftMeta, Reg.TR);
                }
            } else if (rightValue instanceof Addr) {
                if (leftMeta instanceof Imm) {
                    mipsCodeList.add(new IInsLI(Reg.TR, new Imm(((Imm) leftMeta).getValue() * 4)));
                    generateMips(binaryOp, valMeta, Reg.TR, rightMeta);
                } else {
                    mipsCodeList.add(new RIns2Reg1Imm(sll, Reg.TR, (Reg) leftMeta, new Imm(2)));
                    generateMips(binaryOp, valMeta, Reg.TR, rightMeta);
                }
            } else {
                generateMips(binaryOp, valMeta, leftMeta, rightMeta);
            }
        } else {
            UnaryOperate.UnaryOp unaryOp = ((UnaryOperate) assign.getSourceValue()).getUnaryOp();
            Reg valMeta = (Reg) getValueMeta(assign.getTargetValue(), false, false);
            ValueMeta rightMeta = getValueMeta(((UnaryOperate) assign.getSourceValue()).getValue(), true, false);
            generateMips(unaryOp, valMeta, rightMeta);
        }
    }

    public void generateMips(BinaryOperate.BinaryOp binaryOp, Reg valMeta, ValueMeta leftMeta, ValueMeta rightMeta) {
        if (leftMeta instanceof Imm && rightMeta instanceof Imm) {
            mipsCodeList.add(new IInsLI(Reg.TR, (Imm) leftMeta));
            mipsCodeList.add(new IInsLI(valMeta, (Imm) rightMeta));
            generateMips(valMeta, binaryOp, Reg.TR, valMeta);
        } else if (leftMeta instanceof Imm) {
            mipsCodeList.add(new IInsLI(Reg.TR, (Imm) leftMeta));
            generateMips(valMeta, binaryOp, Reg.TR, (Reg) rightMeta);
        } else if (rightMeta instanceof Imm) {
            mipsCodeList.add(new IInsLI(Reg.TR, (Imm) rightMeta));
            generateMips(valMeta, binaryOp, (Reg) leftMeta, Reg.TR);
        } else {
            generateMips(valMeta, binaryOp, (Reg) leftMeta, (Reg) rightMeta);
        }
    }

    private void generateMips(Reg valMeta, BinaryOperate.BinaryOp binaryOp, Reg leftReg, Reg rightReg) {
        switch (binaryOp) {
            case ADD:
                mipsCodeList.add(new RIns3Reg(addu, valMeta, leftReg, rightReg));
                break;
            case SUB:
                mipsCodeList.add(new RIns3Reg(subu, valMeta, leftReg, rightReg));
                break;
            case MUL:
                mipsCodeList.add(new Mult(leftReg, rightReg));
                mipsCodeList.add(new Mflo(valMeta));
                break;
            case DIV:
                mipsCodeList.add(new Div(leftReg, rightReg));
                mipsCodeList.add(new Mflo(valMeta));
                break;
            case MOD:
                mipsCodeList.add(new Div(leftReg, rightReg));
                mipsCodeList.add(new Mfhi(valMeta));
                break;
            case EQ:
                mipsCodeList.add(new RIns3Reg(seq, valMeta, leftReg, rightReg));
                break;
            case NE:
                mipsCodeList.add(new RIns3Reg(sne, valMeta, leftReg, rightReg));
                break;
            case GE:
                mipsCodeList.add(new RIns3Reg(sge, valMeta, leftReg, rightReg));
                break;
            case GT:
                mipsCodeList.add(new RIns3Reg(sgt, valMeta, leftReg, rightReg));
                break;
            case LE:
                mipsCodeList.add(new RIns3Reg(sle, valMeta, leftReg, rightReg));
                break;
            case LT:
                mipsCodeList.add(new RIns3Reg(slt, valMeta, leftReg, rightReg));
                break;
        }
    }

    public void generateMips(UnaryOperate.UnaryOp unaryOp, Reg valMeta, ValueMeta rightMeta) {
        if (rightMeta instanceof Imm) {
            switch (unaryOp) {
                case POS:
                    mipsCodeList.add(new IInsLI(valMeta, (Imm) rightMeta));
                    break;
                case NEG:
                    mipsCodeList.add(new IInsLI(valMeta, new Imm(-((Imm) rightMeta).getValue())));
                    break;
                case NOT:
                    mipsCodeList.add(new IInsLI(valMeta, new Imm(((Imm) rightMeta).getValue() == 0 ? 1 : 0)));
                    break;
            }
        } else {
            switch (unaryOp) {
                case POS:
                    mipsCodeList.add(new RIns3Reg(addu, valMeta, (Reg) rightMeta, Reg.ZERO));
                    break;
                case NEG:
                    mipsCodeList.add(new RIns3Reg(subu, valMeta, Reg.ZERO, (Reg) rightMeta));
                    break;
                case NOT:
                    mipsCodeList.add(new RIns3Reg(seq, valMeta, (Reg) rightMeta, Reg.ZERO));
                    break;
            }
        }
    }

    public void generateMips(Branch branch) {
        Branch.BranchOp branchOp = branch.getBranchOp();
        ValueMeta leftMeta = getValueMeta(branch.getLeftValue(), true, false);
        if (leftMeta instanceof Imm) {
            mipsCodeList.add(new IInsLI(Reg.TR, (Imm) leftMeta));
            if (branchOp == Branch.BranchOp.EQ) {
                mipsCodeList.add(new IInsBEQ(Reg.TR, Reg.ZERO, branch.getBranchLabel().getLabelName()));
            } else {
                mipsCodeList.add(new IInsBNE(Reg.TR, Reg.ZERO, branch.getBranchLabel().getLabelName()));
            }
        } else if (branchOp == Branch.BranchOp.EQ) {
            mipsCodeList.add(new IInsBEQ((Reg) leftMeta, Reg.ZERO, branch.getBranchLabel().getLabelName()));
        } else {
            mipsCodeList.add(new IInsBNE((Reg) leftMeta, Reg.ZERO, branch.getBranchLabel().getLabelName()));
        }
    }

    public void generateMips(Declare declare) {
        if (declare.getValue() instanceof Word) {
            Reg leftMeta = (Reg) getValueMeta(declare.getValue(), false, false);
            if (!declare.getInitValues().isEmpty()) {
                Value value = declare.getInitValues().get(0);
                ValueMeta rightMeta = getValueMeta(value, true, false);
                if (rightMeta instanceof Imm) {
                    mipsCodeList.add(new IInsLI(leftMeta, (Imm) rightMeta));
                } else {
                    mipsCodeList.add(new RIns3Reg(addu, leftMeta, (Reg) rightMeta, Reg.ZERO));
                }
            }
        } else {
            RelativeAddress addr = (RelativeAddress) valueToAddress.get(declare.getValue());
            for (int i = 0, size = declare.getInitValues().size(); i < size; i++) {
                Value value = declare.getInitValues().get(i);
                ValueMeta rightMeta = getValueMeta(value, true, false);
                if (rightMeta instanceof Imm) {
                    mipsCodeList.add(new IInsLI(Reg.TR, (Imm) rightMeta));
                    mipsCodeList.add(new IInsSW(Reg.TR,
                            new RelativeAddress(addr.getBase(), addr.getOffset() + i * 4)));
                } else {
                    mipsCodeList.add(new IInsSW((Reg) rightMeta,
                            new RelativeAddress(addr.getBase(), addr.getOffset() + i * 4)));
                }
            }
        }
    }

    public void generateMips(Exit exit) {
        mipsCodeList.add(new IInsLI(Reg.RV, new Imm(10)));
        mipsCodeList.add(new Syscall());
    }

    public void generateMips(FuncCall funcCall) {
        pushCount = 0;
        HashMap<Reg, Value> reg2value = scheduler.getReg2value();
        reg2value.forEach((reg, value) -> {
            if (valueToAddress.containsKey(value)) {
                if (value instanceof Word || (value instanceof Addr && ((Addr) value).isTemp())) {
                    mipsCodeList.add(new IInsSW(reg, valueToAddress.get(value)));
                }
            }
        });
        mipsCodeList.add(new IInsSW(Reg.RA, new RelativeAddress(Reg.SP, 0)));
        mipsCodeList.add(new JInsJAL(funcCall.getName()));
        mipsCodeList.add(new IInsLW(Reg.RA, new RelativeAddress(Reg.SP, 0)));
        scheduler.clear();
    }

    public void generateMips(FuncEntry funcEntry) {
        scheduler.clear();
        valueFromArg.clear();
        int fp = 4;
        LinkedList<Value> values = MidCodeTable.getInstance().getValInfos(funcEntry.getEntryLabel().getLabelName());
        for (int i = values.size() - 1; i >= 0; i--) {
            valueToAddress.put(values.get(i), new RelativeAddress(Reg.SP, fp));
            fp += MidCodeTable.getInstance().getValSize(values.get(i)) * 4;
            System.out.println(values.get(i) + " " + valueToAddress.get(values.get(i)));
        }
        frameSize = fp;
        mipsCodeList.add(new IIns2Reg1Imm(addiu, Reg.SP, Reg.SP, new Imm(-fp)));
    }

    public void generateMips(IntGet intGet) {
        mipsCodeList.add(new IInsLI(Reg.RV, new Imm(5)));
        mipsCodeList.add(new Syscall());
    }

    public void generateMips(CharGet charGet) {
        mipsCodeList.add(new IInsLI(Reg.RV, new Imm(12)));
        mipsCodeList.add(new Syscall());
    }

    public void generateMips(Jump jump) {
        mipsCodeList.add(new JInsJ(jump.getLabel().getLabelName()));
    }

    public void generateMips(Load load) {
        ValueMeta leftMeta = getValueMeta(load.getTargetValue(), false, false);
        ValueMeta rightMeta = getValueMeta(load.getSourceValue(), true, false);
        mipsCodeList.add(new IInsLW((Reg) leftMeta, new RelativeAddress((Reg) rightMeta, 0)));
    }

    public void generateMips(Move move) {
        ValueMeta leftMeta = getValueMeta(move.getTargetValue(), false, false);
        ValueMeta rightMeta = getValueMeta(move.getSourceValue(), true, false);
        if (rightMeta instanceof Reg) {
            mipsCodeList.add(new IIns2Reg1Imm(addiu, (Reg) leftMeta, (Reg) rightMeta, new Imm(0)));
        } else {
            mipsCodeList.add(new IInsLI((Reg) leftMeta, (Imm) rightMeta));
        }
    }

    public void generateMips(ParaGet paraGet) {
        valueFromArg.add(paraGet.getValue());
        ValueMeta valueMeta = getValueMeta(paraGet.getValue(), false, false);
        mipsCodeList.add(new IInsLW((Reg) valueMeta, valueToAddress.get(paraGet.getValue())));
    }

    public void generateMips(Print print) {
        // 1. 去除""
        String formatString = print.getFmtString().substring(1, print.getFmtString().length() - 1);

        // 2. 找到%d
        int index = getIndex(formatString);

        // 3. 计数
        int count = 1;
        while (index >= 0) {
            // 1. 获取字符常量
            String out = formatString.substring(0, index);

            // 2. 加入到全局变量中
            if (!out.isEmpty()) {
                String label = "string" + strCount++;
                macroCodeList.add(new StringMacro(label, out));
                mipsCodeList.add(new IInsLA(Reg.AR, new AbsoluteAddress(label)));
                mipsCodeList.add(new IInsLI(Reg.RV, new Imm(4)));
                mipsCodeList.add(new Syscall());
            }

            // 3. 打印int或char
            if(formatString.charAt(index+1) == 'd'){
                mipsCodeList.add(new IInsLW(Reg.AR, new RelativeAddress(Reg.SP, -count * 4)));
                mipsCodeList.add(new IInsLI(Reg.RV, new Imm(1)));
                mipsCodeList.add(new Syscall());
            } else {
                mipsCodeList.add(new IInsLB(Reg.AR, new RelativeAddress(Reg.SP, -count * 4)));
                mipsCodeList.add(new IInsLI(Reg.RV, new Imm(11)));
                mipsCodeList.add(new Syscall());
            }

            // 4. 截取剩余字符串
            formatString = formatString.substring(index + 2);
            index = getIndex(formatString);
            count++;
        }

        if (!formatString.isEmpty()) {
            String label = "string" + strCount++;
            macroCodeList.add(new StringMacro(label, formatString));
            mipsCodeList.add(new IInsLA(Reg.AR, new AbsoluteAddress(label)));
            mipsCodeList.add(new IInsLI(Reg.RV, new Imm(4)));
            mipsCodeList.add(new Syscall());
        }

        pushCount = 0;
    }

    private int getIndex(String string){
        int index1 = string.indexOf("%d");
        int index2 = string.indexOf("%c");
        if(index1 == -1){
            return index2;
        } else if (index2 == -1){
            return index1;
        } else {
            if(index1 < index2){
                return index1;
            } else {
                return index2;
            }
        }
    }

    public void generateMips(Return ret) {
        if (ret.getValue() != null) {
            ValueMeta valueMeta = getValueMeta(ret.getValue(), true, false);
            if (valueMeta instanceof Imm) {
                mipsCodeList.add(new IInsLI(Reg.RV, (Imm) valueMeta));
            } else {
                mipsCodeList.add(new IIns2Reg1Imm(addiu, Reg.RV, (Reg) valueMeta, new Imm(0)));
            }
        }
        mipsCodeList.add(new IIns2Reg1Imm(addiu, Reg.SP, Reg.SP, new Imm(frameSize)));
        mipsCodeList.add(new RInsJR(Reg.RA));
    }

    public void generateMips(Store store) {
        ValueMeta leftMeta = getValueMeta(store.getTargetValue(), true, true);
        ValueMeta rightMeta = getValueMeta(store.getSourceValue(), true, false);
        if (rightMeta instanceof Reg) {
            mipsCodeList.add(new IInsSW((Reg) rightMeta, new RelativeAddress((Reg) leftMeta, 0)));
        } else {
            mipsCodeList.add(new IInsLI(Reg.TR, (Imm) rightMeta));
            mipsCodeList.add(new IInsSW(Reg.TR, new RelativeAddress((Reg) leftMeta, 0)));
        }
    }

    @Override
    public String toString() {
        mipsCode.append(".data\n");
        for (MipsCode code : macroCodeList) {
            mipsCode.append(code.toString()).append("\n");
        }
        mipsCode.append(".text\n");
        for (MipsCode code : mipsCodeList) {
            mipsCode.append(code.toString()).append("\n");
        }
        return mipsCode.toString();
    }
}
