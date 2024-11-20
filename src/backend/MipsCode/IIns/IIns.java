package backend.MipsCode.IIns;

import backend.Address.Address;
import backend.MipsCode.MipsCode;
import backend.ValueMeta.Reg;
import midend.MidCode.Value.Imm;

public class IIns implements MipsCode {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. I类型指令
    public enum IOpCode {
        addiu
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. 制作一个addi指令，需要指定具体指令，寄存器rs，寄存器rt，立即数imm
    public static class IR2I1 extends IIns {
        private final IOpCode opCode;
        private final Reg rs;
        private final Reg rt;
        private final Imm imm;

        public IR2I1(IOpCode opCode, Reg rs, Reg rt, Imm imm) {
            this.opCode = opCode;
            this.rs = rs;
            this.rt = rt;
            this.imm = imm;
        }

        @Override
        public String toString() {
            return opCode.toString() + " " + rs + ", " + rt + ", " + imm;
        }
    }

    // 2. 制作一个beq指令，需要指定寄存器rs，寄存器rt，立即数label
    public static class beq extends IIns {
        private final Reg rs;
        private final Reg rt;
        private final String label;

        public beq(Reg rs, Reg rt, String label) {
            this.rs = rs;
            this.rt = rt;
            this.label = label;
        }

        @Override
        public String toString() {
            return "beq " + rs + ", " + rt + ", " + label;
        }
    }

    // 3. 制作一个bne指令，需要指定寄存器rs，寄存器rt，立即数label
    public static class bne extends IIns {
        private final Reg rs;
        private final Reg rt;
        private final String label;

        public bne(Reg rs, Reg rt, String label) {
            this.rs = rs;
            this.rt = rt;
            this.label = label;
        }

        @Override
        public String toString() {
            return "bne " + rs + ", " + rt + ", " + label;
        }
    }

    // 4. 制作一个la指令，需要指定寄存器rt，立即数address
    public static class la extends IIns {
        private final Reg rt;
        private final Address address;

        public la(Reg rt, Address address) {
            this.rt = rt;
            this.address = address;
        }

        @Override
        public String toString() {
            return "la " + rt + ", " + address;
        }
    }

    // 5. 制作一个li指令，需要指定寄存器rt，立即数imm
    public static class li extends IIns {
        private final Reg rt;
        private final Imm imm;

        public li(Reg rt, Imm imm) {
            this.rt = rt;
            this.imm = imm;
        }

        @Override
        public String toString() {
            return "li " + rt + ", " + imm;
        }
    }

    // 6. 制作一个lw指令，需要指定寄存器rt，立即数address
    public static class lw extends IIns {
        private final Reg rt;
        private final Address address;

        public lw(Reg rt, Address address) {
            this.rt = rt;
            this.address = address;
        }

        @Override
        public String toString() {
            return "lw " + rt + ", " + address;
        }
    }

    // 7. 制作一个sw指令，需要指定寄存器rt，立即数address
    public static class sw extends IIns {
        private final Reg rt;
        private final Address address;

        public sw(Reg rt, Address address) {
            this.rt = rt;
            this.address = address;
        }

        @Override
        public String toString() {
            return "sw " + rt + ", " + address;
        }
    }
}
