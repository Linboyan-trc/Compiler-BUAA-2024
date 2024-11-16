import java.io.*;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Lexer;
import Parser.Parser;
import SyntaxTable.SymbolItem;
import SyntaxTable.SymbolTable;
import SyntaxTree.DefNode;
import SyntaxTree.FuncDefNode;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // 1. 源程序文件，解析结果存储在字符串中
        String inputFile = "testfile.txt";
        String grammarAnalysisFile = "parser.txt";
        String symbolTableFile = "symbol.txt";
        String errorHandlerFile = "error.txt";
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        FileWriter fwGrammar = new FileWriter(grammarAnalysisFile);
        FileWriter fwSymbol = new FileWriter(symbolTableFile);
        FileWriter fwErr = new FileWriter(errorHandlerFile);

        // 2. 词法分析
        Lexer lexer = new Lexer(br);

        // 3. 语法分析
        Parser parser = new Parser(lexer,fwGrammar);
        parser.parseCompUnit();
        fwGrammar.close();

        // 4. 语义分析
        // 4.1 全局
        int scope = 1;
        SymbolTable symbolTable = parser.getSymbolTable();
        for(SymbolItem<DefNode> item:symbolTable.getVariables()){
            fwSymbol.write(scope + " " + item.getName() + " " + item.getNode().getDefNodeType().toString() + "\n");
        }
        for(SymbolItem<FuncDefNode> item:symbolTable.getFunctions()){
            fwSymbol.write(scope + " " + item.getName() + " " + item.getNode().getFuncDefType().toString() + "\n");
        }
        // 4.2 逐个
        for(SymbolTable child:symbolTable.getChildren()){
            scope++;
            scope = child.print(fwSymbol,scope);
        }
        fwSymbol.close();

        // 5. 错误处理
        ErrorHandler.getInstance().sortErrorsByLineNumber();
        for (ErrorRecord errorRecord : ErrorHandler.getInstance().getErrors()) {
            fwErr.write(errorRecord + "\n");
        }
        fwErr.close();
    }
}