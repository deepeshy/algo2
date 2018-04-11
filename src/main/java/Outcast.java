import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.io.IOException;

public class Outcast {
    private WordNet wordNet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordNet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        int maxDistance = Integer.MIN_VALUE;
        int outcast = -1;
        for (int i = 0; i < nouns.length; i++) {
            int distance = 0;
            for (int j = 0; j < nouns.length; j++) {
                distance += wordNet.distance(nouns[i], nouns[j]);
            }
            System.out.println(nouns[i] + " distance: " + distance + " and max distance: " + maxDistance);
            System.out.println("_____________________________");
            if (distance > maxDistance) {
                maxDistance = distance;
                outcast = i;
            }
        }
        System.out.println(nouns[outcast]);
        return nouns[outcast];
    }

    public static void main(String[] args) throws IOException {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}