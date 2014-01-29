import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }
    
    public Set<Tile> unseen = new HashSet<Tile>();
    
    
    
    @Override
	public void setup(int loadTime, int turnTime, int rows, int cols,
			int turns, int viewRadius2, int attackRadius2, int spawnRadius2) {
		super.setup(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2, spawnRadius2);
		unseen = new HashSet<Tile>();
		for(int col = 0; col < cols; ++col) {
			for(int row = 0; row < rows; ++row) {
				unseen.add(new Tile(row,col));
			}
		}
	}

	/**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
        Ants ants = getAnts();
        Set<Tile> toMove = new HashSet<Tile>();
        Random rnd = new Random();
        for(int col = 0; col < ants.getCols(); ++col) {
			for(int row = 0; row < ants.getRows(); ++row) {
				Tile t = new Tile(row, col);
				if(ants.isVisible(t)) unseen.remove(t);
			}
		}
        for (Tile myAnt : ants.getMyAnts()) {
        	Set<Tile> targets = new HashSet<Tile>(ants.getFoodTiles());
        	targets.addAll(ants.getEnemyAnts());
        	targets.addAll(ants.getEnemyHills());
        	targets.addAll(unseen);
        	Tile nearest = getNearest(myAnt, targets, ants);
        	if(nearest == null /*|| ants.getDistance(myAnt, nearest) > ants.getViewRadius2() * 2 */) {
        		List<Aim> dirs = new ArrayList<Aim>(Arrays.asList(Aim.values()));
        		Collections.shuffle(dirs);
        		for(Aim direction : dirs) {
            		Tile to = ants.getTile(myAnt, direction);
	                if (ants.getIlk(myAnt, direction).isPassable() && !toMove.contains(to) && !ants.getMyAnts().contains(to)) {
	                    ants.issueOrder(myAnt, direction);
	                    toMove.add(to);
	                    break;
	                }
            	}
        	} else {
        		Tile to = towards(myAnt, nearest, ants);
        		if(!toMove.contains(to) && !ants.getMyAnts().contains(to)) {
        			toMove.add(to);
        			ants.issueOrder(myAnt, tileToAim(myAnt, to, ants));
        		} else {
        			toMove.add(myAnt);
        		}
        	}
        }
    }

	private Tile getNearest(Tile myAnt, Set<Tile> foodTiles, Ants ants) {
		int min = Integer.MAX_VALUE;
		Tile nearest = null;
		for(Tile t : foodTiles) {
			int d = ants.getDistance(myAnt, t);
			if(d < min) {
				min = d;
				nearest = t;
			}
		}
		return nearest;
	}

	
	public static class Node {
		
		public Node(Tile from, List<Tile> path2) {
			t = from;
			path = path2;
		}
		
		public Tile t;
		public List<Tile> path = new ArrayList<Tile>();
	}
	
	public static List<Tile> getPath(Tile from, Tile to, Ants world) {
		Queue<Node> tbd = new ArrayDeque<MyBot.Node>();
		List<Tile> path = new ArrayList<Tile>();
		Set<Tile> visited = new HashSet<Tile>();
		path.add(from);
		visited.add(from);
		tbd.add(new Node(from, path));
		while(!tbd.isEmpty()) {
			Node n = tbd.remove();
			if(n.t.equals(to)) return n.path;
			for(Tile nt : getNeighbours(n.t, world)) {
				if(!visited.contains(nt) && world.getIlk(nt).isPassable()) {
					List<Tile> p = new ArrayList<Tile>(n.path);
					p.add(nt);
					tbd.add(new Node(nt, p));
					visited.add(nt);
				}
			}
		}
		return Collections.emptyList();
	}
	
	private static Set<Tile> getNeighbours(Tile t, Ants world) {
		Set<Tile> result = new HashSet<Tile>();
		for(Aim d : Aim.values()) {
			result.add(world.getTile(t, d));
		}
		return result;
	}

	public static Aim tileToAim(Tile from, Tile next, Ants world) {
		return world.getDirections(from, next).get(0);
	}
	
	public static Tile towards(Tile from, Tile to, Ants world) {
		List<Tile> path = getPath(from, to, world);
		// TODO : Jotain parempaa!
		if(path.size() < 2) return getNeighbours(from, world).iterator().next();
		Tile next = path.get(1);
		return next;
	}
}
