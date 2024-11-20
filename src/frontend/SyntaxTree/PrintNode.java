package frontend.SyntaxTree;

import frontend.ErrorHandler.ErrorHandler;
import frontend.ErrorHandler.ErrorRecord;
import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import midend.MidCode.Value.Value;
import midend.MidCode.MidCode.*;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class PrintNode implements StmtNode {
    // 1. <StringConst> + <Exp>
    private final SymbolTable symbolTable;
    private Pair string;
    private LinkedList<ExpNode> arguments = new LinkedList<>();
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2.
    public PrintNode(SymbolTable symbolTable, Pair string, LinkedList<ExpNode> arguments) {
        this.symbolTable = symbolTable;
        this.string = string;
        this.arguments = arguments;
    }

    // 3. 检查
    // 3. printf的<StringConst>中的格式字符于表达式个数不匹配，l类错误
    public void checkForError() {
        // 1. 获取<StringConst>中的格式字符个数
        int i = 0, cnt = 0;
        String[] findStrings = {"%d", "%c"};
        while(i < string.getWord().length()) {
            // 1.1 找到两者的字串位置
            int nextIndexForD = string.getWord().indexOf(findStrings[0],i);
            int nextIndexForC = string.getWord().indexOf(findStrings[1],i);
            // 1.2 比较得到更小位置的字串
            int nextIndex = -1;
            if (nextIndexForD != -1 && (nextIndexForC == -1 || nextIndexForD < nextIndexForC)) {
                nextIndex = nextIndexForD;
            } else if (nextIndexForC != -1) {
                nextIndex = nextIndexForC;
            }
            // 1.3 如果都为-1，说明格式字符串数量为0
            if (nextIndex == -1) {
                break;
            }
            // 1.4 更新i和cnt
            i = nextIndex + 2;
            cnt++;
        }
        // 2. 判断格式字符数量是否和表达式数量相等
        if(cnt != arguments.size()) {
            errorHandler.addError(new ErrorRecord(string.getLineNumber(), 'l'));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public PrintNode simplify() {
        LinkedList<ExpNode> newArguments = new LinkedList<>();
        for(ExpNode arg : arguments) {
            newArguments.add(arg.simplify());
        }
        arguments = newArguments;
        return this;
    }

    @Override
    public Value generateMidCode() {
        LinkedList<midend.MidCode.Value.Value> values = new LinkedList<>();
        for(ExpNode arg : arguments) {
            values.add(arg.generateMidCode());
        }
        values.forEach(ArgPush::new);
        new Print(string.getWord());
        return null;
    }
}
