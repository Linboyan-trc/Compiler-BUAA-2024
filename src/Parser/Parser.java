package Parser;

import ErrorHandler.ErrorHandler;
import ErrorHandler.ErrorRecord;
import Lexer.Lexer;
import Lexer.Pair;
import Lexer.Token;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;
import SyntaxTree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private int forDepth = 0;
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

    public boolean getToken(Token... tokenExpecteds) throws IOException {
        // 1.1 当用到最新token的时候才需要读取新的token
        tokenIndex++;
        if (tokens.size() == tokenIndex) {
            tokens.add(lexer.parseAndGetPair());
        }

        // 1.2 没有用到最新token从当前token拿就可以
        // 1.2.1 如果符合期望就正常读
        for(Token tokenExpected:tokenExpecteds) {
            if(tokens.get(tokenIndex).getToken() == tokenExpected) {
                pair = tokens.get(tokenIndex);
                token = tokens.get(tokenIndex).getToken();
                return true;
            }
        }

        // 1.2.2 如果不符合期望就取消这次token的读取，防止影响后续语法分析
        tokenIndex--;
        return false;
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
    public CompUnit parseCompUnit() throws IOException {
        // 1. 创建顶层语法点
        CompUnit compUnit = new CompUnit();

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
        // 2. 获取第一个Token
        getToken();
        while(token == Token.CONSTTK || token == Token.INTTK || token == Token.CHARTK) {
            // 1. <ConstDecl>
            if(token == Token.CONSTTK) {
                retract(1);
                compUnit.addDeclNode(parseConstDecl());
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
                compUnit.addDeclNode(parseVarDecl());
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
                compUnit.addDeclNode(parseVarDecl());
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
                compUnit.addFuncDefNode(parseFuncDef());
            }
            // 2. int main() 和 int a()
            else if (token == Token.INTTK) {
                getToken();
                if (token != Token.IDENFR) {
                    retract(1);
                    break;
                }
                retract(2);
                compUnit.addFuncDefNode(parseFuncDef());
            }
            // 3. char a()
            else {
                retract(1);
                compUnit.addFuncDefNode(parseFuncDef());
            }
            getToken();
        }

        // 4. 解析MainFuncDef
        // 4.1 回退一开始读的int
        // 4.2 解析<MainFuncDef>
        // 4.3 最后追加语法成分
        retract(1);
        compUnit.setMainFuncDefNode(parseMainFuncDef());
        return compUnit;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 2. 解析<ConstDecl>, <ConstDef>, <ConstInitVal>
    public DeclNode parseConstDecl() throws IOException {
        // 1. 解析<ConstDecl>
        // 1. 包含'const' + BType + <ConstDef>:
            // const int a = 0;
            // const int a[10] = {0,1,2,...,9};
            // const char a = 0;
            // const char a[10] = {'0','1','2',...,'9'};

        // 1.1 读取'const'
        getToken();
        // 1.2 读取BType
        // 1.2 创建<DeclNode>
        getToken();
        DeclNode declNode = null;
        if(token == Token.INTTK) {
            declNode = new DeclNode(SyntaxType.ConstInt);
        } else {
            declNode = new DeclNode(SyntaxType.ConstChar);
        }
        // 1.3 解析<ConstDef>
        declNode.addDefNode(parseConstDef(declNode));

        // 2. 解析完继续获取下一个Token
        // 2.1 如果是',',那么继续解析<ConstDef>,并不断获取下一个Token
        // 2.2 输出';'的Token
        // 2.3 追加语法成分
        while(getToken(Token.COMMA)) {
            declNode.addDefNode(parseConstDef(declNode));
        }
        if (!getToken(Token.SEMICN)) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }
        return declNode;
    }

    public DefNode parseConstDef(DeclNode declNode) throws IOException {
        // 1. 解析<ConstDef>
        // 1. 包含
            // IDENFR
            // 可能有: '[' + <ConstExp> + ']'
            // '=' + <ConstInitVal>

        // 1.读IDENFR的Token
        // 1. 创建DefNode节点,传入IDENFR
        getToken();
        DefNode defNode = new DefNode(declNode,pair);

        // 2.读下一个Token
        // 2.1 如果是'['就是数组
            // 解析<ConstExp>
            // ']'
        if(getToken(Token.LBRACK)) {
            defNode.setLength(parseConstExp());
            defNode.getParent().toArray();
            if(!getToken(Token.RBRACK)) {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 3.1 读'=''
        getToken();

        // 3.2 解析<ConstInitVal>
        // 3.3 追加语法成分
        defNode.setInitValues(parseConstInitVal());

        // 4. 符号表: 添加一个变量
        symbolTable.addToVariables(defNode);
        return defNode;
    }

    public LinkedList<ExpNode> parseConstInitVal() throws IOException {
        // 1. 解析<ConstInitVal>
        // 1.1 有三种可能: <ConstExp> | {<ConstExp>, ... } | <StirngConst> -> "..."

        // 1.{
            // 1.1 }
            // 1.2 <ConstExp> }

        // 1. 初始化一个LinkedList<ExpNode>
        LinkedList<ExpNode> initValues = new LinkedList<>();

        // 1.1 {<ConstExp>, ... }
        getToken();
        if (token == Token.LBRACE) {
            // 1.1 <ConstExp>, ... }
            getToken();
            if (token != Token.RBRACE) {
                retract(1);
                initValues.add(parseConstExp());
                getToken();
                while(token == Token.COMMA) {
                    initValues.add(parseConstExp());
                    getToken();
                }
            }
        }
        // 2."..."
        else if (token == Token.STRCON) {
            retract(1);
            // TODO: 把字符串转化为字符存储到initValues
            parseStringConst();
        }
        // 3. <ConstExp>
        else {
            retract(1);
            initValues.add(parseConstExp());
        }

        // 2. 追加语法成分
        return initValues;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 3. 解析<VarDecl>, <VarDef>, <InitVal>
    public DeclNode parseVarDecl() throws IOException {
        // 1. 解析<VarDecl>
        // 1. <VarDecl> = BType <VarDef> { ',' <VarDef> } ';'

        // 1. 先读BType
        getToken();
        DeclNode declNode = null;
        if(token == Token.INTTK) {
            declNode = new DeclNode(SyntaxType.Int);
        } else {
            declNode = new DeclNode(SyntaxType.Char);
        }

        // 2. 解析<VarDef>
        declNode.addDefNode(parseVarDef(declNode));

        // 3. 如果下一个是,继续解析<VarDef>
        while(getToken(Token.COMMA)) {
            declNode.addDefNode(parseVarDef(declNode));
        }

        // 4. 解析';'
        // 4. 最后追加语法成分
        if (!getToken(Token.SEMICN)) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }
        return declNode;
    }

    public DefNode parseVarDef(DeclNode parent) throws IOException {
        // 1. 解析<VarDef>
        // 1. <VarDef>有两种情况
            // IDENFR 或 IDENFR '[' + <ConstExp> + ']' -> 也就是不赋初值
            // IDENFR '=' + <InitVal> 或 IDENFR '[' + <ConstExp> + ']' + '=' + <InitVal> -> 也就是赋初值

        // 2. 解析IDENFR
        // 2. 创建<DefNode>
        getToken();
        DefNode defNode = new DefNode(parent,pair);

        // 3. 如果是数组
        // 3.1 解析'['
        // 3.2 解析<ConstExp>
        // 3.3 解析']'
        if(getToken(Token.LBRACK)) {
            defNode.setLength(parseConstExp());
            defNode.getParent().toArray();
            if(!getToken(Token.RBRACK)) {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 解析'='
        // 4.1 如果可以解析'=', 说明有<InitVal>
        // 4.2 追加语法成分
        if(getToken(Token.ASSIGN)) {
            defNode.setInitValues(parseInitVal());
        }

        // 5. 符号表
        symbolTable.addToVariables(defNode);
        return defNode;
    }

    public LinkedList<ExpNode> parseInitVal() throws IOException {
        // 1. 解析<InitVal>
        // 2. <InitVal>有三种
            // <Exp>
            // { <Exp>, ... }
            // <StringConst>

        // 1. 初始化一个LinkedList<ExpNode>
        LinkedList<ExpNode> initValues = new LinkedList<>();

        // 1. 获取第一个Token
        // 1.1 查看是否是'{'
        // 不是则回退直接解析<Exp>
        // 否则解析'{',然后循环解析<Exp>
        // 1.2 最后追加语法成分
        // 1.3.1 { <Exp>, ... }
        getToken();
        if (token == token.LBRACE) {
            // 1. <Exp>,<Exp>
            getToken();
            if (token != Token.RBRACE) {
                retract(1);
                initValues.add(parseExp());
                getToken();
                while(token == Token.COMMA) {
                    initValues.add(parseExp());
                    getToken();
                }
            }
        }
        // 1.3.2 <StringConst>
        else if (token == Token.STRCON) {
            retract(1);
            // TODO: 把字符串转化为字符存储到initValues
            parseStringConst();
        }
        // 1.3.3 <Exp>
        else {
            retract(1);
            initValues.add(parseExp());
        }

        // 2. 追加语法成分
        return initValues;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 4. 解析<FuncDef>, <FuncType>, <FuncFParams>, <FuncFParam>
    public FuncDefNode parseFuncDef() throws IOException {
        // 1. 解析<FuncDef>
        // 1. <FuncDef> = <FuncType> IDENFR '(' <FuncFParams>')' <Block>

        // 2.1 解析<FuncType>
        // 2.1 初始化一个<FuncDefNode> = funcDefType + pair + 参数列表 + 块
        parseFuncType();
        FuncDefNode funcDefNode = null;
        if(token == Token.VOIDTK){
            funcDefNode = new FuncDefNode(SyntaxType.VoidFunc);
        } else if (token == Token.INTTK) {
            funcDefNode = new FuncDefNode(SyntaxType.IntFunc);
        } else {
            funcDefNode = new FuncDefNode(SyntaxType.CharFunc);
        }

        // 2.2 解析IDENFR
        getToken();
        funcDefNode.setPair(pair);
        symbolTable.addToFunctions(funcDefNode);
        symbolTable = new SymbolTable(symbolTable);

        // 2.3 解析'('
        getToken();

        // 2.4 解析<FuncFParams>
        // 2.4 如果不是')', 说明是<FuncFParams>，回退一位然后解析
        // 2.4 否则就是')', 直接解析即可
        getToken();
        if (token != Token.RPARENT && token != Token.LBRACE) {
            retract(1);
            funcDefNode.setFuncFParams(parseFuncFParams());
            getToken();
        }
        if(token != Token.RPARENT) {
            retract(1);
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
        }
        // 2.5 解析<Block>
        // 2.5 追加语法成分
        funcDefNode.setBlockNode(parseBlock());

        // 3. 符号表退栈
        symbolTable = symbolTable.getParent();
        return funcDefNode;
    }

    public void parseFuncType() throws IOException {
        getToken();
    }

    public LinkedList<FuncFParamNode> parseFuncFParams() throws IOException {
        // 1. 解析<FuncFParams>
        // 1. <FuncFParams> = <FuncFParam> { ',' <FuncFParam> }

        // 1. 创建LinkedList<FuncFParamNode>
        LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();

        // 1. 解析<FuncFParam>
        funcFParamNodes.add(parseFuncFParam());

        // 2. 循环解析,' <FuncFParam>
        while(getToken(Token.COMMA)) {
            funcFParamNodes.add(parseFuncFParam());
        }
        return funcFParamNodes;
    }

    public FuncFParamNode parseFuncFParam() throws IOException {
        // 1. 解析<FuncFParam>
        // 1. <FuncFParam>
            // BType + IDENFR
            // BType + IDENFR + '[' + ']'

        // 1. BType
        getToken();

        // 2. IDENFR
        getToken();
        FuncFParamNode funcFParamNode = new FuncFParamNode(pair);

        // 3. '['
        if (getToken(Token.LBRACK)) {
            funcFParamNode.setLength(new NumberNode(0));
            if(!getToken(Token.RBRACK)) {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 符号表
        symbolTable.addToVariables(funcFParamNode);

        // 5. 追加语法成分
        return funcFParamNode;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 5. <MainFuncDef>, <Block>, <BlockItem>
    public FuncDefNode parseMainFuncDef() throws IOException {
        // 1. 解析<MainFuncDef>
        // 1. <MainFuncDef> = int main() <Block>

        // 1. int
        getToken();
        FuncDefNode mainFuncDefNode = new FuncDefNode();

        // 2. main
        getToken();

        // 3. ()
        getToken();
        if (!getToken(Token.RPARENT)) {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
        }

        // 4. 建立新的符号表
        symbolTable = new SymbolTable(symbolTable);

        // 5. <Block>
        mainFuncDefNode.setBlockNode(parseBlock());

        // 6. 符号表退栈
        symbolTable = symbolTable.getParent();

        // 7. 追加语法成分
        return mainFuncDefNode;
    }

    public BlockNode parseBlock() throws IOException {
        // 1. 解析<Block>
        // 1. <Block> = '{' + {BlockItem} + '}'

        // 1. 创建节点
        BlockNode blockNode = new BlockNode();

        // 1. '{'
        getToken();

        // 2. <BlockItem>
        getToken();
        while(token != Token.RBRACE) {
            retract(1);
            blockNode.addBlockItemNode(parseBlockItem());
            getToken();
        }

        // 3. '}'

        // 4. 追加语法成分
        return blockNode;
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

        // 1. 创建节点
        StmtNode stmtNode = new NopNode();

        getToken();
        switch (token) {
            // ';'
            case SEMICN:
                break;
            // <Blcok>
            case LBRACE:
                retract(1);
                symbolTable = new SymbolTable(symbolTable);
                stmtNode = parseBlock();
                symbolTable = symbolTable.getParent();
                break;
            // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
            case IFTK:
                // 1. 更新节点
                stmtNode = new BranchNode();
                // 2. 'if'
                // 3. '('
                getToken();
                // 4. <Conf>
                ((BranchNode) stmtNode).setCond(parseCond());
                // 5. ')'
                if(!getToken(Token.RPARENT)) {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 6. <Stmt>
                ((BranchNode) stmtNode).setIfStmt(parseStmt());
                // 7. 可能有:'else' <Stmt>
                if (getToken(Token.ELSETK)) {
                    ((BranchNode) stmtNode).setElseStmt(parseStmt());
                }
                break;
            // 'for' '(' 没有 | <ForStmt> ';' 没有 | <Cond> ';' 没有 | <ForStmt> ')' <Stmt>
            case FORTK:
                // 1. 更新节点
                stmtNode = new ForNode();
                // 2. 'for'
                // 3. '('
                getToken();
                // 4. 没有 | <ForStmt>
                getToken();
                if(token != Token.SEMICN) {
                    retract(1);
                    ((ForNode) stmtNode).setForStmtNodeFirst(parseForStmt());
                    getToken();
                }
                // 4. ';'
                // 5. 没有 | <Cond>
                getToken();
                if(token != Token.SEMICN) {
                    retract(1);
                    ((ForNode) stmtNode).setCond(parseCond());
                    getToken();
                }
                // 6. ';'
                // 7. 没有 | <ForStmt>
                getToken();
                if(token != Token.RPARENT) {
                    retract(1);
                    ((ForNode) stmtNode).setForStmtNodeSecond(parseForStmt());
                    getToken();
                }
                // 8. ')'
                if(token != Token.RPARENT) {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 9. <Stmt>
                forDepth++;
                ((ForNode) stmtNode).setStmt(parseStmt());
                forDepth--;
                break;
            // 'break' ';'
            case BREAKTK:
                // 1. 更新节点
                stmtNode = new BreakNode();
                if(!getToken(Token.SEMICN)){
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'continue' ';'
            case CONTINUETK:
                // 1.更新节点
                stmtNode = new ContinueNode();
                if(!getToken(Token.SEMICN)) {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'return' ';' | 'return' <Exp> ';'
            case RETURNTK:
                // 1. 更新节点
                stmtNode = new ReturnNode(curToken);
                // 2. 'return'
                // 3. 没有 ';' | <Exp> ';'
                getToken();
                if(token == Token.PLUS || token == Token.MINU || token == Token.NOT
                        || token == Token.IDENFR || token == Token.LPARENT
                        || token == Token.INTCON || token == Token.CHRCON) {
                    retract(1);
                    ((ReturnNode) stmtNode).setExpNode(parseExp());
                    getToken();
                }
                // 4. ';'
                if(token != Token.SEMICN) {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
            case PRINTFTK:
                // 1. 更新节点
                stmtNode = new PrintNode();
                // 2. 'printf'
                // 3. '('
                getToken();
                // 4. <StringConst>
                ((PrintNode) stmtNode).setPair(parseStringConst());
                // 5. ',' <Exp> ')' 或 ')'
                while(getToken(Token.COMMA)) {
                    ((PrintNode) stmtNode).addArgument(parseExp());
                }
                // 6. ')'
                if(!getToken(Token.RPARENT)) {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 7. ';'
                if(!getToken(Token.SEMICN)) {
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
