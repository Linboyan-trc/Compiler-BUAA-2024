package Lexer;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;

import java.io.BufferedReader;
import java.io.IOException;

public class Lexer {
    // 1. 属性
    private BufferedReader br;
    // 2. 当前行，行数，列数，字符，Token
    private String line = null;
    private char ch;
    private int columnNumber = 0;
    private int lineNumber = 1;
    // 3. 注释
    private Boolean isRowAnno = false;
    private Boolean isMultiAnno = false;
    // 4. 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 2. 构造函数
    public Lexer(BufferedReader br) throws IOException {
        this.br = br;
        line = br.readLine();
    }

    ///////////////////////////////////////////////////////////
    // 1. 解析
    public Pair parseAndGetPair() throws IOException {
        // 1. 读到文件结束
        if(line == null) {
            return new Pair(Token.EOF, lineNumber);
        } else {
            // 1. 在本行继续读
            while(columnNumber < line.length()) {
                // 1. 跳过注释
                // 2. 换行
                // 3. 解析: 关键词，数字，其他
                // fix: isRowAnno must be initiated outside of for-loop
                // fix: because if '//' is at the end of row, is won't be set to false
                if (isRowAnno) {
                    nextLine();
                    return parseAndGetPair();
                } else if (isMultiAnno) {
                    int ending = line.indexOf("*/", columnNumber);
                    if (ending == -1) {
                        nextLine();
                        return parseAndGetPair();
                    } else {
                        // fix: after set columnNumber to '/', should continue
                        columnNumber = ending + 2;
                        isMultiAnno = false;
                        continue;
                    }
                }

                // 3. 字母或下划线 + 数字 + " + ' + 其他
                ch = line.charAt(columnNumber);
                if(ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                    columnNumber++;
                    continue;
                }

                if(Character.isAlphabetic(ch) || ch == '_') {
                    return parseIDENFR();
                } else if (Character.isDigit(ch)) {
                    return parseINTCON();
                } else if (ch == '\"') {
                    return parseSTRCON();
                } else if (ch == '\'') {
                    return parseCHRCON();
                } else {
                    return parseOther();
                }
            }
            // 2. 如果本行没有读到有效Pair导致while循环结束，换行
            nextLine();
            // 3. 读下一行
            return parseAndGetPair();
        }
    }

    public Pair parseIDENFR() {
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
        return new Pair(token, string, lineNumber);
    }

    public Pair parseINTCON() {
        // 1. 有两种情况
            // 1. 123
            // 2. 0x123 | OX123
        boolean isHex = false;
        if(ch == '0' && columnNumber <= (line.length()-2)) {
            if (line.charAt(columnNumber + 1) == 'x' || line.charAt(columnNumber + 1) == 'X') {
                isHex = true;
            }
        }

        // 2.1 处理Hexadecimal
        if (isHex) {
            // 2.1.1 获取字符
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(line.charAt(columnNumber));
            columnNumber++;
            stringBuilder.append(line.charAt(columnNumber));
            columnNumber++;
            // 2.1.2 判断digit或alphabetic
            for(;columnNumber < line.length();columnNumber++) {
                ch = line.charAt(columnNumber);
                if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
                    stringBuilder.append(ch);
                } else {
                    break;
                }
            }
            // 2.1.3
            return new Pair(Token.HEXCON, stringBuilder.toString(), lineNumber);
        }
        // 2.2 处理Decimal
        else {
            // 1. 获取初始值
            long value = ch - '0';
            // 2. 继续判断
            columnNumber++;
            for (; columnNumber < line.length(); columnNumber++) {
                ch = line.charAt(columnNumber);
                if (Character.isDigit(ch)) {
                    value = value * 10 + ch - '0';
                } else {
                    break;
                }
            }
            // 3. fix: judge token should be outside of loop
            // 3. fix: if the word is at the end of the line, in loop it will not be added to tokens
            return new Pair(Token.INTCON, value, lineNumber);
        }
    }

    public Pair parseSTRCON() {
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
                columnNumber++;
                break;
            } else {
                stringBuilder.append(ch);
            }
        }
        return new Pair(Token.STRCON, stringBuilder.toString(), lineNumber);
    }

    public Pair parseCHRCON() {
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
        columnNumber++;
        return new Pair(Token.CHRCON, stringBuilder.toString(), lineNumber);
    }

    public Pair parseOther() throws IOException{
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
                ////////////////////////////////////////////////////////////////////////////
                // errors: need to be modified
                if (columnNumber + 1 >= line.length()) {
                    // 1. record the error
                    ErrorRecord errorRecord = new ErrorRecord(lineNumber, 'a');
                    errorHandler.addError(errorRecord);
                    // 2. treat is as "||" or "&&", but record word as "|" or "&"
                    columnNumber++;
                    if(ch == '|') {
                        return new Pair(Token.OR, stringBuilder.toString(), lineNumber);
                    } else {
                        return new Pair(Token.AND, stringBuilder.toString(), lineNumber);
                    }
                } else if (line.charAt(columnNumber + 1) != ch) {
                    // 1. record the error
                    ErrorRecord errorRecord = new ErrorRecord(lineNumber, 'a');
                    errorHandler.addError(errorRecord);
                    // 2. treat is as "||" or "&&", but record word as "|" or "&"
                    columnNumber++;
                    if(ch == '|') {
                        return new Pair(Token.OR, stringBuilder.toString(), lineNumber);
                    } else {
                        return new Pair(Token.AND, stringBuilder.toString(), lineNumber);
                    }
                }
                ////////////////////////////////////////////////////////////////////////////
                // 1.更新ch和stringBuilder
                columnNumber++;
                ch = line.charAt(columnNumber);
                stringBuilder.append(ch);
                // 2. 获取Token加入tokens
                token = Category.getInstance().getTokenType(stringBuilder.toString());
                columnNumber++;
                return new Pair(token, stringBuilder.toString(), lineNumber);
            case '!':
            case '<':
            case '>':
            case '=':
                // 1.更新ch和stringBuilder
                if (columnNumber + 1 >= line.length()) {
                    token = Category.getInstance().getTokenType(stringBuilder.toString());
                    columnNumber++;
                    return new Pair(token, stringBuilder.toString(), lineNumber);
                } else if (line.charAt(columnNumber + 1) == '=') {
                    columnNumber++;
                    ch = line.charAt(columnNumber);
                    stringBuilder.append(ch);
                }
                // 2. 获取Token加入tokens
                token = Category.getInstance().getTokenType(stringBuilder.toString());
                columnNumber++;
                return new Pair(token, stringBuilder.toString(), lineNumber);
            case '/':
                // fix: if '/' is last character
                if (columnNumber + 1 == line.length()) {
                    // 1. 获取Token加入tokens
                    token = Category.getInstance().getTokenType(stringBuilder.toString());
                    columnNumber++;
                    return new Pair(token, stringBuilder.toString(), lineNumber);
                }
                //
                else if (line.charAt(columnNumber + 1) == '/') {
                    // 1.更新ch和stringBuilder
                    columnNumber++;
                    ch = line.charAt(columnNumber);
                    stringBuilder.append(ch);
                    // 2. isRowAnno
                    isRowAnno = true;
                    // 3. parse next line
                    nextLine();
                    return parseAndGetPair();
                } else if (line.charAt(columnNumber + 1) == '*') {
                    // 1.更新ch和stringBuilder
                    columnNumber++;
                    ch = line.charAt(columnNumber);
                    stringBuilder.append(ch);
                    // 2. isMultiAnno
                    isMultiAnno = true;
                    // 3. parse next line
                    columnNumber++;
                    return parseAndGetPair();
                } else {
                    // 1. 获取Token加入tokens
                    token = Category.getInstance().getTokenType(stringBuilder.toString());
                    columnNumber++;
                    return new Pair(token, stringBuilder.toString(), lineNumber);
                }
            default:
                // 1. 获取Token加入tokens
                token = Category.getInstance().getTokenType(stringBuilder.toString());
                columnNumber++;
                return new Pair(token, stringBuilder.toString(), lineNumber);
        }
    }

    ///////////////////////////////////////////////////////////
    // 3. 读下一行
    public void nextLine() throws IOException {
        isRowAnno = false;
        line = br.readLine();
        lineNumber++;
        columnNumber = 0;
    }
}
