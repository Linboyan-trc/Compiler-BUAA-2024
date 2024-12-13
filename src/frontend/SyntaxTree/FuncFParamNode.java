package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.MidCode.MidCode.ParaGet;
import midend.MidCode.MidCodeTable;
import midend.MidCode.Value.Addr;
import midend.MidCode.Value.Value;
import midend.MidCode.Value.Word;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class FuncFParamNode extends DefNode {
    public FuncFParamNode(SymbolTable symbolTable, SyntaxType defNodeType, Pair pair) {
        super(symbolTable,
                false,
                defNodeType,
                pair,
                null,
                new LinkedList<>(),
                null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 化简
    @Override
    public FuncFParamNode simplify() {
        return this;
    }

    @Override
    public Value generateMidCode() {
        // 1. 单变量 + 自动添加到中间代码和变量表
        // 1. 在FuncFParam中，int a[]的length为null，但仍为数组类型
        // 1. 首先获取type
        SyntaxType syntaxType = getDefNodeType();

        if (syntaxType.isVariable()) {
            Word value = new Word(super.getPair().getWord() + "@" + super.getSymbolTable().getId());
            MidCodeTable.getInstance().addToMidCodes(
                new ParaGet(value)
            );
            MidCodeTable.getInstance().addToVarInfo(value, 1);
        }

        // 2. 数组 + 自动添加到中间代码和变量表
        else {
            Addr value = new Addr(super.getPair().getWord() + "@" + super.getSymbolTable().getId());
            MidCodeTable.getInstance().addToMidCodes(
                new ParaGet(value)
            );
            MidCodeTable.getInstance().addToVarInfo(value, 1);
        }
        return null;
    }
}
