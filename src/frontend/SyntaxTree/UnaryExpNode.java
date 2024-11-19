package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.Lexer.Token;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

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
                                new CharacterNode(-left.getValue()),
                                new Pair(MINU, 0),
                                simBinary.getRightExp());
                    }
                    // 5.2 如果Binary是+号，就对Binary变号，数字取反，符号取反，返回新的BinaryNode替代此UnaryNode
                    else if (simBinary.getBinaryOp().isToken(MINU)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new CharacterNode(-left.getValue()),
                                new Pair(PLUS, 0),
                                simBinary.getRightExp());
                    }
                    // 5.3 如果Binary是*号，就对Binary变号，数字取反，返回新的BinaryNode替代此UnaryNode
                    else if (simBinary.getBinaryOp().isToken(MULT)) {
                        return new BinaryExpNode(
                                symbolTable,
                                new CharacterNode(-left.getValue()),
                                new Pair(MULT, 0),
                                simBinary.getRightExp());
                    }
                }
            }

            // 5.2 如果此符号是!，说明必定是条件表达式，对条件表达式化简，返回新的BinaryNode
            else if (unaryOp.isToken(NOT)) {
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

    public CharacterNode operateCharacter(Pair unaryOp, CharacterNode expNode) {
        switch (unaryOp.getToken()) {
            case PLUS:
                return new CharacterNode(expNode.getValue());
            case MINU:
                return new CharacterNode(-expNode.getValue());
            case NOT:
                return new CharacterNode(expNode.getValue() == 0 ? 1 : 0);
            default:
                return null;
        }
    }
}
