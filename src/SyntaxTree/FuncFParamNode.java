package SyntaxTree;

import Lexer.Pair;
import Lexer.Token;
import SyntaxTable.SyntaxType;

public class FuncFParamNode extends DefNode {
    public FuncFParamNode(SyntaxType defNodeType,Pair pair) {
        super(defNodeType, pair);
    }
}
