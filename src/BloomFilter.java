import java.lang.Math;

public class BloomFilter {
    boolean[] filter;
    int capacity;
    int maskSize;
    int numHashes;

    public BloomFilter(int capacity) {
        this.capacity = capacity;
        this.maskSize = 0;
        this.filter = new boolean[capacity];
        this.numHashes = 2;
    }

    public BloomFilter() {
        this((int) Math.pow(2, 20));
    }

    private int calculateOptimalHashes() {
        return (int) Math.ceil(((double) capacity / maskSize) * Math.log(2));
    }

    private boolean checkIfHashShouldIncrement() {
        int newHashes = calculateOptimalHashes();
        return newHashes > numHashes;
    }

    private boolean[] hash(String item) {
        return new boolean[0];
    }

    public void insert(String item) {

    }

    public boolean contains(String item) {
        return false;
    }
}
