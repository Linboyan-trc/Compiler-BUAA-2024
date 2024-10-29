package SyntaxTree;

import Lexer.Pair;

public class LValNode implements ExpNode {
    // 1. Pair + 维数
    private Pair pair;
    private ExpNode length = null;

    // 2. 构造 + 设置pair + 设置length
    public LValNode() { }

    public void setPair(Pair pair) {
        this.pair = pair;
    }

    public void setLength(ExpNode length) {
        this.length = length;
    }

    // 3. 检查
    // TODO: 错误处理
}
