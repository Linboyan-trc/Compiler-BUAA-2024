package Lexer;

public class Pair {
    // 1. Pair: 类别码 + word/value/charValue + 行号
    private Token token; // 类别码
    private String word; // 单词
    private int value; // 只对INTCON
    private int lineNumber;

    // 1. 三类构造:String, int
    public Pair(Token token, String word, int lineNumber) {
        this.token = token;
        this.word = word;
        this.value = 0;
        this.lineNumber = lineNumber;
    }

    public Pair(Token token, int value, int lineNumber) {
        this.token = token;
        this.word = null;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    // 2. 工具函数: 获取TOKEN类型， 输出成字符串
    public Token getToken() {
        return token;
    }

    public String getWord() {
        return word;
    }

    public int getValue() {
        return value;
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
