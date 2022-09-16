import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {

        long st = g.closest(stlon, stlat);
        long ed = g.closest(destlon, destlat);

        final HashMap<Long, Double> disTO = new HashMap<>();
        final HashMap<Long, Long> edgeTo = new HashMap<>();
        final HashMap<Long, Boolean> marked = new HashMap<>();
        final HashMap<Long, Double> helpDis = new HashMap<>();
        final Comparator<Long> cmp = (o1, o2) -> {
            // TODO: fix the A*
            Double a = disTO.get(o1) + g.distance(o1, ed);
            Double b = disTO.get(o2) + g.distance(o2, ed);
            return a.compareTo(b);
        };
        // 能不能返回正确的大小关系？ —— 开始确实不能 // (disTO.get(o1) > disTO.get(o2)); // BUG : 使用 (int) (disTO.get(o1) - disTO.get(o2))
        LinkedList<Long> ans = new LinkedList<>();
        PriorityQueue<Long> Q = new PriorityQueue<>(cmp);

        disTO.put(st, 0.0); // 这里是不是 0 ？ —— 是
        Q.add(st);
        while (!Q.isEmpty()){
            long now = Q.poll();

            if (marked.get(now) != null){ // BUG: 节点应该在出队的时候才进行标记
                continue;
            }
            marked.put(now, true);

            for (long i : g.adjacent(now)){
                if (disTO.get(i) == null || disTO.get(i) > disTO.get(now) + g.distance(i, now)){
                    edgeTo.put(i, now);
                    disTO.put(i, disTO.get(now) + g.distance(i, now));
                    Q.add(i);
                }
            }
        }
        if (disTO.get(ed) == null || edgeTo.get(ed) == null) {
            throw new IllegalArgumentException("ed init failed");
        }

        long t = ed;
        while (t != st){
            ans.addFirst(t);
            t = edgeTo.get(t);
        }
        ans.addFirst(st);
        return ans; // FIXME
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> ans = new ArrayList<>();
        NavigationDirection st = new NavigationDirection(0, g.getTag(route.get(0)).get("name"), 0);

        // TODO 251225761 表示的是一个路口，很多路上都会有它

        long prev = route.get(0);
        String curName = st.way;
        NavigationDirection now = st;
        for (int i = 0; i < route.size();i ++ ){
            long curNode = route.get(i);
//            System.out.println("curNode is " + curNode + g.getTag(curNode));
            if (getBear(g, curNode, prev) == 1 || (g.getTag(curNode).get("name") != null && g.getTag(curNode).get("name").equals(curName))){
                now.distance += g.distance(curNode, prev);
            }
            else {
                System.out.println(now);
                ans.add(now);
                int bear = getBear(g, curNode, prev);
                String name = g.getTag(curNode).get("name");
                NavigationDirection tran = new NavigationDirection(bear, name, 0);
                now = tran;
                curName = name;
            }
            prev = curNode;
        }
        return ans;
    }

    private static int getBear(GraphDB g, long a, long b){
        double bear = g.bearing(a, b);
        if (bear > 0){
            if (bear < 15) return 1;
            if (bear < 30) return 3;
            if (bear < 100) return 4;
            else return 7;
        }
        else {
            bear = -bear;
            if (bear < 15) return 1;
            if (bear < 30) return 2;
            if (bear < 100) return 5;
            else return 6;
        }
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public NavigationDirection(int direction, String way, double distance) {
            this.direction = direction;
            this.way = way;
            this.distance = distance;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
