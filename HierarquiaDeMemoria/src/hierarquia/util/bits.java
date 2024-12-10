package hierarquia.util;

public class bits {
    public static int extract_bits(int source, int bstart, int blength) {
        int mask = (1 << blength) - 1;
        return ((source >> bstart) & mask);
    }
}
