import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ElfParser {
    private final Map<Integer, String> NDX = Map.of(
            0, "UND",
            65280, "LORESERVE",
            65311, "HIPROC",
            65521, "ABS",
            65522, "COMMON",
            65535, "HIRESERVE"
            );
    private final Map<Integer, String> BIND = Map.of(
            0, "LOCAL",
            1, "GLOBAL",
            2, "WEAK",
            13, "LOPROC",
            15, "HIPROC"
    );
    private final Map<Integer, String> TYPE = Map.of(
            0, "NOTYPE",
            1, "OBJECT",
            2, "FUNC",
            3, "SECTION",
            4, "FILE",
            13, "LOPROC",
            15, "HIPROC"
    );
    private TextSymtable elfData;
    private int strtabOffset;
    private int startAddr;
    private int[] elf;
    private char[] ident;
    private int type;
    private int machine;
    private int version;
    private int entry;
    private int phoff;
    private int shoff;
    private int flags;
    private int ehsize;
    private int phentsize;
    private int phnum;
    private int shentsize;
    private int shnum;
    private int shstrndx;
    private boolean log;

    ElfParser(InputStream elfFile, boolean log) {
        this.log = log;
        ident = new char[16];
        try {
            byte[] elf_byte = elfFile.readAllBytes();
            elfFile.close();
            elf = new int[elf_byte.length];
            for (int i = 0; i != elf.length; i++) {
                elf[i] = Byte.toUnsignedInt(elf_byte[i]);
            }
            for (int i = 0; i != 16; i++) {
                ident[i] = (char) elf_byte[i];
            }
            type = decodeLSB16(16);
            machine = decodeLSB16(18);
            version = decodeLSB32(20);
            entry = decodeLSB32(24);
            phoff = decodeLSB32(28);
            shoff = decodeLSB32(32);
            flags = decodeLSB32(36);
            ehsize = decodeLSB16(40);
            phentsize = decodeLSB16(42);
            phnum = decodeLSB16(44);
            shentsize = decodeLSB16(46);
            shnum = decodeLSB16(48);
            shstrndx = decodeLSB16(50);
            if (!isValidElf()) {
                throw new IllegalArgumentException("Incorrect elf file");
            }
            writeElfByteCode();
            elfData = parse();
        } catch (IOException e) {
            System.out.println("Reading error " + e.getMessage());
        }
    }

    public int[] getTextData() {
        return elfData.getText();
    }

    public int getStartAddr() {
        return startAddr;
    }

    private TextSymtable parse() {
        return parseSectionHeader();
    }

    private TextSymtable parseSectionHeader() {
        ElfSectionHeader nameSection = makeSectionHeader(shoff + shstrndx * shentsize);
        TextSymtable data = new TextSymtable();
        for (int i = 1; i < shnum; i++) {
            ElfSectionHeader programSection = makeSectionHeader(shoff + i * shentsize);
            write("ProgramSection " + i + "\n");
            String name = getName(nameSection.offset + programSection.name);
            write(name + "\n");
            programSection.write();
            if (name.equals(".text")) {
                data.setText(readCode(programSection.offset, programSection.size));
                startAddr = programSection.addr;
            } else if (name.equals(".symtab")) {
                data.setSymtable(programSection);
            } else if (name.equals(".strtab")) {
                strtabOffset = programSection.offset;
            }
            write("\n");
        }
        return data;
    }

    public void getSymtableString(OutputStream out) {
        int offset = elfData.getSymtable().offset, size = elfData.getSymtable().size, nameOffset = strtabOffset;
        try {
            out.write(String.format("%3s:%9s%7s %-7s %-6s %-7s %9s %s\n", "Num", "Value", "Size", "Type", "Bind", "Vis", "Ndx", "Name").getBytes());
            for (int i = 0; i + 15 < size; i += 16) {
                String name = getName(nameOffset + decodeLSB32(offset + i));
                int value = decodeLSB32(offset + i + 4),
                        symSize = decodeLSB32(offset + i + 8),
                        shndx = decodeLSB16(offset + i + 14),
                        info = decodeLSB16(offset + i + 12) % 256;
                int bind = info >> 4, type = info & 0xf;
                out.write(String.format("%3d: %08x %6d %-7s %-6s %-7s %9s %s\n", i / 16, value, symSize, TYPE.get(type), BIND.get(bind), "DEFAULT", NDX.containsKey(shndx) ? NDX.get(shndx) : Integer.toString(shndx), name).getBytes());
            }
        } catch (IOException e) {
            System.out.println("Writing error " + e.getMessage());
        }
    }

    public Map<Integer,String> getSymtableData() {
        Map<Integer,String> data = new HashMap<>();
        int offset = elfData.getSymtable().offset, size = elfData.getSymtable().size, nameOffset = strtabOffset;
        for (int i = 0; i + 15 < size; i += 16) {
            String name = getName(nameOffset + decodeLSB32(offset + i));
            int value = decodeLSB32(offset + i + 4),
                    bind = (decodeLSB16(offset + i + 12) % 256) >> 4;
            if (bind == 1) {
                data.put(value, name);
            }
        }
        return data;
    }

    private ElfSectionHeader makeSectionHeader(int index) {
        return new ElfSectionHeader(decodeLSB32(index), decodeLSB32(index + 4),
                decodeLSB32(index + 8), decodeLSB32(index + 12), decodeLSB32(index + 16),
                decodeLSB32(index + 20), decodeLSB32(index + 24), decodeLSB32(index + 28),
                decodeLSB32(index + 32), decodeLSB32(index + 36), log);
    }

    private boolean isValidElf() {
        return ident[0] == 127 && ident[1] == 'E' && ident[2] == 'L' && ident[3] == 'F' &&
                ident[4] == 1 && ident[5] == 1 && type == 2 && flags == 0 && ehsize == 52;
    }

    private int[] readCode(int offset, int size) {
        int[] data = new int[size];
        for (int i = offset; i + 3 < offset + size; i += 4) {
            writeBytes(data, i - offset, i, 4);
            write("\n");
        }
        return data;
    }

    private int decodeLSB16(int index) {
        int x1 = elf[index], x2 =  elf[index + 1];
        return x1 + x2 * (1 << 8);
    }

    private int decodeLSB32(int index) {
        int x1 = elf[index], x2 =  elf[index + 1], x3 = elf[index + 2], x4 =  elf[index + 3];
        return x1 + x2 * (1 << 8) + x3 * (1 << 16) + x4 * (1 << 24);
    }

    private void writeElfByteCode() {
        for (int i = 0; i != 16; i++) {
            write(+ident[i] + " ");
        }
        write("\n");
        write("type: " + type + "\nmachine: " + machine + "\nversion: " + version +
            "\nentry: " + entry + "\nphoff: " + phoff + "\nshoff: " + shoff + "\nflags: " + flags +
            "\nehsize: " + ehsize + "\nphentsize: " + phentsize + "\nphnum: " + phnum +
            "\nshentsize: " + shentsize + "\nshnum: " + shnum + "\nshstrndx: " + shstrndx + "\n");
        for (int i = 0; i != elf.length; ++i) {
            write(Integer.toHexString(elf[i]) + " ");
        }
        write("\n");
    }

    private void writeBytes(int[] data, int offset, int index, int size) {
        for (int i = index + size - 1; i >= index; i--) {
            write(getBits(i));
            data[offset + index + size - 1 - i] = elf[i];
            if (i + 1 != index + size) {
                write("'");
            }
        }
    }

    private String getBits(int index) {
        String bits = Integer.toBinaryString(elf[index]);
        for (; bits.length() != 8; bits = "0" + bits);
        return bits;
    }

    private String getName(int index) {
        String name = "";
        for (int i = index; elf[i] != 0; i++) {
            name = name + ((char) elf[i]);
        }
        return name;
    }

    private void write(String message) {
        if (log) {
            System.out.print(message);
        }
    }
}
