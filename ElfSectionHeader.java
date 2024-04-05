public class ElfSectionHeader {
    int name;
    private int type;
    private int flags;
    int addr;
    int offset;
    int size;
    private int link;
    private int info;
    private int addralign;
    private int entsize;
    private boolean log;

    public ElfSectionHeader(int name, int type, int flags, int addr, int offset, int size, int link, int info, int addralign, int entsize, boolean log) {
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.addr = addr;
        this.offset = offset;
        this.size = size;
        this.link = link;
        this.info = info;
        this.addralign = addralign;
        this.entsize = entsize;
        this.log = log;
    }

    public void write() {
        if (log) {
            System.out.println("name: " + name + "\ntype: " + type + "\nflags: " + flags +
                    "\naddr: " + addr + "\noffset: " + offset + "\nsize: " +
                    size + "\nlink: " + link + "\ninfo: " + info +
                    "\naddralign: " + addralign + "\nentsize: " + entsize);
        }
    }
}
