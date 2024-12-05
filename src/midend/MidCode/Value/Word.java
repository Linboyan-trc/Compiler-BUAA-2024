package midend.MidCode.Value;

public class Word extends Value {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 表示一个变量
    private final String name;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 指定变量名字
    public Word(String name) {
        this.name = name;
    }

    // 2. 或根据变量计数器来分配
    public Word() {
        this.name = String.valueOf(tempCnt++);
    }

    // 3. 获取变量名字
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Void truncTo8(){
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成中间代码
    @Override
    public String toString() {
        return "$" + name;
    }
}
