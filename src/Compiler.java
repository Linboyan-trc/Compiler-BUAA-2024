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

        // 2. Lexer解析源文件，结果写入字符串中
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        Lexer lexer = new Lexer(br);

        // 3. 打印结果
        String outputFile = "parser.txt";
        FileWriter fwOut = new FileWriter(outputFile);
        Parser parser = new Parser(lexer,fwOut);
        parser.parseCompUnit();
        fwOut.close();

        // 4. errors
        String errFile = "error.txt";
        FileWriter fwErr = new FileWriter(errFile);
        ErrorHandler.getInstance().sortErrorsByLineNumber();
        for (ErrorRecord errorRecord : ErrorHandler.getInstance().getErrors()) {
            fwErr.write(errorRecord + "\n");
        }
        fwErr.close();

        // 5. symbol
        String symFile = "symbol.txt";
        FileWriter fwSym = new FileWriter(symFile);
        // 5.1 全局
        int scope = 1;
        SymbolTable symbolTable = parser.getSymbolTable();
        for(SymbolItem<DefNode> item:symbolTable.getVariables()){
            fwSym.write(scope + " " + item.getName() + " " + item.getNode().getDefNodeType().toString() + "\n");
        }
        for(SymbolItem<FuncDefNode> item:symbolTable.getFunctions()){
            fwSym.write(scope + " " + item.getName() + " " + item.getNode().getFuncDefType().toString() + "\n");
        }
        // 5.2 逐个
        for(SymbolTable child:symbolTable.getChildren()){
            scope++;
            scope = child.print(fwSym,scope);
        }
        fwSym.close();
    }
}