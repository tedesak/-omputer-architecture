public class TextSymtable {
    private int[] text;
    private ElfSectionHeader symtable;

    public TextSymtable() {}

    public int[] getText() {
        return text;
    }

    public ElfSectionHeader getSymtable() {
        return symtable;
    }

    public void setText(int[] text) {
        this.text = text;
    }

    public void setSymtable(ElfSectionHeader symtable) {
        this.symtable = symtable;
    }
}
