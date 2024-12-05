package midend.MidCode.Value;

public class Addr extends Value {
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 表示一个地址
    private final String name;

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 指定地址名字
    public Addr(String name) {
        this.name = name;
    }

    // 2. 或根据变量计数器来分配名字，此时表示临时地址
    public Addr() {
        this.name = String.valueOf(tempCnt++);
    }

    // 3. 判断是否是临时地址
    public boolean isTemp() {
        return Character.isDigit(name.charAt(0));
    }

    // 4. 获取名字
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
        return "&" + name;
    }
}
