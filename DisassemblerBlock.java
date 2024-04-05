import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisassemblerBlock {
    private final Map<String, String> OPCODE_UJ = Map.of(
            "0110111", "LUI",
            "0010111", "AUIPC",
            "1101111", "JAL"
    );

    private final Map<String, Map <Integer, String>> OPCODE_ISB = Map.of(
            "1100111", Map.of(
                    0,"JALR"),
            "1100011", Map.of(
                    0,"BEQ",
                    1,"BNE",
                    4,"BLT",
                    5,"BGE",
                    6,"BLTU",
                    7,"BGEU"
            ),
            "0000011", Map.of(
                    0, "LB",
                    1, "LH",
                    2, "LW",
                    4, "LBU",
                    5, "LHU"
            ),
            "0100011", Map.of(
                    0, "SB",
                    1, "SH",
                    2, "SW"
            ),
            "0010011", Map.of(
                    0, "ADDI",
                    2, "SLTI",
                    3, "SLTIU",
                    4, "XORI",
                    6, "ORI",
                    7, "ANDI"
            )
    );
    private final Map<String, Map <Integer, Map <String, String>>> OPCODE_R = Map.of(
            "0010011", Map.of(
                    1, Map.of(
                            "0000000", "SLLI"
                    ),
                    5, Map.of(
                            "0000000", "SRLI",
                            "0100000", "SRAI"
                    )
            ),
            "0110011", Map.of(
                    0, Map.of(
                            "0000000", "ADD",
                            "0100000", "SUB",
                            "0000001", "MUL"
                    ),
                    1, Map.of(
                            "0000000", "SLL",
                            "0000001", "MULH"
                    ),
                    2, Map.of(
                            "0000000", "SLT",
                            "0000001", "MULHSU"
                    ),
                    3, Map.of(
                            "0000000", "SLTU",
                            "0000001", "MULHU"
                    ),
                    4, Map.of(
                            "0000000", "XOR",
                            "0000001", "DIV"
                    ),
                    5, Map.of(
                            "0000000", "SRL",
                            "0100000", "SRA",
                            "0000001", "DIVU"
                    ),
                    6, Map.of(
                            "0000000", "OR",
                            "0000001", "REM"
                    ),
                    7, Map.of(
                            "0000000", "AND",
                            "0000001", "REMU"
                    )
            )
    );
    private final List<String> regName = List.of(
            "zero",
            "ra",
            "sp",
            "gp",
            "tp",
            "t0",
            "t1",
            "t2",
            "s0",
            "s1",
            "a0",
            "a1",
            "a2",
            "a3",
            "a4",
            "a5",
            "a6",
            "a7",
            "s2",
            "s3",
            "s4",
            "s5",
            "s6",
            "s7",
            "s8",
            "s9",
            "s10",
            "s11",
            "t3",
            "t4",
            "t5",
            "t6"
            );
    private final Map<String, Character> TYPE;
    private int indexL;
    public DisassemblerBlock() {
        TYPE = new HashMap<>();
        indexL = 0;
        TYPE.putAll(Map.of(
                "LUI", 'U',
                "AUIPC", 'U',
                "JAL", 'J',
                "JALR", 'I',
                "BEQ", 'B',
                "BNE", 'B',
                "BLT", 'B',
                "BGE", 'B',
                "BLTU", 'B',
                "BGEU", 'B'
        ));
        TYPE.putAll(Map.of(
                "LB", 'I',
                "LH", 'I',
                "LW", 'I',
                "LBU", 'I',
                "LHU", 'I',
                "SB", 'S',
                "SH", 'S',
                "SW", 'S',
                "ADDI", 'I',
                "SLTI", 'I'
        ));
        TYPE.putAll(Map.of(
                "SLTIU", 'I',
                "XORI", 'I',
                "ORI", 'I',
                "ANDI", 'I',
                "SLLI", 'R',
                "SRLI", 'R',
                "SRAI", 'R',
                "ADD", 'R',
                "SUB", 'R',
                "SLL", 'R'
        ));
        TYPE.putAll(Map.of(
                "SLTU", 'R',
                "XOR", 'R',
                "SRL", 'R',
                "SRA", 'R',
                "OR", 'R',
                "AND", 'R',
                "MUL", 'R',
                "MULH", 'R',
                "MULHSU", 'R',
                "MULHU", 'R'
        ));
        TYPE.putAll(Map.of(
                "DIV", 'R',
                "DIVU", 'R',
                "REM", 'R',
                "REMU", 'R'
        ));
    }

    public String parseCommands(final int[] commands, int addrStart, final Map<Integer,String> symtabData) {
        int addr = addrStart;
        for (int i = 0; i + 3 < commands.length; i += 4) {
            parseCommand(get32Bits(commands[i], commands[i + 1], commands[i + 2], commands[i + 3]), addr, symtabData);
            addr += 4;
        }
        StringBuilder ansWithLabel = new StringBuilder();
        addr = addrStart;
        for (int i = 0; i + 3 < commands.length; i += 4) {
            if (symtabData.containsKey(addr)) {
                ansWithLabel.append(String.format( "\n%08x <%s>:\n", addr, symtabData.get(addr)));
            }
            ansWithLabel.append(String.format("%8x:\t", addr));
            ansWithLabel.append(parseCommand(get32Bits(commands[i], commands[i + 1], commands[i + 2], commands[i + 3]), addr, symtabData));
            ansWithLabel.append("\n");
            addr += 4;
        }
        return ansWithLabel.toString();
    }

    private String parseCommand(final String bits, int addr, Map<Integer,String> symtabData) {
        String name = getName(bits);
        name = (name == null ? "unknown_instruction" : name.toLowerCase());
        String outString = String.format("%s\t\t%-20s", codeToHex(bits), name);
        if (name.equals("unknown_instruction") || name.equals("ecall") || name.equals("ebreak")) {
            return outString;
        }
        char type = getType(name.toUpperCase());
        if (type == 'R') {
            return outString + parseTypeR(bits);
        } else if (type == 'I') {
            return outString + (Set.of("lb", "lh", "lw", "lbu", "lhu", "jalr").contains(name) ? parseTypeI_LJ(bits) : parseTypeI_Other(bits));
        } else if (type == 'S') {
            return outString + parseTypeS(bits);
        } else if (type == 'B') {
            return outString + parseTypeB(bits, addr, symtabData);
        } else if (type == 'U') {
            return outString + parseTypeU(bits);
        } else if (type == 'J') {
            return outString + parseTypeJ(bits, addr, symtabData);
        } else {
            return codeToHex(bits) + "\tunknown_instruction";
        }
    }

    private String parseTypeR(String bits) {
        int rs2 = getRegisterNumber(bits, 7, 5), rs1 = getRegisterNumber(bits, 12, 5), rd = getRegisterNumber(bits, 20, 5);
        return regName.get(rd) + "," + regName.get(rs1) + "," + regName.get(rs2);
    }

    private String parseTypeI_LJ(String bits) {
        int imm = getNumber(bits, 0, 12), rs1 = getRegisterNumber(bits, 12, 5), rd = getRegisterNumber(bits, 20, 5);
        return regName.get(rd) + "," + imm + "(" + regName.get(rs1) + ")";
    }

    private String parseTypeI_Other(String bits) {
        int imm = getNumber(bits, 0, 12), rs1 = getRegisterNumber(bits, 12, 5), rd = getRegisterNumber(bits, 20, 5);
        return regName.get(rd) + "," + regName.get(rs1) + "," + imm;
    }

    private String parseTypeS(String bits) {
        int rs2 = getRegisterNumber(bits, 7, 5), rs1 = getRegisterNumber(bits, 12, 5),
                imm = getNumber(bits.substring(0,7) + bits.substring(20,25), 0, 12);
        return regName.get(rs2) + "," + imm + "(" + regName.get(rs1) + ")";
    }

    private String parseTypeB(String bits, int addr, Map<Integer,String> symtabData) {
        int rs2 = getRegisterNumber(bits, 7, 5), rs1 = getRegisterNumber(bits, 12, 5),
                imm = 2 * getNumber(bits.substring(0,1) + bits.substring(24,25) + bits.substring(1,7) + bits.substring(20,24), 0, 12);
        return regName.get(rs1) + "," + regName.get(rs2) + "," + Integer.toHexString(imm + addr) + " <" + getLabel(imm + addr, symtabData) + ">";
    }

    private String parseTypeU(String bits) {
        int imm = getNumber(bits, 0, 20), rd = getRegisterNumber(bits, 20, 5);
        return regName.get(rd) + ",0x" + Integer.toHexString(imm);
    }

    private String parseTypeJ(String bits, int addr, Map<Integer,String> symtabData) {
        int rd = getRegisterNumber(bits, 20, 5),
                imm = 2 * getNumber(bits.substring(0,1) + bits.substring(12,20) + bits.substring(11,12) + bits.substring(1,11), 0, 20);
        return regName.get(rd) + "," + Integer.toHexString(imm + addr) + " <" + getLabel(imm + addr, symtabData) + ">";
    }

    private String getLabel(int addr, Map<Integer,String> symtabData) {
        if (!symtabData.containsKey(addr)) {
            symtabData.put(addr, "L" + indexL);
            indexL++;
        }
        return symtabData.get(addr);
    }

    private int getRegisterNumber(String bits, int first, int size) {
        int value = 0;
        for (int i = first; i != first + size; i++) {
            value = value<<1;
            if (bits.charAt(i) == '1') {
                value += 1;
            }
        }
        return value;
    }

    private int getNumber(String bits, int first, int size) {
        int value = 0;
        for (int i = first; i != first + size; i++) {
            value = value<<1;
            if (bits.charAt(i) == '1') {
                if (i == first) {
                    value -= 1;
                } else {
                    value += 1;
                }
            }
        }
        return value;
    }

    private char getType(final String name) {
        return TYPE.get(name);
    }

    private String codeToHex(String bits) {
        return Integer.toHexString(getRegisterNumber(bits, 0 , 4)) + Integer.toHexString(getRegisterNumber(bits, 4 , 4)) +
                Integer.toHexString(getRegisterNumber(bits, 8 , 4)) + Integer.toHexString(getRegisterNumber(bits, 12 , 4)) +
                Integer.toHexString(getRegisterNumber(bits, 16 , 4)) + Integer.toHexString(getRegisterNumber(bits, 20 , 4)) +
                Integer.toHexString(getRegisterNumber(bits, 24 , 4)) + Integer.toHexString(getRegisterNumber(bits, 28 , 4));
    }

    private String getName(final String bits) {
        if (bits.equals("00000000000000000000000001110011")) {
            return "ECALL";
        }
        if (bits.equals("00000000000100000000000001110011")) {
            return "EBREAK";
        }
        String opcode = bits.substring(25, 32),
                funct7 = bits.substring(0, 7);
        int funct3 = Integer.parseInt(bits.substring(17, 20), 2);
        if (OPCODE_UJ.containsKey(opcode)) {
            return OPCODE_UJ.get(opcode);
        } else if (OPCODE_ISB.containsKey(opcode) && OPCODE_ISB.get(opcode).containsKey(funct3)) {
            return OPCODE_ISB.get(opcode).get(funct3);
        } else if (OPCODE_R.containsKey(opcode) && OPCODE_R.get(opcode).containsKey(funct3) && OPCODE_R.get(opcode).get(funct3).containsKey(funct7)) {
            return OPCODE_R.get(opcode).get(funct3).get(funct7);
        } else {
            return null;
        }
    }

    private String get32Bits(int b1, int b2, int b3, int b4) {
        return get8Bits(b1) + get8Bits(b2) + get8Bits(b3) + get8Bits(b4);
    }

    private String get8Bits(int x) {
        String bits = Integer.toBinaryString(x);
        for (; bits.length() != 8; bits = "0" + bits);
        return bits;
    }
}
