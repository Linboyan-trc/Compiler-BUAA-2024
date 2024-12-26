package frontend.Parser;

import frontend.ErrorHandler.*;
import frontend.Lexer.Pair;
import static frontend.Lexer.Token.*;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import frontend.SyntaxTree.*;
import frontend.SyntaxTree.ExpNode.*;
import frontend.SyntaxTree.StmtNode.*;

import static frontend.SyntaxTable.SyntaxType.*;

import java.util.LinkedList;
import java.util.List;

public class ParsedUnit {
    ////////////////////////////////////////////////////////////////////////////////
    // 1. name是ParsedUnit的名字
    private final String name;

    // 2. units是此ParsedUnit所拥有的节点
    private int unitIndex = -1;
    private ParsedUnit unit;
    private final List<ParsedUnit> units;

    // 3. 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getInstance();

    // 4. 所有节点共用符号表
    private static int forDepth = 0;
    private static SymbolTable symbolTable = new SymbolTable(null,1);

    ////////////////////////////////////////////////////////////////////////////////
    // 1. 创建一个ParsedUnit，指定名字和子节点
    public ParsedUnit(String name, List<ParsedUnit> units) {
        this.name = name;
        this.units = units;
    }

    // 2. 从本ParsedUnit中获取一个子节点
    public ParsedUnit getUnit() {
        unitIndex++;
        return unit = units.get(unitIndex);
    }

    // 3. 查看下一个节点是否是指定类型中的一种
    public boolean getUnit(String... name) {
        unitIndex++;
        if (unitIndex != units.size()) {
            unit = units.get(unitIndex);
            for (String s : name) {
                if (unit.name.equals(s)) {
                    return true;
                }
            }
        }
        unitIndex--;
        return false;
    }

    // 4. 直接返回当前unit
    public ParsedUnit getUnitNow() {
        return unit;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <CompUnitNode>, <DeclNode>, <DefNode>
    // 1. <CompUnitNode>
    public CompUnitNode toCompUnitNode() {
        // 1. 一个<CompUnitNode>包括了:符号表 + <DeclNode> + <FuncDefNode> + <FuncDefNode:mainFuncDefNode>
        LinkedList<DeclNode> declNodes = new LinkedList<>();
        LinkedList<FuncDefNode> funcDefNodes = new LinkedList<>();
        FuncDefNode mainFuncDefNode = null;

        // 2. 遍历子节点
        // 2.1 是<ConstDecl>或<VarDecl>就转DeclNode
        // 2.2 是<FuncDef>就转<FuncDefNode>
        // 2.3 是"MainFuncDef"就转<FuncDefNode>
        for (ParsedUnit parsedUnit : units) {
            switch (parsedUnit.name) {
                case "ConstDecl":
                case "VarDecl":
                    declNodes.add(parsedUnit.toDeclNode());
                    break;
                case "FuncDef":
                    funcDefNodes.add(parsedUnit.toFuncDefNode(false));
                    break;
                case "MainFuncDef":
                    mainFuncDefNode = parsedUnit.toFuncDefNode(true);
                    break;
            }
        }

        // 3. 返回一个<CompUnitNode>:符号表 + <DeclNode> + <FuncDefNode> + <FuncDefNode:mainFuncDefNode>
        return new CompUnitNode(symbolTable, declNodes, funcDefNodes, mainFuncDefNode);
    }

    // 2. 转<DeclNode>
    public DeclNode toDeclNode() {
        // 1. <DeclNode> = <DefNode>
        LinkedList<DefNode> defNodes = new LinkedList<>();

        // 2. 遍历子节点
        // 2.1 子节点是<ConstDef>, <VarDef>:SyntaxtType + Pair:IDENFR + 维数 + 初始值
        for (ParsedUnit parsedUnit : units) {
            if (parsedUnit.name.equals("ConstDef") || parsedUnit.name.equals("VarDef")) {
                defNodes.add(parsedUnit.toDefNode(units));
            }
        }

        // 3. 返回一个<DeclNode>:符号表 + <DefNode>
        // 3.1 <ConstDecl>则此<DeclNode>为isFinal为true
        return new DeclNode(symbolTable, name.equals("ConstDecl"), defNodes);
    }

    // 3. 转DefNode
    public DefNode toDefNode(List<ParsedUnit> declUnits) {
        // 1. <DefNode>:SyntaxtType + isFinal + defNodeType + Pair:IDENFR + 长度 + 初始值
        // 1.1 对于"ConstDef"此时units为:IDENFR '=' <CosntInitVal> | IDENFR '[' <Exp> ']' '=' <CosntInitVal>
        // 1.1 对于"VarDef"此时units为:IDENFR | IDENFR '[' <Exp> ']' + null | '=' <InitVal>

        // 2. defNodeType
        SyntaxType defNodeType = null;
        Pair decalFirstUnit = declUnits.get(0).toPair();
        if (decalFirstUnit.getToken() == CONSTTK) {
            Pair decalSecondUnit = declUnits.get(1).toPair();
            if (decalSecondUnit.getToken() == INTTK) {
                defNodeType = ConstInt;
            } else {
                defNodeType = ConstChar;
            }
        } else {
            if (decalFirstUnit.getToken() == INTTK) {
                defNodeType = Int;
            } else {
                defNodeType = Char;
            }
        }

        // 3. 制作一个<DefNode>
        // 3. 获取Pair:IDENFR
        Pair pair = getUnit().toPair();

        // 4. 设置维数
        // 4.1 获取节点'[' + 获取节点转<ExpNode> + 获取节点']'
        ExpNode length = null;
        if (getUnit("LBRACK")) {
            length = getUnit().toExpNode();
            defNodeType = defNodeType.toArray();
            getUnit("RBRACK");
        }

        // 5. 设置初始值
        // 5.1 获取节点'=' + 获取节点转"..."或<ExpNodeList>
        LinkedList<ExpNode> initValues = new LinkedList<>();
        Pair initValueForSTRCON = null;
        boolean isGetInt = false;
        if (getUnit("ASSIGN")) {
            if(getUnit("GETINTTK")){
                isGetInt = true;
            } else if (getUnit().getUnit("STRCON")){
                initValueForSTRCON = unit.getUnitNow().toPair();
            } else {
                initValues.addAll(unit.toExpNodeList());
            }
        }

        // 6. 创建<DefNode>
        // 6.1 <ConstDef>则此<DefNode>为isFinal为true
        DefNode defNode = new DefNode(symbolTable, name.equals("ConstDef"), defNodeType, pair, length, initValues, initValueForSTRCON, isGetInt);

        // 7. 添加到符号表中
        symbolTable.addToVariables(defNode);

        // 8. 返回一个<DefNode>
        return defNode;
    }

    public Pair toPair() {
        return (Pair) this;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <FuncDefNode>, <FuncFParamNode>
    // 1. <FuncDefNode>
    public FuncDefNode toFuncDefNode(boolean isMainFuncDefNode) {
        // 1. <FuncDefNode> = symbolTable + funcDefType + pair + funcFParamNodes + BlockNode

        // 2. funcDefType
        SyntaxType funcDefType = null;
        getUnit();
        if (!isMainFuncDefNode) {
            if (unit.getUnit("VOIDTK")) {
                funcDefType = VoidFunc;
            } else if (unit.getUnit("INTTK")) {
                funcDefType = IntFunc;
            } else {
                funcDefType = CharFunc;
            }
        } else {
            funcDefType = IntFunc;
        }

        // 3. 获取Pair:IDENFR
        Pair pair = getUnit().toPair();

        // 4. 创建FuncDefNode
        LinkedList<FuncFParamNode> funcFParamNodes = new LinkedList<>();
        FuncDefNode funcDefNode = new FuncDefNode(symbolTable, funcDefType, pair, funcFParamNodes);

        // 5. 添加符号表
        symbolTable.addToFunctions(funcDefNode);

        // 6. 创建新符号表
        symbolTable = new SymbolTable(symbolTable);
        symbolTable.getParent().addChild(symbolTable);

        // 7. 解析funcFParam
        getUnit("LPARENT");
        if (getUnit("FuncFParams")) {
            unit.units.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("FuncFParam")) {
                    funcFParamNodes.add(parsedUnit.toFuncFParamNode());
                }
            });
        }
        getUnit("RPARENT");

        // 8. 解析BlockNode
        funcDefNode.setBlockNode(getUnit().toBlockNode());

        // 9. 返回上层符号表
        symbolTable = symbolTable.getParent();

        // 10. 错误处理
        funcDefNode.checkForError();

        // 11. 返回节点
        return funcDefNode;
    }

    // 2.  <FuncFParamNode>
    public FuncFParamNode toFuncFParamNode() {
        // 1. <FuncFParam> = symbolTable + defNodeType + pair
        // 1. <FuncFParam> = 'BType' IDENFR '[' ']'

        // 2. defNodeType
        SyntaxType defNodeType = null;
        getUnit();
        if(unit.name.equals("INTTK")){
            defNodeType = Int;
        } else {
            defNodeType = Char;
        }

        // 2.Pair:IDENFR
        Pair pair = getUnit().toPair();

        // 3.判断是否是数组
        if (getUnit("LBRACK")) {
            defNodeType = defNodeType.toArray();
            getUnit("RBRACK");
        }

        // 4. 添加到符号表
        FuncFParamNode funcFParamNode = new FuncFParamNode(symbolTable, defNodeType, pair);
        symbolTable.addToVariables(funcFParamNode);

        // 5. 返回节点
        return funcFParamNode;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <BlockItemNode>, <BlockNode>, <StmtNode>
    // 1. <BlockItemNode>
    public BlockItemNode toBlockItemNode() {
        if (name.equals("Stmt")) {
            return this.toStmtNode();
        } else {
            return this.toDeclNode();
        }
    }

    // 2. <BlockNode>
    public BlockNode toBlockNode() {
        // 2. <BlockNode> = symbolTable + BlockItems + endLineNumber
        // 2. <Block> = '{' <BlockItem> <BlockItem> ... '}'
        getUnit("LBRACE");
        LinkedList<BlockItemNode> blockItemNodes = new LinkedList<>();
        while (!getUnit("RBRACE")) {
            BlockItemNode temp = getUnit().toBlockItemNode();
            blockItemNodes.add(temp);
            if(temp instanceof DeclNode){
                DeclNode temp1 = (DeclNode) temp;
                for(DefNode defNode : temp1.getDefNodes()){
                    if(defNode.isGetInt()){
                        LValNode temp2 = new LValNode(symbolTable, defNode.getPair(), null);
                        GetIntNode temp3 = new GetIntNode(symbolTable, temp2);
                        blockItemNodes.add(temp3);
                    }
                }
            }
        }
        return new BlockNode(symbolTable, blockItemNodes, unit.toPair().getLineNumber());
    }

    // 3. <StmtNode>
    public StmtNode toStmtNode() {
        // <LVal> '=' <Exp> ';'
        // <LVal> '=' 'getint' '(' ')' ';'
        // <LVal> '=' 'getchar' '(' ')' ';'
        // <Exp> ';'
        // <Blcok>
        // 'if' '(' <Cond> ')' <Stmt> 可能有:'else' <Stmt>
        // 'for' '(' 没有 | <ForStmt> ';' 没有 | <Cond> ';' 没有 <ForStmt> ')' <Stmt>
        // 'break' ';'
        // 'continue' ';'
        // 'return' ';' | 'return' <Exp> ';'
        // 'printf' '(' <StringConst> { ',' <Exp> } ')' ';'
        // ';'
        switch (getUnit().name) {
            case "LVal":
                LValNode lValNode = unit.toLValNode();
                getUnit("ASSIGN");
                if (getUnit("Exp")) {
                    AssignNode assignNode = new AssignNode(symbolTable, lValNode, unit.toExpNode());
                    assignNode.checkForError();
                    return assignNode;
                } else if(getUnit("GETINTTK")){
                    GetIntNode getIntNode = new GetIntNode(symbolTable, lValNode);
                    getIntNode.checkForError();
                    return getIntNode;
                } else {
                    GetCharNode getCharNode = new GetCharNode(symbolTable, lValNode);
                    getCharNode.checkForError();
                    return getCharNode;
                }
            case "Exp":
                return unit.toExpNode();
            case "Block":
                symbolTable = new SymbolTable(symbolTable);
                symbolTable.getParent().addChild(symbolTable);
                BlockNode blockNode = unit.toBlockNode();
                symbolTable = symbolTable.getParent();
                return blockNode;
            case "IFTK":
                getUnit("LPARENT");
                ExpNode cond1 = getUnit().toExpNode();
                getUnit("RPARENT");
                StmtNode ifStmt = getUnit().toStmtNode();
                StmtNode elseStmt = null;
                if (getUnit("ELSETK")) {
                    elseStmt = getUnit().toStmtNode();
                }
                return new BranchNode(symbolTable, cond1, ifStmt, elseStmt);
            case "FORTK":
                // 1. 声明
                ForStmtNode forStmtNodeFirst = null;
                ExpNode cond2 = null;
                ForStmtNode forStmtNodeSecond = null;
                StmtNode stmt = null;
                // 2. (
                getUnit("LPARENT");
                // 3. <ForStmt>
                if(getUnit("ForStmt")){
                    forStmtNodeFirst = unit.toForStmtNode();
                }
                // 4. ;
                getUnit("SEMICN");
                // 5. <Cond>
                if(getUnit("Cond")){
                    cond2 = unit.toExpNode();
                }
                // 6. ;
                getUnit("SEMICN");
                // 7. <ForStmt>
                if(getUnit("ForStmt")){
                    forStmtNodeSecond = unit.toForStmtNode();
                }
                // 8. )
                getUnit("RPARENT");
                // 9. <Stmt>
                forDepth++;
                stmt = getUnit().toStmtNode();
                forDepth--;
                return new ForNode(symbolTable, forStmtNodeFirst, cond2, forStmtNodeSecond, stmt);
            case "BREAKTK":
                if (forDepth == 0) {
                    errorHandler.addError(new ErrorRecord(unit.toPair().getLineNumber(), 'm'));
                }
                return new BreakNode(symbolTable);
            case "CONTINUETK":
                if (forDepth == 0) {
                    errorHandler.addError(new ErrorRecord(unit.toPair().getLineNumber(), 'm'));
                }
                return new ContinueNode(symbolTable);
            case "RETURNTK":
                Pair pair = unit.toPair();
                if (getUnit("Exp")) {
                    return new ReturnNode(symbolTable, pair, unit.toExpNode());
                } else {
                    return new ReturnNode(symbolTable, pair, null);
                }
            case "PRINTFTK":
                // 1. <PrintNode> = symbolTable + Pair:STRCON + arguments
                getUnit("LPARENT");
                Pair string = getUnit().toPair();
                LinkedList<ExpNode> arguments = new LinkedList<>();
                PrintNode printNode = new PrintNode(symbolTable, string, arguments);
                while (getUnit("COMMA")) {
                    arguments.add(getUnit().toExpNode());
                }
                printNode.checkForError();
                return printNode;
            default:
                return new NopNode();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <ExpNode>, <ExpNode>:List, <LVal>, <Number>, <Character>
    // 1. <ExpNode>
    public ExpNode toExpNode() {
        // 7.1 <Cond>, <ConstExp>, <Exp> -> <Exp>
        switch (name) {
            case "Cond":
            case "ConstExp":
            case "Exp":
                return getUnit().toExpNode();

            // 7.2 <LVal>, <Number>, <Character>, (<Exp>)
            case "PrimaryExp":
                if (getUnit("LVal")) {
                    return unit.toLValNode();
                } else if (getUnit("Number")) {
                    return unit.toNumberNode();
                } else if (getUnit("Character")) {
                    return unit.toCharacterNode();
                } else {
                    getUnit("LPARENT");
                    return getUnit().toExpNode();
                }

            // 7.3 <PrimaryExp>, <FuncCallNode>, <UnaryExp>
            case "UnaryExp":
                if (getUnit("PrimaryExp")) {
                    return unit.toExpNode();
                } else if (getUnit("IDENFR")) {
                    // 1. <FuncCallNode> = symbolTable + Pair:IDENFR + arguments
                    Pair pair = unit.toPair();
                    getUnit("LPARENT");
                    LinkedList<ExpNode> arguments = new LinkedList<>();
                    FuncCallNode funcCallNode = new FuncCallNode(symbolTable, pair, arguments);
                    if (getUnit("FuncRParams")) {
                        arguments.addAll(unit.toExpNodeList());
                    }
                    funcCallNode.checkForError();
                    return funcCallNode;
                } else {
                    return new UnaryExpNode(symbolTable, getUnit().getUnit().toPair(), getUnit().toExpNode());
                }
                // 7.4 <AddExp>, <MulExp>
                // 7.4 <LOrExp>, <LAndExp>, <EqExp>, <RelExp> -> <AddExp>
            default:
                ExpNode leftExp = getUnit().toExpNode();
                if (getUnit("PLUS", "MINU", "MULT", "DIV", "MOD", "BITAND", "LSS", "LEQ", "GRE", "GEQ", "EQL", "NEQ", "AND", "OR")) {
                    return new BinaryExpNode(symbolTable, leftExp, unit.toPair(), getUnit().toExpNode());
                } else {
                    return leftExp;
                }
        }
    }

    // 2. <ExpNode>:List
    public LinkedList<ExpNode> toExpNodeList() {
        // 1. <ExpNode>:List
        LinkedList<ExpNode> expNodes = new LinkedList<>();

        // 2. <ConstInitVal> = <Exp>, { <Exp>, <Exp>}
        if (name.equals("ConstInitVal")) {
            units.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("ConstExp")) {
                    expNodes.add(parsedUnit.toExpNode());
                }
            });
        }

        // 3. <InitVal> = <Exp>, { <Exp>, <Exp>}
        else if (name.equals("InitVal")) {
            units.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("Exp")) {
                    expNodes.add(parsedUnit.toExpNode());
                }
            });
        }

        // 4. <FuncRParams> = <Exp>, { <Exp>, <Exp>}
        else {
            units.forEach(parsedUnit -> {
                if (parsedUnit.name.equals("Exp")) {
                    expNodes.add(parsedUnit.toExpNode());
                }
            });
        }

        // 5. 返回<ExpNode>:List
        return expNodes;
    }

    // 3. <LVal>
    public LValNode toLValNode() {
        // 1. <LValNode> = symbolTable + Pair:IDENFR + length
        // 1. <LVal> = IDENFR | '[' <Exp> ']'
        Pair pair = getUnit().toPair();
        ExpNode length = null;
        if (getUnit("LBRACK")) {
            length = getUnit().toExpNode();
            getUnit("RBRACK");
        }
        LValNode lValNode = new LValNode(symbolTable, pair, length);
        lValNode.checkForError();
        return lValNode;
    }

    private ForStmtNode toForStmtNode() {
        // 1. <ForStmtNode> = symbolTable + lValNode + expNode
        // 2. <ForStmt> = <LVal> '=' <Exp>
        LValNode lValNode = getUnit().toLValNode();
        getUnit("ASSIGN");
        ExpNode expNode = getUnit().toExpNode();
        // 3. check
        ForStmtNode forStmtNode = new ForStmtNode(symbolTable, lValNode, expNode);
        forStmtNode.checkForError();
        return forStmtNode;
    }

    private NumberNode toNumberNode() {
        return new NumberNode(getUnit().toPair().getValue());
    }

    private CharacterNode toCharacterNode() {
        // 1. 转义字符
        getUnit();
        if(unit.toPair().getWord().length() == 4){
            char ch = unit.toPair().getWord().charAt(2);
            switch (ch){
                case 'a':
                    return new CharacterNode(7);
                case 'b':
                    return new CharacterNode(8);
                case 't':
                    return new CharacterNode(9);
                case 'n':
                    return new CharacterNode(10);
                case 'v':
                    return new CharacterNode(11);
                case 'f':
                    return new CharacterNode(12);
                case '\"':
                    return new CharacterNode(34);
                case '\'':
                    return new CharacterNode(39);
                case '\\':
                    return new CharacterNode(92);
                case '0':
                    return new CharacterNode(0);
                default:
                    return new CharacterNode(0);
            }
        } else {
            return new CharacterNode(unit.toPair().getWord().charAt(1));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        // 1. 先输出所有子节点的字符串
        // 2. 最后输出本节点的'<' + name + '>'
        return units.stream().map(ParsedUnit::toString).reduce((s1, s2) -> s1 + "\n" + s2).orElse("")
                + "\n<" + name + ">";
    }
}
