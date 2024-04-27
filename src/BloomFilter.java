import java.lang.Math;
import java.util.Arrays;

public class BloomFilter<E> {
    private boolean[] filter;
    private int capacity;

    // number of elements inserted
    private int size;

    // the number of hash functions to use
    private int numHashes;

    // the desired maximum false positive rate
    private static final double FPR_THRESHOLD = 0.05f;

    // factor to increase the size of the filter array during reallocation
    private static final double REALLOC_RATIO = 1.2;

    public BloomFilter(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.filter = new boolean[(int) (capacity * REALLOC_RATIO)];
        this.numHashes = calcIdealNumHash();
    }

    public BloomFilter() {
        this((int) Math.pow(2, 20));
    }

    private int calculateOptimalHashes() {
        return (int) Math.ceil(((double) capacity / size) * Math.log(2));
    }

    private boolean checkIfHashShouldIncrement() {
        int newHashes = calculateOptimalHashes();
        return newHashes > numHashes;
    }

    /**
     * Hashes an item multiple times, and returns an array of each of the results. The hash values will always be the same for any two items that are equal by their .equals method
     * @param item The item to hash
     * @return An array of pseudo-random hash values for the item.
     */
    private int[] hash(E item) {
        int[] hashIndices = new int[numHashes];
        hashIndices[0] = item.hashCode() % capacity;
        for (int i = 1; i < numHashes; i++) {
            hashIndices[i] = String.valueOf(hashIndices[i - 1] ^ i * item.hashCode()).hashCode();
        }

        return hashIndices;
    }

    /**
     * Inserts an item into the bloom filter, updating the capacity of the filter if needed
     * @param item The item to insert
     */
    public void insert(E item) {
        int[] hashIndices = hash(item);
        for (int i : hashIndices) {
            filter[i] = true;
        }
        size++;

        // increase number of hashes if needed
        numHashes = calculateOptimalHashes();
        if (expectedFPR() > FPR_THRESHOLD) {
            // if more hashes isn't enough, increase capacity
            capacity = calcIdealCapacity();
            // reset number of hashes to ideal now that capacity is larger
            numHashes = calcIdealNumHash();
            if (capacity > filter.length) {
                // reallocate array if it can no longer fit the desired capacity
                reallocate();
            }
        }
    }

    /**
     * Increases the size of the filter by a constant ratio, preserving the values that are already stored
     */
    private void reallocate() {
        int newLength = (int) (filter.length * REALLOC_RATIO);
        filter = Arrays.copyOf(filter, capacity);
    }

    /**
     * Checks if the item is contained in the bloom filter. If it returns false, the item is certainly not in the filter. However, it can return false positives, so an element is not guaranteed to be in the bloom filter even if this method returns true.
     * @param item Item to test the presence of in the filter
     * @return False if the item is not in the set, true if the item is probably in the set
     */
    public boolean contains(E item) {
        int[] hashIndices = hash(item);
        return Arrays.stream(hashIndices).allMatch(i -> filter[i]);
    }

    /**
     * Calculates the ideal number of hash functions to use given the FPR threshold
     * @return The ideal number of hashes
     */
    private int calcIdealNumHash() {
        return -1 * (int) Math.ceil(Math.log(FPR_THRESHOLD) / Math.log(2));
    }

    /**
     * Calculates the ideal number of bits per element for the desired false positive rate
     */
    private int calcIdealCapacity() {
        return (int) Math.ceil(size * -Math.log(FPR_THRESHOLD) / (Math.log(2) * Math.log(2)));
//        return -1 * (int) Math.ceil((size * Math.log(desiredFPR)) /
//                Math.pow(Math.log(2), 2));
    }

    /**
     * Calculates the approximate probability of a contains() call returning a false positive
     * @return The expected false positive rate
     */
    public double expectedFPR() {
        return Math.pow(1 - Math.exp((double) (-1 * (numHashes * size))
                / capacity), numHashes);
    }
}
