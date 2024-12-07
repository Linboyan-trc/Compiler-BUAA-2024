package frontend.SyntaxTree.ExpNode;

import frontend.Lexer.Pair;
import frontend.Lexer.Token;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.StmtNode.*;
import midend.MidCode.MidCode.Assign;
import midend.MidCode.Operate.BinaryOperate;
import midend.MidCode.Value.*;

import java.util.HashMap;

public class BinaryExpNode implements ExpNode {
    // 1. 就是()*(), ()+()
    private final SymbolTable symbolTable;
    private ExpNode leftExp;
    private Pair binaryOp;
    private ExpNode rightExp;

    // 2.
    public BinaryExpNode(SymbolTable symbolTable, ExpNode leftExp, Pair binaryOp, ExpNode rightExp) {
        this.symbolTable = symbolTable;
        this.leftExp = leftExp;
        this.binaryOp = binaryOp;
        this.rightExp = rightExp;
    }

    public Pair getBinaryOp() {
        return binaryOp;
    }

    public ExpNode getLeftExp() {
        return leftExp;
    }

    public ExpNode getRightExp() {
        return rightExp;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 如果经过了+，-，*，/，%的运算，就是INT
        if(binaryOp.getToken() == Token.PLUS ||
                binaryOp.getToken() == Token.MINU ||
                binaryOp.getToken() == Token.MULT ||
                binaryOp.getToken() == Token.DIV ||
                binaryOp.getToken() == Token.MOD) {
            return SyntaxType.Int;
        }
        // 2. 否则是<Cond>
        else {
            return SyntaxType.Bool;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public ExpNode simplify() {
        // 1. 获取左右化简
        ExpNode simLeft = leftExp.simplify();
        ExpNode simRight = rightExp.simplify();

        // 2. 如果都是数字，根据binaryOp计算运算结果，返回NumberNode
        if ((simLeft instanceof NumberNode || simLeft instanceof CharacterNode)
                && (simRight instanceof NumberNode || simRight instanceof CharacterNode)) {
            return fullyCalculate(binaryOp, simLeft, simRight);
        }

        // 3. 其中一个是NumberNode，计算部分结果，返回ExpNode
        else if ((simLeft instanceof NumberNode || simLeft instanceof CharacterNode)
                || (simRight instanceof NumberNode || simRight instanceof CharacterNode)) {
            return partialCalculate(binaryOp, simLeft, simRight);
        }

        // 4. 没有NumberNode，两侧化简后返回即可
        return new BinaryExpNode(symbolTable, simLeft, binaryOp, simRight);
    }

    public NumberNode fullyCalculate(Pair binaryOp, ExpNode leftExp, ExpNode rightExp) {
        // 1. getNumberOrCharacterValue()
        long value1 = getNumberOrCharacterValue(leftExp);
        long value2 = getNumberOrCharacterValue(rightExp);

        // 2. calculate
        switch (binaryOp.getToken()) {
            // 1. + -
            case PLUS:
                return new NumberNode(value1 + value2);
            case MINU:
                return new NumberNode(value1 - value2);
            // 2. * / %
            case MULT:
                return new NumberNode(value1 * value2);
            case DIV:
                return new NumberNode(value1 / value2);
            case MOD:
                return new NumberNode(value1 % value2);
            // 3. > >= < <=
            case GRE:
                return new NumberNode(value1 > value2 ? 1 : 0);
            case GEQ:
                return new NumberNode(value1 >= value2 ? 1 : 0);
            case LSS:
                return new NumberNode(value1 < value2 ? 1 : 0);
            case LEQ:
                return new NumberNode(value1 <= value2 ? 1 : 0);
            // 4. == !=
            case EQL:
                return new NumberNode(value1 == value2 ? 1 : 0);
            case NEQ:
                return new NumberNode(value1 != value2 ? 1 : 0);
            // 5. && ||
            case AND:
                return new NumberNode((value1 != 0 && value2 != 0) ? 1 : 0);
            case OR:
                return new NumberNode((value1 != 0 || value2 != 0) ? 1 : 0);
            default:
                return null;
        }
    }

    public ExpNode partialCalculate(Pair binaryOp, ExpNode leftExp, ExpNode rightExp) {
        // 1. 左边是数字
        if (leftExp instanceof NumberNode || leftExp instanceof CharacterNode) {
            // 1. 对于数字和字符，都转换为数字
            NumberNode left = new NumberNode(getNumberOrCharacterValue(leftExp));

            // 2. 根据BinaryOp分类
            switch (binaryOp.getToken()) {
                case PLUS:
                    // 1. 加法，且左边是0，直接返回右边
                    if (left.getValue() == 0) {
                        return rightExp;
                    }

                    // 2. 加法，且右侧是Binary，右侧的左边是数字，右侧的运算是+-，就合并左侧和右侧的左边
                    else if (rightExp instanceof BinaryExpNode) {
                        BinaryExpNode right = (BinaryExpNode) rightExp;
                        if ((right.binaryOp.isToken(PLUS) || right.binaryOp.isToken(MINU))
                                && (right.leftExp instanceof NumberNode || right.leftExp instanceof CharacterNode)) {
                            NumberNode temp = new NumberNode(getNumberOrCharacterValue(right.leftExp));
                            return new BinaryExpNode(
                                    symbolTable,
                                    new NumberNode(left.getValue() + temp.getValue()),
                                    right.binaryOp,
                                    right.rightExp).simplify();
                        }
                    }
                    return new BinaryExpNode(
                            symbolTable, left, binaryOp, rightExp);
                case MINU:
                    // 1. 减法，且左边是0，直接返回Unary，op为-，exp为右边
                    if (left.getValue() == 0) {
                        return new UnaryExpNode(symbolTable, new Pair(MINU, 0), rightExp).simplify();
                    }

                    // 2. 减法，右侧为Binary，右侧的左边是数字，右侧的运算是+-，就合并左侧和右侧的左边
                    else if (rightExp instanceof BinaryExpNode) {
                        BinaryExpNode right = (BinaryExpNode) rightExp;
                        if ((right.binaryOp.isToken(PLUS) || right.binaryOp.isToken(MINU))
                                && (right.leftExp instanceof NumberNode || right.leftExp instanceof CharacterNode)) {
                            NumberNode temp = new NumberNode(getNumberOrCharacterValue(right.leftExp));
                            return new BinaryExpNode(
                                    symbolTable,
                                    new NumberNode(left.getValue() - temp.getValue()),
                                    right.binaryOp,
                                    right.rightExp).simplify();
                        }
                    }
                    return new BinaryExpNode(symbolTable, left, binaryOp, rightExp);
                case MULT:
                    // 1. 乘法，左侧为0，直接返回0
                    if (left.getValue() == 0) {
                        return new NumberNode(0);
                    }

                    // 2. 乘法，左侧为1，直接返回右侧
                    else if (left.getValue() == 1) {
                        return rightExp;
                    }

                    // 3. 乘法，左侧为-1，直接返回Unary，op为-，exp为右边
                    else if (left.getValue() == -1) {
                        return new UnaryExpNode(symbolTable, new Pair(MINU, 0), rightExp).simplify();
                    }

                    // 4. 乘法，右侧为Binary，右侧的左边为数字，右侧的运算为乘法，就合并，为加减法，就分配
                    else if (rightExp instanceof BinaryExpNode) {
                        BinaryExpNode right = (BinaryExpNode) rightExp;
                        if (right.leftExp instanceof NumberNode || right.leftExp instanceof CharacterNode) {
                            NumberNode temp = new NumberNode(getNumberOrCharacterValue(right.leftExp));
                            if (right.binaryOp.isToken(MULT)) {
                                return new BinaryExpNode(
                                        symbolTable,
                                        new NumberNode(left.getValue() * temp.getValue()),
                                        right.binaryOp,
                                        right.rightExp).simplify();
                            } else if (right.binaryOp.isToken(PLUS) || right.binaryOp.isToken(MINU)) {
                                return new BinaryExpNode(
                                        symbolTable,
                                        new NumberNode(left.getValue() * temp.getValue()),
                                        right.binaryOp,
                                        new BinaryExpNode(symbolTable, left, binaryOp, right.rightExp).simplify()).simplify();
                            }
                        }
                    }
                    return new BinaryExpNode(symbolTable, left, binaryOp, rightExp);
                // 1. / % 当左侧为0就返回0
                case DIV:
                case MOD:
                    return left.getValue() == 0 ? new NumberNode(0) :
                            new BinaryExpNode(symbolTable, left, binaryOp, rightExp);
                // 2. && 当左侧为0就返回0
                case AND:
                    return left.getValue() == 0 ? new NumberNode(0) : rightExp;
                // 3. || 当左侧不为0就返回1
                case OR:
                    return left.getValue() != 0 ? new NumberNode(1) : rightExp;
                default:
                    return new BinaryExpNode(symbolTable, leftExp, binaryOp, rightExp);
            }
        }

        // 2. 右边是数字
        else {
            // 1. 对于数字和字符，都转换为数字
            NumberNode right = new NumberNode(getNumberOrCharacterValue(rightExp));

            // 2. 根据BinaryOp分类
            switch (binaryOp.getToken()) {
                case PLUS:
                    // 1. 加法，右边为0，直接返回左边
                    if (right.getValue() == 0) {
                        return leftExp;
                    } else {
                        return new BinaryExpNode(symbolTable, right, binaryOp, leftExp).simplify();
                    }
                case MINU:
                    // 2. 减法，右边为0，直接返回左边
                    if (right.getValue() == 0) {
                        return leftExp;
                    } else {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-right.getValue()),
                                new Pair(PLUS, 0),
                                leftExp).simplify();
                    }
                case MULT:
                    // 1. 乘法，右边为0，直接返回0
                    if (right.getValue() == 0) {
                        return new NumberNode(0);
                    }

                    // 2. 乘法，右边为1，直接返回左边
                    else if (right.getValue() == 1) {
                        return leftExp;
                    }

                    // 3. 乘法，右边为-1，返回Unary，op为-，exp为左边
                    else if (right.getValue() == -1) {
                        return new UnaryExpNode(symbolTable, new Pair(MINU, 0), leftExp).simplify();
                    } else {
                        return new BinaryExpNode(symbolTable, right, binaryOp, leftExp).simplify();
                    }
                case DIV:
                    // 1. 除法，右边为1，直接返回左边
                    if (right.getValue() == 1) {
                        return leftExp;
                    }

                    // 2. 除法，右边为-1，返回Unary，op为-，exp为左边
                    else if (right.getValue() == -1) {
                        return new UnaryExpNode(symbolTable, new Pair(MINU, 0), leftExp).simplify();
                    } else {
                        return new BinaryExpNode(symbolTable, leftExp, binaryOp, right);
                    }
                case MOD:
                    // 1. 取模，右边为+-1，直接返回0
                    return right.getValue() == 1 || right.getValue() == -1 ? new NumberNode(0) :
                            new BinaryExpNode(symbolTable, leftExp, binaryOp, right);
                //case AND:
                    // 1. &&，右边为0，直接返回0
                    //return right.getValue() == 0 ? new NumberNode(0) : leftExp;
                //case OR:
                    // 2. ||，右边不为0，直接返回1
                    //return right.getValue() != 0 ? new NumberNode(1) : leftExp;
                default:
                    return new BinaryExpNode(symbolTable, leftExp, binaryOp, rightExp);
            }
        }
    }

    public long getNumberOrCharacterValue(ExpNode node){
        if (node instanceof NumberNode) {
            return ((NumberNode) node).getValue();
        } else {
            return ((CharacterNode) node).getValue();
        }
    }

    @Override
    public boolean hasContinue(AssignNode assignNode) {
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 建立Token和UnaryOp之间的映射
        HashMap<Token, BinaryOperate.BinaryOp> map = new HashMap<Token, BinaryOperate.BinaryOp>() {{
            put(PLUS, BinaryOperate.BinaryOp.ADD);
            put(MINU, BinaryOperate.BinaryOp.SUB);
            put(MULT, BinaryOperate.BinaryOp.MUL);
            put(DIV, BinaryOperate.BinaryOp.DIV);
            put(MOD, BinaryOperate.BinaryOp.MOD);
            put(GRE, BinaryOperate.BinaryOp.GT);
            put(GEQ, BinaryOperate.BinaryOp.GE);
            put(LSS, BinaryOperate.BinaryOp.LT);
            put(LEQ, BinaryOperate.BinaryOp.LE);
            put(EQL, BinaryOperate.BinaryOp.EQ);
            put(NEQ, BinaryOperate.BinaryOp.NE);
            put(AND, BinaryOperate.BinaryOp.AND);
            put(OR, BinaryOperate.BinaryOp.OR);
        }};

        // 2. 左右两个expNode生成中间代码
        Value leftValue = leftExp.generateMidCode();
        Value rightValue = rightExp.generateMidCode();
        Value value = (leftValue instanceof Addr || rightValue instanceof Addr) ? new Addr() : new Word();
        new Assign(true, value, new BinaryOperate(map.get(binaryOp.getToken()), leftValue, rightValue));
        return value;
    }
}
