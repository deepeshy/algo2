import edu.princeton.cs.algs4.Digraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WordNet {
    private UberNoun uberNoun;
    private Map<Integer, Synset> synsetIdToSynsetMap;
    private Digraph digraph;
    private SAP sap;

    private Set<Noun> getNounList() {
        return uberNoun.getNounList();
    }

    private class UberNoun {
        private Set<Noun> nounList;
        private Map<String, Noun> nounMapStringToNoun;
        private Map<Integer, Noun> nounMapIdToNoun;
        private Map<String, Set<Integer>> nounStringToSynsetIds;

        public Set<Noun> getNounList() {
            return nounList;
        }

        public UberNoun() {
            nounList = new HashSet<>();
            nounMapIdToNoun = new HashMap<>();
            nounMapStringToNoun = new HashMap<>();
            nounStringToSynsetIds = new HashMap<>();
        }

        private void addNoun(Noun newNoun) {
            String nounString = newNoun.getNounString();
            int synsetId = newNoun.getSynsetId();

            nounList.add(newNoun);
            nounMapStringToNoun.put(nounString, newNoun);
            nounMapIdToNoun.put(newNoun.getNounId(), newNoun);
            // noun String -> Ids of all the synsets this noun belongs to
            if (nounStringToSynsetIds.get(nounString) != null) {
                nounStringToSynsetIds.get(nounString).add(synsetId);
            } else {
                Set<Integer> listOfIds = new HashSet<>();
                listOfIds.add(synsetId);
                nounStringToSynsetIds.put(nounString, listOfIds);
            }
        }


        public Set<Integer> getAllSynsetsForNoun(String nounString) {
            return nounStringToSynsetIds.get(nounString);
        }

        public Noun getNounByString(String nounString) {
            return nounMapStringToNoun.get(nounString);
        }

        public Iterable<String> getNounStringIter() {
            return nounMapStringToNoun.keySet();
        }

        public Noun getNounByNounId(int nounId) {
            return nounMapIdToNoun.get(nounId);
        }
    }


    private class Synset {
        private int synsetId;
        private Set<Noun> nouns;
        private String nounStringsInOriginalForm;

        public Synset(int synsetId, Set<Noun> nouns, String nounStringsInOriginalForm) {
            this.synsetId = synsetId;
            this.nouns = nouns;
            this.nounStringsInOriginalForm = nounStringsInOriginalForm;
        }

        public String getNounStringsInOriginalForm() {
            return nounStringsInOriginalForm;
        }

        public int getSynsetId() {
            return synsetId;
        }

        public Set<Noun> getNouns() {
            return nouns;
        }

        @Override
        public String toString() {
            return "Synset{" +
                    "synsetId=" + synsetId +
                    ", nounStringsInOriginalForm='" + nounStringsInOriginalForm + '\'' +
                    '}';
        }
    }

    private class Noun {
        private int synsetId;
        private String nounString;
        private int nounId;

        public Noun(int synsetId, String nounString, int nounId) {
            this.synsetId = synsetId;
            this.nounString = nounString;
            this.nounId = nounId;
        }

        public int getSynsetId() {
            return synsetId;
        }

        public String getNounString() {
            return nounString;
        }

        public int getNounId() {
            return nounId;
        }

        @Override
        public String toString() {
            return "Noun{" +
                    "synsetId=" + synsetId +
                    ", nounString='" + nounString + '\'' +
                    ", nounId=" + nounId +
                    '}';
        }
    }

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) throws IOException {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException("cannot accept null as file names");
        }

        // Initialization
        uberNoun = new UberNoun();
        synsetIdToSynsetMap = new HashMap<>();
//        Map<Integer, Set<Noun>> synsetToNoun = new HashMap<>();

        // Read synsets
        List<String> synsetLines = getStringsFromFile(synsets);
        int nounCount = 0;
        for (String s : synsetLines) {
            // Parsing the line that looks likes this
            // 35532,discussion give-and-take word,an exchange of views on some topic; "we had a good discussion"; "we had a word or two about it"
            String[] tokens = s.split(",");
            // 0: 35532|1: discussion give-and-take word|2: an exchange of views on some topic; "we had a good discussion"; "we had a word or two about it"
            int synsetId = Integer.parseInt(tokens[0]);
            String[] nounsRaw = tokens[1].split(" ");
            Set<Noun> nounListForCurrentSet = new HashSet<>();
            for (String noun : nounsRaw) {
                Noun currNoun = new Noun(synsetId, noun, nounCount++);
                uberNoun.addNoun(currNoun);
                nounListForCurrentSet.add(currNoun);
            }
            synsetIdToSynsetMap.put(synsetId, new Synset(synsetId, nounListForCurrentSet, tokens[1]));
        }

        // Read hypernyms
        List<String> hypernymLines = getStringsFromFile(hypernyms);
        System.out.println("number of lines in hypernym: " + hypernymLines.size());
        // 164,21012,56099
        digraph = new Digraph(getNounList().size());
        for (String s : hypernymLines) {
            String[] tokens = s.split(",");
            // 164,21012,56099: Take the first Id and build relation to the other Ids, however do it with their nounIds since each Id represents multiple nouns
            int vId = Integer.parseInt(tokens[0]);

            Set<Noun> vs = synsetIdToSynsetMap.get(vId).getNouns();
            for (Noun vNoun : vs) {
                // get ws
                for (String w : tokens) {
                    Set<Noun> ws = synsetIdToSynsetMap.get(Integer.parseInt(w)).getNouns();
                    for (Noun wNoun : ws) {
                        digraph.addEdge(vNoun.getNounId(), wNoun.getNounId());
                    }
                }
            }
        }
        //System.out.println(digraph);
        sap = new SAP(digraph);
    }

    private List<String> getStringsFromFile(String synsets) throws IOException {
        return Files.readAllLines(Paths.get(synsets));
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return uberNoun.getNounStringIter();
    }


    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return uberNoun.getNounList().contains(word);
    }

    //    distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        Set<Integer> nounAsynsets = uberNoun.getAllSynsetsForNoun(nounA);
        Set<Integer> nounBsynsets = uberNoun.getAllSynsetsForNoun(nounB);

        Set<Integer> membersOfA = getNounIdsForSynsets(nounAsynsets);
        Set<Integer> membersOfB = getNounIdsForSynsets(nounBsynsets);

        // Debug printing
        System.out.println("______________Noun A Group:" + nounA);
        for (Integer i : membersOfA) {
            System.out.println(uberNoun.getNounByNounId(i).getNounString());
        }

        System.out.println("______________Noun B Group:" + nounB);
        for (Integer i : membersOfB) {
            System.out.println(uberNoun.getNounByNounId(i).getNounString());
        }

        return sap.length(membersOfA, membersOfB);
    }

    private Set<Integer> getNounIdsForSynsets(Set<Integer> nounAsynsets) {
        Set<Integer> membersOfA = new HashSet<>();
        for (Integer nA : nounAsynsets) {
            for (Noun Anoun : synsetIdToSynsetMap.get(nA).getNouns()) {
                membersOfA.add(Anoun.getNounId());
            }
        }
        return membersOfA;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
//    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        Integer nounAId = uberNoun.getNounByString(nounA).getNounId();
        Integer nounBId = uberNoun.getNounByString(nounB).getNounId();
        int ancestor = sap.ancestor(nounAId, nounBId);
        System.out.println("Ancestor: " + uberNoun.getNounByNounId(ancestor));
        int synsetId = uberNoun.getNounByNounId(ancestor).getSynsetId();
        System.out.println("Parent synset nouns: " + synsetIdToSynsetMap.get(synsetId).getNounStringsInOriginalForm());
        return synsetIdToSynsetMap.get(synsetId).getNounStringsInOriginalForm();
    }

    //    // do unit testing of this class
    public static void main(String[] args) throws IOException {
        testWordNet();
    }

    private static void testWordNet() throws IOException {
        WordNet wordNet = new WordNet("C:\\Developer\\algo2\\src\\test\\resources\\wordnet\\synsets.txt", "C:\\Developer\\algo2\\src\\test\\resources\\wordnet\\hypernyms.txt");
//        wordNet.getNounList().stream().forEach(System.out::println);
        System.out.println("_______________________");

        String[] input = new String[]{"worm","bird"};

        System.out.println("Final Answer: " + wordNet.sap(input[0], input[1]));
        System.out.println("Final Answer: "+wordNet.distance(input[0], input[1]));


    }
}