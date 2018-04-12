import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;

import java.util.Iterator;

public class SAP {
    private Digraph G;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        this.G = G;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return processCommonAncestor(v, w)[1];
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        return processCommonAncestor(v, w)[0];
    }

    private int[] processCommonAncestor(int v, int w) {
        BreadthFirstDirectedPaths bfsPathV = new BreadthFirstDirectedPaths(G, v);
        BreadthFirstDirectedPaths bfsPathW = new BreadthFirstDirectedPaths(G, w);
        int shortestPathLen = Integer.MAX_VALUE;
        int closestAncestor = -1;

        for (int i = 0; i < G.V(); i++) {
            int pathLen = 0;
            // Common ancestor
            if (bfsPathV.hasPathTo(i) && bfsPathW.hasPathTo(i)) {
                pathLen = bfsPathV.distTo(i) + bfsPathW.distTo(i);
                if (pathLen < shortestPathLen) {
                    shortestPathLen = pathLen;
                    closestAncestor = i;
                }
            }
        }

        if (shortestPathLen == Integer.MAX_VALUE) {
            shortestPathLen = -1;
            closestAncestor = -1;
        }

        if (false) {
            System.out.println("Path from v: " + v);
            for (Integer pathVal : bfsPathV.pathTo(closestAncestor)) {
                System.out.print(pathVal);
                System.out.print("->");
            }
            System.out.println();

            System.out.println("Path from w: " + w);

            for (Integer pathVal : bfsPathW.pathTo(closestAncestor)) {
                System.out.print(pathVal);
                System.out.print("->");
            }
            System.out.println();
        }
        return new int[]{closestAncestor, shortestPathLen};
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null | w == null) {
            throw new IllegalArgumentException("cannot get sap for null iterators");
        }
        return processAncestorsForGroups(v, w)[0];
    }

    private int[] processAncestorsForGroups(Iterable<Integer> v, Iterable<Integer> w) {
        int shortestPath = Integer.MAX_VALUE;
        int finalClosestAncestor = -1;
        Iterator<Integer> Viterator = v.iterator();
        Iterator<Integer> Witerator = w.iterator();

        while (Viterator.hasNext()) {
            Integer currentV = Viterator.next();
            while (Witerator.hasNext()) {
                Integer currentW = Witerator.next();
                int sapLength = length(currentV, currentW);
                int closestAncestor = ancestor(currentV, currentW);
                if (sapLength != -1) {
                    if (sapLength < shortestPath) {
                        shortestPath = sapLength;
                        finalClosestAncestor = closestAncestor;
                    }
                }
            }
        }

        if (shortestPath == Integer.MAX_VALUE) {
            shortestPath = -1;
            finalClosestAncestor = -1;
        }
        return new int[]{shortestPath, finalClosestAncestor};
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null | w == null) {
            throw new IllegalArgumentException("cannot get sap for null iterators");
        }

        return processAncestorsForGroups(v, w)[1];
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
