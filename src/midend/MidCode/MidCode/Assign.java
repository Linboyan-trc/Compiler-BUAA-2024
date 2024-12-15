package midend.MidCode.MidCode;

import midend.MidCode.Operate.*;
import midend.MidCode.Optimize.DefUnit;
import midend.MidCode.Optimize.UseUnit;
import midend.MidCode.Value.*;

import java.util.LinkedList;

import static midend.MidCode.Operate.UnaryOperate.UnaryOp.*;
import static midend.MidCode.Operate.BinaryOperate.BinaryOp.*;

public class Assign extends MidCode implements DefUnit, UseUnit {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 不用加载，直接变量之间的赋值
    // 1. 是否是临时变量 + 目标值 + 源值
    private final boolean isTemp;
    private final Value targetValue;
    private final Operate sourceValue;

    public Assign(boolean isTemp, Value targetValue, Operate sourceValue) {
        this.isTemp = isTemp;
        this.targetValue = targetValue;
        this.sourceValue = sourceValue;
    }

    public boolean isTemp() {
        return isTemp;
    }

    public Value getTargetValue() {
        return targetValue;
    }

    public Operate getSourceValue() {
        return sourceValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    public void simplify() {
        // 1. 右值是UnaryOperate
        if (sourceValue instanceof UnaryOperate) {
            // 1.1 获取右值
            UnaryOperate operate = (UnaryOperate) sourceValue;

            // 1.2 右值是Imm
            if (operate.getValue() instanceof Imm) {
                // 1.2.1 获取右值
                Imm imm = (Imm) operate.getValue();

                // 1.2.2 获取符号
                // 1.2.3 把这个Assign替换为一个Move
                switch (operate.getUnaryOp()) {
                    case POS:
                        changeToAnother(new Move(isTemp, targetValue, imm));
                        break;
                    case NEG:
                        changeToAnother(new Move(isTemp, targetValue, new Imm(-imm.getValue())));
                        break;
                    case NOT:
                        changeToAnother(new Move(isTemp, targetValue, new Imm(imm.getValue() != 0 ? 0 : 1)));
                        break;
                }
            }

            // 1.3 右值不是Imm，但是UnaryOp是+，也把Assign替换为一个Move
            else if (operate.getUnaryOp() == POS) {
                changeToAnother(new Move(isTemp, targetValue, operate.getValue()));
            }
        }

        // 2. 右值是BinaryOperate
        else {
            // 2.1 获取右值
            BinaryOperate operate = (BinaryOperate) sourceValue;

            // 2.2 两侧都是Imm
            if (operate.getLeftValue() instanceof Imm && operate.getRightValue() instanceof Imm) {
                // 2.2.1 获取两次Imm
                Imm leftImm = (Imm) operate.getLeftValue();
                Imm rightImm = (Imm) operate.getRightValue();

                // 2.2.2 把这个Assign替换为一个Move
                changeToAnother(new Move(isTemp, targetValue, fullyCalculate(operate.getBinaryOp(), leftImm, rightImm)));
            }

            // 2.3 左侧是Imm
            else if (operate.getLeftValue() instanceof Imm) {
                // 2.3.1 获取左侧Imm
                Imm leftImm = (Imm) operate.getLeftValue();

                // 2.3.2 获取BinaryOp
                switch (operate.getBinaryOp()) {
                    // 2.3.3 '0+'替换为Move
                    case ADD:
                        if (leftImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, operate.getRightValue()));
                        }
                        break;
                    // 2.3.4 '0-'替换为新的Assign
                    case SUB:
                        if (leftImm.getValue() == 0) {
                            changeToAnother(new Assign(isTemp, targetValue, new UnaryOperate(NEG, operate.getRightValue())));
                        }
                        break;
                    // 2.3.5 '0*', '1*'替换为Move
                    // 2.3.5 '-1*'替换为新的Assign
                    case MUL:
                        if (leftImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                        } else if (leftImm.getValue() == 1) {
                            changeToAnother(new Move(isTemp, targetValue, operate.getRightValue()));
                        } else if (leftImm.getValue() == -1) {
                            changeToAnother(new Assign(isTemp, targetValue, new UnaryOperate(NEG, operate.getRightValue())));
                        }
                        break;
                    // 2.3.6 '0/', '0%'替换为Move    
                    case DIV:
                    case MOD:
                        if (leftImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                        }
                        break;
                    // 2.3.7 '0&&', '非0&&'替换为Move    
                    case AND:
                        if (leftImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                        } else {
                            changeToAnother(new Move(isTemp, targetValue, operate.getRightValue()));
                        }
                        break;
                    // 2.3.8 '0||', '非0||', 替换为新的Move
                    case OR:
                        if (leftImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, operate.getRightValue()));
                        } else {
                            changeToAnother(new Move(isTemp, targetValue, new Imm(1)));
                        }
                        break;
                }
            }

            // 2.4 右侧是Imm
            else if (operate.getRightValue() instanceof Imm) {
                // 2.4.1 获取右侧Imm
                Imm rightImm = (Imm) operate.getRightValue();

                // 2.4.2 获取BinaryOp
                switch (operate.getBinaryOp()) {
                    // 2.4.3 '-0'替换为Move
                    case ADD:
                    case SUB:
                        if (rightImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, operate.getLeftValue()));
                        }
                        break;
                    case MUL:
                        if (rightImm.getValue() == 0) {
                            changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                        } else if (rightImm.getValue() == 1) {
                            changeToAnother(new Move(isTemp, targetValue, operate.getLeftValue()));
                        } else if (rightImm.getValue() == -1) {
                            changeToAnother(new Assign(isTemp, targetValue, new UnaryOperate(NEG, targetValue)));
                        }
                        break;
                    case DIV:
                        if (rightImm.getValue() == 1) {
                            changeToAnother(new Move(isTemp, targetValue, operate.getLeftValue()));
                        } else if (rightImm.getValue() == -1) {
                            changeToAnother(new Assign(isTemp, targetValue, new UnaryOperate(NEG, operate.getLeftValue())));
                        }
                        break;
                    case MOD:
                        if (rightImm.getValue() == 1 || rightImm.getValue() == -1) {
                            changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                        }
                        break;
                    // case AND:
                    //    if (rightImm.getValue() == 0) {
                    //        changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                    //    } else {
                    //        changeToAnother(new Move(isTemp, targetValue, operate.getLeftValue()));
                    //    }
                    //    break;
                    //case OR:
                    //    if (rightImm.getValue() == 0) {
                    //        changeToAnother(new Move(isTemp, targetValue, operate.getLeftValue()));
                    //    } else {
                    //        changeToAnother(new Move(isTemp, targetValue, new Imm(1)));
                    //    }
                    //    break;
                }
            }

            // 2.5 对于'==', '!='加一种判断
            else if (operate.getLeftValue() == operate.getRightValue()) {
                if (operate.getBinaryOp() == EQ) {
                    changeToAnother(new Move(isTemp, targetValue, new Imm(1)));
                } else if (operate.getBinaryOp() == NE) {
                    changeToAnother(new Move(isTemp, targetValue, new Imm(0)));
                }
            }
        }
    }

    // 2. 化简右侧
    public Imm fullyCalculate(BinaryOperate.BinaryOp op, Imm targetValue, Imm sourceValue) {
        switch (op) {
            case ADD:
                return new Imm(targetValue.getValue() + sourceValue.getValue());
            case SUB:
                return new Imm(targetValue.getValue() - sourceValue.getValue());
            case MUL:
                return new Imm(targetValue.getValue() * sourceValue.getValue());
            case DIV:
                return new Imm(targetValue.getValue() / sourceValue.getValue());
            case MOD:
                return new Imm(targetValue.getValue() % sourceValue.getValue());
            case AND:
                return new Imm(targetValue.getValue() == 1 && sourceValue.getValue() == 1 ? 1 : 0);
            case OR:
                return new Imm(targetValue.getValue() == 1 || sourceValue.getValue() == 1 ? 1 : 0);
            case EQ:
                return new Imm(targetValue.getValue() == sourceValue.getValue() ? 1 : 0);
            case NE:
                return new Imm(targetValue.getValue() != sourceValue.getValue() ? 1 : 0);
            case LT:
                return new Imm(targetValue.getValue() < sourceValue.getValue() ? 1 : 0);
            case GT:
                return new Imm(targetValue.getValue() > sourceValue.getValue() ? 1 : 0);
            case LE:
                return new Imm(targetValue.getValue() <= sourceValue.getValue() ? 1 : 0);
            case GE:
                return new Imm(targetValue.getValue() >= sourceValue.getValue() ? 1 : 0);
            default:
                return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return targetValue + " <- " + sourceValue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成定义变量
    @Override
    public Value getDefUnit() {
        // 1. 返回左值即可
        return targetValue;
    }

    // 2. 生成使用变量
    @Override
    public LinkedList<Value> getUseUnit() {
        // 2. 右值生成使用变量
        return sourceValue.getUseUnit();
    }

    // 3. 更换使用变量
    @Override
    public void changeToAnotherUnit(Value oldValue, Value newValue) {
        // 3. 右值更换使用变量
        sourceValue.changeToAnotherUnit(oldValue, newValue);
    }
}
