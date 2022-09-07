package hw3.hash;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;

public class OomageTestUtility {
    public static boolean haveNiceHashCodeSpread(List<Oomage> oomages, int M) {
        /* TODO:
         * Write a utility function that returns true if the given oomages
         * have hashCodes that would distribute them fairly evenly across
         * M buckets. To do this, convert each oomage's hashcode in the
         * same way as in the visualizer, i.e. (& 0x7FFFFFFF) % M.
         * and ensure that no bucket has fewer than N / 50
         * Oomages and no bucket has more than N / 2.5 Oomages.
         */
        int[] cnt = new int[M];
        int N = oomages.size();
        for (Oomage o: oomages){
            int bucketNum = (o.hashCode() & 0x7FFFFFFF) % M;
            // bucketNum < M && bucketNum >= 0
//            System.out.println(bucketNum);
            cnt[bucketNum] ++;
        }
        for (int c : cnt){
//            System.out.println(c);
            if (c < N / 50 || c > N / 2.5){
                return false;
            }
        }
        return true;
    }
}
