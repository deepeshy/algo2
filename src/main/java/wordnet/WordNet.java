package wordnet;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Topological;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WordNet {
    private final Set<String> nounListStrings;
    private final Map<Integer, String> synsetIdToNounsMap;
    private final Map<String, Set<Integer>> nounStringsToSynsetIds;
    private final SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException("cannot accept null as file names");
        }

        // Initialization
        synsetIdToNounsMap = new HashMap<>();
        nounListStrings = new HashSet<>();
        nounStringsToSynsetIds = new HashMap<>();


        // Read synsets
        String line;
        In in = new In(synsets);

        while ((line = in.readLine()) != null) {
            // Parsing the line that looks likes this
            // 35532,discussion give-and-take word,an exchange of views on some topic; "we had a good discussion"; "we had a word or two about it"
            String[] tokens = line.split(",");
            // 0: 35532|1: discussion give-and-take word|2: an exchange of views on some topic; "we had a good discussion"; "we had a word or two about it"
            int synsetId = Integer.parseInt(tokens[0]);
            String[] nounsRaw = tokens[1].split(" ");

            for (String noun : nounsRaw) {
                nounListStrings.add(noun);
                if (nounStringsToSynsetIds.get(noun) != null) {
                    nounStringsToSynsetIds.get(noun).add(synsetId);
                } else {
                    Set<Integer> synsetIdList = new HashSet<>();
                    synsetIdList.add(synsetId);
                    nounStringsToSynsetIds.put(noun, synsetIdList);
                }
            }
            synsetIdToNounsMap.put(synsetId, tokens[1]);
        }

        // Read hypernyms
        // 164,21012,56099
        In inHyper = new In(hypernyms);
        String[] hyperLines = inHyper.readAllLines();
        Digraph digraph = new Digraph(synsetIdToNounsMap.size());
        for (String lineHyper : hyperLines) {
            String[] tokens = lineHyper.split(",");
            // 164,21012,56099: Take the first Id and build relation to the other synset Ids
            int vId = Integer.parseInt(tokens[0]);
            for (int i = 1; i < tokens.length; i++) { // Don't create a self edge
                digraph.addEdge(vId, Integer.parseInt(tokens[i]));
            }
        }

        // Check if the digraph is DAG
        DirectedCycle dirCycle = new DirectedCycle(digraph);
        if (dirCycle.hasCycle()) throw new IllegalArgumentException("Not a DAG");

        Topological topo = new Topological(digraph);
        if (!topo.hasOrder()) throw new IllegalArgumentException("Not a DAG");

        sap = new SAP(digraph);
//        System.out.println("Vertices: " + digraph.V() + " Edges:" + digraph.E());
    }

    // returns all wordnet.WordNet nouns
    public Iterable<String> nouns() {
        return nounListStrings;
    }

    // is the word a wordnet.WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Word is null");
        }
        return nounListStrings.contains(word);
    }

    //    distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new IllegalArgumentException("distance can be calculated only if the nouns are not null ");
        }

        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("distance can be calculated only if the nouns are in the list. Nouns are " + nounA + " and " + nounB);
        }

        Set<Integer> nounAsynsets = nounStringsToSynsetIds.get(nounA);
        Set<Integer> nounBsynsets = nounStringsToSynsetIds.get(nounB);
        return sap.length(nounAsynsets, nounBsynsets);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new IllegalArgumentException("distance can be calculated only if the nouns are not null ");
        }

        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("distance can be calculated only if the nouns are in the list ");
        }
        Set<Integer> allSynsetsForNounA = nounStringsToSynsetIds.get(nounA);
        Set<Integer> allSynsetsForNounB = nounStringsToSynsetIds.get(nounB);

        int ancestor = sap.ancestor(allSynsetsForNounA, allSynsetsForNounB);
        return synsetIdToNounsMap.get(ancestor);
    }

    public static void main(String[] args) {
        testWordNet();
    }

    private static void testWordNet() {
        WordNet wordNet = new WordNet("C:\\Developer\\algo2\\src\\test\\resources\\wordnet\\synsets3.txt", "C:\\Developer\\algo2\\src\\test\\resources\\wordnet\\hypernyms3InvalidTwoRoots.txt");
//        wordNet.getNounList().stream().forEach(System.out::println);
//        System.out.println("_______________________");

//        String[] input = new String[]{"bear", "horse"};
//        System.out.println("Final Answer: " + wordNet.sap(input[0], input[1]));
//        System.out.println("Final Answer: " + wordNet.distance(input[0], input[1]));
//        System.out.println(wordNet.nounListStrings.size());
    }
}