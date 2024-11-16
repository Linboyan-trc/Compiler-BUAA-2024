package Parser;

import SyntaxTable.SymbolTable;

import java.util.List;

public class ParsedUnit {
    ////////////////////////////////////////////////////////////////////////////////
    // 1. name是ParsedUnit的名字
    // 2. subUnits是此ParsedUnit所拥有的节点，包括Pair
    private final String name;
    private final List<ParsedUnit> units;

    // 1. 当前解析到的子节点
    private int unitIndex = -1;
    private ParsedUnit unit;

    // 1. 所有节点共用符号表
    private static int forDepth = 0;
    private static SymbolTable symbolTable = new SymbolTable(null,1);

    ////////////////////////////////////////////////////////////////////////////////
    // 1. 创建一个ParsedUnit，指定名字和子节点
    public ParsedUnit(String name, List<ParsedUnit> units) {
        this.name = name;
        this.units = units;
    }
}
