package midend.MidCode.MidCode;

public class MidCode {
    private MidCode previous;
    private MidCode next;

    public MidCode link(MidCode next) {
        this.next = next;
        if (next != null) {
            next.previous = this;
        }
        return next;
    }
}
