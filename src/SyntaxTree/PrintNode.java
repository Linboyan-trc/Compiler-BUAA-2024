package SyntaxTree;

import Lexer.Pair;

import java.util.LinkedList;

public class PrintNode implements StmtNode {
    // 1. <StringConst> + <Exp>
    private Pair pair;
    private LinkedList<ExpNode> arguments = new LinkedList<>();

    // 2.
    public PrintNode() { }

    public void setPair(Pair pair) {
        this.pair = pair;
    }

    public void addArgument(ExpNode argument) {
        this.arguments.add(argument);
    }

    // 3. 检查
    // TODO: 错误处理
}
