package backend.MipsCode;

public class Comment implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 注释
    public String comment;

    public Comment(String comment) {
        this.comment = comment;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 生成mips代码
    @Override
    public String toString() {
        return "# " + comment;
    }
}
