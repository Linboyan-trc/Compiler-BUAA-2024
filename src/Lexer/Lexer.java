package Lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    // 1. 属性
    private static Lexer instance;
    private List<Pair> tokens = new ArrayList<>();
    private String line = null;
    private int columnNumber;
    private int lineNumber;
    private char ch;
    private Boolean isRowAnno = false;
    private Boolean isMultiAnno = false;

    // erros
    private List<String> errors = new ArrayList<>();

    // 2. 构造函数
    private Lexer() {}

    public static Lexer getInstance() {
        if (instance == null) {
            instance = new Lexer();
        }
        return instance;
    }

    ///////////////////////////////////////////////////////////
    // 1. 解析
    public void parse(BufferedReader br) throws IOException {
        lineNumber = 1;
        while((line = br.readLine()) != null) {
            // 1. 跳过注释
            // 2. 换行
            // 3. 解析: 关键词，数字，其他
            // fix: isRowAnno must be initiated outside of for-loop
            // fix: because if '//' is at the end of row, is won't be set to false
            isRowAnno = false;
            for(columnNumber = 0;columnNumber < line.length();columnNumber++) {
                if (isRowAnno) {
                    break;
                } else if (isMultiAnno) {
                    int ending = line.indexOf("*/", columnNumber);
                    if (ending == -1) {
                        break;
                    } else {
                        // fix: after set columnNumber to '/', should continue
                        columnNumber = ending + 1;
                        isMultiAnno = false;
                        continue;
                    }
                }
                // 3. 字母或下划线 + 数字 + " + ' + 其他
                ch = line.charAt(columnNumber);
                if(ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                    continue;
                }

                if(Character.isAlphabetic(ch) || ch == '_') {
                    parseIDENFR();
                } else if (Character.isDigit(ch)) {
                    parseINTCON();
                } else if (ch == '\"') {
                    parseSTRCON();
                } else if (ch == '\'') {
                    parseCHRCON();
                } else {
                    parseOther();
                }
            }
            lineNumber++;
        }
    }

    public void parseIDENFR() {
        // 1. 获取开头字符
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        // 2. 继续判断下一个字符
        columnNumber++;
        for(;columnNumber < line.length();columnNumber++) {
            ch = line.charAt(columnNumber);
            if (Character.isAlphabetic(ch) || ch == '_' || Character.isDigit(ch)) {
                stringBuilder.append(ch);
            } else {
                // 3. 拿到单词和单词类型
                break;
            }
        }
        // 3. fix: judge token should be outside of loop
        // 3. fix: if the word is at the end of the line, in loop it will not be added to tokens
        String string = stringBuilder.toString();
        Token token = Category.getInstance().getTokenType(string);
        tokens.add(new Pair(token, string, lineNumber));
        columnNumber--;
    }

    public void parseINTCON() {
        // 1. 获取初始值
        long value = ch - '0';
        // 2. 继续判断
        columnNumber++;
        for(;columnNumber < line.length();columnNumber++) {
            ch = line.charAt(columnNumber);
            if (Character.isDigit(ch)) {
                value = value * 10 + ch - '0';
            } else {
                break;
            }
        }
        // 3. fix: judge token should be outside of loop
        // 3. fix: if the word is at the end of the line, in loop it will not be added to tokens
        tokens.add(new Pair(Token.INTCON, value, lineNumber));
        columnNumber--;
    }

    public void parseSTRCON() {
        // 1. 获取初始值
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        // 2. 继续读取
        columnNumber++;
        for(;columnNumber < line.length();columnNumber++) {
            // 3. 如果是"就结束，加入tokens
            ch = line.charAt(columnNumber);
            if (ch == '\"') {
                 stringBuilder.append(ch);
                tokens.add(new Pair(Token.STRCON, stringBuilder.toString(), lineNumber));
                break;
            } else {
                stringBuilder.append(ch);
            }
        }
    }

    public void parseCHRCON() {
        // 1. 获取字符
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        // 2. 找到字符
        // fix: character can be 'c', alse can be '\0'
        columnNumber++;
        ch = line.charAt(columnNumber);
        stringBuilder.append(ch);
        if (ch == '\\') {
            columnNumber++;
            ch = line.charAt(columnNumber);
            stringBuilder.append(ch);
        }
        // 3. 找到\'
        columnNumber++;
        ch = line.charAt(columnNumber);
        stringBuilder.append(ch);
        // 4. 加入tokens
        tokens.add(new Pair(Token.CHRCON, stringBuilder.toString(), lineNumber));
    }

    public void parseOther() {
        // 1. 获取字符
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ch);
        Token token;
        // 2. 分类处理
        // 2.1 && ||
        // 2.2 ! != < <= > >= = ==
        // 2.3 / // /*
        // 2.4 + - * % ; , ( ) [ ] { }
        switch (ch) {
            case '&':
            case '|':
                // errors
                if (columnNumber + 1 >= line.length()) {
                    errors.add(lineNumber + " " + "a");
                    break;
                } else if (line.charAt(columnNumber + 1) != ch) {
                    errors.add(lineNumber + " " + "a");
                    break;
                }
                // 1.更新ch和stringBuilder
                columnNumber++;
                ch = line.charAt(columnNumber);
                stringBuilder.append(ch);
                // 2. 获取Token加入tokens
                token = Category.getInstance().getTokenType(stringBuilder.toString());
                tokens.add(new Pair(token, stringBuilder.toString(), lineNumber));
                // 3. break
                break;
            case '!':
            case '<':
            case '>':
            case '=':
                // 1.更新ch和stringBuilder
                if (columnNumber + 1 >= line.length()) {
                    token = Category.getInstance().getTokenType(stringBuilder.toString());
                    tokens.add(new Pair(token, stringBuilder.toString(), lineNumber));
                    break;
                } else if (line.charAt(columnNumber + 1) == '=') {
                    columnNumber++;
                    ch = line.charAt(columnNumber);
                    stringBuilder.append(ch);
                }
                // 2. 获取Token加入tokens
                token = Category.getInstance().getTokenType(stringBuilder.toString());
                tokens.add(new Pair(token, stringBuilder.toString(), lineNumber));
                // 3. break
                break;
            case '/':
                // fix: if '/' is last character
                if (columnNumber + 1 == line.length()) {
                    // 1. 获取Token加入tokens
                    token = Category.getInstance().getTokenType(stringBuilder.toString());
                    tokens.add(new Pair(token, stringBuilder.toString(), lineNumber));
                    // 2. break
                    break;
                }
                //
                else if (line.charAt(columnNumber + 1) == '/') {
                    // 1.更新ch和stringBuilder
                    columnNumber++;
                    ch = line.charAt(columnNumber);
                    stringBuilder.append(ch);
                    // 2. isRowAnno
                    isRowAnno = true;
                    // 3. break
                    break;
                } else if (line.charAt(columnNumber + 1) == '*') {
                    // 1.更新ch和stringBuilder
                    columnNumber++;
                    ch = line.charAt(columnNumber);
                    stringBuilder.append(ch);
                    // 2. isMultiAnno
                    isMultiAnno = true;
                    // 3. break
                    break;
                } else {
                    // 1. 获取Token加入tokens
                    token = Category.getInstance().getTokenType(stringBuilder.toString());
                    tokens.add(new Pair(token, stringBuilder.toString(), lineNumber));
                    // 2. break
                    break;
                }
            default:
                // 1. 获取Token加入tokens
                token = Category.getInstance().getTokenType(stringBuilder.toString());
                tokens.add(new Pair(token, stringBuilder.toString(), lineNumber));
                // 2. break
                break;
        }
    }

    ///////////////////////////////////////////////////////////
    // 2. 获取tokens
    public List<Pair> getTokens() {
        return tokens;
    }

    // erros
    public List<String> getErrors() {
        return errors;
    }
}
