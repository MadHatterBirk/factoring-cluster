package job.server.factor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the factor tree for a number.
 */
public class FactorTree implements Serializable {

    private Node root;

    private class Node implements Serializable {

        private BigInteger number;
        private boolean prime;
        private Node leftFactor;
        private Node rightFactor;

        public Node(BigInteger number) {
            this.number = number;
            this.prime = false;
            leftFactor = null;
            rightFactor = null;
        }
    }

    /**
     * returns the node that match a number.
     *
     * @param number
     * @return
     */
    private Node find(BigInteger number) {
        return find(root, number);
    }

    /**
     * Helper method that recursively finds the node that match a number.
     *
     * @param root
     * @param number
     */
    private Node find(Node root, BigInteger number) {
        if (root == null) {
            return null;
        }

        if (root.number.equals(number)) {
            return root;
        }

        if (root.leftFactor != null) {
            Node left = find(root.leftFactor, number);
            if (left != null) {
                return left;
            }
        }

        if (root.rightFactor != null) {
            return find(root.rightFactor, number);
        }

        return null;
    }

    /**
     * Creates a FactorTree with a root node of number.
     *
     * @param number
     */
    public FactorTree(BigInteger number) {
        root = new Node(number);
    }

    /**
     * Returns the root number for the factor tree.
     *
     * @return
     */
    public BigInteger getNumber() {
        return root.number;
    }

    /**
     * sets the factors for a number. If there are no numbers in the tree that
     * match number no factors will be set and IllegalArgumentException will be
     * thrown.
     *
     * @param number
     * @param left
     * @param right
     * @throws IllegalArgumentException if number is not in the tree.
     */
    public void setFactors(BigInteger number, BigInteger left, BigInteger right) throws IllegalArgumentException {
        Node n = find(number);
        if (n == null) {
            throw new IllegalArgumentException(number + " is not in tree");
        }
        Node leftNode = find(left);
        if (leftNode == null) {
            leftNode = new Node(left);
        }
        n.leftFactor = leftNode;

        //find right node. if left == right the left node is returned.
        Node rightNode = find(right);
        if (rightNode == null) {
            rightNode = new Node(right);
        }
        n.rightFactor = rightNode;
    }

    /**
     * returns true if the number is marked as prime in this tree.
     *
     * @param number
     * @return
     * @throws IllegalArgumentException if number is not in the tree.
     */
    public boolean isPrime(BigInteger number) throws IllegalArgumentException {
        Node node = find(number);
        if (node == null) {
            throw new IllegalArgumentException(number + " is not in the tree.");
        }
        return node.prime;
    }

    /**
     * Sets all numbers that match to prime in the tree.
     *
     * @param number
     * @param prime
     * @throws IllegalArgumentException if number is not in tree.
     */
    public void setPrime(BigInteger number, boolean prime) throws IllegalArgumentException {
        Node node = find(number);
        if (node == null) {
            throw new IllegalArgumentException(number + " is not in the tree.");
        }
        node.prime = prime;
    }

    /**
     * returns the leaves of the factor tree.
     *
     * @return A map containing the leaves and how many times the leaf appears
     * in the tree.
     */
    public Map<BigInteger, Integer> getLeaves() {
        Map<BigInteger, Integer> factors = new HashMap<BigInteger, Integer>();
        List<Node> leaves = new ArrayList<Node>();
        getLeaves(root, leaves);
        Integer count = 0;
        for (Node i : leaves) {
            count = factors.get(i.number);
            count = count == null ? 0 : count;
            factors.put(i.number, count + 1);
        }
        return factors;
    }

    /**
     * adds leaf nodes to leaves. Helper method for getLeaves.
     *
     * @param root
     * @param leaves
     */
    private void getLeaves(Node root, List<Node> leaves) {
        if (root == null) {
            return;
        }

        if (root.leftFactor == null && root.rightFactor == null) {
            leaves.add(root);
        }

        getLeaves(root.leftFactor, leaves);
        getLeaves(root.rightFactor, leaves);
    }

    /**
     * performs a depth-first search for a leaf that has not been solved. A
     * leave is solved if it has factors or is marked as prime.
     *
     * @return the first leave that has not been solved.
     */
    public BigInteger getNextUnsolvedNumber() {
        Node node = getNextUsolvedNode(root);
        if (node != null) {
            return node.number;
        }

        return null;
    }

    /**
     * helper method for getNextUnfactoredNumber(). Does the search and returns
     * the node object needed.
     *
     * @param root
     * @return
     */
    private Node getNextUsolvedNode(Node root) {
        if (root == null) {
            return null;
        }

        if (root.leftFactor == null && root.rightFactor == null) {
            if (root.prime) {
                return null;
            } else {
                return root;
            }
        } else if((root.leftFactor != null && root.rightFactor == null) || (root.leftFactor == null && root.rightFactor != null)) {
            throw new IllegalStateException(root.number + " has only one factor set.");
        }

        Node left = getNextUsolvedNode(root.leftFactor);
        if (left != null) {
            return left;
        }

        Node right = getNextUsolvedNode(root.rightFactor);
        return right;
    }

    /**
     * true if the FactorTree is completely solved.
     *
     * @return
     */
    public boolean isComplete() {
        return getNextUnsolvedNumber() == null;
    }
}
