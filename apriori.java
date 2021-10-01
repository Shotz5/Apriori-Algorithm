import java.util.*;
import java.io.*;

public class apriori {
    public static int NUMBER_OF_TRANSACTIONS = 0;
    public static void main(String[] args) throws FileNotFoundException {
    
        File dataSet;
        int min_sup;

        if (args.length != 2) {
            System.out.println("Incorrect args! Exiting");
            return;
        } else {
            dataSet = new File(args[0]);
            min_sup = Integer.parseInt(args[1]);
        }

        Scanner input = new Scanner(dataSet);
        NUMBER_OF_TRANSACTIONS = Integer.parseInt(input.nextLine());

        Map<Integer, Set<Integer>> transactionDB = new HashMap<Integer, Set<Integer>>();
        while(input.hasNextLine()) {
            String transaction = input.nextLine();
            String[] transactionSplit = transaction.split("\\s");
            int transID = Integer.parseInt(transactionSplit[0]);
            // transactionSplit[1] not needed
            Set<Integer> transItems = new HashSet<>();
            for (int i = 2; i < transactionSplit.length; i++) {
                transItems.add(Integer.parseInt(transactionSplit[i]));
            }
            transactionDB.put(transID, transItems);
        }

        input.close();

        long startTime = System.currentTimeMillis();
        Map<Set<Integer>, Integer> fps = apriori(transactionDB, min_sup);
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Found " + fps.size() + " FPs and executed in " + totalTime + " milliseconds");

        outputToFile(fps);
    }

    private static Map<Set<Integer>, Integer> apriori(Map<Integer, Set<Integer>> T, int min_sup) {
        // Make the large 1-itemsets, also acts as L_k
        Map<Set<Integer>, Integer> itemset = new HashMap<Set<Integer>, Integer>();

        // Get all the keys so we can iterate through the transaction database
        Set<Integer> keys = T.keySet();
        Iterator<Integer> transiterator = keys.iterator();

        // Iterate through the transaction database
        // Count all the occurances of each item in the and store in itemset
        while (transiterator.hasNext()) {
            int hashKey = transiterator.next();
            Set<Integer> transaction = T.get(hashKey);

            Iterator<Integer> itemIter = transaction.iterator();

            while(itemIter.hasNext()) {
                int itemID = itemIter.next();
                Set<Integer> idSet = new HashSet<>();
                idSet.add(itemID);
                if (itemset.containsKey(idSet)) {
                    itemset.put(idSet, (itemset.get(idSet) + 1));
                } else {
                    itemset.put(idSet, 1);
                }
            }
        }

        // First prune
        pruneMin(itemset, min_sup);

        // Variables needed to know when we've run out of possible candidates
        boolean go = true;
        int k = 2;

        // While we have possible candidates
        while(go) {
            Map<Set<Integer>, Integer> candidate = apriori_gen(itemset, k);

            // Reset out iterator for another pass through the transDB
            transiterator = keys.iterator();

            // Iterate through the transactionDB for our candidates and count them
            while(transiterator.hasNext()) {
                int hashKey = transiterator.next();

                Set<Set<Integer>> cKeys = candidate.keySet();
                Iterator<Set<Integer>> cIter = cKeys.iterator();

                Set<Integer> items = T.get(hashKey);

                // Scan every candidate against the current transaction and increment if the candidate appears
                while(cIter.hasNext()) {
                    Set<Integer> cHashKey = cIter.next();
                    if (items.containsAll(cHashKey)) {
                        candidate.put(cHashKey, candidate.get(cHashKey) + 1);
                    }
                }
            }

            // Prune everything below min_sup
            pruneMin(candidate, min_sup);

            // Union the pruned candidates into the set of already scanned and pruned candidates
            itemset.putAll(candidate);

            k++;
            if (candidate.size() == 0) {
                go = false;
            }
        }
        return itemset;
    }

    // Generates the candidate sets for counting against the transactionDB
    private static Map<Set<Integer>,Integer> apriori_gen(Map<Set<Integer>, Integer> itemset, int k) {
        Map<Set<Integer>, Integer> candidate = new HashMap<Set<Integer>, Integer>();

        Set<Set<Integer>> itemKeys = itemset.keySet();
        Iterator<Set<Integer>> itemIter = itemKeys.iterator();;

        // This is the stupid thing that generates the sets of candidates by size
        // I doubt this is very efficient, but it works
        while (itemIter.hasNext()) {

            Set<Integer> hashKey = itemIter.next();
            Iterator<Set<Integer>> iter2 = itemKeys.iterator();

            while(iter2.hasNext()) {
                Set<Integer> hashKey2 = iter2.next();
                Set<Integer> newSet = new HashSet<>();
                newSet.addAll(hashKey);
                newSet.addAll(hashKey2);

                if (newSet.size() >= k) {
                    candidate.put(newSet, 0);
                }
            }
        }

        return candidate;
    }

    // Prunes all candidates below min_sup by sending them to oblivion
    private static Map<Set<Integer>,Integer> pruneMin(Map<Set<Integer>, Integer> candidateSet, int min_sup) {
        Set<Set<Integer>> candidateKeys = candidateSet.keySet();
        Iterator<Set<Integer>> candIter = candidateKeys.iterator();
        while(candIter.hasNext()) {
            Set<Integer> hashKey = candIter.next();
            if (candidateSet.get(hashKey) < min_sup) {
                candIter.remove();
            }
        }
        return candidateSet;
    }

    private static boolean outputToFile(Map<Set<Integer>, Integer> fps) throws FileNotFoundException {
        PrintWriter fileOutput = new PrintWriter("MiningResult.txt");
        fileOutput.println("|FPs| = " + fps.size());

        Set<Set<Integer>> fpsKeys = fps.keySet();
        Iterator<Set<Integer>> fpsItem = fpsKeys.iterator();

        while(fpsItem.hasNext()) {
            Set<Integer> currentFps = fpsItem.next();
            fileOutput.println(currentFps + " : " + fps.get(currentFps));
        }

        fileOutput.close();

        return true;
    }
}


/*
| Ti | Values
---------------------
| 1 | {1, 3, 4}
| 2 | {2, 3, 5}
| 3 | {1, 2, 3, 5}
| 4 | {2, 5}
---------------------
*/