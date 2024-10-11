import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Lexer;
import Parser.Parser;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // 1. 源程序文件，解析结果存储在字符串中
        String inputFile = "src/testfile.txt";

        // 2. Lexer解析源文件，结果写入字符串中
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        Lexer lexer = new Lexer(br);

        // 3. 打印结果
        String outputFile = "src/parser.txt";
        FileWriter fwOut = new FileWriter(outputFile);
        Parser parser = new Parser(lexer,fwOut);
        parser.parseCompUnit();
        fwOut.close();

        // 4. errors
        String errFile = "src/error.txt";
        FileWriter fwErr = new FileWriter(errFile);
        for (ErrorRecord errorRecord : ErrorHandler.getInstance().getErrors()) {
            fwErr.write(errorRecord + "\n");
        }
        fwErr.close();
    }
}