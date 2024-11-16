import java.io.*;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Lexer;
import Parser.*;
import SyntaxTable.SymbolItem;
import SyntaxTable.SymbolTable;
import SyntaxTree.CompUnitNode;
import SyntaxTree.DefNode;
import SyntaxTree.FuncDefNode;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // 1. 源程序文件，解析结果存储在字符串中
        BufferedReader inputFile = new BufferedReader(new FileReader("testfile.txt"));
        BufferedWriter grammarFile = new BufferedWriter(new FileWriter("parser.txt"));
        BufferedWriter symbolTableFile = new BufferedWriter(new FileWriter("symbol.txt"));
        BufferedWriter errorHandlerFile = new BufferedWriter(new FileWriter("error.txt"));

        // 2. 词法分析
        Lexer lexer = new Lexer(inputFile);

        // 3. 语法分析
        Parser parser = new Parser(lexer);
        ParsedUnit compUnit = parser.parseCompUnit();
        grammarFile.write(compUnit.toString());
        grammarFile.close();

        // 4. 语义分析
        CompUnitNode compUnitNode = compUnit.toCompUnitNode();

        // 5. 错误处理
        ErrorHandler.getInstance().print(errorHandlerFile);
    }
}