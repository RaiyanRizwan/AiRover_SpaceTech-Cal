package world;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import static world.WorldUtils.flatten;
import static world.WorldUtils.squareCords;


/**
 * Created to simulate an AI agent exploring an environment with blocked paths,
 * and attempting to find a path to its target.
 * @author Raiyan Rizwan
 */
public class World {

    // TODO: fix known blacklist issues & case of no path

    private final Map<Integer, Boolean> exploreWorld;
    private final Boolean[] realWorld;
    private Agent agent;
    private final int dim;
    private final int targetX;
    private final int targetY;

    public static void main(String[] args) {
        // USE FOR TESTING
        World world = new World(15, 0, 0, 1, 0.75);
        System.out.println(world);
        world.agent.look();
        System.out.println(world.exploreWorldVisualizer());

        int moves = 0;
        while (!world.agentReachedTarget()) {
            world.agent.move(world.targetX, world.targetY);
            System.out.println(world.exploreWorldVisualizer());
            moves++;
        }
        System.out.println("Agent succeeded! Moves: " + moves);

    }

    /** Creates an N x N World grid.
     * realWorld, true = open, false = closed */
    public World(int N, int agentX0, int agentY0, int agentFov, double openness) {

        dim = N;
        WorldUtils.dim = dim;
        exploreWorld = new HashMap<>();

        agent = new Agent(agentX0, agentY0, agentFov);
        exploreWorld.put(flatten(agent.xpos, agent.ypos), true);
        realWorld = new Boolean[N * N];
        setupRealWorld(openness);

        targetX = N - 1;
        targetY = N - 1;
    }

    public boolean agentReachedTarget() {
        return agent.xpos == targetX && agent.ypos == targetY;
    }

    /** Updates explored data around spots the agent has looked at with a camera
     * or sensor in the real world. */
    public void updateExplored(List<Integer> flats) {
        for (int flat: flats) {
            exploreWorld.put(flat, realWorld[flat]);
        }
    }

    /** Reset the Agent and the World Data. */
    public void resetAgentWorld(int agentX0, int agentY0, int agentFov) {
        agent = new Agent(agentX0, agentY0, agentFov);
        exploreWorld.clear();
        exploreWorld.put(flatten(agent.xpos, agent.ypos), true);
    }

    /** Creates RealWorld with ~ openRate % of positions open. */
    private void setupRealWorld(double openRate) {
        Random rand = new Random();
        for (int s = 0; s < dim * dim; s++) {
            int prob = rand.nextInt(100);
            realWorld[s] = prob <= (int) (openRate * 100);
        }
        realWorld[flatten(agent.xpos, agent.ypos)] = true; // agent pos always open
        realWorld[flatten(targetX, targetY)] = true; // target always open
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Real World: \n");

        for (int y = 0; y < dim; y++) {
            sb.append("[");
            for (int x = 0; x < dim; x++) {
                boolean rWS = realWorld[flatten(x, y)];
                if (rWS) {
                    sb.append(" 0 ");
                } else {
                    sb.append(" 1 ");
                }
            }
            sb.append("]\n");
        }

        return sb.toString();
    }

    /** Console display of explored world. ? positions are unknown. 1 = closed. 0 = open. */
    public String exploreWorldVisualizer() {

        StringBuilder sb = new StringBuilder();

        sb.append("Explored World: \n");

        for (int y = 0; y < dim; y++) {
            sb.append("[");
            for (int x = 0; x < dim; x++) {
                Boolean rWS = exploreWorld.getOrDefault(flatten(x, y), null);
                if (y == agent.ypos && x == agent.xpos) {
                    sb.append(" R ");
                } else if (y == targetY && x == targetX) {
                    sb.append(" T ");
                } else if (rWS == null) {
                    sb.append(" ? ");
                } else if (rWS) {
                    sb.append(" 0 ");
                } else {
                    sb.append(" 1 ");
                }
            }
            sb.append("]\n");
        }

        return sb.toString();

    }

    private class Agent {

        private int xpos;
        private int ypos;
        private final int viewDistance;
        private final List<Integer> blackListed;

        private Agent(int x0, int y0, int fov) {
            xpos = x0;
            ypos = y0;
            viewDistance = fov;
            blackListed = new ArrayList<>();
        }

        /** Agent searches and updates knowledge of surroundings. */
        private void look() {
            for (int fov = 1; fov <= viewDistance; fov++) {
                List<Integer> surroundings = squareCords(xpos, ypos, fov);
                updateExplored(surroundings);
            }
        }

        /** Moves agent to new position. */
        private void move(int x, int y) {

            int[] moves = generateMoves(xpos, ypos, x, y);

            xpos += moves[0];
            ypos += moves[1];
            look();
        }

        /** @return arr[0]: x movement, arr[1]: y movement */
        private int[] generateMoves(int x, int y, int tx, int ty) {

            int[] moves = new int[2];

            int xdir = Integer.signum(tx - x);
            int ydir = Integer.signum(ty - y);

            int contestedX = flatten(x + xdir, y);
            int contestedY = flatten(x, y + ydir);
            int contestedD = flatten(x + xdir, y + ydir);

            // same x or y as target & movement not obstructed
            if (x - tx == 0 && open(contestedY) && notBlackListed(contestedY)) {
                moves[1] = ydir;
            } else if (y - ty == 0 && open(contestedX) && notBlackListed(contestedX)) {
                moves[0] = xdir;
            }

            if (open(contestedX) && notBlackListed(contestedX)) {
                moves[0] = xdir;
                moves[1] = 0;
            }

            if (open(contestedY) && notBlackListed(contestedY)) {
                moves[0] = 0;
                moves[1] = ydir;
            }

            if (open(contestedD) && notBlackListed(contestedD)) {
                moves[0] = xdir;
                moves[1] = ydir;
            }

            // all obstructed, therefore blacklist & move back
            if (moves[0] == 0 && moves[1] == 0) {

                blackListed.add(flatten(xpos, ypos));

                int backContestedX = flatten(x - xdir, y);
                int backContestedY = flatten(x, y - ydir);
                int backContestedD = flatten(x - xdir, y - ydir);

                if (open(backContestedX) && notBlackListed(backContestedX)) {
                    moves[0] = -xdir;
                }

                if (open(backContestedY) && notBlackListed(backContestedY)) {
                    moves[1] = -ydir;
                }

                if (open(backContestedD) && notBlackListed(backContestedD)) {
                    moves[0] = -xdir;
                    moves[1] = -ydir;
                }
            }

            return moves;
        }

        /** Check whether spot is obstructed, "1" in explored world. */
        private boolean open(int flat) {
            return exploreWorld.get(flat);
        }

        /** Check whether spot is blacklisted, aka no path from here on. */
        private boolean notBlackListed(int flat) {
            return !blackListed.contains(flat);
        }

    }

}
