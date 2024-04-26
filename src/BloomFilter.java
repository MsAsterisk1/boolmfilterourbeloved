import java.lang.Math;
import java.util.Arrays;

public class BloomFilter<T> {
    boolean[] filter;
    int capacity;
    int maskSize;
    int numHashes;
    private final float IDEAL_EPSILON = 0.05f;

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

    private int[] hash(T item) {
        int[] hashIndices = new int[numHashes];
        hashIndices[0] = item.hashCode() % capacity;
        for (int i = 1; i < numHashes; i++) {
            hashIndices[i] = String.valueOf(hashIndices[i - 1] ^ i * item.hashCode()).hashCode();
        }

        return hashIndices;
    }

    public void insert(T item) {
        int[] hashIndices = hash(item);
        for (int i : hashIndices) {
            filter[i] = true;
        }
        maskSize++;
    }

    public boolean contains(T item) {
        int[] hashIndices = hash(item);
        return Arrays.stream(hashIndices).allMatch(i -> filter[i]);
    }

    private int calcIdealNumHash(float desiredEpsilon) {
        return -1 * (int) Math.ceil(Math.log(desiredEpsilon) / Math.log(2));
    }

    private int calcIdealCapacity(float desiredEpsilon) {
        return -1 * (int) Math.ceil((maskSize * Math.log(desiredEpsilon)) /
                Math.pow(Math.log(2), 2));
    }

    public double expectedProbFP() {
        return Math.pow(1 - Math.exp((double) (-1 * (numHashes * maskSize))
                / capacity), numHashes);
    }
}
