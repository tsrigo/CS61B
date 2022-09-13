import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 *
 * @author 123
 *
 */
public class Rasterer {
    /*
    TODO: 在拖动到某些位置时，地图会突然跳转，特别是在放大情况无法移动到下端
     */
    /**
     * 两个角落的坐标.
     */
    public static final double
            ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;

    /**
     *  图片的长和宽各有256个像素.
     */
    public static final double IMAGE_PIXELS = 256;
    /**
     * 图像层级，和放大的倍数有关.
     */
    private int depth = -1;
    /**
     * 我们计算结果的图像的LonDpp.
     */
    private double ansLonDpp = -1;
    /**
     * 由图像层级确定的每一 行/列 的图像个数.
     */
    private int imageNum = -1;


    /**
     * 暂时不知道要干嘛.
     */
    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     *  包含ulX, ulY, lrX, lrY的数组.
     */
    private int[] ulAndlr = new int[4];

    /**
     * 设置四个角落的坐标，给定 start 和 target 和 set 确定是哪个角落的哪个坐标（整数）.
     * @param start 来自四个原始角落的横纵坐标
     * @param target 来自四个目标角落的横纵坐标
     * @param set set 的 0-3 对应了 ulX, ulY, lrX, lrY.
     * @return 返回坐标所对应的经度/纬度
     */
    private double getcor(double start, double target, int set) {
        int flag = 1, bias = 0;
        if (set == 1 || set == 3) {
            flag = -1;
        }
        if (set == 0 || set == 1) {
            bias = -1;
        }

        if ((flag) * target < (flag) * start) {
            ulAndlr[set] = 0;
            return start;
        }
        if (flag == 1) {
            for (int i = 0; i <= imageNum; i++) {
                if ((flag) * start + i * 256 * ansLonDpp > (flag) * target) {
                    ulAndlr[set] = i - 1;
                    return start + (flag) * (i + bias) * 256 * ansLonDpp;
                }
            }
            ulAndlr[set] = imageNum - 1;
            return start + imageNum * 256 * ansLonDpp;
        } else {
            double culLatDpp = (ROOT_ULLAT - ROOT_LRLAT) / ((1 << depth) * 256);
            for (int i = 0; i <= imageNum; i++) {
                if ((flag) * start + i * 256 * culLatDpp > (flag) * target) {
                    ulAndlr[set] = i - 1;
                    return start + (flag) * (i + bias) * 256 * culLatDpp;
                }
            }
            ulAndlr[set] = imageNum - 1;
            return start + imageNum * 256 * culLatDpp;
        }
    }

    /**
     * 对参数进行检查，看是否不合法.
     * @param params 以 Map 打包而成的参数，具体含义见getMapRaster的javadoc
     * @return 返回参数是否合法
     */
    private boolean check(Map<String, Double> params) {
        double lrlon = params.get("lrlon"), ullon = params.get("ullon"),
                w = params.get("w"), h = params.get("h"),
                ullat = params.get("ullat"), lrlat = params.get("lrlat");

        return !(lrlon <= ROOT_ULLON) && !(ullon >= ROOT_LRLON)
                && !(ullat <= ROOT_LRLAT) && !(lrlat >= ROOT_ULLAT)
                && !(w <= 0) && !(h <= 0);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel
     *     (LonDPP) possible, while still covering less than or equal to the amount of
     *     longitudinal distance per pixel in the query box for the user viewport size. </li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the
     *     above condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        System.out.println(params);
        Map<String, Object> results = new HashMap<>();

        if (!check(params)) {
            results.put("query_success", false);
            return results;
        }
        double lrlon = params.get("lrlon"), ullon = params.get("ullon"), w = params.get("w"),
                h = params.get("h"), ullat = params.get("ullat"), lrlat = params.get("lrlat");

        double tarLonDpp = (lrlon - ullon) / w;

        /*
        找出小于目标LonDpp的最大LonDpp，由于dep越大，LonDpp就会越大，所以一但找到就可以返回
         */
        for (int dep = 0; dep <= 7; dep++) {
            imageNum = 1 << dep;
            double tepLonDpp = (ROOT_LRLON - ROOT_ULLON) / (imageNum * IMAGE_PIXELS);
            /*
            如果没有满足条件的，应该用现有的最小LonDpp，即dep == 7对应的LonDpp
             */
            if (dep == 7 || tepLonDpp < tarLonDpp) {
                ansLonDpp = tepLonDpp;
                depth = dep;
                break;
            }
        }

        /*
        找出lr和ul的坐标，精度，维度。ulX, ulY, lrX, lrY; x对应了lon，y对应了lat
         */
        results.put("raster_lr_lon", getcor(ROOT_ULLON, lrlon, 2));
        results.put("raster_ul_lon", getcor(ROOT_ULLON, ullon, 0));
        results.put("raster_lr_lat", getcor(ROOT_ULLAT, lrlat, 3));
        results.put("raster_ul_lat", getcor(ROOT_ULLAT, ullat, 1));
        results.put("depth", depth);
        results.put("query_success", true);

        int ulX = ulAndlr[0], ulY = ulAndlr[1], lrX = ulAndlr[2], lrY = ulAndlr[3];
        String[][] grid = new String[lrY - ulY + 1][lrX - ulX + 1];
        int size = 0;
        for (int i = ulY, row = 0; i <= lrY; i++, row++) {
            for (int j = ulX, col = 0; j <= lrX; j++, col++) {
                grid[row][col] = String.format("d%d_x%d_y%d.png", depth, j, i);
                size++;
            }
        }
        if (size == 0) {
            results.put("query_success", false);
            return results;
        }
        results.put("render_grid", grid);
        return results;
    }

}
