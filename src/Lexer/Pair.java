package Lexer;

import Parser.ParsedUnit;

import java.util.Collections;

public class Pair extends ParsedUnit {
    // 1. Pair: 类别码 + word/value/charValue + 行号
    // fix: value must be unsigned int
    // fix: but in java there is no unsigned int, so we use long to store the unsigned int value
    private Token token; // 类别码
    private String word; // 单词
    private long value; // 只对INTCON
    private int lineNumber;

    // 1. 三类构造:String, int, EOF
    public Pair(Token token, String word, int lineNumber) {
        super(token.toString(), Collections.emptyList());
        this.token = token;
        this.word = word;
        this.value = 0;
        this.lineNumber = lineNumber;
    }

    public Pair(Token token, long value, int lineNumber) {
        super(token.toString(), Collections.emptyList());
        this.token = token;
        this.word = null;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public Pair(Token token, int lineNumber) {
        super(token.toString(), Collections.emptyList());
        this.token = token;
        this.word = null;
        this.value = 0;
        this.lineNumber = lineNumber;
    }

    // 2. 工具函数: 获取TOKEN类型， 输出成字符串
    public Token getToken() {
        return token;
    }

    public String getWord() {
        return word;
    }

    public long getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        if (token == Token.INTCON) {
            return token + " " + value;
        } else {
            return token + " " + word;
        }
    }
}
