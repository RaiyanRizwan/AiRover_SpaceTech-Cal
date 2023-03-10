package world;
import java.util.ArrayList;
import java.util.List;

/**
 * A class dedicated to managing the low level implementation of World, with all the edge and
 * corner cases, no pun intended.
 * @author Raiyan Rizwan
 */
public class WorldUtils {

    public static int dim;

    /** Concatenate items into an ArrayList, if lowerB <= item < upperB. */
    private static List<Integer> boundedConcat(int[][] arrays, int lowerBound, int upperBound) {

        List<Integer> arrSum = new ArrayList<>();

        for (int[] arr : arrays) {
            for (int i : arr) {
                if (lowerBound <= i && i < upperBound) {
                    arrSum.add(i);
                }
            }
        }

        return arrSum;
    }

    /** Converts (x, y) grid position to flat 1D position.
     * ex. (3, 3) --> 18 for a 5 x 5 grid. */
    public static int flatten(int x, int y) {
        return x + dim * y;
    }

    /** Converts flattened 1D coordinate back to 2D cords. */
    public static int[] getVector(int flat) {
        return new int[]{flat % dim, flat / dim};
    }

    /** Returns 2D cords of diagonals relative to pos @ fov distance.
     * @return {bottom right, bottom left, top right, top left} */
    private static int[] diagonals(int flat, int fov) {

        int[] xy = getVector(flat);
        int x = xy[0];
        int y = xy[1];

        int[] d = new int[4];
        d[0] = flatten(x + fov, y + fov);
        d[1] = flatten(x - fov, y + fov);
        d[2] = flatten(x + fov, y - fov);
        d[3] = flatten(x - fov, y - fov);

        return d;
    }

    /** Returns 1D cords vertically between flat1 and flat2 on grid. */
    private static int[] verticalSweepLeft(int agentPos, int flat1, int flat2, int fov) {
        return verticalSweepHelper(agentPos, flat1, flat2, -1, fov);
    }

    /** Returns 1D cords vertically between flat1 and flat2 on grid. */
    private static int[] verticalSweepRight(int agentPos, int flat1, int flat2, int fov) {
        return verticalSweepHelper(agentPos, flat1, flat2, 1, fov);
    }

    /** Returns 1D cords horizontally between flat1 and flat2 on grid. */
    private static int[] horizontalSweepTop(int agentPos, int flat1, int flat2, int fov) {
        return horizontalSweepHelper(agentPos, flat1, flat2, -1, fov);
    }

    /** Returns 1D cords horizontally between flat1 and flat2 on grid. */
    private static int[] horizontalSweepBottom(int agentPos, int flat1, int flat2, int fov) {
        return horizontalSweepHelper(agentPos, flat1, flat2, 1, fov);
    }

    private static int[] verticalSweepHelper(int agentPos, int flat1, int flat2, int rightOrLeft, int fov) {

        int absDif = Math.abs(flat2 - flat1);
        int entries = absDif / dim + 1;

        // up against top wall
        if (boundaryCheck(agentPos)[1]) {
            entries -= fov;
            flat1 = agentPos + fov * rightOrLeft; // directly right or left of agent pos
        }

        // up against bottom wall
        if (boundaryCheck(agentPos)[3]) {
            entries -= fov;
            flat2 = agentPos + fov * rightOrLeft; // directly right or left of agent pos
        }

        // collect relevant points
        int[] verticalFlats = new int[entries];
        int index = 0;
        while (flat1 <= flat2) {
            verticalFlats[index] = flat1;
            flat1 += dim;
            index++;
        }

        return verticalFlats;
    }

    private static int[] horizontalSweepHelper(int agentPos, int flat1, int flat2, int topOrBottom, int fov) {

        int absDif = Math.abs(flat2 - flat1);
        int entries = absDif + 1;

        // up against left wall
        if (boundaryCheck(agentPos)[0]) {
            entries -= fov;
            flat1 = agentPos + topOrBottom * dim * fov; // directly beneath or above
        }

        // up against right wall
        if (boundaryCheck(agentPos)[2]) {
            entries -= fov;
            flat2 = agentPos + topOrBottom * dim * fov; // directly beneath or above
        }

        // collect relevant points
        int[] horizontalFlats = new int[entries];
        int index = 0;
        while (flat1 <= flat2) {
            horizontalFlats[index] = flat1;
            index++;
            flat1++;
        }

        return horizontalFlats;
    }

    /** Checks if agent is against edges of map.
     * @return leftWall, topWall, rightWall, bottomWall */
    private static boolean[] boundaryCheck(int flat) {

        // get 2D format
        int[] xy = getVector(flat);
        int x = xy[0];
        int y = xy[1];

        boolean[] boundaryChecks = new boolean[4];

        // left wall
        boundaryChecks[0] = x == 0;

        // top wall
        boundaryChecks[1] = y == 0;

        // right wall
        boundaryChecks[2] = x == dim - 1;

        // bottom wall
        boundaryChecks[3] = y == dim - 1;

        return boundaryChecks;
    }

    /** Returns a list of the 1D grid cords of a square whose corners are
     * "fov" in every diagonal direction. */
    public static List<Integer> squareCords(int x, int y, int fov) {

        int pos = flatten(x, y);

        boolean[] boundsCheck = boundaryCheck(pos);
        boolean leftWall = boundsCheck[0];
        boolean topWall = boundsCheck[1];
        boolean rightWall = boundsCheck[2];
        boolean bottomWall = boundsCheck[3];

        int[] ds = diagonals(pos, fov);

        int[] vRight = new int[0];
        if (!rightWall) {
            vRight = verticalSweepRight(pos, ds[2], ds[0], fov);
        }
        int[] vLeft = new int[0];
        if (!leftWall) {
            vLeft = verticalSweepLeft(pos, ds[3], ds[1], fov);
        }
        int[] hTop = new int[0];
        if (!topWall) {
            hTop = horizontalSweepTop(pos, ds[3], ds[2], fov);
        }
        int[] hBtm = new int[0];
        if (!bottomWall) {
            hBtm = horizontalSweepBottom(pos, ds[1], ds[0], fov);
        }

        return boundedConcat(new int[][]{vRight, vLeft, hTop, hBtm}, 0, dim * dim);
    }

}
