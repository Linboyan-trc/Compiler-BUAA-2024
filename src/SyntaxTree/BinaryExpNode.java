package SyntaxTree;

import Lexer.Pair;
import Lexer.Token;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;

public class BinaryExpNode implements ExpNode {
    // 1. 就是()*(), ()+()
    private ExpNode leftExp;
    private Pair binaryOp;
    private ExpNode rightExp;

    // 2.
    public BinaryExpNode() { }

    public void setBinaryOp(Pair binaryOp) {
        this.binaryOp = binaryOp;
    }

    public void setLeftExp(ExpNode expNode) {
        this.leftExp = expNode;
    }

    public void setRightExp(ExpNode expNode) {
        this.rightExp = expNode;
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
}
