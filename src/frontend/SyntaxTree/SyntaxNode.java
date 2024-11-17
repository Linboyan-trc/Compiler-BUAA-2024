package frontend.SyntaxTree;

import midend.MidCode.Value;

public interface SyntaxNode {
    ////////////////////////////////////////////////////////////////////////////////
    // 1. 所有节点的接口
    // 1. 所有节点都需要实现simplify()和generateMidCode()两个接口
    SyntaxNode simplify();
    Value generateMidCode();
}
