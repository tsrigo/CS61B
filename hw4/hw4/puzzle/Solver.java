package hw4.puzzle;

import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Stack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Solver {
    static class searchNode {
        private WorldState state;
        private int dist;
        private searchNode prev;

        public searchNode(WorldState state, int dist, searchNode prev){
            this.state = state;
            this.dist = dist;
            this.prev = prev;
        }
        @Override
        public int hashCode() {
            return state.hashCode();
        }
    }

    static private HashMap<WorldState, Integer> Help = new HashMap<>();
    static private Comparator<searchNode> cmp = (o1, o2) -> {
        Integer t1 = Help.get(o1.state), t2 = Help.get(o2.state);
        int est1 = t1 == null ? o1.state.estimatedDistanceToGoal() : t1;
        int est2 = t2 == null ? o2.state.estimatedDistanceToGoal() : t2;
        return (o1.dist + est1) - (o2.dist + est2);
    };

    /*
    Constructor which solves the puzzle, computing
    everything necessary for moves() and solution() to
    not have to solve the problem again. Solves the
    puzzle using the A* algorithm. Assumes a solution exists.
     */
    private int moves;
    private Stack<WorldState> S = new Stack<>();
    public Solver(WorldState initial){
//        HashMap<searchNode, Boolean> H = new HashMap<>();
        int hh = 1;
        MinPQ<searchNode> Q = new MinPQ<>(cmp);
        searchNode init = new searchNode(initial, 0, null);
        Q.insert(init);
        while (!Q.isEmpty()){
            searchNode st = Q.delMin();
            //System.out.println(st.state);

            if (st.state.isGoal()) {
//                System.out.println(hh);
                moves = st.dist;
                S.push(st.state);
                for (searchNode i = st; !i.state.equals(initial);i = i.prev){
                    S.push(i.prev.state);
                }
                return;
            }
            for (WorldState e : st.state.neighbors()){
                searchNode ne = new searchNode(e, st.dist + 1, st);
                if (st.prev == null ||  !ne.state.equals(st.prev.state)){// ?H.get(ne) == null
                    Q.insert(ne);
//                    hh ++ ;
                }
            }
        }
    }

    public int moves(){
        return moves;
    }
    public Iterable<WorldState> solution(){
        return S;
    }
}