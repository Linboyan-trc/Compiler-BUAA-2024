package frontend.SyntaxTree.ExpNode;

import frontend.Lexer.Pair;
import frontend.Lexer.Token;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.StmtNode.*;
import midend.MidCode.MidCode.Assign;
import midend.MidCode.Operate.UnaryOperate;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.Word;

import java.util.HashMap;

public class UnaryExpNode implements ExpNode {
    // 1. <UnaryOP> + <UnaryExp>
    private final SymbolTable symbolTable;
    private Pair unaryOp;
    private ExpNode expNode;

    // 2.
    public UnaryExpNode(SymbolTable symbolTable, Pair unaryOp, ExpNode expNode) {
        this.symbolTable = symbolTable;
        this.unaryOp = unaryOp;
        this.expNode = expNode;
    }

    // 1. 获取SyntaxType
    @Override
    public SyntaxType getSyntaxType(SymbolTable symbolTable) {
        // 1. 节点自带类型
        return expNode.getSyntaxType(symbolTable);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public ExpNode simplify() {
        // 1. 化简后的节点
        ExpNode simExp = expNode.simplify();

        // 2. 节点化简后是数字，就结合符号直接返回一个数字
        if (simExp instanceof NumberNode) {
            return operateNumber(unaryOp, (NumberNode) simExp);
        } else if (simExp instanceof CharacterNode) {
            return operateCharacter(unaryOp, (CharacterNode) simExp);
        }

        // 3. 如果符号是加号，无论节点是什么都直接返回
        else if (unaryOp.isToken(PLUS)) {
            return simExp;
        }

        // 4. 节点是UnaryExp，说明这是和自己递归了
        // 4. 提取出下一个Unary节点，然后只对--和!!两种情况化简
        else if (simExp instanceof UnaryExpNode) {
            UnaryExpNode simUnary = (UnaryExpNode) simExp;
            if (simUnary.unaryOp.isToken(MINU) && unaryOp.isToken(MINU)) {
                return simUnary.expNode;
            } else if (simUnary.unaryOp.isToken(NOT) && unaryOp.isToken(NOT)) {
                return simUnary.expNode;
            }
        }

        // 5. 节点是BinaryExp，就是递归的表达式中的一种
        else if (simExp instanceof BinaryExpNode) {
            // 5.1 如果此符号是-，节点左侧是数字
            BinaryExpNode simBinary = (BinaryExpNode) simExp;
            if (unaryOp.isToken(MINU)) {
                if (simBinary.getLeftExp() instanceof NumberNode) {
                    // 5.1 如果Binary是+号，就对Binary变号，数字取反，符号取反，返回新的BinaryNode替代此UnaryNode
                    NumberNode left = (NumberNode) simBinary.getLeftExp();
                    if (simBinary.getBinaryOp().isToken(PLUS)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-left.getValue()),
                                new Pair(MINU, 0),
                                simBinary.getRightExp());
                    }
                    // 5.2 如果Binary是+号，就对Binary变号，数字取反，符号取反，返回新的BinaryNode替代此UnaryNode
                    else if (simBinary.getBinaryOp().isToken(MINU)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-left.getValue()),
                                new Pair(PLUS, 0),
                                simBinary.getRightExp());
                    }
                    // 5.3 如果Binary是*号，就对Binary变号，数字取反，返回新的BinaryNode替代此UnaryNode
                    else if (simBinary.getBinaryOp().isToken(MULT)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-left.getValue()),
                                new Pair(MULT, 0),
                                simBinary.getRightExp());
                    }
                } else if (simBinary.getLeftExp() instanceof CharacterNode) {
                    // 5.1 如果Binary是+号，就对Binary变号，数字取反，符号取反，返回新的BinaryNode替代此UnaryNode
                    CharacterNode left = (CharacterNode) simBinary.getLeftExp();
                    if (simBinary.getBinaryOp().isToken(PLUS)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-left.getValue()),
                                new Pair(MINU, 0),
                                simBinary.getRightExp());
                    }
                    // 5.2 如果Binary是+号，就对Binary变号，数字取反，符号取反，返回新的BinaryNode替代此UnaryNode
                    else if (simBinary.getBinaryOp().isToken(MINU)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-left.getValue()),
                                new Pair(PLUS, 0),
                                simBinary.getRightExp());
                    }
                    // 5.3 如果Binary是*号，就对Binary变号，数字取反，返回新的BinaryNode替代此UnaryNode
                    else if (simBinary.getBinaryOp().isToken(MULT)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new NumberNode(-left.getValue()),
                                new Pair(MULT, 0),
                                simBinary.getRightExp());
                    }
                }
            }

            // 5.2 如果此符号是!，说明必定是条件表达式，对条件表达式化简，返回新的BinaryNode
            else if (unaryOp.isToken(NOT) && (simBinary.getBinaryOp().isToken(AND) || simBinary.getBinaryOp().isToken(OR))) {
                // 5.3 对Binary两侧化简
                ExpNode left = new UnaryExpNode(symbolTable, unaryOp, simBinary.getLeftExp()).simplify();
                ExpNode right = new UnaryExpNode(symbolTable, unaryOp, simBinary.getRightExp()).simplify();
                return new BinaryExpNode(
                        symbolTable,
                        left,
                        simBinary.getBinaryOp().isToken(AND) ? new Pair(OR, 0) : new Pair(AND, 0),
                        right);
            }
        }
        return new UnaryExpNode(symbolTable, unaryOp, simExp);
    }

    public NumberNode operateNumber(Pair unaryOp, NumberNode expNode) {
        switch (unaryOp.getToken()) {
            case PLUS:
                return new NumberNode(expNode.getValue());
            case MINU:
                return new NumberNode(-expNode.getValue());
            case NOT:
                return new NumberNode(expNode.getValue() == 0 ? 1 : 0);
            default:
                return null;
        }
    }

    public NumberNode operateCharacter(Pair unaryOp, CharacterNode expNode) {
        switch (unaryOp.getToken()) {
            case PLUS:
                return new NumberNode(expNode.getValue());
            case MINU:
                return new NumberNode(-expNode.getValue());
            case NOT:
                return new NumberNode(expNode.getValue() == 0 ? 1 : 0);
            default:
                return null;
        }
    }

    @Override
    public boolean hasContinue(AssignNode assignNode) {
        return false;
    }

    @Override
    public Value generateMidCode() {
        // 1. 建立Token和UnaryOp之间的映射
        HashMap<Token, UnaryOperate.UnaryOp> map = new HashMap<Token, UnaryOperate.UnaryOp>() {{
            put(PLUS, UnaryOperate.UnaryOp.POS);
            put(MINU, UnaryOperate.UnaryOp.NEG);
            put(NOT, UnaryOperate.UnaryOp.NOT);
        }};

        // 2.expNode生成中间代码
        Value expValue = expNode.generateMidCode();

        // 3. 作为一个Word
        Word value = new Word();
        new Assign(true, value, new UnaryOperate(map.get(unaryOp.getToken()), expValue));

        // 4. 返回一个value
        return value;
    }
}
