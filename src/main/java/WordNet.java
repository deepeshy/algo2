import edu.princeton.cs.algs4.Digraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WordNet {
    private UberNoun uberNoun;
    private Map<Integer, Synset> synsetObjectsMap;
    private Map<String, Set<Long>> nounStringToSynsetIds;
    private Digraph digraph;
    private SAP sap;

    private Set<Noun> getNounList() {
        return uberNoun.getNounList();
    }

    private class UberNoun {
        private Set<Noun> nounList;
        private Map<String, Noun> nounMapStringToNoun;
        private Map<Integer, Noun> nounMapIdToNoun;

        public Set<Noun> getNounList() {
            return nounList;
        }

        public UberNoun() {
            nounList = new HashSet<>();
            nounMapIdToNoun = new HashMap<>();
            nounMapStringToNoun = new HashMap<>();
        }

        private void addNoun(Noun newNoun) {
            nounList.add(newNoun);
            nounMapStringToNoun.put(newNoun.getNounString(), newNoun);
            nounMapIdToNoun.put(newNoun.getNounId(), newNoun);
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
        private long synsetId;
        private Set<Noun> nouns;
        private String nounStringsInOriginalForm;

        public Synset(long synsetId, Set<Noun> nouns, String nounStringsInOriginalForm) {
            this.synsetId = synsetId;
            this.nouns = nouns;
            this.nounStringsInOriginalForm = nounStringsInOriginalForm;
        }


        public String getNounStringsInOriginalForm() {
            return nounStringsInOriginalForm;
        }

        public long getSynsetId() {
            return synsetId;
        }

        public Set<Noun> getNouns() {
            return nouns;
        }
    }


    private class Noun {
        private long synsetId;
        private String nounString;
        private int nounId;

        public Noun(long synsetId, String nounString, int nounId) {
            this.synsetId = synsetId;
            this.nounString = nounString;
            this.nounId = nounId;
        }

        public long getSynsetId() {
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
        nounStringToSynsetIds = new HashMap<>();
        synsetObjectsMap = new HashMap<>();
        Map<Integer, Set<Noun>> synsetToNoun = new HashMap<>();

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
                if (nounStringToSynsetIds.get(noun) != null) {
                    nounStringToSynsetIds.get(noun).add((long) synsetId);
                } else {
                    Set<Long> listOfIds = new HashSet<Long>();
                    listOfIds.add((long) synsetId);
                    nounStringToSynsetIds.put(noun, listOfIds);
                }

                if (synsetObjectsMap.get(synsetId) != null) {
                    synsetObjectsMap.get(synsetId).getNouns().add(currNoun);
                } else {
                    Set<Noun> listOfNouns = new HashSet<>();
                    listOfNouns.add(currNoun);
                    synsetObjectsMap.put(synsetId, new Synset(synsetId, listOfNouns, tokens[1]));
                }
                nounListForCurrentSet.add(currNoun);
            }
            synsetToNoun.put(synsetId, nounListForCurrentSet);
        }

//        System.out.println(synsetToNoun);
        // Read hypernyms
        List<String> hypernymLines = getStringsFromFile(hypernyms);

        // 164,21012,56099
        digraph = new Digraph(getNounList().size());
        for (String s : hypernymLines) {
            String[] tokens = s.split(",");
            // 164,21012,56099: Take the first Id and build relation to the other Ids, however do it with their nounIds since each Id represents multiple nouns
            int vId = Integer.parseInt(tokens[0]);
            Set<Noun> vs = synsetToNoun.get(vId);
            for (Noun vNoun : vs) {
                // get ws
                for (String w : tokens) {
                    Set<Noun> ws = synsetToNoun.get(Integer.parseInt(w));
                    for (Noun wNoun : ws) {
                        digraph.addEdge(vNoun.getNounId(), wNoun.getNounId());
                    }
                }
            }
        }
        System.out.println(digraph);
        sap = new SAP(digraph);
    }

    private List<String> getStringsFromFile(String synsets) throws IOException {
        return Files.readAllLines(Paths.get(synsets));
    }

    //
//    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return uberNoun.getNounStringIter();
    }


    //    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return uberNoun.getNounList().contains(word);
    }

    //    distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        Set<Long> nounAsynsets = nounStringToSynsetIds.get(nounA);
        Set<Long> nounBsynsets = nounStringToSynsetIds.get(nounB);

        Set<Integer> membersOfA = getNounIdsForSynsets(nounAsynsets);
        Set<Integer> membersOfB = getNounIdsForSynsets(nounBsynsets);
        return sap.length(membersOfA, membersOfB);
    }

    private Set<Integer> getNounIdsForSynsets(Set<Long> nounAsynsets) {
        Set<Integer> membersOfA = new HashSet<>();
        for (Long nA : nounAsynsets) {
            for (Noun Anoun : synsetObjectsMap.get(nA).getNouns()) {
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
        long synsetId = uberNoun.getNounByNounId(ancestor).getSynsetId();
        return synsetObjectsMap.get(synsetId).getNounStringsInOriginalForm();
    }

    //    // do unit testing of this class
    public static void main(String[] args) throws IOException {
        testWordNet();
    }

    private static void testWordNet() throws IOException {
        WordNet wordNet = new WordNet("C:\\Developer\\algo2\\src\\test\\resources\\wordnet\\synsets15.txt", "C:\\Developer\\algo2\\src\\test\\resources\\wordnet\\hypernyms15Path.txt");
        wordNet.getNounList().stream().forEach(System.out::println);
    }
}