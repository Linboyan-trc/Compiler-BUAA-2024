package Parser;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Lexer;
import Lexer.Pair;
import Lexer.Token;
import SyntaxTable.SymbolTable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    // 1. lexer + token:指针, 当前token, token表
    private Lexer lexer;
    private Pair pair;
    private Token token;
    private int tokenIndex = -1;
    private List<Pair> tokens = new ArrayList<>();
    // 2. 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getInstance();
    // 3. 语义分析
    private SymbolTable symbolTable  = new SymbolTable(null,1);

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 1. 第一种getToken用于简单获取下一个
    // 2. 第二种getToken用于错误处理却少情况:i, j, k
    public void getToken() throws IOException {
        // 1.1 当用到最新token的时候才需要读取新的token
        tokenIndex++;
        if (tokens.size() == tokenIndex) {
            tokens.add(lexer.parseAndGetPair());
        }
        // 1.2 没有用到最新token从当前token拿就可以
        pair = tokens.get(tokenIndex);
        token = tokens.get(tokenIndex).getToken();
    }

    public boolean getToken(Token tokenExpected) throws IOException {
        // 1.1 当用到最新token的时候才需要读取新的token
        tokenIndex++;
        if (tokens.size() == tokenIndex) {
            tokens.add(lexer.parseAndGetPair());
        }
        // 1.2 没有用到最新token从当前token拿就可以
        // 1.2.1 如果符合期望就正常读
        // 1.2.2 如果不符合期望就取消这次token的读取，防止影响后续语法分析
        if(tokens.get(tokenIndex).getToken() == tokenExpected) {
            pair = tokens.get(tokenIndex);
            token = tokens.get(tokenIndex).getToken();
            return true;
        } else {
            tokenIndex--;
            return false;
        }
    }

    // 1. 回退
    public void retract(int stride) {
        tokenIndex -= stride;
        if (tokenIndex >= 0) {
            pair = tokens.get(tokenIndex);
            token = tokens.get(tokenIndex).getToken();
        }
    }

    public void retractAbsolutely(int index) {
        tokenIndex = index;
        if (tokenIndex >= 0) {
            pair = tokens.get(tokenIndex);
            token = tokens.get(tokenIndex).getToken();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 1. 解析<CompUnit>
    public void parseCompUnit() throws IOException {
        // 1. 获取第一个Token
        getToken();
        // 2. 解析<Decl>
        // 2.1 <Decl>有两种情况
            // const int|char a = ...; -> 回退 + 解析<ConstDecl>
            // int a = ...; -> 继续读一个
                // 如果不是IDENFR，那么可能是int main了，回退 + 结束解析<Decl>
                // 否则继续读一个
                    // 是'('说明是函数，回退结束解析<Decl>
                    // 否则是变量，回退 + 解析<VarDecl>
            // char a = ...; -> 继续读一个
                // 继续读一个
                    // 是'('说明是函数，回退结束解析<Decl>
                    // 否则是变量，回退 + 解析<VarDecl>
        while(token == Token.CONSTTK || token == Token.INTTK || token == Token.CHARTK) {
            // 1. <ConstDecl>
            if(token == Token.CONSTTK) {
                retract(1);
                //fw.write("///////////////////// ConstDecl /////////////////////\n");
                parseConstDecl();
            }
            // 2. <VarDecl>
            // 2.1 int main + int a() + int a
            else if (token == Token.INTTK) {
                // 1. int main
                getToken();
                if (token != Token.IDENFR) {
                    retract(1);
                    break;
                }
                // 2. int a()
                getToken();
                if (token == Token.LPARENT) {
                    retract(2);
                    break;
                }
                // 3. int a
                retract(3);
                //fw.write("///////////////////// VarDecl /////////////////////\n");
                parseVarDecl();
            }
            // 2.2 char a() + char a
            else {
                // 1. IDENFR
                getToken();
                // 2. '('
                getToken();
                if (token == Token.LPARENT) {
                    retract(2);
                    break;
                }
                retract(3);
                //fw.write("///////////////////// VarDecl /////////////////////\n");
                parseVarDecl();
            }
            getToken();
        }

        // 3. 解析<FuncDef>
        // 3.1 是void: 回退 + 解析<FuncDef>
        // 3.2 是int: 继续读一个
            // 是IDENFR，回退 + 解析<FuncDef>
            // 不是IDENFR，那么是int main，退回结束解析<FuncDef>
        // 3.3 是char: 继续读一个
            // 回退 + 解析<FuncDef>
        // 3.4 继续获取下一个Token
        while(token == Token.VOIDTK || token == Token.INTTK || token == Token.CHARTK) {
            //fw.write("///////////////////// FuncDef /////////////////////\n");
            // 1. void
            if(token == Token.VOIDTK) {
                retract(1);
                parseFuncDef();
            }
            // 2. int main() 和 int a()
            else if (token == Token.INTTK) {
                getToken();
                if (token != Token.IDENFR) {
                    retract(1);
                    break;
                }
                retract(2);
                parseFuncDef();
            }
            // 3. char a()
            else {
                retract(1);
                parseFuncDef();
            }
            getToken();
        }

        // 4. 解析MainFuncDef
        // 4.1 回退一开始读的int
        // 4.2 解析<MainFuncDef>
        // 4.3 最后追加语法成分
        retract(1);
        //fw.write("///////////////////// MainFuncDef /////////////////////\n");
        parseMainFuncDef();
        fw.write("<CompUnit>\n");
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 2. 解析<ConstDecl>, <ConstDef>, <ConstInitVal>
    public void parseConstDecl() throws IOException {
        // 1. 解析<ConstDecl>
        // 1. 包含'const' + BType + <ConstDef>:
            // const int a = 0;
            // const int a[10] = {0,1,2,...,9};
            // const char a = 0;
            // const char a[10] = {'0','1','2',...,'9'};

        // 1.1 读取'const'
        getToken();
        fw.write(pair.toString() + "\n");
        // 1.2 读取BType
        getToken();
        fw.write(pair.toString() + "\n");
        // 1.3 解析<ConstDef>
        parseConstDef();

        // 2. 解析完继续获取下一个Token
        // 2.1 如果是',',那么继续解析<ConstDef>,并不断获取下一个Token
        // 2.2 输出';'的Token
        // 2.3 追加语法成分
        while(getToken(Token.COMMA)) {
            fw.write(pair.toString() + "\n");
            parseConstDef();
        }
        if (getToken(Token.SEMICN)) {
            fw.write(pair.toString() + "\n");
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }
        fw.write("<ConstDecl>\n");
    }

    public void parseConstDef() throws IOException {
        // 1. 解析<ConstDef>
        // 1. 包含
            // IDENFR
            // 可能有: '[' + <ConstExp> + ']'
            // '=' + <ConstInitVal>

        // 1.读IDENFR的Token
        getToken();
        fw.write(pair.toString() + "\n");

        // 2.读下一个Token
        // 2.1 如果是'['就是数组
            // 解析<ConstExp>
            // ']'
        if(getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            parseConstExp();
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 3.1 读'=''
        // 3.2 解析<ConstInitVal>
        // 3.3 追加语法成分
        getToken();
        fw.write(pair.toString() + "\n");
        parseConstInitVal();
        fw.write("<ConstDef>\n");
    }

    public void parseConstInitVal() throws IOException {
        // 1. 解析<ConstInitVal>
        // 1.1 有三种可能: <ConstExp> | {<ConstExp>, ... } | <StirngConst> -> "..."

        // 1.{
            // 1.1 }
            // 1.2 <ConstExp> }
        getToken();
        if (token == Token.LBRACE) {
            fw.write(pair.toString() + "\n");
            // 1.1 <ConstExp>, ... }
            getToken();
            if (token != Token.RBRACE) {
                retract(1);
                parseConstExp();
                getToken();
                while(token == Token.COMMA) {
                    fw.write(pair.toString() + "\n");
                    parseConstExp();
                    getToken();
                }
            }
            // 1.2 }
            fw.write(pair.toString() + "\n");
        }
        // 2."..."
        else if (token == Token.STRCON) {
            retract(1);
            parseStringConst();
        }
        // 3. <ConstExp>
        else {
            retract(1);
            parseConstExp();
        }

        // 2. 追加语法成分
        fw.write("<ConstInitVal>\n");
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 3. 解析<VarDecl>, <VarDef>, <InitVal>
    public void parseVarDecl() throws IOException {
        // 1. 解析<VarDecl>
        // 1. <VarDecl> = BType <VarDef> { ',' <VarDef> } ';'

        // 1. 先读BType
        getToken();
        fw.write(pair.toString() + "\n");

        // 2. 解析<VarDef>
        parseVarDef();

        // 3. 如果下一个是,继续解析<VarDef>
        while(getToken(Token.COMMA)) {
            fw.write(pair.toString() + "\n");
            parseVarDef();
        }

        // 4. 解析';'
        // 4. 最后追加语法成分
        if (getToken(Token.SEMICN)) {
            fw.write(pair.toString() + "\n");
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }
        fw.write("<VarDecl>\n");
    }

    public void parseVarDef() throws IOException {
        // 1. 解析<VarDef>
        // 1. <VarDef>有两种情况
            // IDENFR 或 IDENFR '[' + <ConstExp> + ']' -> 也就是不赋初值
            // IDENFR '=' + <InitVal> 或 IDENFR '[' + <ConstExp> + ']' + '=' + <InitVal> -> 也就是赋初值

        // 2. 解析IDENFR
        getToken();
        fw.write(pair.toString() + "\n");

        // 3. 如果是数组
        // 3.1 解析'['
        // 3.2 解析<ConstExp>
        // 3.3 解析']'
        if(getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            parseConstExp();
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 解析'='
        // 4.1 如果可以解析'=', 说明有<InitVal>
        // 4.2 追加语法成分
        if(getToken(Token.ASSIGN)) {
            fw.write(pair.toString() + "\n");
            parseInitVal();
        }
        fw.write("<VarDef>\n");
    }

    public void parseInitVal() throws IOException {
        // 1. 解析<InitVal>
        // 2. <InitVal>有三种
            // <Exp>
            // { <Exp>, ... }
            // <StringConst>

        // 1. 获取第一个Token
        // 1.1 查看是否是'{'
            // 不是则回退直接解析<Exp>
            // 否则解析'{',然后循环解析<Exp>
        // 1.2 最后追加语法成分
        // 1.3.1 { <Exp>, ... }
        getToken();
        if (token == token.LBRACE) {
            // 1. {
            fw.write(pair.toString() + "\n");
            // 2. <Exp>,<Exp>
            getToken();
            if (token != Token.RBRACE) {
                retract(1);
                parseExp();
                getToken();
                while(token == Token.COMMA) {
                    fw.write(pair.toString() + "\n");
                    parseExp();
                    getToken();
                }
            }
            // 3. }
            fw.write(pair.toString() + "\n");
        }
        // 1.3.2 <StringConst>
        else if (token == Token.STRCON) {
            retract(1);
            parseStringConst();
        }
        // 1.3.3 <Exp>
        else {
            retract(1);
            parseExp();
        }
        fw.write("<InitVal>\n");
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 4. 解析<FuncDef>, <FuncType>, <FuncFParams>, <FuncFParam>
    public void parseFuncDef() throws IOException {
        // 1. 解析<FuncDef>
        // 1. <FuncDef> = <FuncType> IDENFR '(' <FuncFParams>')' <Block>

        // 2.1 解析<FuncType>
        parseFuncType();

        // 2.2 解析IDENFR
        getToken();
        fw.write(pair.toString() + "\n");

        // 2.3 解析'('
        getToken();
        fw.write(pair.toString() + "\n");

        // 2.4 解析<FuncFParams>
        // 2.4 如果不是')', 说明是<FuncFParams>，回退一位然后解析
        // 2.4 否则就是')', 直接解析即可
        getToken();
        if (token != Token.RPARENT && token != Token.LBRACE) {
            retract(1);
            parseFuncFParams();
            getToken();
        }
        if(token == Token.RPARENT) {
            fw.write(pair.toString() + "\n");
        } else {
            retract(1);
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
        }
        // 2.5 解析<Block>
        // 2.5 追加语法成分
        parseBlock();
        fw.write("<FuncDef>\n");
    }

    public void parseFuncType() throws IOException {
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<FuncType>\n");
    }

    public void parseFuncFParams() throws IOException {
        // 1. 解析<FuncFParams>
        // 1. <FuncFParams> = <FuncFParam> { ',' <FuncFParam> }

        // 1. 解析<FuncFParam>
        parseFuncFParam();

        // 2. 循环解析,' <FuncFParam>
        getToken();
        while(token == Token.COMMA) {
            fw.write(pair.toString() + "\n");
            parseFuncFParam();
            getToken();
        }
        retract(1);
        fw.write("<FuncFParams>\n");
    }

    public void parseFuncFParam() throws IOException {
        // 1. 解析<FuncFParam>
        // 1. <FuncFParam>
            // BType + IDENFR
            // BType + IDENFR + '[' + ']'

        // 1. BType
        getToken();
        fw.write(pair.toString() + "\n");

        // 2. IDENFR
        getToken();
        fw.write(pair.toString() + "\n");

        // 3. '['
        if (getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 追加语法成分
        fw.write("<FuncFParam>\n");
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 5. <MainFuncDef>, <Block>, <BlockItem>
    public void parseMainFuncDef() throws IOException {
        // 1. 解析<MainFuncDef>
        // 1. <MainFuncDef> = int main() <Block>

        // 1. int
        getToken();
        fw.write(pair.toString() + "\n");

        // 2. main
        getToken();
        fw.write(pair.toString() + "\n");

        // 3. ()
        getToken();
        fw.write(pair.toString() + "\n");
        if (getToken(Token.RPARENT)) {
            fw.write(pair.toString() + "\n");
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
        }

        // 4. <Block>
        parseBlock();

        // 5. 追加语法成分
        fw.write("<MainFuncDef>\n");
    }

    public void parseBlock() throws IOException {
        // 1. 解析<Block>
        // 1. <Block> = '{' + {BlockItem} + '}'

        // 1. '{'
        getToken();
        fw.write(pair.toString() + "\n");

        // 2. <BlockItem>
        getToken();
        while(token != Token.RBRACE) {
            retract(1);
            parseBlockItem();
            getToken();
        }

        // 3. '}'
        fw.write(pair.toString() + "\n");

        // 4. 追加语法成分
        fw.write("<Block>\n");
    }

    public void parseBlockItem() throws IOException {
        // 1. 解析<BlockItem>
        // 1. <BlockItem> = <ConstDecl> | <VarDecl> | <Stmt>

        // 1. <ConstDecl>
        getToken();
        if(token == Token.CONSTTK) {
            retract(1);
            parseConstDecl();
        }
        // 2. <VarDecl>
        else if (token == Token.INTTK || token == Token.CHARTK) {
            retract(1);
            parseVarDecl();
        }
        // 3. <Stmt>
        else {
            retract(1);
            parseStmt();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 6. <Stmt>, <ForStmt>, <Exp>, <Cond>, <LVal>, <PrimaryExp>, <Number>, <Character>, <StringConst>
    public void parseStmt() throws IOException {
        // 1. 解析<Stmt>
        // 1. <Stmt> =
            // <LVal> '=' <Exp> ';'
            // ';' | <Exp> ';'
            // <Block>
            // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
            // 'for' '(' <ForStmt> | 没有 ';' <Cond> ｜ 没有 ';' <ForStmt> | 没有 ')' <Stmt>
            // 'break' ';'
            // 'continue' ';'
            // 'return' ';' | 'return' <Exp> ';'
            // <LVal> '=' 'getint' '(' ')' ';'
            // <LVal> '=' 'getchar' '(' ')' ';'
            // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'

        // 1. 分类讨论
        // 1. 讨论顺序
            // ';'
            // <Blcok>
            // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
            // 'for' '(' 没有 | <ForStmt> ';' 没有 | <Cond> ';' 没有 <ForStmt> ')' <Stmt>
            // 'break' ';'
            // 'continue' ';'
            // 'return' ';' | 'return' <Exp> ';'
            // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
            // <LVal>
                // <LVal> '=' <Exp> ';'
                // <Exp> ';'
                // <LVal> '=' 'getint' '(' ')' ';'
                // <LVal> '=' 'getchar' '(' ')' ';'
        getToken();
        switch (token) {
            // ';'
            case SEMICN:
                fw.write(pair.toString() + "\n");
                break;
            // <Blcok>
            case LBRACE:
                retract(1);
                parseBlock();
                break;
            // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
            case IFTK:
                // 1. 'if'
                fw.write(pair.toString() + "\n");
                // 2. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 3. <Conf>
                parseCond();
                // 4. ')'
                if(getToken(Token.RPARENT)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 5. <Stmt>
                parseStmt();
                // 6. 可能有:'else' <Stmt>
                if (getToken(Token.ELSETK)) {
                    fw.write(pair.toString() + "\n");
                    parseStmt();
                }
                break;
            // 'for' '(' 没有 | <ForStmt> ';' 没有 | <Cond> ';' 没有 | <ForStmt> ')' <Stmt>
            case FORTK:
                // 1. 'for'
                fw.write(pair.toString() + "\n");
                // 2. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 3. 没有 | <ForStmt>
                getToken();
                if(token != Token.SEMICN) {
                    retract(1);
                    parseForStmt();
                    getToken();
                }
                // 4. ';'
                fw.write(pair.toString() + "\n");
                // 5. 没有 | <Cond>
                getToken();
                if(token != Token.SEMICN) {
                    retract(1);
                    parseCond();
                    getToken();
                }
                // 6. ';'
                fw.write(pair.toString() + "\n");
                // 7. 没有 | <ForStmt>
                getToken();
                if(token != Token.RPARENT) {
                    retract(1);
                    parseForStmt();
                    getToken();
                }
                // 8. ')'
                if(token == Token.RPARENT) {
                    fw.write(pair.toString() + "\n");
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 9. <Stmt>
                parseStmt();
                break;
            // 'break' ';'
            case BREAKTK:
                fw.write(pair.toString() + "\n");
                if(getToken(Token.SEMICN)){
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'continue' ';'
            case CONTINUETK:
                fw.write(pair.toString() + "\n");
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'return' ';' | 'return' <Exp> ';'
            case RETURNTK:
                // 1. 'return'
                fw.write(pair.toString() + "\n");
                // 2. 没有 ';' | <Exp> ';'
                getToken();
                if(token == Token.PLUS || token == Token.MINU || token == Token.NOT
                        || token == Token.IDENFR || token == Token.LPARENT
                        || token == Token.INTCON || token == Token.CHRCON) {
                    retract(1);
                    parseExp();
                    getToken();
                }
                // 3. ';'
                if(token == Token.SEMICN) {
                    fw.write(pair.toString() + "\n");
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
            case PRINTFTK:
                // 1. 'printf'
                fw.write(pair.toString() + "\n");
                // 2. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 3. <StringConst>
                parseStringConst();
                // 4. ',' <Exp> ')' 或 ')'
                while(getToken(Token.COMMA)) {
                    fw.write(pair.toString() + "\n");
                    parseExp();
                }
                // 5. ')'
                if(getToken(Token.RPARENT)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 6. ';'
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // <LVal>
                // <LVal> '=' <Exp> ';'
                // <Exp> ';'
                // <LVal> '=' 'getint' '(' ')' ';'
                // <LVal> '=' 'getchar' '(' ')' ';'
            case IDENFR:
                // 1. <LVal> -> a = 1;
                // 2. <Exp> -> a();
                int anchor = tokenIndex - 1;
                if(getToken(Token.LPARENT)) {
                    retractAbsolutely(anchor);
                    parseExp();
                } else {
                    // 1. 先扫一遍会不会出现<LVal>中的a[ = {1};错误
                    // fix: in deeper recursive layer, fw will be set original and errorHandler will be turned on
                    fw = new FileWriter("temp.txt");
                    fw.write("///////// in temp /////////\n");
                    errorHandler.turnOff();
                    retractAbsolutely(anchor);
                    parseLVal();
                    errorHandler.turnOn();
                    fw.write("///////// out temp /////////\n");
                    fw.close();
                    fw = fwOrigin;
                    // 2. 读等号
                    if(getToken(Token.ASSIGN)) {
                        // 1. 重新扫描
                        retractAbsolutely(anchor);
                        parseLVal();
                        // 2. 读等号
                        getToken();
                        fw.write(pair.toString() + "\n");
                        // 3. 'getint' | 'getchar' | <Exp>
                        getToken();
                        if (token == Token.GETINTTK || token == Token.GETCHARTK) {
                            fw.write(pair.toString() + "\n");
                            getToken();
                            fw.write(pair.toString() + "\n");
                            if (getToken(Token.RPARENT)) {
                                fw.write(pair.toString() + "\n");
                            } else {
                                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                            }
                        } else {
                            retract(1);
                            parseExp();
                        }
                    }
                    // 3. 没有等号就是<Exp>
                    else {
                        // 1. 重新扫描
                        retractAbsolutely(anchor);
                        // 2. <Exp>
                        parseExp();
                    }
                }
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            default:
                // 1. 此时只剩其他类型的<Exp>
                retract(1);
                parseExp();
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
        }
        fw.write("<Stmt>\n");
    }

    public void parseForStmt() throws IOException {
        // 1. 解析<ForStmt>
        // 1. <ForStmt> = <LVal> '=' <Exp>

        // 1. <LVal>
        parseLVal();
        // 2. '='
        getToken();
        fw.write(pair.toString() + "\n");
        // 3. <Exp>
        parseExp();
        fw.write("<ForStmt>\n");
    }

    public void parseExp() throws IOException {
        // 1. 解析<Exp>
        // 2. <Exp> = <AddExp>
        parseAddExp();
        fw.write("<Exp>\n");
    }

    public void parseCond() throws IOException {
        // 1. 解析<Cond>
        // 2. <Cond> = <LOrExp>
        parseLOrExp();
        fw.write("<Cond>\n");
    }

    public void parseLVal() throws IOException {
        // 1. 解析<LVal>
        // 2. <LVal> = IDENFR | IDENFR [ <Exp> ]

        // 1. IDENFR
        getToken();
        fw.write(pair.toString() + "\n");

        // 2. 没有 | [ <Exp> ]
        if (getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            parseExp();
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }
        fw.write("<LVal>\n");
    }

    public void parsePrimaryExp() throws IOException {
        // 1. 解析<PrimaryExp>
        // 1. <PrimaryExp>
            // '(' + <Exp> + ')'
            // <LVal>
            // <Number>
            // <Character>

        // 1. '(' + <Exp> + ')'
        getToken();
        if (token == Token.LPARENT) {
            fw.write(pair.toString() + "\n");
            parseExp();
            if(getToken(Token.RPARENT)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
            }
        }
        // 2. <Number>
        else if (token == Token.INTCON) {
            retract(1);
            parseNumber();
        }
        // 3. <Character>
        else if (token == Token.CHRCON) {
            retract(1);
            parseCharacter();
        }
        // 4. <LVal>
        else {
            retract(1);
            parseLVal();
        }
        fw.write("<PrimaryExp>\n");
    }

    public void parseNumber() throws IOException {
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<Number>\n");
    }

    public void parseCharacter() throws IOException {
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<Character>\n");
    }

    public void parseStringConst() throws IOException {
        getToken();
        fw.write(pair.toString() + "\n");
        //fw.write("<StringConst>\n");
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 7. <UnaryExp>, <UnaryOp>, <FuncRParams>, <MulExp>, <AddExp>, <RelExp>, <EqExp>, <LAndExp>, <LOrExp>, <ConstExp>
    public void parseUnaryExp() throws IOException {
        // 1. 解析<UnaryExp>
        // 1. <UnaryExp>
            // <PrimaryExp>
            // IDENFR '(' 没有 | <FuncRParams> ')'
            // <UnaryOp> <UnaryExp>

        // 1. <UnaryOp> <UnaryExp>
        getToken();
        if (token == Token.PLUS || token == Token.MINU || token == Token.NOT) {
            retract(1);
            parseUnaryOp();
            parseUnaryExp();
        }
        // 2. IDENFR '(' 没有 | <FuncRParams> ')'
        // 2. <PrimaryExp>的开头也可能是IDENFR
        // 2. 所以要进一步判断'('
        else if (token == Token.IDENFR) {
            // 1. '('
            getToken();
            if (token == Token.LPARENT) {
                // 1. IDENFR
                retract(1);
                fw.write(pair.toString() + "\n");
                // 2. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 3. ')' | <FuncRParams> ')'
                // fix: if errors 'j' happens, missing ')', this will enter <parseFuncRParams> branch
                getToken();
                if(token == Token.PLUS || token == Token.MINU || token == Token.NOT
                        || token == Token.IDENFR || token == Token.LPARENT
                        || token == Token.INTCON || token == Token.CHRCON) {
                    retract(1);
                    parseFuncRParams();
                    getToken();
                }
                // 4. ')'
                if(token == Token.RPARENT) {
                    fw.write(pair.toString() + "\n");
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
            } else {
                retract(2);
                parsePrimaryExp();
            }
        }
        // 3. <PrimaryExp>
        else {
            retract(1);
            parsePrimaryExp();
        }
        fw.write("<UnaryExp>\n");
    }

    public void parseUnaryOp() throws IOException {
        // 1. 解析<UnaryOp>
        // 1. <UnaryOp> = '+' | '-' | '!'
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<UnaryOp>\n");
    }

    public void parseFuncRParams() throws IOException {
        // 1. 解析<FuncRParams>
        // 1. <FuncRParams> = <Exp> { ',' <Exp> }

        // 1. <Exp>
        parseExp();

        // 2. { ',' <Exp> }
        getToken();
        while (token == Token.COMMA) {
            fw.write(pair.toString() + "\n");
            parseExp();
            getToken();
        }
        retract(1);
        fw.write("<FuncRParams>\n");
    }

    public void parseMulExp() throws IOException {
        // 1. 解析<MulExp>
        // 1. <MulExp>
            // <UnaryExp>
            // <MulExp> '*' | '/' | '%' <UnaryExp>
            // -> 这里存在左递归
            // -> 文法改写为 <MulExp> = <UnaryExp> {'*' | '/' | '%' <UnaryExp>}
        // 1. <UnaryExp>
        parseUnaryExp();
        fw.write("<MulExp>\n");
        // 2. '*' | '/' | '%' <UnaryExp> {'*' | '/' | '%' <UnaryExp>}
        getToken();
        while(token == Token.MULT || token == Token.DIV || token == Token.MOD) {
            fw.write(pair.toString() + "\n");
            parseUnaryExp();
            fw.write("<MulExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseAddExp() throws IOException {
        // 1. 解析<AddExp>
        // 1. <AddExp> = <MulExp> | <AddExp> '+' | '-' <MulExp>
            // 1. 改写文法<AddExp> = <MulExp> {'+' | '-' <MulExp>}

        // 1. <MulExp>
        parseMulExp();
        fw.write("<AddExp>\n");
        // 2. {'+' | '-' <MulExp>}
        getToken();
        while(token == Token.PLUS || token == Token.MINU) {
            fw.write(pair.toString() + "\n");
            parseMulExp();
            fw.write("<AddExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseRelExp() throws IOException {
        // 1. 解析<RelExp>
        // 1. <RelExp> = <AddExp> | <RelExp> '<' | '>' | "<=" | ">=" <AddExp>
            // 1. 改写文法 <RelExp> = <AddExp> {'<' | '>' | "<=" | ">=" <AddExp>}
        // 1. <AddExp>
        parseAddExp();
        fw.write("<RelExp>\n");
        // 2. {'<' | '>' | "<=" | ">=" <AddExp>}
        getToken();
        while(token == Token.LSS || token == Token.GRE || token == Token.LEQ || token == Token.GEQ) {
            fw.write(pair.toString() + "\n");
            parseAddExp();
            fw.write("<RelExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseEqExp() throws IOException {
        // 1. 解析<EqExp>
        // 1. <EqExp> = <RelExp> | <EqExp> "==" | "!=" <RelExp>
            // 1. 改写文法: <EqExp> = <RelExp> {"==" | "!=" <RelExp>}
        // 1. <RelExp>
        parseRelExp();
        fw.write("<EqExp>\n");
        // 2. {"==" | "!=" <RelExp>}
        getToken();
        while(token == Token.EQL || token == Token.NEQ) {
            fw.write(pair.toString() + "\n");
            parseRelExp();
            fw.write("<EqExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseLAndExp() throws IOException {
        // 1. 解析<LAndExp>
        // 1. <LAndExp> = <EqExp> | <LAndExp> "&&" <EqExp>
            // 1. 改写文法： <LAndExp> = <EqExp> {"&&" <EqExp>}
        // 1. <EqExp>
        parseEqExp();
        fw.write("<LAndExp>\n");
        // 2. {"&&" <EqExp>}
        getToken();
        while(token == Token.AND) {
            fw.write(pair.toString() + "\n");
            parseEqExp();
            fw.write("<LAndExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseLOrExp() throws IOException {
        // 1. 解析<LOrExp>
        // 1. <LOrExp> = <LAndExp> | <LOrExp> "||" <LAndExp>
            // 1. 改写文法: <LOrExp> = <LAndExp> {"||" <LAndExp>}
        // 1. <LAndExp>
        parseLAndExp();
        fw.write("<LOrExp>\n");
        // 2. {"||" <LAndExp>}
        getToken();
        while(token == Token.OR) {
            fw.write(pair.toString() + "\n");
            parseLAndExp();
            fw.write("<LOrExp>\n");
            getToken();
        }
        retract(1);
    }

    public void parseConstExp() throws IOException {
        // 1. 解析<ConstExp>
        // 1. <ConstExp> = <AddExp>
        parseAddExp();
        fw.write("<ConstExp>\n");
    }
}
