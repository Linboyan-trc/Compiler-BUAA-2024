package frontend.Lexer;

public enum Token {
    IDENFR,
    INTCON, STRCON, CHRCON,
    MAINTK, VOIDTK, CONSTTK,
    INTTK, CHARTK,
    BREAKTK, CONTINUETK, IFTK, ELSETK, FORTK, RETURNTK,
    PRINTFTK, GETINTTK, GETCHARTK,
    NOT, AND, OR,
    PLUS, MINU, MULT, DIV, MOD,
    LSS, LEQ, GRE, GEQ, EQL, NEQ, ASSIGN,
    SEMICN, COMMA,
    LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE,
    EOF;

    public String toString() {
        return name();
    }
}