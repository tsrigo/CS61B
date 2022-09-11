package hw4.puzzle;

import edu.princeton.cs.algs4.Queue;

public class Board implements WorldState{
    private static final int BLANK = 0;
    private final int[][] a;    // a 要求是immutable的
    private final int N;
    public Board(int[][] tiles){
        N = tiles.length;       // BUG: 一开始想把tiles变成 1 维的，但是空间是不够的，无法复制
        a = new int[N][N];      // BUG: 没有把tiles的值复制给a，而是令a等于tiles
        for (int x = 0; x < N; x++) {
            System.arraycopy(tiles[x], 0, a[x], 0, N);
        }
    }
    public int tileAt(int i, int j){
        if (i > size() || j > size() || i < 0 || j < 0) throw new IllegalArgumentException("Out of range");
        return a[i][j];
    }
    public int size(){
        return N;   // BUG: 没有理解题意，写成了N*N，会和neighbour冲突
    }

    /**
     * Returns neighbors of this board.
     * SPOILERZ: This is the answer.
     */
    @Override
    public Iterable<WorldState> neighbors() {
        Queue<WorldState> neighbors = new Queue<>();
        int N = size();
        int zeroX = -1;
        int zeroY = -1;
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                // System.out.println(x + " " + y);
                if (tileAt(x, y) == BLANK) {
                    zeroX = x;
                    zeroY = y;
                }
            }
        }
        // System.out.println();
        int[][] copy = new int[N][N];
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                copy[x][y] = tileAt(x, y);
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (Math.abs(i - zeroX) + Math.abs(j - zeroY) - 1 == 0) {
                    copy[zeroX][zeroY] = copy[i][j];
                    copy[i][j] = BLANK;
                    Board neighbor = new Board(copy);
                    neighbors.enqueue(neighbor);
                    copy[i][j] = copy[zeroX][zeroY];
                    copy[zeroX][zeroY] = BLANK;
                }
            }
        }
        return neighbors;
    }
    public int hamming(){
        int res = 0;
        for (int i = 0; i < N; i ++ ){
            for (int j = 0; j < N; j ++ ){
                if (a[i][j] == 0) continue;     // BUG: 不用考虑 0
                if (a[i][j] != i * N + j + 1){
                    res ++ ;
                }
            }
        }
        return res;
    }
    public int manhattan(){
        int res = 0;
        for (int i = 0; i < N; i ++ ){
            for (int j = 0; j < N; j ++ ){
                int now = a[i][j] - 1, nx, ny;
                if (now == -1) {  // BUG: 不用考虑 0
                    continue;
                }
                else {
                    nx = now / N;
                    ny = now % N;
                }
                res += Math.abs(nx - i) + Math.abs(ny - j); // BUG: 忘记加绝对值
            }
        }
        return res;
    }
    public int estimatedDistanceToGoal(){
        return manhattan();
    }
    public boolean equals(Object y){
        if (!(y instanceof Board ny)){
            throw new IllegalArgumentException("y is not instance of Board");
        }
        if (this.size() != ny.size()) return false;
        for (int i = 0; i < N; i ++ ){
            for (int j = 0; j < N; j ++ ){
                if (tileAt(i, j) != ny.tileAt(i, j))
                    return false;
            }
        }
        return true;
    }
    /** Returns the string representation of the board.
      * Uncomment this method. */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int N = size();
        s.append(N).append("\n");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                s.append(String.format("%2d ", tileAt(i,j)));
            }
            s.append("\n");
        }
        s.append("\n");
        return s.toString();
    }

}
