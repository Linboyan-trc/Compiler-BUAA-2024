package SyntaxTree;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Pair;

import java.util.LinkedList;

public class PrintNode implements StmtNode {
    // 1. <StringConst> + <Exp>
    private Pair string;
    private LinkedList<ExpNode> arguments = new LinkedList<>();
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2.
    public PrintNode() { }

    public void setString(Pair string) {
        this.string = string;
    }

    public void addArgument(ExpNode argument) {
        this.arguments.add(argument);
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
}
