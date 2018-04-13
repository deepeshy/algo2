import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SAP {
    private final Digraph G;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        this.G = new Digraph(G.V());
        for (int i = 0; i < G.V(); i++) {
            Iterator<Integer> iterator = G.adj(i).iterator();
            while (iterator.hasNext()) {
                this.G.addEdge(i, iterator.next());
            }
        }
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

        return new int[]{closestAncestor, shortestPathLen};
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException("cannot get sap for null iterators");
        }
        return processAncestorsForGroups(v, w)[0];
    }

    private int[] processAncestorsForGroups(Iterable<Integer> v, Iterable<Integer> w) {
        int shortestPath = Integer.MAX_VALUE;
        int finalClosestAncestor = -1;
        Iterator<Integer> vIterator = v.iterator();
        Iterator<Integer> wIterator = w.iterator();

        while (vIterator.hasNext()) {
            int currentV = vIterator.next();
            while (wIterator.hasNext()) {
                int currentW = wIterator.next();
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
        if (v == null || w == null) {
            throw new IllegalArgumentException("cannot get sap for null iterators");
        }

        return processAncestorsForGroups(v, w)[1];
    }

    //  do unit testing of this class
  /*  public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);

        List<Integer> side1List = new ArrayList<>();
        List<Integer> side2List = new ArrayList<>();
        side1List.add(38743);
        side1List.add(77808);
        side2List.add(29652);
        // Right answer is 7

        int length = sap.length(side1List, side2List);
        int ancestor = sap.ancestor(side1List, side2List);
        StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);


        digraph-wordnet.txt

        - failed on trial 1 of 100
        - v = 38743 77808
        - w = 29652
        - student   length() = 15
        - reference length() = 7

        * 100 random subsets of 2 and 2 vertices in digraph-wordnet.txt
        - failed on trial 2 of 100
        - v = 34855 64045
        - w = 2644 55935
        - student   length() = 11
        - reference length() = 10
    }*/
}
