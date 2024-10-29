package SyntaxTree;

import Lexer.Pair;

import java.util.LinkedList;

public class FuncCallNode implements ExpNode {
    // 1. 函数名 + 参数
    private Pair pair;
    private LinkedList<ExpNode> arguments = new LinkedList<>();

    // 2. 构造 + 设置参数
    public FuncCallNode(Pair pair) {
        this.pair = pair;
    }

    public void setArgs(LinkedList<ExpNode> arguments) {
        this.arguments = arguments;
    }

    // 3. 检查
    // TODO: 错误处理
}
