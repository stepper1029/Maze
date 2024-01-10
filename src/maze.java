import java.util.*;
import tester.*;
import javalib.impworld.*;
import javalib.worldcanvas.WorldCanvas;
import java.awt.Color;
import javalib.worldimages.*;

//represents a node
class Node {
  int x;
  int y;
  int randSeed;

  Node(int x, int y) {
    this.x = x;
    this.y = y;
  }

  // additional constructor with seed to test random
  Node(int x, int y, int r) {
    this.x = x;
    this.y = y;
    this.randSeed = r;
  }

  // checks if the node's tree contains the given node
  boolean contains(Node destination, ArrayList<Node> alreadychecked, ArrayList<Edge> mazeedges) {
    ArrayList<Node> worklist = new ArrayList<Node>();
    ArrayList<Node> checked = new ArrayList<Node>(alreadychecked);
    worklist.add(this);

    while (!worklist.isEmpty()) {
      Node curr = worklist.remove(0);
      if (curr.equals(destination)) {
        return true;
      }
      if (!checked.contains(curr)) {
        checked.add(curr);
        for (Node n : curr.listofDirectConnections(mazeedges)) {
          worklist.add(n);
        }
      }
    }
    return false;
  }

  // generates a hash code
  public int hashCode() {
    Random rand = new Random((Math.abs(x * y) + 1));
    return rand.nextInt(Math.abs(this.randSeed) + 1) + rand.nextInt(Math.abs(this.randSeed) + 1);
  }

  // renders the node with a given color
  public void displaycolor(Color color, WorldScene canvas, int squaresize) {
    canvas.placeImageXY(new RectangleImage(squaresize, squaresize, "solid", color), this.x, this.y);
  }

  // determines if one object is equal to another
  public boolean equals(Object other) {
    if (!(other instanceof Node)) {
      return false;
    }
    // this cast is safe, because we just checked instanceof
    Node that = (Node) other;
    return (that.x == this.x) && (that.y == this.y);
  }

  // renders the border
  void displayborderEdge(WorldScene canvas, Node lastNode, int squaresize, int buffer) {
    int maxx = lastNode.x;
    int maxy = lastNode.y;

    if (this.x == buffer) {
      canvas.placeImageXY(new LineImage(new Posn(0, squaresize), Color.BLACK), x - squaresize / 2,
          this.y);
    }

    if (this.x == maxx) {
      canvas.placeImageXY(new LineImage(new Posn(0, squaresize), Color.BLACK), x + squaresize / 2,
          this.y);
    }

    if (this.y == maxy) {
      canvas.placeImageXY(new LineImage(new Posn(squaresize, 0), Color.BLACK), x,
          this.y + squaresize / 2);
    }

    if (this.y == buffer) {
      canvas.placeImageXY(new LineImage(new Posn(squaresize, 0), Color.BLACK), x,
          this.y - squaresize / 2);
    }
  }

  // checks if this edge is connected to that edge given the representatives of
  // connections
  boolean isConnected(Node that, HashMap<Node, Node> representatives) {
    ArrayList<Node> connectionsofthis = new ArrayList<Node>();
    Node curr = this;
    while (curr != representatives.get(curr)) {
      connectionsofthis.add(curr);
      curr = representatives.get(curr);
    }
    connectionsofthis.add(curr);
    Node alt = that;
    while (alt != representatives.get(alt) && !connectionsofthis.contains(alt)) {
      Node temp = representatives.get(alt);
      alt = temp;
    }
    return connectionsofthis.contains(alt);
  }

  // connects the this not to the to-node
  // Effect: this nodes connection leads to that node in the representatives
  // hashMap
  void connect(Node to, HashMap<Node, Node> representatives) {
    Node curr = this;
    while (curr != representatives.get(curr)) {
      curr = representatives.get(curr);
    }
    representatives.put(curr, to);
  }

  // returns the posn of the node
  Posn pointAsPosn() {
    return new Posn(this.x, this.y);
  }

  // x-coordinate of the midpoint till that node
  int nodeMidX(Node that) {
    return (this.x + that.x) / 2;
  }

  // y-coordinate of the midpoint till that node
  int nodeMidY(Node that) {
    return (this.y + that.y) / 2;
  }

  // gives a list of all the direct connections to this node given the node
  ArrayList<Node> listofDirectConnections(ArrayList<Edge> mazeedges) {
    ArrayList<Node> directconnectingnodes = new ArrayList<Node>();

    for (int i = 0; i < mazeedges.size(); i = i + 1) {
      Edge e = mazeedges.get(i);
      if (e.isConnectingNode(this)) {
        directconnectingnodes.add(e.getConnectingNode(this));
      }
    }
    return directconnectingnodes;
  }
}

//represents an edge
class Edge {
  Node from;
  Node to;
  int weight;
  boolean horizontal;

  Edge(Node from, Node to, int weight, boolean horizontal) {
    this.from = from;
    this.to = to;
    this.weight = weight;
    this.horizontal = horizontal;
  }

  // checks if the to and from of this edge are already connected to each other
  // given the representatives of connections
  boolean bothAlreadyConnected(HashMap<Node, Node> representatives) {
    return this.from.isConnected(this.to, representatives)
        || this.to.isConnected(this.from, representatives);
  }

  // draws a connection between the nodes. Incorportates this node in the maze
  // Effect: the nodes are reflected as being connected in the representatives
  // hashMap
  void connect(HashMap<Node, Node> representatives) {
    this.from.connect(this.to, representatives);
  }

  // renders an edge
  void displayOnCanvas(WorldScene c, int squaresize) {
    int midx = this.from.nodeMidX(this.to);
    int midy = this.from.nodeMidY(this.to);
    if (this.horizontal) {
      c.placeImageXY(new LineImage(new Posn(0, squaresize), Color.black), midx, midy);
    }
    else {
      c.placeImageXY(new LineImage(new Posn(squaresize, 0), Color.black), midx, midy);
    }
  }

  // determines which object has a smaller weight
  public boolean hasSmallerWeightThan(Edge that) {
    return this.weight <= that.weight;
  }

  // determines if this edge connects the given node
  public boolean isConnectingNode(Node n1) {
    return this.to.equals(n1) || this.from.equals(n1);
  }

  // gets the connected node for the given node
  public Node getConnectingNode(Node n1) {
    if (n1.equals(this.from)) {
      return this.to;
    }
    else if (n1.equals(this.to)) {
      return this.from;
    }
    else {
      throw new IllegalArgumentException("This edge does not connect the given node");
    }
  }
}

//represents the maze
class MazeWorld extends World {
  HashMap<Node, Node> representatives = new HashMap<Node, Node>();
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> copyofedgesInTree;
  int squaresize = 20;
  Random rand = new Random();
  ArrayList<Edge> worklist;
  ArrayList<Node> allnodes;
  ArrayList<Edge> edgesNotInTree;
  int maxx;
  int maxy;
  int randSeed;
  ArrayList<Node> visited = new ArrayList<Node>();
  Node current;
  Node topleft;
  Node destination;
  boolean bfs = false;
  boolean dfs = false;
  Deque<Node> solvinglist = new ArrayDeque<Node>();
  boolean found = false;
  ArrayList<Node> solution = new ArrayList<Node>();
  boolean currentlysolving = false;
  int numberofxnodes;
  int numberofynodes;
  boolean mazemade = false;

  MazeWorld(int numberofxnodes, int numberofynodes) {
    this.initNodesandEdges(numberofxnodes, numberofynodes);
    this.initMaze();
    this.initSolution();
    this.numberofxnodes = numberofxnodes;
    this.numberofynodes = numberofynodes;
  }

  // additional constructor to test random given a seed
  MazeWorld(int numberofxnodes, int numberofynodes, int randSeed) {
    this.initNodesandEdges(numberofxnodes, numberofynodes);
    this.initMaze();
    this.initSolution();
    this.randSeed = randSeed;
    this.rand = new Random(randSeed);
  }

  // initializes nodes and edges
  void initNodesandEdges(int numberofxnodes, int numberofynodes) {
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<Edge> edges = new ArrayList<Edge>();

    int buffer = 10;
    for (int y = 0; y < numberofynodes; y = y + 1) {
      for (int x = 0; x < numberofxnodes; x = x + 1) {
        Node curr = new Node((x * this.squaresize) + buffer, (y * this.squaresize) + buffer);

        if (x != 0) {
          Node prev = nodes.get(x + (y * numberofxnodes) - 1);
          Edge connectingedge = new Edge(curr, prev, this.rand.nextInt(), true);
          edges.add(connectingedge);
        }
        if (y != 0) {
          Node above = nodes.get(x + (y * numberofxnodes) - numberofxnodes);
          Edge verticaledge = new Edge(curr, above, this.rand.nextInt(), false);
          edges.add(verticaledge);
        }
        nodes.add(curr);
        this.representatives.put(curr, curr);
        if (x == 0 && y == 0) {
          this.topleft = curr;
        }
        if (x == (numberofxnodes - 1) && y == (numberofynodes - 1)) {
          this.destination = curr;
        }

      }
    }
    this.worklist = edges;
    this.sortWorklist();
    this.allnodes = nodes;
  }

  // Extra Credit
  // initializes nodes and edges and is biased towards adding more horizontal
  // corridors
  void initNodesandEdgesMoreHorizontal(int numberofxnodes, int numberofynodes) {
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<Edge> edges = new ArrayList<Edge>();

    int buffer = 10;
    for (int y = 0; y < numberofynodes; y = y + 1) {
      for (int x = 0; x < numberofxnodes; x = x + 1) {
        Node curr = new Node((x * this.squaresize) + buffer, (y * this.squaresize) + buffer);

        if (x != 0) {
          Node prev = nodes.get(x + (y * numberofxnodes) - 1);
          Edge connectingedge = new Edge(curr, prev, this.rand.nextInt(10), true);
          edges.add(connectingedge);
        }
        if (y != 0) {
          Node above = nodes.get(x + (y * numberofxnodes) - numberofxnodes);
          Edge verticaledge = new Edge(curr, above, this.rand.nextInt(100000000), false);
          edges.add(verticaledge);
        }
        nodes.add(curr);
        this.representatives.put(curr, curr);
        if (x == 0 && y == 0) {
          this.topleft = curr;
        }
        if (x == (numberofxnodes - 1) && y == (numberofynodes - 1)) {
          this.destination = curr;
        }

      }
    }
    this.worklist = edges;
    this.sortWorklist();
    this.allnodes = nodes;
  }

  // Extra Credit
  // initializes nodes and edges and is biased towards adding more vertical
  // corridors
  void initNodesandEdgesMoreVertical(int numberofxnodes, int numberofynodes) {
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<Edge> edges = new ArrayList<Edge>();

    int buffer = 10;
    for (int y = 0; y < numberofynodes; y = y + 1) {
      for (int x = 0; x < numberofxnodes; x = x + 1) {
        Node curr = new Node((x * this.squaresize) + buffer, (y * this.squaresize) + buffer);

        if (x != 0) {
          Node prev = nodes.get(x + (y * numberofxnodes) - 1);
          Edge connectingedge = new Edge(curr, prev, this.rand.nextInt(100000000), true);
          edges.add(connectingedge);
        }
        if (y != 0) {
          Node above = nodes.get(x + (y * numberofxnodes) - numberofxnodes);
          Edge verticaledge = new Edge(curr, above, this.rand.nextInt(10), false);
          edges.add(verticaledge);
        }
        nodes.add(curr);
        this.representatives.put(curr, curr);
        if (x == 0 && y == 0) {
          this.topleft = curr;
        }
        if (x == (numberofxnodes - 1) && y == (numberofynodes - 1)) {
          this.destination = curr;
        }

      }
    }
    this.worklist = edges;
    this.sortWorklist();
    this.allnodes = nodes;
  }

  // mutates to change the world after one tick
  public void onTick() {
    if (!this.mazemade) {
      Edge first = this.copyofedgesInTree.remove(0);
      this.edgesNotInTree.remove(first);
    }
    if (this.copyofedgesInTree.isEmpty()) {
      this.mazemade = true;
    }
    if (this.bfs && !this.solvinglist.isEmpty() && !this.found) {
      Node currentnode = this.solvinglist.remove();
      if (currentnode.equals(this.destination)) {
        this.found = true;
      }
      else {
        this.visited.add(currentnode);
        this.addAllConnectingNodestoSolvingList(currentnode);
      }
    }
    if (this.dfs && !this.solvinglist.isEmpty() && !this.found) {
      Node currentnode = this.solvinglist.removeLast();
      if (currentnode.equals(this.destination)) {
        this.found = true;
      }
      else {
        this.visited.add(currentnode);
        this.addAllConnectingNodestoSolvingList(currentnode);
      }
    }
  }

  // finds the solution to the maze
  void initSolution() {
    ArrayList<Node> alreadychecked = new ArrayList<Node>();
    ArrayList<Node> solution = new ArrayList<Node>();
    Node curr = this.topleft;
    while (!curr.equals(this.destination)) {
      if (!alreadychecked.contains(curr)) {
        alreadychecked.add(curr);

        solution.add(curr);

        for (Node n : curr.listofDirectConnections(edgesInTree)) {

          if (n.contains(destination, alreadychecked, edgesInTree)) {
            curr = n;
          }
        }
      }
    }
    this.solution = solution;
  }

  // adds nodes connected to this to solving list
  void addAllConnectingNodestoSolvingList(Node n) {
    for (int i = 0; i < this.edgesInTree.size(); i++) {
      Edge currentedge = this.edgesInTree.get(i);
      if (currentedge.isConnectingNode(n)) {
        this.solvinglist.add(currentedge.getConnectingNode(n));
        this.edgesInTree.remove(i);
        i = i - 1;
      }
    }
  }

  // initializes the maze
  void initMaze() {
    ArrayList<Edge> finaledges = new ArrayList<Edge>();
    ArrayList<Edge> alledges = new ArrayList<Edge>(this.worklist);
    ArrayList<Edge> mockedges = new ArrayList<Edge>();
    // !this.checkIfOnly1Tree() && !alledges.isEmpty()
    while ((finaledges.size() != (this.allnodes.size() - 1)) && !alledges.isEmpty()) {
      Edge currsmallest = alledges.remove(0);
      if (currsmallest.bothAlreadyConnected(representatives)) {
        mockedges.add(currsmallest);
      }
      else {
        currsmallest.connect(representatives);
        finaledges.add(currsmallest);
      }
    }
    alledges.addAll(mockedges);
    this.edgesNotInTree = new ArrayList<Edge>(this.worklist);
    this.edgesInTree = finaledges;
    this.copyofedgesInTree = new ArrayList<Edge>(finaledges);
  }

  // sorts the list
  void sortWorklist() {
    for (int i = 0; i < this.worklist.size(); i = i + 1) {
      int minindex = i;
      Edge minweightedge = this.worklist.get(i);
      for (int j = i + 1; j < this.worklist.size(); j = j + 1) {
        if (this.worklist.get(j).hasSmallerWeightThan(minweightedge)) {
          minweightedge = this.worklist.get(j);
          minindex = j;
        }
      }
      Edge temp = this.worklist.get(i);
      this.worklist.set(i, this.worklist.get(minindex));
      this.worklist.set(minindex, temp);
    }
  }

  // restarts the game if "r" key is pressed, shows a breadth first search when
  // "b" key is
  // pressed, shows a depth first search when "d" key is pressed
  public void onKeyEvent(String key) {
    if (!this.currentlysolving && this.mazemade && (key.equals("b") || key.equals("B"))) {

      this.solvinglist.add(this.topleft);
      this.bfs = true;
      this.currentlysolving = true;
    }
    if (!this.currentlysolving && this.mazemade && (key.equals("d") || key.equals("D"))) {
      this.solvinglist.add(this.topleft);
      this.dfs = true;
      this.currentlysolving = true;
    }
    if (key.equals("r") || key.equals("R")) {
      this.initNodesandEdges(numberofxnodes, numberofynodes);
      this.initMaze();
      this.initSolution();
      representatives = new HashMap<Node, Node>();
      Random rand1 = new Random();
      rand = new Random(rand1.nextInt());
    }
  }

  // renders the world scene
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(2000, 1600);
    this.destination.displaycolor(Color.GREEN, canvas, squaresize);
    this.topleft.displaycolor(Color.MAGENTA, canvas, squaresize);
    for (Node n : this.visited) {
      n.displaycolor(Color.CYAN, canvas, squaresize);
    }
    if (this.found) {
      for (Node n : this.solution) {
        n.displaycolor(Color.GREEN, canvas, squaresize);
      }
      canvas.placeImageXY(new TextImage("Wrong Moves:" + (this.visited.size() - 
          this.solution.size()), 18, Color.BLACK), 700, 500);
    }
    for (Edge e : this.edgesNotInTree) {
      e.displayOnCanvas(canvas, squaresize);
    }
    Node maxNode = this.allnodes.get(this.allnodes.size() - 1);
    for (Node n : this.allnodes) {
      n.displayborderEdge(canvas, maxNode, squaresize, squaresize / 2);
    }
    if (!this.mazemade) {
      canvas.placeImageXY(new TextImage("Please wait maze is building up", 18, Color.BLACK), 700,
          600);
    }
    if (this.mazemade) {
      canvas.placeImageXY(new TextImage("Press D for DFS and B for BFS", 18, Color.BLACK), 700,
          600);
      canvas.placeImageXY(new TextImage("Press R for Reset", 18, Color.BLACK), 700, 700);
    }
    return canvas;
  }
}

// examples and tests for the classes Node, Edge, and MazeWorld
class ExamplesMaze {
  MazeWorld mw1;
  MazeWorld mw2;
  MazeWorld mw3;
  WorldCanvas canvas;
  WorldCanvas canvasCopy;
  WorldScene scene;
  WorldScene sceneCopy;

  Node randomN;
  Node n1;
  Node n2;
  Node n3;
  Node n4;
  Node mwn1;
  Node mwn2;
  Node mwn3;
  Node mwn4;
  Edge mwe1;
  Edge mwe2;
  Edge mwe3;
  Edge mwe4;

  Edge e1;
  Edge e2;
  HashMap<Node, Node> nodes;
  ArrayList<Node> allnodes;
  ArrayList<Node> allmwnodes;
  ArrayList<Edge> edges;
  ArrayList<Edge> mwedges;
  ArrayList<Edge> unsortedmwedges;
  Deque<Node> solvinglist;

  // initial conditions for examples
  void init() {
    mw1 = new MazeWorld(20, 20);
    mw2 = new MazeWorld(2, 2, 6);
    mw3 = new MazeWorld(2, 3, 90);
    scene = new WorldScene(2000, 1600);
    sceneCopy = new WorldScene(2000, 1600);

    randomN = new Node(10, 20, 5);
    n1 = new Node(10, 20);
    n2 = new Node(20, 20);
    n3 = new Node(20, 10);
    n4 = new Node(20, 30);
    mwn1 = new Node(10, 10);
    mwn2 = new Node(30, 10);
    mwn3 = new Node(10, 30);
    mwn4 = new Node(30, 30);
    mwe1 = new Edge(mwn3, mwn1, -2002862101, true);
    mwe2 = new Edge(mwn4, mwn2, 1028181886, false);
    mwe3 = new Edge(mwn4, mwn3, 1476812721, true);
    mwe4 = new Edge(mwn2, mwn1, 2141236506, true);

    e1 = new Edge(n1, n2, 10, true);
    e2 = new Edge(n2, n3, 15, false);
    nodes = new HashMap<Node, Node>();
    allnodes = new ArrayList<Node>();
    allmwnodes = new ArrayList<Node>(Arrays.asList(mwn1, mwn2, mwn3, mwn4));
    edges = new ArrayList<Edge>(Arrays.asList(e1, e2));
    mwedges = new ArrayList<Edge>(Arrays.asList(mwe1, mwe2, mwe3, mwe4));
    unsortedmwedges = new ArrayList<Edge>(Arrays.asList(mwe3, mwe2, mwe4, mwe1));
    solvinglist = new ArrayDeque<Node>();

    nodes.put(n1, n2);
    nodes.put(n2, n2);
    nodes.put(n3, n3);
    nodes.put(n4, n4);
    allnodes.add(n1);
    allnodes.add(n2);
    allnodes.add(n3);
    allnodes.add(n4);
  }

  // starts the game
  void testMaze(Tester t) {
    init();
    mw1.bigBang(2000, 1600, 0.001);
  }

  // tests the method contains
  void testContains(Tester t) {
    init();
    t.checkExpect(n2.contains(n4, new ArrayList<Node>(Arrays.asList(n1, n2)), edges), false);
    t.checkExpect(n4.contains(n4, new ArrayList<Node>(Arrays.asList(n1, n2)), edges), true);
    t.checkExpect(n2.contains(n4, new ArrayList<Node>(), edges), false);
    t.checkExpect(n4.contains(n4, new ArrayList<Node>(), edges), true);
  }

  // tests the method displayColor
  void testDisplayColor(Tester t) {
    init();
    t.checkExpect(scene, sceneCopy);
    n1.displaycolor(Color.BLUE, scene, 20);
    sceneCopy.placeImageXY(new RectangleImage(20, 20, "solid", Color.BLUE), 10, 20);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    n4.displaycolor(Color.PINK, scene, 20);
    sceneCopy.placeImageXY(new RectangleImage(20, 20, "solid", Color.PINK), 20, 30);
    t.checkExpect(scene, sceneCopy);
  }

  // tests the method hashCode
  void testHashCode(Tester t) {
    init();
    t.checkExpect(randomN.hashCode(), 3);
    t.checkExpect(new Node(5, 25, 100).hashCode(), 177);
  }

  // tests the method equals
  void testEquals(Tester t) {
    init();
    t.checkExpect(n1.equals(n2), false);
    t.checkExpect(n1.equals(e1), false);
    t.checkExpect(n1.equals(n1), true);
    t.checkExpect(n3.equals("Hello"), false);
  }

  // tests the method displayborderEdge
  void testDisplayBorderEdge(Tester t) {
    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n1.x, 10);
    t.checkExpect(n1.y, 20);
    n1.displayborderEdge(scene, n4, 20, 10);
    sceneCopy.placeImageXY(new LineImage(new Posn(0, 20), Color.BLACK), 0, 20);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n4.x, 20);
    t.checkExpect(n4.y, 30);
    n4.displayborderEdge(scene, n4, 20, 10);
    sceneCopy.placeImageXY(new LineImage(new Posn(0, 20), Color.BLACK), 30, 30);
    sceneCopy.placeImageXY(new LineImage(new Posn(20, 0), Color.BLACK), 20, 40);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n2.x, 20);
    t.checkExpect(n2.y, 20);
    n2.displayborderEdge(scene, n4, 20, 10);
    sceneCopy.placeImageXY(new LineImage(new Posn(0, 20), Color.BLACK), 30, 20);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n3.x, 20);
    t.checkExpect(n3.y, 10);
    n3.displayborderEdge(scene, n4, 20, 5);
    sceneCopy.placeImageXY(new LineImage(new Posn(0, 20), Color.BLACK), 30, 10);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n3.x, 20);
    t.checkExpect(n3.y, 10);
    n3.displayborderEdge(scene, new Node(10, 50), 20, 10);
    sceneCopy.placeImageXY(new LineImage(new Posn(20, 0), Color.BLACK), 20, 0);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n3.x, 20);
    t.checkExpect(n3.y, 10);
    n3.displayborderEdge(scene, n4, 20, 10);
    sceneCopy.placeImageXY(new LineImage(new Posn(0, 20), Color.BLACK), 30, 10);
    sceneCopy.placeImageXY(new LineImage(new Posn(20, 0), Color.BLACK), 20, 0);
    t.checkExpect(scene, sceneCopy);
  }

  // tests the method isConnected
  void testIsConnected(Tester t) {
    init();
    t.checkExpect(n1.isConnected(n2, nodes), true);
    t.checkExpect(n2.isConnected(n1, nodes), true);
    t.checkExpect(n1.isConnected(n1, nodes), true);
    t.checkExpect(n1.isConnected(n4, nodes), false);
    t.checkExpect(n2.isConnected(n3, nodes), false);
  }

  // tests the method connect for a node
  void testConnect(Tester t) {
    init();
    t.checkExpect(n1.isConnected(n2, nodes), true);
    n1.connect(n2, nodes);
    t.checkExpect(n1.isConnected(n2, nodes), true);

    t.checkExpect(n1.isConnected(n3, nodes), false);
    n1.connect(n3, nodes);
    t.checkExpect(n1.isConnected(n3, nodes), true);

    t.checkExpect(n2.isConnected(n4, nodes), false);
    n2.connect(n4, nodes);
    t.checkExpect(n2.isConnected(n4, nodes), true);
  }

  // tests the method pointAsPosn
  void testPointAsPosn(Tester t) {
    init();
    t.checkExpect(n1.pointAsPosn(), new Posn(10, 20));
    t.checkExpect(n4.pointAsPosn(), new Posn(20, 30));
  }

  // tests the method nodeMidX
  void testNodeMidX(Tester t) {
    init();
    t.checkExpect(n1.nodeMidX(n1), 10);
    t.checkExpect(n1.nodeMidX(n2), 15);
    t.checkExpect(n1.nodeMidX(n4), 15);
  }

  // tests the method nodeMidY
  void testNodeMidY(Tester t) {
    init();
    t.checkExpect(n1.nodeMidY(n1), 20);
    t.checkExpect(n1.nodeMidX(n2), 15);
    t.checkExpect(n1.nodeMidX(n4), 15);
  }

  // tests the method bothAlreadyConnected
  void testBothAlreadyConnected(Tester t) {
    init();
    t.checkExpect(e1.bothAlreadyConnected(nodes), true);
    t.checkExpect(e2.bothAlreadyConnected(nodes), false);
  }

  // tests the method connect for an edge
  void testConnectEdge(Tester t) {
    init();
    t.checkExpect(e1.from, n1);
    t.checkExpect(e1.to, n2);
    t.checkExpect(n1.isConnected(n2, nodes), true);
    e1.connect(nodes);
    t.checkExpect(n1.isConnected(n2, nodes), true);

    t.checkExpect(e2.from, n2);
    t.checkExpect(e2.to, n3);
    t.checkExpect(n2.isConnected(n3, nodes), false);
    e2.connect(nodes);
    t.checkExpect(n2.isConnected(n3, nodes), true);
  }

  // tests the method displayOnCanvas
  void testDisplayOnCanvas(Tester t) {
    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n1.nodeMidX(n2), 15);
    t.checkExpect(n1.nodeMidY(n2), 20);
    e1.displayOnCanvas(scene, 20);
    sceneCopy.placeImageXY(new LineImage(new Posn(0, 20), Color.black), 15, 20);
    t.checkExpect(scene, sceneCopy);

    init();
    t.checkExpect(scene, sceneCopy);
    t.checkExpect(n2.nodeMidX(n3), 20);
    t.checkExpect(n2.nodeMidY(n3), 15);
    e2.displayOnCanvas(scene, 20);
    sceneCopy.placeImageXY(new LineImage(new Posn(20, 0), Color.black), 20, 15);
    t.checkExpect(scene, sceneCopy);
  }

  // tests the method listofDirectConnections
  void testListOfDirectConnections(Tester t) {
    init();
    t.checkExpect(n1.listofDirectConnections(edges), new ArrayList<Node>(Arrays.asList(n2)));
    t.checkExpect(n2.listofDirectConnections(edges), new ArrayList<Node>(Arrays.asList(n1, n3)));
    t.checkExpect(n3.listofDirectConnections(edges), new ArrayList<Node>(Arrays.asList(n2)));
    t.checkExpect(n4.listofDirectConnections(edges), new ArrayList<Node>());
  }

  // tests the method isConnectingNode
  void testIsConnectingNode(Tester t) {
    init();
    t.checkExpect(e1.isConnectingNode(n2), true);
    t.checkExpect(e1.isConnectingNode(n1), true);
    t.checkExpect(e1.isConnectingNode(n3), false);
    t.checkExpect(e2.isConnectingNode(n1), false);
    t.checkExpect(e2.isConnectingNode(n2), true);
    t.checkExpect(e2.isConnectingNode(n3), true);
  }

  // tests the method getConnectingNode
  void testGetConnectingNode(Tester t) {
    init();
    t.checkExpect(e1.getConnectingNode(n1), n2);
    t.checkExpect(e1.getConnectingNode(n2), n1);
    t.checkException(new IllegalArgumentException("This edge does not connect the given node"), e1,
        "getConnectingNode", n4);
  }

  // tests the method hasSmallerWeightThan
  void testHasSmallerWeightThan(Tester t) {
    init();
    t.checkExpect(e1.hasSmallerWeightThan(e2), true);
    t.checkExpect(e2.hasSmallerWeightThan(e1), false);
    t.checkExpect(e1.hasSmallerWeightThan(e1), true);
  }

  // tests the method initNodesandEdges
  void testInitNodesAndEdges(Tester t) {
    init();
    t.checkExpect(mw2.allnodes, allmwnodes);
    mw2.initNodesandEdges(2, 2);
    t.checkExpect(mw2.allnodes.size(), 4);
    mw3.initNodesandEdges(2, 3);
    t.checkExpect(mw3.allnodes.size(), 6);
  }

  // tests the method initNodesandEdgesMoreVertical
  void testInitNodesandEdgesMoreVertical(Tester t) {
    init();
    t.checkExpect(mw2.allnodes, allmwnodes);
    mw2.initNodesandEdges(2, 2);
    t.checkExpect(mw2.allnodes.size(), 4);
    mw3.initNodesandEdges(2, 3);
    t.checkExpect(mw3.allnodes.size(), 6);
  }

  // tests the method initNodesandEdgesMoreHorizontal
  void testInitNodesandEdgesMoreHorizontal(Tester t) {
    init();
    t.checkExpect(mw2.allnodes, allmwnodes);
    mw2.initNodesandEdges(2, 2);
    t.checkExpect(mw2.allnodes.size(), 4);
    mw3.initNodesandEdges(2, 3);
    t.checkExpect(mw3.allnodes.size(), 6);
  }

  // tests the method initMaze
  void testInitMaze(Tester t) {
    init();
    mw2.allnodes = allnodes;
    t.checkExpect(mw2.allnodes, allnodes);
    t.checkExpect(mw2.edgesInTree.size(), 3);
    t.checkExpect(mw2.edgesNotInTree.size(), 4);
    mw2.initMaze();
    t.checkExpect(mw2.edgesInTree.size(), 0);
    t.checkExpect(mw2.edgesNotInTree.size(), 4);
  }

  // tests the method sortworklist
  void testSortWorkList(Tester t) {
    init();
    mw2.worklist = mwedges;
    mw2.sortWorklist();
    t.checkExpect(mw2.worklist, mwedges);

    mw2.worklist = unsortedmwedges;
    mw2.sortWorklist();
    t.checkExpect(mw2.worklist, mwedges);

    mw3.worklist = mwedges;
    mw3.sortWorklist();
    t.checkExpect(mw2.worklist, mwedges);

    mw3.worklist = unsortedmwedges;
    mw3.sortWorklist();
    t.checkExpect(mw3.worklist, mwedges);
  }

  // tests the method onTick
  void testOnTick(Tester t) {
    init();
    t.checkExpect(mw2.mazemade, false);
    t.checkExpect(mw2.bfs, false);
    t.checkExpect(mw2.dfs, false);
    mw2.edgesNotInTree = edges;
    mw2.copyofedgesInTree = edges;
    mw2.onTick();
    t.checkExpect(mw2.edgesNotInTree, new ArrayList<Edge>(Arrays.asList(e2)));

    init();
    t.checkExpect(mw2.mazemade, false);
    mw2.copyofedgesInTree = new ArrayList<Edge>(Arrays.asList(e1));
    mw2.onTick();
    t.checkExpect(mw2.mazemade, true);

    init();
    t.checkExpect(mw2.bfs, false);
    mw2.bfs = true;
    mw2.solvinglist = new ArrayDeque<Node>(Arrays.asList(n1, n2, n3));
    mw2.found = false;
    mw2.destination = n1;
    mw2.onTick();
    t.checkExpect(mw2.found, true);
    mw2.destination = n4;
    mw2.onTick();
    t.checkExpect(mw2.visited, new ArrayList<Node>());

    init();
    mw2.dfs = true;
    mw2.solvinglist = new ArrayDeque<Node>(Arrays.asList(n1, n2));
    mw2.found = false;
    mw2.destination = n2;
    mw2.onTick();
    t.checkExpect(mw2.found, true);
    mw2.destination = n4;
    mw2.onTick();
    t.checkExpect(mw2.visited, new ArrayList<Node>());
  }

  // tests the method initSolution
  void testInitSolution(Tester t) {
    init();
    mw2.topleft = n1;
    mw2.destination = n1;
    mw2.initSolution();
    t.checkExpect(mw2.solution, new ArrayList<Node>());

    mw2.destination = n2;
    mw2.initSolution();
    t.checkExpect(mw2.solution, new ArrayList<Node>(Arrays.asList(n1)));
  }

  // tests the method addAllConnectingNodestoSolvingList
  void testAddAllConnectingNodesToSolvingList(Tester t) {
    init();
    mw2.solvinglist = solvinglist;
    t.checkExpect(mw2.solvinglist, new ArrayDeque<Node>());
    mw2.addAllConnectingNodestoSolvingList(n1);
    t.checkExpect(mw2.solvinglist, new ArrayDeque<Node>(Arrays.asList(n2)));
  }

  // tests the method onKeyEvent
  void testOnKey(Tester t) {
    init();
    mw2.currentlysolving = false;
    mw2.topleft = n1;
    mw2.solvinglist = solvinglist;
    mw2.mazemade = true;
    mw2.onKeyEvent("b");
    t.checkExpect(mw2.solvinglist, new ArrayDeque<Node>(Arrays.asList(n1)));
    t.checkExpect(mw2.bfs, true);
    t.checkExpect(mw2.currentlysolving, true);

    init();
    mw2.currentlysolving = false;
    mw2.topleft = n1;
    mw2.solvinglist = solvinglist;
    mw2.mazemade = true;
    mw2.onKeyEvent("B");
    t.checkExpect(mw2.solvinglist, new ArrayDeque<Node>(Arrays.asList(n1)));
    t.checkExpect(mw2.bfs, true);
    t.checkExpect(mw2.currentlysolving, true);

    init();
    mw2.currentlysolving = false;
    mw2.topleft = n1;
    mw2.solvinglist = solvinglist;
    mw2.mazemade = true;
    mw2.onKeyEvent("d");
    t.checkExpect(mw2.solvinglist, new ArrayDeque<Node>(Arrays.asList(n1)));
    t.checkExpect(mw2.dfs, true);
    t.checkExpect(mw2.currentlysolving, true);

    init();
    mw2.currentlysolving = false;
    mw2.topleft = n1;
    mw2.solvinglist = solvinglist;
    mw2.mazemade = true;
    mw2.onKeyEvent("D");
    t.checkExpect(mw2.solvinglist, new ArrayDeque<Node>(Arrays.asList(n1)));
    t.checkExpect(mw2.dfs, true);
    t.checkExpect(mw2.currentlysolving, true);
  }

  // tests the method makeScene
  void testMakeScene(Tester t) {
    init();
    t.checkExpect(scene, sceneCopy);
    mw2.found = true;
    mw2.topleft = n1;
    mw2.destination = n3;
    mw2.mazemade = true;
    scene = mw2.makeScene();
    n3.displaycolor(Color.GREEN, sceneCopy, 20);
    n1.displaycolor(Color.MAGENTA, sceneCopy, 20);
    n2.displaycolor(Color.GREEN, sceneCopy, 20);
    n1.displayborderEdge(sceneCopy, n4, 20, 10);
    sceneCopy.placeImageXY(new TextImage("Wrong Moves:" + 0, 18, Color.BLACK), 700, 500);
    sceneCopy.placeImageXY(new TextImage("Press D for DFS and B for BFS", 18, Color.BLACK), 700,
        600);
    sceneCopy.placeImageXY(new TextImage("Press R for Reset", 18, Color.BLACK), 700, 700);
    t.checkExpect(mw2.makeScene(), scene);

    init();
    mw2.found = false;
    mw2.mazemade = false;
    mw2.visited = new ArrayList<Node>(Arrays.asList(n1, n2));
    mw2.topleft = n1;
    mw2.destination = n4;
    scene = mw2.makeScene();
    sceneCopy.placeImageXY(new TextImage("Wrong Moves:" + 0, 18, Color.BLACK), 700, 500);
    sceneCopy.placeImageXY(new TextImage("Press D for DFS and B for BFS", 18, Color.BLACK), 700,
        600);
    sceneCopy.placeImageXY(new TextImage("Press R for Reset", 18, Color.BLACK), 700, 700);
    n1.displaycolor(Color.MAGENTA, sceneCopy, 20);
    n4.displaycolor(Color.GREEN, sceneCopy, 20);
    n3.displaycolor(Color.GREEN, sceneCopy, 20);
    n1.displaycolor(Color.MAGENTA, sceneCopy, 20);
    n2.displaycolor(Color.GREEN, sceneCopy, 20);
    n1.displayborderEdge(sceneCopy, n4, 20, 10);
    t.checkExpect(mw2.makeScene(), scene);
  }
}
