import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            InputStream input = new FileInputStream(args[1]);
            ElfParser parser = new ElfParser(input, false);
            DisassemblerBlock disassembler = new DisassemblerBlock();
            OutputStream output = new FileOutputStream(args[2]);
            output.write(".text".getBytes());
            output.write(disassembler.parseCommands(parser.getTextData(), parser.getStartAddr(), parser.getSymtableData()).getBytes());
            output.write("\n.symtab\n".getBytes());
            parser.getSymtableString(output);
            output.close();
        } catch (IOException e) {
            System.out.println("Open file error " + e.getMessage());
        }
    }
}