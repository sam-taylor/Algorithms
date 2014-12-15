import java.util.Collections;
import java.util.ArrayList;
import java.util.Hashtable;

//usage: WordWeb synsets.txt hypernyms.txt word1 word2
//returns distance between words and closest common ancestor

public class WordWeb {
    
    private ArrayList<String> synsets;
    private Hashtable<String, Bag<Integer>> words;
    private SAP sap;
    
    // constructor takes the name of the two input files
    public WordWeb(String synsets, String hypernyms)
    {
        this.words = new Hashtable<String, Bag<Integer>>();
        this.synsets = new ArrayList<String>();
        int numSynsets = parseSynsets(synsets);        
        Digraph digraph = parseHypernyms(hypernyms, numSynsets);
        this.sap = new SAP(digraph); 
    }
    
    private Digraph parseHypernyms(String hypernyms, int synsetNum){
        Digraph di = new Digraph(synsetNum);
        In hyp = new In(hypernyms);
        while (!hyp.isEmpty())
        {
            String[] s = hyp.readLine().split(",");
            int from = Integer.parseInt(s[0]);
            for (int i=1; i<s.length ; i++)
            {
                int to = Integer.parseInt(s[i]);
                di.addEdge(from,to);
            } 
        }
        hyp.close();
        
        //check for cycles
        DirectedCycle cycle = new DirectedCycle(di);
        if (cycle.hasCycle())
            throw new java.lang.IllegalArgumentException("Cycle Detected");
        
        //check for one root
        int rts =0;
        for (int v=0; v<synsetNum; v++)
        {
            if(!di.adj(v).iterator().hasNext())
                rts++;
        }
        if (rts>1)
            throw new java.lang.IllegalArgumentException("Digraph has multiple roots");
        return di;
    }
    
    private int parseSynsets(String synsetsF){
        int num=0;
        In syns = new In(synsetsF);
        
        //add synset to our list
        while (!syns.isEmpty()){
            String[]s=syns.readLine().split(",");
            if (s.length<2) throw new java.lang.IllegalArgumentException();
            int id = Integer.parseInt(s[0]);
            this.synsets.add(id, s[1]);
            
            //check to see if words in synset have already been encountered
            //add information to our hashtable
            String[] wordsArr = s[1].split(" ");
            for (String w : wordsArr){
                Bag bag;
                if (this.words.containsKey(w)) bag = this.words.get(w);
                else bag = new Bag<Integer>();
                bag.add(id);
                this.words.put(w,bag);
            }
            num++;
        }
        //close input
        syns.close();
        return num;
    }
    
    // returns all WordWeb nouns (no duplicates!)
    public Iterable<String> nouns(){
        return Collections.list(this.words.keys());
    }
    
    // is the word a WordWeb noun?
    public boolean isNoun(String word){
        return this.words.containsKey(word);
    }
    
    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB){
        if (!isNoun(nounA)||!isNoun(nounB))
            throw new java.lang.IllegalArgumentException("Noun pair not recognized");
        Bag<Integer> v = this.words.get(nounA);
        Bag<Integer> w = this.words.get(nounB);
        return this.sap.length(v,w);
    }
    
    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB)
    {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new java.lang.IllegalArgumentException();
        Bag<Integer> v = this.words.get(nounA);
        Bag<Integer> w = this.words.get(nounB);
        int root = this.sap.ancestor(v, w);
        return this.synsets.get(root);
    }
    
    // do unit testing of this class
    public static void main(String[] args) 
    {
        String synsets = args[0];
        String hypernyms = args[1];
        WordWeb wn = new WordWeb(synsets, hypernyms);
        //print words to choose from
        StdOut.println(wn.nouns());
        while (!StdIn.isEmpty()) 
        {
            String v = StdIn.readString();            
            String w = StdIn.readString();
            String ancestor = wn.sap(v,w);
            int distance = wn.distance(v,w);
            StdOut.printf("distance = %d, ancestor = %s\n", distance, ancestor);
        }
    }
}


