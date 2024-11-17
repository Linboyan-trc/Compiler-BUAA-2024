package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.Lexer.Token;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;

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
