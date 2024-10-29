package SyntaxTree;

import java.util.LinkedList;

public class PrintNode implements StmtNode {
    // 1. <StringConst> + <Exp>
    private String string;
    private LinkedList<ExpNode> arguments = new LinkedList<>();

    // 2.
    public PrintNode() { }

    public void setString(String string) {
        this.string = string;
    }

    public void addArgument(ExpNode argument) {
        this.arguments.add(argument);
    }

    // 3. 检查
    // TODO: 错误处理
}
