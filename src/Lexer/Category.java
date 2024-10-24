package Lexer;

import java.util.HashMap;

public class Category {
    // 1. 属性
    private static Category instance;
    private HashMap<String, Token> strTokenTable = new HashMap<String, Token>();

    // 2. 构造函数
    private Category() {}

    public static Category getInstance() {
        if(instance == null) {
            instance = new Category();
            instance.fillInStrTokenTable();
        }
        return instance;
    }

    private void fillInStrTokenTable() {
        strTokenTable.put("main", Token.MAINTK);
        strTokenTable.put("void", Token.VOIDTK);
        strTokenTable.put("const", Token.CONSTTK);
        strTokenTable.put("int", Token.INTTK);
        strTokenTable.put("char", Token.CHARTK);
        strTokenTable.put("break", Token.BREAKTK);
        strTokenTable.put("continue", Token.CONTINUETK);
        strTokenTable.put("if", Token.IFTK);
        strTokenTable.put("else", Token.ELSETK);
        strTokenTable.put("for", Token.FORTK);
        strTokenTable.put("repeat", Token.REPEATTK);
        strTokenTable.put("until",Token.UNTILTK);
        strTokenTable.put("return", Token.RETURNTK);
        strTokenTable.put("printf", Token.PRINTFTK);
        strTokenTable.put("getint", Token.GETINTTK);
        strTokenTable.put("getchar", Token.GETCHARTK);
        strTokenTable.put("!", Token.NOT);
        strTokenTable.put("&&", Token.AND);
        strTokenTable.put("||", Token.OR);
        strTokenTable.put("<=", Token.LEQ);
        strTokenTable.put(">=", Token.GEQ);
        strTokenTable.put("==", Token.EQL);
        strTokenTable.put("!=", Token.NEQ);
        strTokenTable.put("+", Token.PLUS);
        strTokenTable.put("-", Token.MINU);
        strTokenTable.put("*", Token.MULT);
        strTokenTable.put("/", Token.DIV);
        strTokenTable.put("%", Token.MOD);
        strTokenTable.put("<", Token.LSS);
        strTokenTable.put(">", Token.GRE);
        strTokenTable.put("=", Token.ASSIGN);
        strTokenTable.put(";", Token.SEMICN);
        strTokenTable.put(",", Token.COMMA);
        strTokenTable.put("(", Token.LPARENT);
        strTokenTable.put(")", Token.RPARENT);
        strTokenTable.put("[", Token.LBRACK);
        strTokenTable.put("]", Token.RBRACK);
        strTokenTable.put("{", Token.LBRACE);
        strTokenTable.put("}", Token.RBRACE);
    }

    // 1. 获取Token类型
    public Token getTokenType(String str) {
        return strTokenTable.getOrDefault(str, Token.IDENFR);
    }
}
