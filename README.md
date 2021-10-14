# Apriori Algothm
This is a Java implmentation of an Apriori Algorithm, which is designed to find commonalities between transactions in a database.
Execute by CDing into folder and typing `java apriori ./[filename] [min_sup_percentage]`

Program is not designed to work with unformatted data. Any input files must be of format:
```
[# of transactions]
[transaction #]\s[# of items in transaction]\s[item 1] [item 2] ... [item n]
```

Unit testing on a Ryzen 5 3800x reveals the following average execution speeds for given datasets:
1. java apriori ./1k5L.txt 5 - 0 FPs in 15ms
2. java apriori ./1k5L.txt 2 - 45 FPs in 101ms
3. java apriori ./1k5L.txt 1 - 213 FPs in 1391ms
4. java apriori ./t25i10d10k.txt 20 - 0 FPs in 190ms
5. java apriori ./t25i10d10k.txt 10 - 20 FPs in 293ms
6. java apriori ./t25i10d10k.txt 5 - 142 FPs in 6028ms
7. java apriori ./connect.txt 75 - 