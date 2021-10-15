import java.util.*;
import java.io.*;

public class apriori {
    public static int NUMBER_OF_TRANSACTIONS = 0;
    public static void main(String[] args) throws FileNotFoundException {
    
        File dataSet;
        int min_sup_percentage;
        float min_sup_float;
        int min_sup;

        if (args.length != 2) {
            System.out.println("Incorrect args! Exiting");
            return;
        } else {
            dataSet = new File(args[0]);
            min_sup_percentage = Integer.parseInt(args[1]);
            if (min_sup_percentage > 100 || min_sup_percentage < 0) {
                System.out.println("Cannot have more than 100% or less than 0% min_sup. Exiting.");
            }
        }

        Scanner input = new Scanner(dataSet);
        NUMBER_OF_TRANSACTIONS = Integer.parseInt(input.nextLine());

        min_sup_float = NUMBER_OF_TRANSACTIONS * (min_sup_percentage / 100f);
        min_sup = Math.round(min_sup_float);

        Map<Integer, Set<Integer>> transactionDB = new HashMap<Integer, Set<Integer>>();
        while(input.hasNextLine()) {
            String transaction = input.nextLine();
            String[] transactionSplit = transaction.split("\\s"); // Split on every whitespace character (tab or space)
            int transID = Integer.parseInt(transactionSplit[0]); // TransactionID will always be the first value
            // transactionSplit[1] not needed (size of transaction)
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

    /**
     * Conducts the apriori method of data mining on a transaction database according to the minimum support.
     * @param T Transaction Database
     * @param min_sup Minimum support needed for a value to appear
     * @return Returns the completed FPs in a Map
     */
    private static Map<Set<Integer>, Integer> apriori(Map<Integer, Set<Integer>> T, int min_sup) {
        // Make the large 1-itemsets, also acts as L_k
        Map<Set<Integer>, Integer> itemset = new HashMap<Set<Integer>, Integer>();

        // Our FP map to store the values that meet min_sup. LinkedHashMap so that it remembers order of insertion.
        Map<Set<Integer>, Integer> FPs = new LinkedHashMap<Set<Integer>, Integer>();

        // Get all the keys so we can iterate through the transaction database
        Set<Integer> keys = T.keySet();
        Iterator<Integer> transiterator = keys.iterator();

        // Iterate through the transaction database
        // Count all the occurances of each item in the database and either add one to the current value or inset it into the table
        // Advantage of adding as we go along is that we don't have memory allocated for "0s" that will get pruned later
        // As well it's less to iterate through when we do begin pruning
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

        // Add to our final set
        FPs.putAll(itemset);

        // Variables needed to know when we've run out of possible candidates
        boolean go = true;
        int k = 2;

        // While we have possible candidates
        while(go) {
            Map<Set<Integer>, Integer> candidate = apriori_gen(itemset, k);

            // Reset our iterator for another pass through the transDB
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
            FPs.putAll(candidate);

            // Clear itemset for next iteration and update with previous candidates to be gen'd
            itemset.clear();
            itemset.putAll(candidate);

            k++;
            if (candidate.size() == 0) {
                go = false;
            }
        }
        return FPs;
    }

    /**
     * Generates the next candidate set to be used in the algorithm
     * @param itemset Current set of keys to be joined to each other
     * @param k Set size (k = 2 joins all combinations of length 2)
     * @return Returns the candidate set ready to be counted against the transaction database
     */
    private static Map<Set<Integer>,Integer> apriori_gen(Map<Set<Integer>, Integer> itemset, int k) {
        Map<Set<Integer>, Integer> candidate = new HashMap<Set<Integer>, Integer>();

        // Get our keyset to iterate through the itemset
        Set<Set<Integer>> itemKeys = itemset.keySet();
        Iterator<Set<Integer>> itemIter = itemKeys.iterator();

        // Generates the candidates, stupid stupid stupid inefficient sad
        // Does this by first: taking the previous set of candidates
        // Duplicating the set
        // Then multiplying the duplicated set into the initial set
        // And turfing the results that are smaller than size k
        // Sets automatically disallow duplicates, so there is no chance of [1, 1, 2] results
        while (itemIter.hasNext()) {

            Set<Integer> hashKey = itemIter.next();
            Iterator<Set<Integer>> iter2 = itemKeys.iterator();


            while(iter2.hasNext()) {
                Set<Integer> hashKey2 = iter2.next();
                Set<Integer> newSet = new HashSet<>();
                newSet.addAll(hashKey);
                newSet.addAll(hashKey2);

                if (newSet.size() == k) {
                    candidate.put(newSet, 0);
                }
            }
            itemIter.remove();
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