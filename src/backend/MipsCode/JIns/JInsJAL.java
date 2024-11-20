package backend.MipsCode.JIns;

public class JInsJAL extends JIns {
    private String label;

    public JInsJAL(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "jal " + label;
    }
}
