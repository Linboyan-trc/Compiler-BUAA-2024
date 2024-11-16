package Parser;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Lexer;
import Lexer.Pair;
import Lexer.Token;
import static Lexer.Token.*;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;
import SyntaxTree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Parser {
    // 1. lexer + 当前pair, 当前token, 当前token指针，token表
    private Lexer lexer;
    private Pair pair;
    private Token token;
    private int tokenIndex = -1;
    private List<Pair> tokens = new ArrayList<>();
    // 2. 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 1. 获取一个新的token，pair, token, tokenIndex指向当前token
    public void getToken() throws IOException {
        // 1.1 当用到tokenIndex = tokens.size()，需要获取新的token
        tokenIndex++;
        if (tokens.size() == tokenIndex) {
            tokens.add(lexer.parseAndGetPair());
        }
        // 1.2 更新pair，token
        pair = tokens.get(tokenIndex);
        token = tokens.get(tokenIndex).getToken();
    }

    // 2. 判断当前token是否是指定多个token中的一个
    // 2.1 如果是更新pair, token, tokenIndex并就返回true
    // 2.1 如果不是指定多个token中的一个就回退到上一个pair, token, tokenIndex，返回false
    public boolean getToken(Token... tokenExpecteds) throws IOException {
        // 1. 获取新token，判断是否符合条件
        getToken();
        for(Token tokenExpected:tokenExpecteds) {
            if(token == tokenExpected) {
                return true;
            }
        }

        // 2. 如果不符合条件则回退
        retract(1);
        return false;
    }

    // 3. 回退
    public void retract(int stride) {
        tokenIndex -= stride;
        if (tokenIndex >= 0) {
            pair = tokens.get(tokenIndex);
            token = tokens.get(tokenIndex).getToken();
        }
    }

    // 4. 回退
    public void retractAbsolutely(int index) {
        tokenIndex = index;
        if (tokenIndex >= 0) {
            pair = tokens.get(tokenIndex);
            token = tokens.get(tokenIndex).getToken();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 1. 解析<CompUnit>
    public ParsedUnit parseCompUnit() throws IOException {
        // 1. 统一使用<ParsedUnit>作为节点
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. 解析<Decl>
        // 2.1 常量:CONSTTK
        // 2.2 变量:INTTK, CHARTK
        // const int|char a = ...; -> 回退 + 解析<ConstDecl>
        // int main -> 回退 + 结束解析
        // int sum() | char sum() -> 回退 + 结束解析
        // int a | char a -> 回退 + 解析<VarDecl>
        for(int anchor = tokenIndex; getToken(CONSTTK, INTTK, CHARTK); anchor = tokenIndex) {
            // 1.1 getToken之后:index, curToken都是最新的
            // 1.1 回到起始帧:解析本Node的前一个位置，然后解析ConstDecl
            if(token == CONSTTK) {
                retractAbsolutely(anchor);
                units.add(parseConstDecl());
            }

            // 1.2 如果是int,char 但是下一个又不是IDENFR，说明是main
            // 1.2 回到起始帧，退出Decl解析
            else if (!getToken(IDENFR)) {
                retractAbsolutely(anchor);
                break;
            }

            // 1.3 如果是int，char + IDENFR，并且下一个是(，说明是FuncDef
            // 1.3 回到起始帧，退出Decl解析
            else if (getToken(LPARENT)) {
                retractAbsolutely(anchor);
                break;
            }

            // 1.4 如果是int，char + IDENFR
            // 1.4 回到起始帧，然后解析VarDecl
            else {
                // 3. int a
                retractAbsolutely(anchor);
                units.add(parseVarDecl());
            }
        }

        // 3. 解析<FuncDef>
        // 3.1 类型:VOIDTK, INTTK, CHARTK
        // void -> 回退 + 解析<FuncDef>
        // int sum() | char sum() -> 回退 + 解析<FuncDef>
        // int main() -> 回退 + 结束解析
        for(int anchor = tokenIndex; getToken(VOIDTK, INTTK, CHARTK); anchor = tokenIndex) {
            // 3.1 是VOIDTK
            // 3.1 回到起始帧，解析FuncDef
            if(token == VOIDTK) {
                retractAbsolutely(anchor);
                units.add(parseFuncDef());
            }

            // 3.2 是INTTK或CHARTK，并且下一个是IDENFR
            // 3.2 回到起始帧，解析FuncDef
            else if (getToken(IDENFR)) {
                retractAbsolutely(anchor);
                units.add(parseFuncDef());
            }

            // 3.3 否则是main
            // 3.3 回到起始帧，退出FuncDef解析
            else {
                retractAbsolutely(anchor);
                break;
            }
        }

        // 4. 解析MainFuncDef
        units.add(parseMainFuncDef());

        // 5. 返回节点:名称 + 子节点
        return new ParsedUnit("CompUnit", units);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 2. 解析<ConstDecl>, <ConstDef>, <ConstInitVal>
    // 2.1 <ConstDecl> = const BType <ConstDef> {, <ConstDef>}
    // 2.1 <ConstDef> = <Def> '=' <ConstInitVal>
    // 2.1 <ConstInitVal> = <ConstExp>, {<ConstExp>,<ConstExp>}, "StringConst"
    // 2.2 <Def> = IDENFR [<ConstExp>]
    public ParsedUnit parseConstDecl() throws IOException {
        // 1. 解析<ConstDecl>
        // const int a = 0;
        // const int a[10] = {0,1,2,...,9};
        // const char a = 0;
        // const char a[10] = {'0','1','2',...,'9'};
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2.1 const
        getToken(CONSTTK);
        units.add(pair);

        // 2.2 BType
        getToken(INTTK,CHARTK);
        units.add(pair);

        // 2.2 为<ConstDef> {, <ConstDef>}对应的<DefNode>类型做准备
        SyntaxType syntaxType;
        if(token == INTTK){
            syntaxType = SyntaxType.ConstInt;
        } else {
            syntaxType = SyntaxType.ConstChar;
        }

        // 2.3 <ConstDef> {, <ConstDef>}
        units.add(parseConstDef(syntaxType));
        while(getToken(COMMA)) {
            units.add(pair);
            units.add(parseConstDef(syntaxType));
        }

        // 2.4 ;
        if (getToken(SEMICN)) {
            units.add(pair);
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }

        // 2.5 返回节点
        return new ParsedUnit("ConstDecl", units);
    }

    // 2.1 <ConstDecl> = const BType <ConstDef> {, <ConstDef>}
    // 2.1 <ConstDef> = <Def> '=' <ConstInitVal>
    // 2.1 <ConstInitVal> = <ConstExp>, {<ConstExp>,<ConstExp>}, "StringConst"
    // 2.2 <Def> = IDENFR [<ConstExp>]
    public ParsedUnit parseConstDef(SyntaxType syntaxType) throws IOException {
        // 1. <Def>
        LinkedList<ParsedUnit> units = parseDef(syntaxType);

        // 2. =
        getToken(ASSIGN);
        units.add(pair);

        // 3. <ConstInitVal>
        units.add(parseConstInitVal());

        // 4. 返回节点
        return new ParsedUnit("ConstDef", units);
    }

    // 2.1 <ConstDecl> = const BType <ConstDef> {, <ConstDef>}
    // 2.1 <ConstDef> = <Def> '=' <ConstInitVal>
    // 2.1 <ConstInitVal> = <ConstExp>, {<ConstExp>,<ConstExp>}, "StringConst"
    // 2.2 <Def> = IDENFR [<ConstExp>]
    public LinkedList<ParsedUnit> parseDef(SyntaxType syntaxType) throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. IDENFR
        getToken(IDENFR);
        units.add(pair);

        // 3. [
        if(getToken(LBRACK)) {
            units.add(pair);

            // TODO: 4. IDENFR对应的类型需要转换

            // 5. <ConstExp>
            units.add(parseConstExp());

            // 6. ]
            if(getToken(RBRACK)) {
                units.add(pair);
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 返回units
        return units;
    }

    // 2.1 <ConstDecl> = const BType <ConstDef> {, <ConstDef>}
    // 2.1 <ConstDef> = <Def> '=' <ConstInitVal>
    // 2.1 <ConstInitVal> = <ConstExp>, {<ConstExp>,<ConstExp>}, "StringConst"
    // 2.2 <Def> = IDENFR [<ConstExp>]
    public ParsedUnit parseConstInitVal() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        // 1.{
            // 1.1 }
            // 1.2 <ConstExp> }

        // 2. <ConstExp> 或 "StringConst"
        if(!getToken(LBRACE)) {
            if(getToken(STRCON)){
                units.add(pair);
            } else {
                units.add(parseConstExp());
            }
        }

        // 3. {<ConstExp>,<ConstExp>}
        else {
            // 3.1 {
            units.add(pair);

            // 3.2 <ConstExp>
            if (!getToken(RBRACE)) {
                units.add(parseConstExp());

                // 3.3 , <ConstExp>
                while(getToken(COMMA)) {
                    units.add(pair);
                    units.add(parseConstExp());
                }

                // 3.4 }
                getToken(RBRACE);
            }
            units.add(pair);
        }

        // 4. 返回节点
        return new ParsedUnit("ConstInitVal", units);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 3. 解析<VarDecl>, <VarDef>, <InitVal>
    // 3.1 <VarDecl> = BType <VarDef> {, <VarDef>}
    // 3.1 <VarDef> = <Def> , <Def> '=' <InitVal>
    // 3.1 <InitVal> = <Exp>, {<Exp>, <Exp>}, "StringConst"
    // 3.2 <Def> = IDENFR [<ConstExp>]
    public ParsedUnit parseVarDecl() throws IOException {
        // 1. 解析<VarDecl>
        // int a = 0;
        // int a[10] = {0,1,2,...,9};
        // char a = 0;
        // char a[10] = {'0','1','2',...,'9'};
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2.1 BType
        getToken(INTTK,CHARTK);
        units.add(pair);

        // 2.2 为<VarDef> {, <VarDef>}对应的<DefNode>类型做准备
        SyntaxType syntaxType;
        if(token == INTTK) {
            syntaxType = SyntaxType.Int;
        } else {
            syntaxType = SyntaxType.Char;
        }

        // 2.3 <VarDef> {, <VarDef>}
        units.add(parseVarDef(syntaxType));
        while(getToken(COMMA)) {
            units.add(pair);
            units.add(parseVarDef(syntaxType));
        }

        // 2.4 ;
        if (getToken(SEMICN)) {
            units.add(pair);
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }

        // 2.5 返回节点
        return new ParsedUnit("VarDecl", units);
    }

    // 3.1 <VarDecl> = BType <VarDef> {, <VarDef>}
    // 3.1 <VarDef> = <Def> , <Def> '=' <InitVal>
    // 3.1 <InitVal> = <Exp>, {<Exp>, <Exp>}, "StringConst"
    // 3.2 <Def> = IDENFR [<ConstExp>]
    public ParsedUnit parseVarDef(SyntaxType syntaxType) throws IOException {
        // 1. <Def>
        LinkedList<ParsedUnit> units = parseDef(syntaxType);

        // 2. ('=', <InitVal>)
        boolean isSpecial = false;
        if (getToken(ASSIGN)) {
            // 2.1 '='
            units.add(pair);

            // 2.2 特殊处理getint(), getchar()
            if(getToken(GETINTTK,GETCHARTK)) {
                isSpecial = true;
                units.add(pair);
                getToken(LPARENT);
                units.add(pair);
                getToken(RPARENT);
                units.add(pair);
            }

            // 2.3 <InitVal>
            else {
                units.add(parseInitVal());
            }
        }

        // 3. 返回节点:Special: getint(), getchat()
        // 3. 返回节点:<VarDef>
        return new ParsedUnit(isSpecial? "Special" : "VarDef", units);
    }

    // 3.1 <VarDecl> = BType <VarDef> {, <VarDef>}
    // 3.1 <VarDef> = <Def> , <Def> '=' <InitVal>
    // 3.1 <InitVal> = <Exp>, {<Exp>, <Exp>}, "StringConst"
    // 3.2 <Def> = IDENFR [<ConstExp>]
    public ParsedUnit parseInitVal() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <Exp> 或 "StringConst"
        if(!getToken(LBRACE)) {
            if(getToken(STRCON)){
                units.add(pair);
            } else {
                units.add(parseExp());
            }
        }


        // 3. {<Exp>,<Exp>}
        else {
            // 3.1 {
            units.add(pair);

            // 3.2 <Exp>
            if (!getToken(RBRACE)) {
                units.add(parseExp());

                // 3.3 , <Exp>
                while(getToken(COMMA)) {
                    units.add(pair);
                    units.add(parseExp());
                }

                // 3.4 }
                getToken(RBRACE);
            }
            units.add(pair);
        }

        // 4. 返回节点
        return new ParsedUnit("InitVal", units);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 4. 解析<FuncDef>, <FuncType>, <FuncFParams>, <FuncFParam>
    // 4.1 <FuncDef> = <FuncType> IDENFR '(' <FuncFParams> ')' <Block>
    // 4.1 <FuncType> = void | int | char
    // 4.1 <FuncFParams> = <FuncFParam> {, <FuncFParam>}
    // 4.1 <FuncFParam> = BType IDENFR, BType IDENFR '[' ']'
    public ParsedUnit parseFuncDef() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <FuncType>
        units.add(parseFuncType());

        // 3. IDENFR
        getToken(IDENFR);
        units.add(pair);

        // 4. '('
        getToken(LPARENT);
        units.add(pair);

        // 5. 错误处理: ( {
        if (getToken(LBRACE)) {
            retract(1);
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
        }

        // 6. <FuncFParams>
        else if (!getToken(RPARENT)) {
            units.add(parseFuncFParams());
            if (getToken(RPARENT)) {
                units.add(pair);
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
            }
        }

        // 7. ')'
        else {
            units.add(pair);
        }

        // 8. <Block>
        units.add(parseBlock());

        // 9. 返回节点
        return new ParsedUnit("FuncDef", units);
    }

    // 4.1 <FuncDef> = <FuncType> IDENFR '(' <FuncFParams> ')' <Block>
    // 4.1 <FuncType> = void | int | char
    // 4.1 <FuncFParams> = <FuncFParam> {, <FuncFParam>}
    // 4.1 <FuncFParam> = BType IDENFR, BType IDENFR '[' ']'
    public ParsedUnit parseFuncType() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        getToken(VOIDTK, INTTK, CHARTK);
        units.add(pair);
        return new ParsedUnit("FuncType", units);
    }

    // 4.1 <FuncDef> = <FuncType> IDENFR '(' <FuncFParams> ')' <Block>
    // 4.1 <FuncType> = void | int | char
    // 4.1 <FuncFParams> = <FuncFParam> {, <FuncFParam>}
    // 4.1 <FuncFParam> = BType IDENFR, BType IDENFR '[' ']'
    public ParsedUnit parseFuncFParams() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <FuncFParam> {, <FuncFParam>}
        units.add(parseFuncFParam());
        while (getToken(COMMA)) {
            units.add(pair);
            units.add(parseFuncFParam());
        }

        // 3. 返回节点
        return new ParsedUnit("FuncFParams", units);
    }

    // 4.1 <FuncDef> = <FuncType> IDENFR '(' <FuncFParams> ')' <Block>
    // 4.1 <FuncType> = void | int | char
    // 4.1 <FuncFParams> = <FuncFParam> {, <FuncFParam>}
    // 4.1 <FuncFParam> = BType IDENFR, BType IDENFR '[' ']'
    public ParsedUnit parseFuncFParam() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. BType
        getToken(INTTK,CHARTK);
        units.add(pair);

        // 3. TODO: type judge
        SyntaxType type;
        if(token == INTTK) {
            type = SyntaxType.Int;
        } else {
            type = SyntaxType.Char;
        }

        // 4. IDENFR
        getToken(IDENFR);
        units.add(pair);

        // 5. '['
        if (getToken(LBRACK)) {
            units.add(pair);

            if(getToken(RBRACK)) {
                units.add(pair);
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 5. 返回节点
        return new ParsedUnit("FuncFParam", units);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 5. <MainFuncDef>, <Block>, <BlockItem>
    // 5.1 <MainFuncDef> = int main '(' ')' <Block>
    // 5.2 <Block> = '{' <BlockItem> <BlockItem> ... '}'
    // 5.3 <BlockItem> = <Decl>, <Stmt>
    public ParsedUnit parseMainFuncDef() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. int
        getToken(INTTK);
        units.add(pair);

        // 3. main
        getToken(MAINTK);
        units.add(pair);

        // 4. '('
        getToken(LPARENT);
        units.add(pair);

        // 5. ')'
        if (getToken(RPARENT)) {
            units.add(pair);
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
        }

        // 6. <Block>
        units.add(parseBlock());

        // 7. 返回节点
        return new ParsedUnit("MainFuncDef", units);
    }

    // 5.1 <MainFuncDef> = int main '(' ')' <Block>
    // 5.2 <Block> = '{' <BlockItem> <BlockItem> ... '}'
    // 5.3 <BlockItem> = <Decl>, <Stmt>
    public ParsedUnit parseBlock() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. '{'
        getToken(LBRACE);
        units.add(pair);

        // 3. <BlockItem> <BlockItem> ...
        while(!getToken(RBRACE)) {
            units.add(parseBlockItem());
        }

        // 4. '}'
        units.add(pair);

        // 5. 追加语法成分
        return new ParsedUnit("Block", units);
    }

    // 5.1 <MainFuncDef> = int main '(' ')' <Block>
    // 5.2 <Block> = '{' <BlockItem> <BlockItem> ... '}'
    // 5.3 <BlockItem> = <Decl>, <Stmt>
    public ParsedUnit parseBlockItem() throws IOException {
        // 1. <ConstDecl>
        if(getToken(CONSTTK)) {
            retract(1);
            return parseConstDecl();
        }

        // 2. <VarDecl>
        else if (getToken(INTTK,CHARTK)) {
            retract(1);
            return parseVarDecl();
        }

        // 3. <Stmt>
        else {
            return parseStmt();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 6. <Stmt>, <ForStmt>, <Exp>, <Cond>, <LVal>, <PrimaryExp>, <Number>, <Character>, <StringConst>
    // 6.1 <Stmt> =
    //      <LVal> '=' <Exp> ';'
    //      ';' | <Exp> ';'
    //      <Block>
    //      'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
    //      'for' '(' <ForStmt> | 没有 ';' <Cond> ｜ 没有 ';' <ForStmt> | 没有 ')' <Stmt>
    //      'break' ';'
    //      'continue' ';'
    //      'return' ';' | 'return' <Exp> ';'
    //      <LVal> '=' 'getint' '(' ')' ';'
    //      <LVal> '=' 'getchar' '(' ')' ';'
    //      'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
    public ParsedUnit parseStmt() throws IOException {
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

        // 2. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 3. 分类判断
        getToken();
        switch (token) {
            // ';'
            case SEMICN:
                units.add(pair);
                break;
            // <Blcok>
            case LBRACE:
                retract(1);
                units.add(parseBlock());
                break;
            // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
            case IFTK:
                // 1. 'if'
                units.add(pair);
                // 2. '('
                getToken(LPARENT);
                units.add(pair);
                // 3. <Conf>
                units.add(parseCond());
                // 4. ')'
                if(getToken(RPARENT)) {
                    units.add(pair);
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 5. <Stmt>
                units.add(parseStmt());
                // 6. 可能有:'else' <Stmt>
                if (getToken(ELSETK)) {
                    // 6.1 'else'
                    units.add(pair);
                    // 6.2 <Stmt>
                    units.add(parseStmt());
                }
                break;
            // 'for' '(' 没有 | <ForStmt> ';' 没有 | <Cond> ';' 没有 | <ForStmt> ')' <Stmt>
            case FORTK:
                // 1. 'for'
                units.add(pair);
                // 2. '('
                getToken(LPARENT);
                units.add(pair);
                // 3. 没有 | <ForStmt>
                getToken();
                if(token != SEMICN) {
                    retract(1);
                    units.add(parseForStmt());
                    getToken();
                }
                // 4. ';'
                units.add(pair);
                // 5. 没有 | <Cond>
                getToken();
                if(token != SEMICN) {
                    retract(1);
                    units.add(parseCond());
                    getToken();
                }
                // 6. ';'
                units.add(pair);
                // 7. 没有 | <ForStmt>
                getToken();
                if(token != RPARENT) {
                    retract(1);
                    units.add(parseForStmt());
                    getToken();
                }
                // 8. ')'
                if(token == RPARENT) {
                    units.add(pair);
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 9. <Stmt>
                units.add(parseStmt());
                break;
            // 'break' ';'
            // 'continue' ';'
            case BREAKTK:
            case CONTINUETK:
                units.add(pair);
                if(getToken(SEMICN)) {
                    units.add(pair);
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'return' ';' | 'return' <Exp> ';'
            case RETURNTK:
                // 1. 'return'
                units.add(pair);
                // 2. 没有 ';' | <Exp> ';'
                getToken();
                if(token == PLUS || token == MINU || token == NOT
                        || token == IDENFR || token == LPARENT
                        || token == INTCON || token == CHRCON) {
                    retract(1);
                    units.add(parseExp());
                    getToken();
                }
                // 3. ';'
                if(token == SEMICN) {
                    units.add(pair);
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
            case PRINTFTK:
                // 1. 'printf'
                units.add(pair);
                // 2. '('
                getToken(LPARENT);
                units.add(pair);
                // 3. <StringConst>
                getToken(STRCON);
                units.add(pair);
                // 4. ',' <Exp> ')' 或 ')'
                while(getToken(COMMA)) {
                    units.add(pair);
                    units.add(parseExp());
                }
                // 5. ')'
                if(getToken(RPARENT)) {
                    units.add(pair);
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 6. ';'
                if(getToken(SEMICN)) {
                    units.add(pair);
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
                // 1. <Exp> -> a();
                int anchor = tokenIndex - 1;
                if(getToken(LPARENT)) {
                    retractAbsolutely(anchor);
                    units.add(parseExp());
                }
                // 2. <LVal> -> a = 1;
                else {
                    // 1. 先扫一遍会不会出现<LVal>中的a[ = {1};错误
                    // fix: in deeper recursive layer, fw will be set original and errorHandler will be turned on
                    errorHandler.turnOff();
                    retractAbsolutely(anchor);
                    parseLVal();
                    errorHandler.turnOn();
                    // 2. 读等号
                    if(getToken(ASSIGN)) {
                        // 1. 重新扫描
                        retractAbsolutely(anchor);
                        units.add(parseLVal());
                        // 2. 读等号
                        getToken(ASSIGN);
                        units.add(pair);
                        // 3. 'getint' | 'getchar' | <Exp>
                        if (getToken(GETINTTK,GETCHARTK)) {
                            // 1. 'getint' | 'getchar'
                            units.add(pair);
                            // 2. '('
                            getToken(LPARENT);
                            units.add(pair);
                            // 3. ')'
                            if (getToken(RPARENT)) {
                                units.add(pair);
                            } else {
                                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                            }
                        } else {
                            units.add(parseExp());
                        }
                    }
                    // 3. 没有等号就是<Exp>
                    else {
                        // 1. 重新扫描
                        retractAbsolutely(anchor);
                        // 2. <Exp>
                        units.add(parseExp());
                    }
                }
                // 3. ;
                if(getToken(SEMICN)) {
                    units.add(pair);
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // <Exp> ';'
            default:
                // 1. 此时只剩其他类型的<Exp>
                retract(1);
                units.add(parseExp());
                if(getToken(SEMICN)) {
                    units.add(pair);
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
        }

        // 4. 返回节点
        return new ParsedUnit("Stmt", units);
    }

    // 1. <ForStmt> = <LVal> '=' <Exp>
    public ParsedUnit parseForStmt() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <LVal>
        units.add(parseLVal());

        // 3. '='
        getToken(ASSIGN);
        units.add(pair);

        // 4. <Exp>
        units.add(parseExp());

        // 5. 返回节点
        return new ParsedUnit("ForStmt", units);
    }

    // 1. <Exp> = <AddExp>
    public ParsedUnit parseExp() throws IOException {
        // 1.units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <AddExp>
        units.add(parseAddExp());

        // 3. 返回节点
        return new ParsedUnit("Exp", units);
    }

    // 2. <Cond> = <LOrExp>
    public ParsedUnit parseCond() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        units.add(parseLOrExp());
        return new ParsedUnit("Cond", units);
    }

    // 3. <LVal> = IDENFR | IDENFR '[' <Exp> ']'
    public ParsedUnit parseLVal() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. IDENFR
        getToken(IDENFR);
        units.add(pair);

        // 3.1 '['
        if (getToken(LBRACK)) {
            units.add(pair);

            // 3.2 <Exp>
            units.add(parseExp());

            // 3.3 ']'
            if(getToken(RBRACK)) {
                units.add(pair);
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 返回节点
        return new ParsedUnit("LVal", units);
    }

    // 4. <PrimaryExp> = '(' + <Exp> + ')', <LVal>, <Number>, <Character>
    public ParsedUnit parsePrimaryExp() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. '(' + <Exp> + ')'
        if (getToken(LPARENT)) {
            units.add(pair);
            units.add(parseExp());
            if(getToken(RPARENT)) {
                units.add(pair);
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
            }
        }

        // 3. <Number>
        else if (getToken(INTCON)) {
            retract(1);
            units.add(parseNumber());
        }

        // 4. <Character>
        else if (getToken(CHRCON)) {
            retract(1);
            units.add(parseCharacter());
        }

        // 5. <LVal>
        else {
            units.add(parseLVal());
        }


        // 6. 返回节点
        return new ParsedUnit("PrimaryExp", units);
    }

    // 5. <Number>
    public ParsedUnit parseNumber() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <Number>
        getToken(INTCON);
        units.add(pair);

        // 3. 返回节点
        return new ParsedUnit("Number", units);
    }

    // 6. <Character>
    public ParsedUnit parseCharacter() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <Character>
        getToken(CHRCON);
        units.add(pair);

        // 3. 返回节点
        return new ParsedUnit("Character", units);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 7. <UnaryExp>, <UnaryOp>, <FuncRParams>, <MulExp>, <AddExp>, <RelExp>, <EqExp>, <LAndExp>, <LOrExp>, <ConstExp>
    // 7.1 <UnaryExp> = <PrimaryExp>, IDENFR '(' 没有 | <FuncRParams> ')', <UnaryOp> <UnaryExp>
    // 7.1 <UnaryOp> = '+', '-', '!'
    // 7.1 <FuncRParams> = <Exp> {, <Exp>}
    // 7.2 <MulExp> = <MulExp> */% <UnaryExp>, <UnaryExp>
    // 7.2 <AddExp> = <AddExp> +- <MulExp>, <MulExp>
    // 7.3 <RelExp> = <RelExp> <><=>= <AddExp>, <AddExp>
    // 7.3 <EqExp> = <EqExp> ==!= <RelExp>, <RelExp>
    // 7.3 <LAndExp> = <LAndExp> && <EqExp>, <EqExp>
    // 7.3 <LOrExp> = <LOrExp> || <LAndExp>, <LAndExp>
    // 7.4 <ConstExp> = <AddExp>
    public ParsedUnit parseUnaryExp() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <UnaryOp> <UnaryExp>
        if (getToken(PLUS, MINU, NOT)) {
            retract(1);
            units.add(parseUnaryOp());
            units.add(parseUnaryExp());
        }

        // 3. IDENFR '(' 没有 | <FuncRParams> ')'
        else if (getToken(IDENFR)) {
            // 3.1 IDENFR
            if (getToken(LPARENT)) {
                // 3.1 IDENFR
                retract(1);
                units.add(pair);

                // 3.2 '('
                getToken(LPARENT);
                units.add(pair);

                // 3. ')' | <FuncRParams> ')'
                // fix: if errors 'j' happens, missing ')', this will enter <parseFuncRParams> branch
                if(getToken(PLUS,MINU,NOT,IDENFR,LPARENT,INTCON,CHRCON)) {
                    // 3.3 回退到起始帧，解析<FuncRParams>
                    retract(1);
                    units.add(parseFuncRParams());
                }

                // 3.4 ')'
                if(getToken(RPARENT)) {
                    units.add(pair);
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
            } else {
                retract(1);
                units.add(parsePrimaryExp());
            }
        }

        // 4. <PrimaryExp>
        else {
            units.add(parsePrimaryExp());
        }

        // 5. 返回节点
        return new ParsedUnit("UnaryExp", units);
    }

    // 7.1 <UnaryExp> = <PrimaryExp>, IDENFR '(' 没有 | <FuncRParams> ')', <UnaryOp> <UnaryExp>
    // 7.1 <UnaryOp> = '+', '-', '!'
    // 7.1 <FuncRParams> = <Exp> {, <Exp>}
    // 7.2 <MulExp> = <MulExp> */% <UnaryExp>, <UnaryExp>
    // 7.2 <AddExp> = <AddExp> +- <MulExp>, <MulExp>
    public ParsedUnit parseUnaryOp() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        getToken(PLUS, MINU, NOT);
        units.add(pair);
        return new ParsedUnit("UnaryOp", units);
    }

    // 7.1 <UnaryExp> = <PrimaryExp>, IDENFR '(' 没有 | <FuncRParams> ')', <UnaryOp> <UnaryExp>
    // 7.1 <UnaryOp> = '+', '-', '!'
    // 7.1 <FuncRParams> = <Exp> {, <Exp>}
    // 7.2 <MulExp> = <MulExp> */% <UnaryExp>, <UnaryExp>
    // 7.2 <AddExp> = <AddExp> +- <MulExp>, <MulExp>
    public ParsedUnit parseFuncRParams() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        units.add(parseExp());
        while (getToken(COMMA)) {
            units.add(pair);
            units.add(parseExp());
        }
        return new ParsedUnit("FuncRParams", units);
    }

    // 7.1 <UnaryExp> = <PrimaryExp>, IDENFR '(' 没有 | <FuncRParams> ')', <UnaryOp> <UnaryExp>
    // 7.1 <UnaryOp> = '+', '-', '!'
    // 7.1 <FuncRParams> = <Exp> {, <Exp>}
    // 7.2 <MulExp> = <MulExp> */% <UnaryExp>, <UnaryExp>
    // 7.2 <AddExp> = <AddExp> +- <MulExp>, <MulExp>
    public ParsedUnit parseMulExp() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <UnaryExp>
        units.add(parseUnaryExp());


        // 3. {'*' | '/' | '%' <UnaryExp>}
        while(getToken(MULT,DIV,MOD)) {
            // 3.1 制作一个preUnits替代subUnits，并且包含了第一个<MulExp>
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(units);

            // 3.2 制作一个preUnit，只包含了此时的preUnits，命名为<MulExp>
            // 3.2 后续<MulExp>加入到preUnits中
            ParsedUnit preUnit = new ParsedUnit("MulExp", preUnits);

            // 3.3 清除units
            units.clear();

            // 3.4 用preUnit，作为<MulExp> = <MulExp> */% <UnaryExp>
            units.add(preUnit);

            // 3.5 */%
            units.add(pair);

            // 3.6 <UnaryExp>
            units.add(parseUnaryExp());
        }

        // 4. 返回<MulExp>，则为<MulExp> = <MulExp> */% <UnaryExp>
        return new ParsedUnit("MulExp", units);
    }

    // 7.1 <UnaryExp> = <PrimaryExp>, IDENFR '(' 没有 | <FuncRParams> ')', <UnaryOp> <UnaryExp>
    // 7.1 <UnaryOp> = '+', '-', '!'
    // 7.1 <FuncRParams> = <Exp> {, <Exp>}
    // 7.2 <MulExp> = <MulExp> */% <UnaryExp>, <UnaryExp>
    // 7.2 <AddExp> = <AddExp> +- <MulExp>, <MulExp>
    public ParsedUnit parseAddExp() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <MulExp>
        units.add(parseMulExp());

        // 3. {'+' | '-' <MulExp>}
        // 3. 此时要将第一个<MulExp>解析成<AddExp>
        while(getToken(PLUS,MINU)) {
            // 3.1 制作一个preUnits替代subUnits，并且包含了第一个<MulExp>
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(units);

            // 3.2 制作一个preUnit，只包含了此时的preUnits，命名为<AddExp>
            // 3.2 后续<MulExp>加入到preUnits中
            ParsedUnit preUnit = new ParsedUnit("AddExp", preUnits);

            // 3.3 清除subUnits
            units.clear();

            // 3.4 用preUnit，作为<AddExp> = <AddExp> +- <MulExp>
            units.add(preUnit);

            // 3.5 +-
            units.add(pair);

            // 3.6 <MulExp>
            units.add(parseMulExp());
        }

        // 3.3 返回<AddExp>，则为<AddExp> = <AddExp> +- <MulExp>
        return new ParsedUnit("AddExp", units);
    }

    // 7.3 <RelExp> = <RelExp> <><=>= <AddExp>, <AddExp>
    // 7.3 <EqExp> = <EqExp> ==!= <RelExp>, <RelExp>
    // 7.3 <LAndExp> = <LAndExp> && <EqExp>, <EqExp>
    // 7.3 <LOrExp> = <LOrExp> || <LAndExp>, <LAndExp>
    // 7.4 <ConstExp> = <AddExp>
    public ParsedUnit parseRelExp() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        units.add(parseAddExp());
        while(getToken(LSS,GRE,LEQ,GEQ)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(units);
            ParsedUnit preUnit = new ParsedUnit("RelExp", preUnits);
            units.clear();
            units.add(preUnit);
            units.add(pair);
            units.add(parseAddExp());
        }
        return new ParsedUnit("RelExp", units);
    }

    // 7.3 <RelExp> = <RelExp> <><=>= <AddExp>, <AddExp>
    // 7.3 <EqExp> = <EqExp> ==!= <RelExp>, <RelExp>
    // 7.3 <LAndExp> = <LAndExp> && <EqExp>, <EqExp>
    // 7.3 <LOrExp> = <LOrExp> || <LAndExp>, <LAndExp>
    // 7.4 <ConstExp> = <AddExp>
    public ParsedUnit parseEqExp() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        units.add(parseRelExp());
        while(getToken(EQL,NEQ)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(units);
            ParsedUnit preUnit = new ParsedUnit("EqExp", preUnits);
            units.clear();
            units.add(preUnit);
            units.add(pair);
            units.add(parseRelExp());
        }
        return new ParsedUnit("EqExp", units);
    }

    // 7.3 <RelExp> = <RelExp> <><=>= <AddExp>, <AddExp>
    // 7.3 <EqExp> = <EqExp> ==!= <RelExp>, <RelExp>
    // 7.3 <LAndExp> = <LAndExp> && <EqExp>, <EqExp>
    // 7.3 <LOrExp> = <LOrExp> || <LAndExp>, <LAndExp>
    // 7.4 <ConstExp> = <AddExp>
    public ParsedUnit parseLAndExp() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        units.add(parseEqExp());
        while(getToken(AND)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(units);
            ParsedUnit preUnit = new ParsedUnit("LAndExp", preUnits);
            units.clear();
            units.add(preUnit);
            units.add(pair);
            units.add(parseEqExp());
        }
        return new ParsedUnit("LAndExp", units);
    }

    // 7.3 <RelExp> = <RelExp> <><=>= <AddExp>, <AddExp>
    // 7.3 <EqExp> = <EqExp> ==!= <RelExp>, <RelExp>
    // 7.3 <LAndExp> = <LAndExp> && <EqExp>, <EqExp>
    // 7.3 <LOrExp> = <LOrExp> || <LAndExp>, <LAndExp>
    // 7.4 <ConstExp> = <AddExp>
    public ParsedUnit parseLOrExp() throws IOException {
        LinkedList<ParsedUnit> units = new LinkedList<>();
        units.add(parseLAndExp());
        while(getToken(OR)) {
            LinkedList<ParsedUnit> preUnits = new LinkedList<>(units);
            ParsedUnit preUnit = new ParsedUnit("LOrExp", preUnits);
            units.clear();
            units.add(preUnit);
            units.add(pair);
            units.add(parseLAndExp());
        }
        return new ParsedUnit("LOrExp", units);
    }

    // 7.3 <RelExp> = <RelExp> <><=>= <AddExp>, <AddExp>
    // 7.3 <EqExp> = <EqExp> ==!= <RelExp>, <RelExp>
    // 7.3 <LAndExp> = <LAndExp> && <EqExp>, <EqExp>
    // 7.3 <LOrExp> = <LOrExp> || <LAndExp>, <LAndExp>
    // 7.4 <ConstExp> = <AddExp>
    public ParsedUnit parseConstExp() throws IOException {
        // 1. units
        LinkedList<ParsedUnit> units = new LinkedList<>();

        // 2. <AddExp>
        units.add(parseAddExp());

        return new ParsedUnit("ConstExp", units);
    }
}
