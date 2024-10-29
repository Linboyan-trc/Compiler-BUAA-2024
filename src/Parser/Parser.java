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
    // 2. 输出文件
    FileWriter fw;
    FileWriter fwOrigin;
    // 3. 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getInstance();
    // 4. 语义分析
    private int forDepth = 0;
    private SymbolTable symbolTable  = new SymbolTable(null,1);

    public Parser(Lexer lexer, FileWriter fw) {
        this.lexer = lexer;
        this.fw = fw;
        this.fwOrigin = fw;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
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
        fw.write("<CompUnit>\n");
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
        fw.write(pair.toString() + "\n");
        // 1.2 读取BType
        // 1.2 创建<DeclNode>
        getToken();
        fw.write(pair.toString() + "\n");
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
            fw.write(pair.toString() + "\n");
            declNode.addDefNode(parseConstDef(declNode));
        }
        if (getToken(Token.SEMICN)) {
            fw.write(pair.toString() + "\n");
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }
        fw.write("<ConstDecl>\n");
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
        fw.write(pair.toString() + "\n");
        DefNode defNode = new DefNode(declNode,pair);

        // 2.读下一个Token
        // 2.1 如果是'['就是数组
            // 解析<ConstExp>
            // ']'
        if(getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            defNode.setLength(parseConstExp());
            defNode.getParent().toArray();
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 3.1 读'=''
        getToken();
        fw.write(pair.toString() + "\n");

        // 3.2 解析<ConstInitVal>
        // 3.3 追加语法成分
        parseConstInitVal(defNode);

        // 4. 符号表: 添加一个变量
        symbolTable.addToVariables(defNode);
        fw.write("<ConstDef>\n");
        return defNode;
    }

    public void parseConstInitVal(DefNode defNode) throws IOException {
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
            fw.write(pair.toString() + "\n");
            // 1.1 <ConstExp>, ... }
            getToken();
            if (token != Token.RBRACE) {
                retract(1);
                initValues.add(parseConstExp());
                getToken();
                while(token == Token.COMMA) {
                    fw.write(pair.toString() + "\n");
                    initValues.add(parseConstExp());
                    getToken();
                }
            }
            // 1.2 '}'
            fw.write(pair.toString() + "\n");
        }
        // 2."..."
        else if (token == Token.STRCON) {
            retract(1);
            defNode.setInitValueForSTRCON(parseStringConst());
        }
        // 3. <ConstExp>
        else {
            retract(1);
            initValues.add(parseConstExp());
        }

        // 2. 追加语法成分
        fw.write("<ConstInitVal>\n");
        defNode.setInitValues(initValues);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 3. 解析<VarDecl>, <VarDef>, <InitVal>
    public DeclNode parseVarDecl() throws IOException {
        // 1. 解析<VarDecl>
        // 1. <VarDecl> = BType <VarDef> { ',' <VarDef> } ';'

        // 1. 先读BType
        getToken();
        fw.write(pair.toString() + "\n");
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
            fw.write(pair.toString() + "\n");
            declNode.addDefNode(parseVarDef(declNode));
        }

        // 4. 解析';'
        // 4. 最后追加语法成分
        if (getToken(Token.SEMICN)) {
            fw.write(pair.toString() + "\n");
        } else {
            errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
        }
        fw.write("<VarDecl>\n");
        return declNode;
    }

    public DefNode parseVarDef(DeclNode declNode) throws IOException {
        // 1. 解析<VarDef>
        // 1. <VarDef>有两种情况
            // IDENFR 或 IDENFR '[' + <ConstExp> + ']' -> 也就是不赋初值
            // IDENFR '=' + <InitVal> 或 IDENFR '[' + <ConstExp> + ']' + '=' + <InitVal> -> 也就是赋初值

        // 2. 解析IDENFR
        // 2. 创建<DefNode>
        getToken();
        fw.write(pair.toString() + "\n");
        DefNode defNode = new DefNode(declNode,pair);

        // 3. 如果是数组
        // 3.1 解析'['
        // 3.2 解析<ConstExp>
        // 3.3 解析']'
        if(getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            defNode.setLength(parseConstExp());
            defNode.getParent().toArray();
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
            parseInitVal(defNode);
        }

        // 5. 符号表
        symbolTable.addToVariables(defNode);
        fw.write("<VarDef>\n");
        return defNode;
    }

    public void parseInitVal(DefNode defNode) throws IOException {
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
            // 1. '{'
            fw.write(pair.toString() + "\n");
            // 2. <Exp>,<Exp>
            getToken();
            if (token != Token.RBRACE) {
                retract(1);
                initValues.add(parseExp());
                getToken();
                while(token == Token.COMMA) {
                    fw.write(pair.toString() + "\n");
                    initValues.add(parseExp());
                    getToken();
                }
            }
            // 3. '}'
            fw.write(pair.toString() + "\n");
        }
        // 1.3.2 <StringConst>
        else if (token == Token.STRCON) {
            retract(1);
            defNode.setInitValueForSTRCON(parseStringConst());
        }
        // 1.3.3 <Exp>
        else {
            retract(1);
            initValues.add(parseExp());
        }

        // 2. 追加语法成分
        fw.write("<InitVal>\n");
        defNode.setInitValues(initValues);
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
        fw.write(pair.toString() + "\n");
        funcDefNode.setPair(pair);
        symbolTable.addToFunctions(funcDefNode);
        symbolTable = new SymbolTable(symbolTable);
        symbolTable.getParent().addChild(symbolTable);

        // 2.3 解析'('
        getToken();
        fw.write(pair.toString() + "\n");

        // 2.4 解析<FuncFParams>
        // 2.4 如果不是')', 说明是<FuncFParams>，回退一位然后解析
        // 2.4 否则就是')', 直接解析即可
        getToken();
        if (token != Token.RPARENT && token != Token.LBRACE) {
            retract(1);
            funcDefNode.setFuncFParams(parseFuncFParams());
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
        funcDefNode.setBlockNode(parseBlock());

        // 3. 符号表退栈
        symbolTable = symbolTable.getParent();
        fw.write("<FuncDef>\n");
        return funcDefNode;
    }

    public void parseFuncType() throws IOException {
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<FuncType>\n");
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
            fw.write(pair.toString() + "\n");
            funcFParamNodes.add(parseFuncFParam());
        }
        fw.write("<FuncFParams>\n");
        return funcFParamNodes;
    }

    public FuncFParamNode parseFuncFParam() throws IOException {
        // 1. 解析<FuncFParam>
        // 1. <FuncFParam>
            // BType + IDENFR
            // BType + IDENFR + '[' + ']'

        // 1. BType
        getToken();
        fw.write(pair.toString() + "\n");
        SyntaxType type;
        if(token == Token.INTTK) {
            type = SyntaxType.Int;
        } else {
            type = SyntaxType.Char;
        }

        // 2. IDENFR
        getToken();
        fw.write(pair.toString() + "\n");
        FuncFParamNode funcFParamNode = new FuncFParamNode(type,pair);

        // 3. '['
        if (getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            funcFParamNode.toArray();
            funcFParamNode.setLength(new NumberNode(0));
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }

        // 4. 符号表
        symbolTable.addToVariables(funcFParamNode);

        // 5. 追加语法成分
        fw.write("<FuncFParam>\n");
        return funcFParamNode;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 5. <MainFuncDef>, <Block>, <BlockItem>
    public FuncDefNode parseMainFuncDef() throws IOException {
        // 1. 解析<MainFuncDef>
        // 1. <MainFuncDef> = int main() <Block>

        // 1. int
        getToken();
        fw.write(pair.toString() + "\n");
        FuncDefNode mainFuncDefNode = new FuncDefNode();

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

        // 4. 建立新的符号表
        symbolTable = new SymbolTable(symbolTable);
        symbolTable.getParent().addChild(symbolTable);

        // 5. <Block>
        mainFuncDefNode.setBlockNode(parseBlock());

        // 6. 符号表退栈
        symbolTable = symbolTable.getParent();

        // 7. 追加语法成分
        fw.write("<MainFuncDef>\n");
        return mainFuncDefNode;
    }

    public BlockNode parseBlock() throws IOException {
        // 1. 解析<Block>
        // 1. <Block> = '{' + {BlockItem} + '}'

        // 1. 创建节点
        BlockNode blockNode = new BlockNode();

        // 1. '{'
        getToken();
        fw.write(pair.toString() + "\n");

        // 2. <BlockItem>
        getToken();
        while(token != Token.RBRACE) {
            retract(1);
            blockNode.addBlockItemNode(parseBlockItem());
            getToken();
        }

        // 3. '}'
        fw.write(pair.toString() + "\n");

        // 4. 追加语法成分
        fw.write("<Block>\n");
        return blockNode;
    }

    public BlockItemNode parseBlockItem() throws IOException {
        // 1. 解析<BlockItem>
        // 1. <BlockItem> = <ConstDecl> | <VarDecl> | <Stmt>

        // 1. <ConstDecl>
        getToken();
        if(token == Token.CONSTTK) {
            retract(1);
            return parseConstDecl();
        }
        // 2. <VarDecl>
        else if (token == Token.INTTK || token == Token.CHARTK) {
            retract(1);
            return parseVarDecl();
        }
        // 3. <Stmt>
        else {
            retract(1);
            return parseStmt();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 6. <Stmt>, <ForStmt>, <Exp>, <Cond>, <LVal>, <PrimaryExp>, <Number>, <Character>, <StringConst>
    public StmtNode parseStmt() throws IOException {
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
                fw.write(pair.toString() + "\n");
                break;
            // <Blcok>
            case LBRACE:
                retract(1);
                symbolTable = new SymbolTable(symbolTable);
                symbolTable.getParent().addChild(symbolTable);
                stmtNode = parseBlock();
                symbolTable = symbolTable.getParent();
                break;
            // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
            case IFTK:
                // 1. 更新节点
                stmtNode = new BranchNode();
                // 2. 'if'
                fw.write(pair.toString() + "\n");
                // 3. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 4. <Conf>
                ((BranchNode) stmtNode).setCond(parseCond());
                // 5. ')'
                if(getToken(Token.RPARENT)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 6. <Stmt>
                ((BranchNode) stmtNode).setIfStmt(parseStmt());
                // 7. 可能有:'else' <Stmt>
                if (getToken(Token.ELSETK)) {
                    fw.write(pair.toString() + "\n");
                    ((BranchNode) stmtNode).setElseStmt(parseStmt());
                }
                break;
            // 'for' '(' 没有 | <ForStmt> ';' 没有 | <Cond> ';' 没有 | <ForStmt> ')' <Stmt>
            case FORTK:
                // 1. 更新节点
                stmtNode = new ForNode();
                // 2. 'for'
                fw.write(pair.toString() + "\n");
                // 3. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 4. 没有 | <ForStmt>
                getToken();
                if(token != Token.SEMICN) {
                    retract(1);
                    ((ForNode) stmtNode).setForStmtNodeFirst(parseForStmt());
                    getToken();
                }
                // 4. ';'
                fw.write(pair.toString() + "\n");
                // 5. 没有 | <Cond>
                getToken();
                if(token != Token.SEMICN) {
                    retract(1);
                    ((ForNode) stmtNode).setCond(parseCond());
                    getToken();
                }
                // 6. ';'
                fw.write(pair.toString() + "\n");
                // 7. 没有 | <ForStmt>
                getToken();
                if(token != Token.RPARENT) {
                    retract(1);
                    ((ForNode) stmtNode).setForStmtNodeSecond(parseForStmt());
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
                forDepth++;
                ((ForNode) stmtNode).setStmt(parseStmt());
                forDepth--;
                break;
            // 'break' ';'
            case BREAKTK:
                // 1. 更新节点
                stmtNode = new BreakNode();
                // 2.
                fw.write(pair.toString() + "\n");
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'continue' ';'
            case CONTINUETK:
                // 1.更新节点
                stmtNode = new ContinueNode();
                // 2.
                fw.write(pair.toString() + "\n");
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'return' ';' | 'return' <Exp> ';'
            case RETURNTK:
                // 1. 更新节点
                stmtNode = new ReturnNode(pair);
                // 2. 'return'
                fw.write(pair.toString() + "\n");
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
                if(token == Token.SEMICN) {
                    fw.write(pair.toString() + "\n");
                } else {
                    retract(1);
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
            // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
            case PRINTFTK:
                // 1. 更新节点
                stmtNode = new PrintNode();
                // 2. 'printf'
                fw.write(pair.toString() + "\n");
                // 3. '('
                getToken();
                fw.write(pair.toString() + "\n");
                // 4. <StringConst>
                ((PrintNode) stmtNode).setString(parseStringConst());
                // 5. ',' <Exp> ')' 或 ')'
                while(getToken(Token.COMMA)) {
                    fw.write(pair.toString() + "\n");
                    ((PrintNode) stmtNode).addArgument(parseExp());
                }
                // 6. ')'
                if(!getToken(Token.RPARENT)) {
                    fw.write(pair.toString() + "\n");
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                }
                // 7. ';'
                if(!getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
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
                        LValNode lValNode = parseLVal();
                        // 2. 读等号
                        getToken();
                        fw.write(pair.toString() + "\n");
                        // 3. 'getint' | 'getchar' | <Exp>
                        getToken();
                        if (token == Token.GETINTTK || token == Token.GETCHARTK) {
                            // 1. 更新节点
                            // 1. 'getint' | 'getchar'
                            stmtNode = new GetIntNode(lValNode);
                            fw.write(pair.toString() + "\n");
                            // 2. '('
                            getToken();
                            fw.write(pair.toString() + "\n");
                            // 3. ')'
                            if (getToken(Token.RPARENT)) {
                                fw.write(pair.toString() + "\n");
                            } else {
                                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
                            }
                        } else {
                            // 1. 更新节点
                            stmtNode = new AssignNode();
                            // 2. 设置节点
                            ((AssignNode) stmtNode).setLValNode(lValNode);
                            retract(1);
                            ((AssignNode) stmtNode).setExpNode(parseExp());
                        }
                    }
                    // 3. 没有等号就是<Exp>
                    else {
                        // 1. 重新扫描
                        retractAbsolutely(anchor);
                        // 2. <Exp>
                        stmtNode = parseExp();
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
                stmtNode = parseExp();
                if(getToken(Token.SEMICN)) {
                    fw.write(pair.toString() + "\n");
                } else {
                    errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'i'));
                }
                break;
        }
        fw.write("<Stmt>\n");
        return stmtNode;
    }

    public ForStmtNode parseForStmt() throws IOException {
        // 1. 解析<ForStmt>
        // 1. <ForStmt> = <LVal> '=' <Exp>
        ForStmtNode forStmtNode = new ForStmtNode();

        // 1. <LVal>
        forStmtNode.setlValNode(parseLVal());
        // 2. '='
        getToken();
        fw.write(pair.toString() + "\n");
        // 3. <Exp>
        forStmtNode.setExpNode(parseExp());
        // 4. 追加语法成分
        fw.write("<ForStmt>\n");
        return forStmtNode;
    }

    public ExpNode parseExp() throws IOException {
        // 1. 解析<Exp>
        // 2. <Exp> = <AddExp>
        ExpNode expNode = parseAddExp();
        fw.write("<Exp>\n");
        return expNode;
    }

    public ExpNode parseCond() throws IOException {
        // 1. 解析<Cond>
        // 2. <Cond> = <LOrExp>
        ExpNode expNode = parseLOrExp();
        fw.write("<Cond>\n");
        return expNode;
    }

    public LValNode parseLVal() throws IOException {
        // 1. 解析<LVal>
        // 2. <LVal> = IDENFR | IDENFR [ <Exp> ]

        // 1. 创建节点
        LValNode lValNode = new LValNode();

        // 2. IDENFR
        getToken();
        fw.write(pair.toString() + "\n");
        lValNode.setPair(pair);

        // 3. 没有 | [ <Exp> ]
        if (getToken(Token.LBRACK)) {
            fw.write(pair.toString() + "\n");
            lValNode.setLength(parseExp());
            if(getToken(Token.RBRACK)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'k'));
            }
        }
        fw.write("<LVal>\n");
        return lValNode;
    }

    public ExpNode parsePrimaryExp() throws IOException {
        // 1. 解析<PrimaryExp>
        // 1. <PrimaryExp>
            // '(' + <Exp> + ')'
            // <LVal>
            // <Number>
            // <Character>

        // 1. 创建节点
        ExpNode expNode;

        // 2. '(' + <Exp> + ')'
        getToken();
        if (token == Token.LPARENT) {
            fw.write(pair.toString() + "\n");
            expNode = parseExp();
            if(getToken(Token.RPARENT)) {
                fw.write(pair.toString() + "\n");
            } else {
                errorHandler.addError(new ErrorRecord(pair.getLineNumber(), 'j'));
            }
        }
        // 3. <Number>
        else if (token == Token.INTCON) {
            retract(1);
            expNode = parseNumber();
        }
        // 4. <Character>
        else if (token == Token.CHRCON) {
            retract(1);
            expNode = parseCharacter();
        }
        // 5. <LVal>
        else {
            retract(1);
            expNode = parseLVal();
        }
        fw.write("<PrimaryExp>\n");
        return expNode;
    }

    public NumberNode parseNumber() throws IOException {
        NumberNode numberNode = new NumberNode(pair.getValue());
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<Number>\n");
        return numberNode;
    }

    public CharacterNode parseCharacter() throws IOException {
        CharacterNode characterNode = new CharacterNode(pair.getWord());
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<Character>\n");
        return characterNode;
    }

    public String parseStringConst() throws IOException {
        getToken();
        fw.write(pair.toString() + "\n");
        //fw.write("<StringConst>\n");
        return pair.getWord();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 7. <UnaryExp>, <UnaryOp>, <FuncRParams>, <MulExp>, <AddExp>, <RelExp>, <EqExp>, <LAndExp>, <LOrExp>, <ConstExp>
    public ExpNode parseUnaryExp() throws IOException {
        // 1. 解析<UnaryExp>
        // 1. <UnaryExp>
            // <PrimaryExp>
            // IDENFR '(' 没有 | <FuncRParams> ')'
            // <UnaryOp> <UnaryExp>

        // 1. <UnaryOp> <UnaryExp>
        ExpNode expNode;
        getToken();
        if (token == Token.PLUS || token == Token.MINU || token == Token.NOT) {
            retract(1);
            expNode = new UnaryExpNode();
            ((UnaryExpNode)expNode).setUnaryOp(parseUnaryOp());
            ((UnaryExpNode)expNode).setExpNode(parseUnaryExp());
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
                expNode = new FuncCallNode(pair);
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
                    ((FuncCallNode) expNode).setArgs(parseFuncRParams());
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
                expNode = parsePrimaryExp();
            }
        }
        // 3. <PrimaryExp>
        else {
            retract(1);
            expNode = parsePrimaryExp();
        }
        fw.write("<UnaryExp>\n");
        return expNode;
    }

    public Pair parseUnaryOp() throws IOException {
        // 1. 解析<UnaryOp>
        // 1. <UnaryOp> = '+' | '-' | '!'
        getToken();
        fw.write(pair.toString() + "\n");
        fw.write("<UnaryOp>\n");
        return pair;
    }

    public LinkedList<ExpNode> parseFuncRParams() throws IOException {
        // 1. 解析<FuncRParams>
        // 1. <FuncRParams> = <Exp> { ',' <Exp> }

        // 1. 创建初始列表
        LinkedList<ExpNode> args = new LinkedList<>();

        // 2. <Exp>
        args.add(parseExp());

        // 2. { ',' <Exp> }
        while (getToken(Token.COMMA)) {
            fw.write(pair.toString() + "\n");
            args.add(parseExp());
        }
        fw.write("<FuncRParams>\n");
        return args;
    }

    public ExpNode parseMulExp() throws IOException {
        // 1. 解析<MulExp>
        // 1. <MulExp>
            // <UnaryExp>
            // <MulExp> '*' | '/' | '%' <UnaryExp>
            // -> 这里存在左递归
            // -> 文法改写为 <MulExp> = <UnaryExp> {'*' | '/' | '%' <UnaryExp>}
        // 1. <UnaryExp>
        ExpNode expNode = parseUnaryExp();
        fw.write("<MulExp>\n");
        // 2. '*' | '/' | '%' <UnaryExp> {'*' | '/' | '%' <UnaryExp>}
        while(getToken(Token.MULT,Token.DIV,Token.MOD)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(pair);
            fw.write(pair.toString() + "\n");

            binaryExpNode.setRightExp(parseUnaryExp());
            expNode = binaryExpNode;
            fw.write("<MulExp>\n");
        }
        return expNode;
    }

    public ExpNode parseAddExp() throws IOException {
        // 1. 解析<AddExp>
        // 1. <AddExp> = <MulExp> | <AddExp> '+' | '-' <MulExp>
            // 1. 改写文法<AddExp> = <MulExp> {'+' | '-' <MulExp>}

        // 1. <MulExp>
        ExpNode expNode = parseMulExp();
        fw.write("<AddExp>\n");
        // 2. {'+' | '-' <MulExp>}
        while(getToken(Token.PLUS,Token.MINU)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(pair);
            fw.write(pair.toString() + "\n");

            binaryExpNode.setRightExp(parseMulExp());
            expNode = binaryExpNode;
            fw.write("<AddExp>\n");
        }
        return expNode;
    }

    public ExpNode parseRelExp() throws IOException {
        // 1. 解析<RelExp>
        // 1. <RelExp> = <AddExp> | <RelExp> '<' | '>' | "<=" | ">=" <AddExp>
            // 1. 改写文法 <RelExp> = <AddExp> {'<' | '>' | "<=" | ">=" <AddExp>}
        // 1. <AddExp>
        ExpNode expNode = parseAddExp();
        fw.write("<RelExp>\n");
        // 2. {'<' | '>' | "<=" | ">=" <AddExp>}
        while(getToken(Token.LSS,Token.GRE,Token.LEQ,Token.GEQ)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(pair);
            fw.write(pair.toString() + "\n");

            binaryExpNode.setRightExp(parseAddExp());
            expNode = binaryExpNode;
            fw.write("<RelExp>\n");
        }
        return expNode;
    }

    public ExpNode parseEqExp() throws IOException {
        // 1. 解析<EqExp>
        // 1. <EqExp> = <RelExp> | <EqExp> "==" | "!=" <RelExp>
            // 1. 改写文法: <EqExp> = <RelExp> {"==" | "!=" <RelExp>}
        // 1. <RelExp>
        ExpNode expNode = parseRelExp();
        fw.write("<EqExp>\n");
        // 2. {"==" | "!=" <RelExp>}
        while(getToken(Token.EQL,Token.NEQ)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(pair);
            fw.write(pair.toString() + "\n");

            binaryExpNode.setRightExp(parseRelExp());
            expNode = binaryExpNode;
            fw.write("<EqExp>\n");
        }
        return expNode;
    }

    public ExpNode parseLAndExp() throws IOException {
        // 1. 解析<LAndExp>
        // 1. <LAndExp> = <EqExp> | <LAndExp> "&&" <EqExp>
            // 1. 改写文法： <LAndExp> = <EqExp> {"&&" <EqExp>}
        // 1. <EqExp>
        ExpNode expNode = parseEqExp();
        fw.write("<LAndExp>\n");
        // 2. {"&&" <EqExp>}
        while(getToken(Token.AND)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(pair);
            fw.write(pair.toString() + "\n");

            binaryExpNode.setRightExp(parseEqExp());
            expNode = binaryExpNode;
            fw.write("<LAndExp>\n");
        }
        return expNode;
    }

    public ExpNode parseLOrExp() throws IOException {
        // 1. 解析<LOrExp>
        // 1. <LOrExp> = <LAndExp> | <LOrExp> "||" <LAndExp>
            // 1. 改写文法: <LOrExp> = <LAndExp> {"||" <LAndExp>}
        // 1. <LAndExp>
        ExpNode expNode = parseLAndExp();
        fw.write("<LOrExp>\n");
        // 2. {"||" <LAndExp>}
        while(getToken(Token.OR)) {
            BinaryExpNode binaryExpNode = new BinaryExpNode();
            binaryExpNode.setLeftExp(expNode);
            binaryExpNode.setBinaryOp(pair);
            fw.write(pair.toString() + "\n");

            binaryExpNode.setRightExp(parseLAndExp());
            expNode = binaryExpNode;
            fw.write("<LOrExp>\n");
        }
        return expNode;
    }

    public ExpNode parseConstExp() throws IOException {
        // 1. 解析<ConstExp>
        // 1. <ConstExp> = <AddExp>
        ExpNode expNode = parseAddExp();
        fw.write("<ConstExp>\n");
        return expNode;
    }
}
