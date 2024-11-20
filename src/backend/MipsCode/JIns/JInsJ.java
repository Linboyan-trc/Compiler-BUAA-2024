package backend.MipsCode.JIns;

public class JInsJ extends JIns {
    private String label;

    public JInsJ(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "j " + label;
    }
}
