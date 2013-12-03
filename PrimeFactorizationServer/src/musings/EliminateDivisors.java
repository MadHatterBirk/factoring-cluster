/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author john
 */
public class EliminateDivisors {

    public static Map<Long, Long> primeSize;
    public static Map<Long, Long> primeStart;
    public static SortedSet<Long> primes;

    public static void printHeader(long size) {
        System.out.printf("%1$7s", "");
        for (int i = 1; i <= size; i++) {
            if (i % 10 == 0) {
                System.out.printf("%1$9d", i);
            } else {
                System.out.printf("%1$9s", "");
            }
        }
        System.out.println();
    }

    public static void printLine(Long number, List<Long> line) {
        System.out.printf("%1$7d", number);
        for (Long i : line) {
            System.out.printf("%1$9d", i);
        }
        System.out.println();
    }

    public static void printPattern(List<Long> string) {
        if (string != null) {
            long high = 0;
            long total = 0;
            for (Long i : string) {
                if (i.compareTo(high) > 0) {
                    high = i;
                }
                total += i;
            }
            System.out.printf("%1$7s: ", "");
            System.out.println(string);
            System.out.printf("%1$7s", "");
            System.out.println(" size: " + string.size() + " high: " + high + " total: " + total);
        } else {
            System.out.printf("%1$7s", "");
            System.out.println("There is no pattern with current size.");
        }
    }

    public static void printDivisions(int divisions, long size) {
        BigDecimal times = new BigDecimal(size);
        times = times.divide(BigDecimal.valueOf(divisions), MathContext.DECIMAL128);
        System.out.printf("%1$7s%2$d divisions up to %3$d. %4$.7f times faster.\n", "", divisions, size, times);
    }

    private static boolean match(List<BigInteger> first, List<BigInteger> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int i = 0; i < first.size(); i++) {
            BigInteger bi = first.get(i);
            if (!bi.equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<Long> findPatternNew(List<Long> list) {
        TreeMap<Integer, Integer> sizeIndex = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> sizeCount = new HashMap<Integer, Integer>();
        size_loop:
        for (int i = 1; i <= list.size(); i++) {
            List<Integer> remove = new ArrayList<Integer>();
            boolean removing = false;
            for (Integer j : sizeIndex.keySet()) {
                if (removing) {
                    //System.out.println("removing " + list.subList(0, j));
                    remove.add(j);
                    continue;
                }
                Long stringAt = list.get(sizeIndex.get(j));
                Long listAt = list.get(i - 1);
                //System.out.println("Checking " + list.subList(0, j) + " stringAt[" + sizeIndex.get(j) + "]: " + stringAt + " listAt[" + (i - 1) + "]: " + listAt);
                if (!stringAt.equals(listAt)) {
                    //System.out.println("removing " + list.subList(0, j));
                    remove.add(j);
                } else {
                    if (j - 1 == sizeIndex.get(j)) {
                        //System.out.println("setting back to " + 0 + " " + list.subList(0, j));
                        sizeIndex.put(j, 0);
                        sizeCount.put(j, sizeCount.get(j) + 1);
                        if (sizeCount.get(j) >= 2) {
                            for (Integer x : sizeIndex.keySet()) {
                                if (x != j) {
                                    remove.add(j);
                                }
                            }
                            break size_loop;
                        }
                    } else {
                        //System.out.println("incrementing to " + (sizeIndex.get(j) + 1) + " " + list.subList(0, j));
                        sizeIndex.put(j, sizeIndex.get(j) + 1);
                        removing = true;
                    }
                }
            }
            for (int j : remove) {
                sizeIndex.remove(j);
            }
            //System.out.println("adding: " + list.subList(0, i));
            sizeIndex.put(i, 0);
            sizeCount.put(i, 0);
        }

        int size = sizeIndex.firstEntry().getKey();
        //System.out.println("size: " + size + " list.size()" + list.size() + " count: " + sizeCount.get(size));
        if (size != list.size() && sizeCount.get(size) >= 2) {
            return list.subList(0, size);
        } else {
            return null;
        }
    }

    public static void report(long number, long size) {
        //printHeader(size);
        List<Long> line = new ArrayList<Long>();
        List<Long> counts = new ArrayList<Long>();
        int divisions = 0;
        long counter = 0;

        Iterator<Long> current = primes.iterator();
        Iterator<Long> next = primes.iterator();
        next.next();
        long currentl = current.next();
        long nextl = next.next();

        while (currentl <= number) {
            line.add(currentl);
            counts.add(nextl - currentl);
            divisions++;
            currentl = current.next();
            if (currentl <= number) {
                nextl = next.next();
            }
        }

        line.add(currentl);
        divisions++;
        currentl += 1;
        counter += 1;


        while (currentl <= size) {
            boolean match = true;
            for (long l : primes) {
                if (l > number) {
                    break;
                }
                //System.out.println(bi + " is " + currentbi.mod(bi).equals(BigInteger.ZERO) + " for " + currentbi);
                match &= !(currentl % l == 0);
            }
            //System.out.println("match is " + match);
            if (match) {
                line.add(currentl);
                counts.add(counter);
                counter = 0;
                divisions++;
            }
            counter += 1;
            currentl += 1;
        }
        //printLine(number, line);
        //printLine(number, counts);
        int countSize = counts.size();
        for (Long i : primes) {
            if (i.compareTo(number) > 0) {
                break;
            }
            counts.remove(0);
        }
        System.out.printf("%1$7d: Counts: %2$d Counts removed: %3$d\n", number, countSize, countSize - counts.size());
        printPattern(findPatternNew(counts));
        printDivisions(divisions, size);
    }

    public static void main(String... args) {
        primes = new TreeSet<Long>();
        primeSize = new TreeMap<Long, Long>();
        primeStart = new TreeMap<Long, Long>();

        primes.add(2l);
        primes.add(3l);
        primes.add(5l);
        primes.add(7l);
        primes.add(11l);
        primes.add(13l);
        primes.add(17l);
        primes.add(19l);
        primes.add(23l);
        primes.add(29l);

        primeSize.put(2l, 1000l);
        primeSize.put(3l,  2000l);
        primeSize.put(5l,  5000l);
        primeSize.put(7l,  200000l);
        primeSize.put(11l, 500000l);
        primeSize.put(13l, 1000000l);
        primeSize.put(17l, 2000000l);
        primeSize.put(19l, 100000000l);
        //primeSize.put(23l, 1000000000l);


        for (Long l : primeSize.keySet()) {
            report(l, primeSize.get(l));
        }
    }
}
