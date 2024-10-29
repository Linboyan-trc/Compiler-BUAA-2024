package SyntaxTable;

import SyntaxTree.DefNode;
import SyntaxTree.FuncDefNode;

public class SymbolItem<T> {
    // 1. name + node
    private String name;
    private T node;

    // 2. create + set + get
    public SymbolItem(T node) {
        if (node instanceof DefNode) {
            this.name = ((DefNode)node).getPair().getWord();
        } else {
            this.name = ((FuncDefNode)node).getPair().getWord();
        }
        this.node = node;
    }

    public String getName() {
        return name;
    }

    public T getNode() {
        return node;
    }
}