package Parser;

import SyntaxTable.SymbolTable;

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

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <BlockItemNode>, <BlockNode>

    ////////////////////////////////////////////////////////////////////////////////
    // 1. <CompUnitNode>, <DeclNode>, <DefNode>, <SpecialNode>
    // 1. <CompUnitNode>

    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        // 1. 先输出所有子节点的字符串
        // 2. 最后输出本节点的'<' + name + '>'
        return units.stream().map(ParsedUnit::toString).reduce((s1, s2) -> s1 + "\n" + s2).orElse("")
                + "\n<" + name + ">";
    }
}
