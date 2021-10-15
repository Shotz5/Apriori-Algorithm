# Apriori Algothm
## Execution
This is a Java implmentation of an Apriori Algorithm, which is designed to find commonalities between transactions in a database.
Execute by CDing into folder and typing `java apriori ./[filename] [min_sup_percentage]`

## Data Input
Program is not designed to work with unformatted data. Any input files must be of format (\s meaning any white space character):
```
[# of transactions]
[transaction #]\s[# of items in transaction]\s[item 1]\s[item 2]\s...\s[item n]
.
.
.
```
## Misc. Notes
This was not programmed with a limitation on RAM in mind, if there was need for RAM efficiency, I would replace almost all integers with shorts, maybe would offload the transaction database periodically, and many other bits and pieces to keep it RAM conscious.

`apriori_gen` is the most inefficient part of the entire execution. Unfortunately there's no good method for finding all combinations of sets in Java of length k that I was able to find, so I had to design one myself. It's O(n<sup>2</sup>), and this application has an inherent execution of It's O(n<sup>2</sup>) anyways. But there is pointless repetition loss that occurs when I join two items together and it gets sent to the nether realm by the `if`. In the words of some Valve developer:
> Too Bad!

Unit testing on a Ryzen 7 3800x reveals the following average execution speeds for given datasets:
1. java apriori ./1k5L.txt 5 - 0 FPs in 15ms
2. java apriori ./1k5L.txt 2 - 45 FPs in 101ms
3. java apriori ./1k5L.txt 1 - 213 FPs in 1391ms
4. java apriori ./t25i10d10k.txt 20 - 0 FPs in 190ms
5. java apriori ./t25i10d10k.txt 10 - 20 FPs in 293ms
6. java apriori ./t25i10d10k.txt 5 - 142 FPs in 6028ms
7. java apriori ./retail.txt 50 - 1 FPs in 386ms
8. java apriori ./retail.txt 25 - 3 FPs in 394ms
9. java apriori ./retail.txt 10 - 9 FPs in 493ms
10. java apriori ./retail.txt 5 - 16 FPs in 579ms
11. java apriori ./retail.txt 2 - 55 FPs in 1810ms
12. java apriori ./connect.txt 75 - DNF after 5 hours.