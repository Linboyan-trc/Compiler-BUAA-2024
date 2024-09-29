import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import Lexer.Lexer;
import Lexer.Pair;

public class Compiler {
    public static void main(String[] args) {
        // 1. 源程序文件，解析结果存储在字符串中
        String inputFile = "src/testfile.txt";

        // 2. Lexer解析源文件，结果写入字符串中
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            Lexer.getInstance().parse(br);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. 打印结果
        String outputFile = "src/lexer.txt";
        try (FileWriter fw = new FileWriter(outputFile)) {
            for (Pair pair : Lexer.getInstance().getTokens()) {
                fw.write(pair.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}