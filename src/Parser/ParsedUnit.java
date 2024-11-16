package Parser;

import Lexer.Pair;
import static Lexer.Token.*;
import SyntaxTable.SymbolTable;
import SyntaxTable.SyntaxType;
import SyntaxTree.*;
import static SyntaxTable.SyntaxType.*;

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

    // 3. 所有节点共用符号表
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
    // 1. <CompUnitNode>, <DeclNode>, <DefNode>, <SpecialNode>
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
                    funcDefNodes.add(parsedUnit.toFuncDefNode());
                    break;
                case "MainFuncDef":
                    mainFuncDefNode = parsedUnit.toFuncDefNode();
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
        // 2.2 子节点是getint(), getchar()，转特殊Node
        for (ParsedUnit parsedUnit : units) {
            if (parsedUnit.name.equals("ConstDef") || parsedUnit.name.equals("VarDef")) {
                defNodes.add(parsedUnit.toDefNode(units));
            } else if (parsedUnit.name.equals("Special")) {
                defNodes.add(parsedUnit.toSpecialNode(units));
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
        if (getUnit("ASSIGN")) {
            if (getUnit().getUnit("STRCON")){
                initValueForSTRCON = unit.getUnitNow().toPair();
            } else {
                initValues.addAll(getUnit().toExpNodeList());
            }
        }

        // 6. 创建<DefNode>
        // 6.1 <ConstDef>则此<DefNode>为isFinal为true
        DefNode defNode = new DefNode(symbolTable, name.equals("ConstDef"), defNodeType, pair, length, initValues, initValueForSTRCON);

        // 7. 添加到符号表中
        symbolTable.addToVariables(defNode);

        // 8. 返回一个<DefNode>
        return defNode;
    }

    // 4. 转SpecialNode
    public SpecialNode toSpecialNode(List<ParsedUnit> declUnits) {
        // 1. <DefNode>:SyntaxtType + isFinal + defNodeType + Pair:IDENFR + 长度 + 初始值
        // 1.1 对于"VarDef"此时units为:IDENFR | IDENFR '[' <Exp> ']' = 'getint' | 'getchar' '(' ')'

        // 2. defNodeType
        SyntaxType defNodeType = null;
        Pair decalFirstUnit = declUnits.get(0).toPair();
        if (decalFirstUnit.getToken() == INTTK) {
            defNodeType = Int;
        } else {
            defNodeType = Char;
        }

        // 3. 制作一个<DefNode>
        // 3. 获取Pair:IDENFR
        Pair pair = getUnit().toPair();

        LinkedList<ExpNode> dimensions = new LinkedList<>();
        LinkedList<ExpNode> initValues = new LinkedList<>();
        SpecialNode specialNode = new SpecialNode(curSymbolTable, name.equals("ConstDef"), ident, dimensions, initValues);
        curSymbolTable.addVariable(specialNode);
        return specialNode;
    }

    public Pair toPair() {
        return (Pair) this;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <BlockItemNode>, <BlockNode>

    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        // 1. 先输出所有子节点的字符串
        // 2. 最后输出本节点的'<' + name + '>'
        return units.stream().map(ParsedUnit::toString).reduce((s1, s2) -> s1 + "\n" + s2).orElse("")
                + "\n<" + name + ">";
    }
}
