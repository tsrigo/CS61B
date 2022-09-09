package lab11.graphs;

import edu.princeton.cs.algs4.In;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *  @author Josh Hug
 */
public class MazeBreadthFirstPaths extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    private int s;
    private int t;

    public MazeBreadthFirstPaths(Maze m, int sourceX, int sourceY, int targetX, int targetY) {
        super(m);
        // Add more variables here!
        maze = m;
        s = maze.xyTo1D(sourceX, sourceY);
        t = maze.xyTo1D(targetX, targetY);
        distTo[s] = 0;
        edgeTo[s] = s;
    }

    /** Conducts a breadth first search of the maze starting at the source. */
    private void bfs() {
        // TODO: Your code here. Don't forget to update distTo, edgeTo, and marked, as well as call announce()
        LinkedBlockingQueue<Integer> Q = new LinkedBlockingQueue<>();
        Q.add(s);
        marked[s] = true;
        while (!Q.isEmpty()){
            int head = Q.poll();
            if (head == t){
                return ;
            }
            for (int w : maze.adj(head)){
                if (!marked[w]){
                    marked[w] = true;
                    edgeTo[w] = head;
                    announce();
                    distTo[w] = distTo[head] + 1;
                    Q.add(w);
                }
            }
        }
    }


    @Override
    public void solve() {
        bfs();
    }
}

