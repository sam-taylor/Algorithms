import java.util.Arrays;
import java.util.HashMap;

public class MathematicalElimination
{
    
    private final int numTeams;
    
    private HashMap<Integer, Bag<Integer>> certificates;
    
    private String[] teamsArray;
    private int[] wins;
    private int[] losses;
    private int[] remaining;
    private int[][] games;
    
    
    public MathematicalElimination(String filename)                    // create a baseball division from given filename in format specified below
    {
        In in = new In(filename);
        this.numTeams = in.readInt();
        teamsArray = new String[numTeams];
        remaining = new int[numTeams];
        wins = new int[numTeams];
        losses = new int[numTeams];
        games = new int[numTeams][numTeams];
        certificates = new HashMap<Integer, Bag<Integer>>(numTeams);
        
        for (int i = 0; i < numTeams; i++)
        {
            teamsArray[i] = in.readString();
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            remaining[i] = in.readInt();
            for (int j = 0; j < numTeams; j++)
                games[i][j] = in.readInt();
        }
    }
    
    
    public              int numberOfTeams()                        // number of teams
    {
        return this.numTeams;
    }
    
    public Iterable<String> teams()                                // all teams
    {
        return Arrays.asList(teamsArray);
    }
    
    public              int wins(String team)                      // number of wins for given team
    {
        return this.wins[teamIndex(team)];
    }
    
    private int teamIndex(String team)
    {
        for (int i = 0; i < numTeams; i++)
        {
            if (teamsArray[i].equals(team))
                return i;
        }
        throw new java.lang.IllegalArgumentException(); 
    }
    
    public              int losses(String team)                    // number of losses for given team
    {
        return this.losses[teamIndex(team)];
    }
    
    public              int remaining(String team)                 // number of remaining games for given team
    {
        return this.remaining[teamIndex(team)];
    }
    
    public              int against(String team1, String team2)    // number of remaining games between team1 and team2
    {
        int t1 = teamIndex(team1);
        int t2 = teamIndex(team2);
        return this.games[t1][t2];
    }
    
    public          boolean isEliminated(String team)              // is given team eliminated?
    {
        int x = teamIndex(team);
        if (!certificates.containsKey(x))
            runElim(x);
        
        return !certificates.get(x).isEmpty();
    }
    
    private FlowNetwork createFN(int teamX)
    {
        int numMatchups = (numTeams)*(numTeams-1)/2;
        int numVertices = 2 + numMatchups + numTeams;
        int s = numVertices - 2;
        int t = s + 1;
        int possibleXWins = wins[teamX] + remaining[teamX];
        FlowNetwork fn = new FlowNetwork(numVertices);
        for (int i = 0; i < numTeams; i++)
        {
            if (i != teamX)
                fn.addEdge(new FlowEdge (i, t, possibleXWins - wins[i]));
        }
        for (int i = 0, g = 0; i < numTeams; i++)
        {
            for (int j = i + 1; j < numTeams; j++, g++)
            {
                if (i != teamX && j != teamX)
                {
                    fn.addEdge(new FlowEdge(s, numTeams+g, games[i][j]));
                    fn.addEdge(new FlowEdge(numTeams+g, i, Double.MAX_VALUE));
                    fn.addEdge(new FlowEdge(numTeams+g, j, Double.MAX_VALUE));
                }
            }
        }
        //StdOut.println(fn); 
        return fn;
    }
    
    private FordFulkerson createFF(FlowNetwork fn)
    {
        int t = fn.V()-1;
        int s = t-1;
        return new FordFulkerson(fn, s, t);
    }
    
    private boolean trivialElim(int team)
    {
        int possibleWins = wins[team] + remaining[team];
        for (int i = 0; i < numTeams; i++)
        {
            if (wins[i] > possibleWins)
            {
                Bag<Integer> b = new Bag<Integer>();
                b.add(i);
                certificates.put(team, b);
                return true;                
            }
        }
        return false;
    }
    
    private boolean isFull(FlowNetwork fn)
    {
        int s = fn.V() - 2;
        for (FlowEdge e : fn.adj(s))
        {
            if (e.capacity() != e.flow())
                return false;
        }
        return true;
    }
    
    private void runElim(int team) 
    {       
        if (trivialElim(team))
            return;
        
        FlowNetwork fn = createFN(team);
        FordFulkerson maxflow = createFF(fn);
        Bag<Integer> elims = new Bag<Integer>();
        if (!isFull(fn))        
        {
            for (int v = 0; v < numTeams; v++)
            {
                if (maxflow.inCut(v))
                    elims.add(v);
            }            
        }
        certificates.put(team, elims);
    }
    
    public Iterable<String> certificateOfElimination(String team)  
    {
        int x = teamIndex(team);
        if (!certificates.containsKey(x))
            runElim(x);
        if (certificates.get(x).isEmpty())
            return null;
        Bag<String> cert = new Bag<String>();
        for (int i : certificates.get(x))
        {
            cert.add(teamsArray[i]);
        }
        return cert;
    }
    
    
public static void main(String[] args) {
    MathematicalElimination division = new MathematicalElimination(args[0]);
    for (String team : division.teams()) {
        if (division.isEliminated(team)) {
            StdOut.print(team + " is eliminated by the subset R = { ");
            for (String t : division.certificateOfElimination(team)) 
                StdOut.print(t + " ");
            StdOut.println("}");
        }
        else {
            StdOut.println(team + " is not eliminated");
        }
    }
}
}