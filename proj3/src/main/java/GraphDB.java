import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    private HashMap<Long, Node> V;
    public class Node {
        long id;
        double lat;
        double lon;
        HashMap<String, String> tag;
        HashSet<Long> adj;
        Node(long id, double lat, double lon, HashMap<String, String> param, HashSet<Long> adj){
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.tag = param;
            this.adj = adj;
        }
    }

    public void setTag(long id, String k, String v){
        if (V.get(id) == null) {
            throw new IllegalArgumentException(id + " is not initialized.");
        }
        getTag(id).put(k, v);
    }
    public HashMap<String, String> getTag(long id){
        if (V.get(id) == null) {
            throw new IllegalArgumentException(id + " is not initialized.");
        }
        return V.get(id).tag;
    }
    public void addNode(long id, double lat, double lon){
        if (V == null) V = new HashMap<>();

        if (V.get(id) != null){
            throw new IllegalArgumentException("node " + id + " already exists");
        }
        V.put(id, new Node(id, lat, lon, new HashMap<>(), new HashSet<>()));
    }

    private HashMap<Long, LinkedList<Long>> E;

//    private static class Edge {
//        Long adjvex;
//        Edge next;
////        HashMap<String, String> tag;
//    }

    public void addEdge(long idA, long idB) {
        if (V.get(idA) == null) {
            throw new IllegalArgumentException(idA + " doesn't exist or isn't initialized.");
        }
        if (V.get(idB) == null) {
            throw new IllegalArgumentException(idB + " doesn't exist or isn't initialized.");
        }
        if (E == null) {
            E = new HashMap<>();
        }
        if (V.get(idA).adj == null){
            V.get(idA).adj = new HashSet<>();
        }
        if (V.get(idB).adj == null){
            V.get(idB).adj = new HashSet<>();
        }
        E.computeIfAbsent(idA, k -> new LinkedList<>());
        E.computeIfAbsent(idB, k -> new LinkedList<>());

        E.get(idA).add(idB);

        V.get(idA).adj.add(idB);
        V.get(idB).adj.add(idA);
    }

    HashMap<Long, Way> W;
    public class Way {
        long id;
        HashMap<String, String> tag;
        LinkedList<Long> ref;
        Way(){
            tag = new HashMap<>();
            ref = new LinkedList<>();
        }
    }
    public void addWay(long id){
        if (W == null){
            W = new HashMap<>();
        }
        W.put(id, new Way());
    }

    public void addWaytag(long id, String K, String V){
        if (W.get(id) == null){
            throw new IllegalArgumentException(id + " not existed");
        }
        W.get(id).tag.put(K, V);
    }

    public String getWaytag(long id, String K){
        if (W.get(id) == null){
            throw new IllegalArgumentException(id + " not existed");
        }
        return W.get(id).tag.get(K);
    }
    public GraphDB(){

    }

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        HashSet<Long> tep = new HashSet<>();
        for (long i : vertices()){
            if (V.get(i).adj == null || V.get(i).adj.size() == 0){
                tep.add(i);
            }
        }
        for (long i : tep){
            V.remove(i);
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return V.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        if (V.get(v) == null){
            throw new IllegalArgumentException(v + " is not existed!");
        }
        return V.get(v).adj;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        long ans = -1;
        double dis = 10000000.0;
        for (long i : vertices()){
            double iLon = lon(i), iLat = lat(i);
            double tep = distance(lon, lat, iLon, iLat);
            if (tep < dis){
                ans = i;
                dis = tep;
            }
        }
        return ans;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        if (V.get(v) == null){
            throw new IllegalArgumentException(v + " doesn't exist");
        }
        return V.get(v).lon;
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        if (V.get(v) == null){
            throw new IllegalArgumentException(v + " doesn't exist");
        }
        return V.get(v).lat;
    }
}
