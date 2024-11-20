package backend.MipsCode.JIns;

import backend.MipsCode.MipsCode;

public class JIns implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. J类型指令
    public static class j extends JIns {
        private final String label;

        public j(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "j " + label;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 制作一个jal指令，需要指定，立即数label
    public static class jal extends JIns {
        private final String label;

        public jal(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "jal " + label;
        }
    }
}
