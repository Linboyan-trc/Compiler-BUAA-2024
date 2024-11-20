package frontend.SyntaxTree;

import frontend.Lexer.Pair;
import frontend.SyntaxTable.SymbolTable;
import frontend.SyntaxTable.SyntaxType;
import midend.MidCode.MidCode.ParaGet;
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
        if (super.getLength() == null) {
            new ParaGet(new Word(super.getPair().getWord() + "@" + super.getSymbolTable().getId()));
        }

        // 2. 数组 + 自动添加到中间代码和变量表
        else {
            new ParaGet(new Addr(super.getPair().getWord() + "@" + super.getSymbolTable().getId()));
        }
        return null;
    }
}
