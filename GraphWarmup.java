/*
 * London Bielicke
 * Artificial Intelligence
 * Graph Warmup: reads file describing locations and roads
  and allows user to see which other locations are
  connected to a specific locatiion
 * I have neither given nor received unauthorized aid on this program.
 */

import java.io.*;
import java.util.*;

// Location Class: holds and computes
// information for a specific location
class Location {
  String id;
  float longitude;
  float latitude;

  // constructor
  Location(String id, String la, String lo) {
    this.id = id;
    this.longitude = Float.parseFloat(lo);
    this.latitude = Float.parseFloat(la);
  }
/*
  * distance in miles method: computes distance
  between current point and another point in
  miles relative to earth's radius!
  * https://www.johndcook.com/blog/python_longitude_latitude/
*/
  float distanceinMiles(Location other) {
    float lat1 = this.latitude;
    float long1 = this.longitude;
    float lat2 = other.latitude;
    float long2 = other.longitude;

    // convert lat and long to spherical cordinates in radians.
    double degreesToRadians = Math.PI / 180.0;

    // phi = 90 - latitude
    double phi1 = (90.0 - lat1) * degreesToRadians;
    double phi2 = (90.0 - lat2) * degreesToRadians;

    // theta = longitude
    double theta1 = long1 * degreesToRadians;
    double theta2 = long2 * degreesToRadians;

    // compute spherical distance from spherical coordinates

    /*
    * For two locations in spherical coordinates
    * (1, theta, phi) and (1, theta', phi')
    * cosine( arc length ) =  sin phi sin phi' cos(theta-theta')
                              + cos phi cos phi'
    * distance = rho * arc length
    */

    double cos = Math.sin(phi1)*Math.sin(phi2)*Math.cos(theta1 - theta2) +
                Math.cos(phi1) * Math.cos(phi2);
    double arc = Math.acos(cos);

    // cast to float and  multiply by
    // 3960 to get distance in miles
    return (float) (arc * 3960);
  }

}

// Road class: holds information for a specific road
class Road {
  String id1;
  String id2;
  int speedLimit;
  String name;

  // constructor
  Road(String p1, String p2, String s, String n) {
    id1 = p1;
    id2 = p2;
    speedLimit = Integer.parseInt(s);
    name = n;
  }
}

// Graph class: connects locations/vertices
// by roads/edges using hashmaps
class Graph {
  Map<String, Location> locations; // key=location id, value=location datatype
  Map<String, List<Road>> roads; // key=location id, value=roads that connect to location

  // constructor
  Graph() {
    this.locations = new HashMap<String, Location>();
    this.roads = new HashMap<String, List<Road>>();
  }

  // add location method: add vertex to location map
  Location addLocation(Location l) {
    locations.put(l.id, l);
    return l;
  }

  /*
   * add road method: first, check if either
   * locations are already in graph and add
   * road to map value. Otherwise, add key
   * value pair to road map.
   */
  void addRoad(Road r) {
    // check and add first id
    addRoadHelper(r.id1, r);
    //check and add second id
    addRoadHelper(r.id2, r);
  }

  // this probably isn't very necessary
  // but I felt like including it!
  void addRoadHelper(String id, Road r) {
    if (roads.containsKey(id))
      roads.get(id).add(r);
    else {
      roads.put(id, new ArrayList<Road>());
      roads.get(id).add(r);
    }
  }

  /*
   * print location info: given a key, print
   * information about a location including the
   * connecting locations, roads that connect them,
   * speed limit of each road, and the time it will
   * take to get to each connecting location
   */
  void printLocationInfo(String id) {
    String connection;
    float distance;
    float time;

    if (roads.containsKey(id)) {
      System.out.println("Location " + id + " has roads leading to: ");

      // for each road, find connecting locations,
      // distance between locations, and time to get there.
      for (Road r : roads.get(id)) {
        connection = findConnection(r, id);
        distance = locations.get(id).distanceinMiles(locations.get(connection));
        time = (distance/r.speedLimit)*3600;

        System.out.println("Location " + connection + ", " + r.speedLimit + ", " +
                            r.name + ", " + time + " seconds");
      }
      System.out.println();
    }
    else { System.out.println("id " + id + " does not exist"); }
  }

  // find connection: if the given id is the first id,
  // then the connecting id is the second id and visa vera
  String findConnection(Road r, String id) {
    String connection;
    if (r.id1.equals(id)) {
      connection = r.id2;
    }
    else {
      connection = r.id1;
    }
    return connection;
  }


  // returns actual time to get between two locations
  float g(String id1, String id2, float speedLimit) {
    float distance = locations.get(id1).distanceinMiles(locations.get(id2));
    float time = (distance/speedLimit)*3600;
    return time;
  }

  // returns estimated time between two locations
  float h(String id1, String id2) {
    float distance = locations.get(id1).distanceinMiles(locations.get(id2));
    float time = (distance/65)*3600;
    return time;
  }

  // A* Algorithm:
  //  - takes two location id's and returns a node
  //     with pointers through path solution
  Node aStar(String id1, String id2, boolean debug) {
    PriQueue<Node, Float> frontier = new PriQueue<Node, Float>(); // stores nodes available to look at
    Map<String, Node> reached = new HashMap<String, Node>(); // stores nodes already looked at

    // gather information for init location
    float g = 0;
    float h = h(id1, id2);
    float f = g + h;
    Node n = new Node(id1, null, f, g, h);

    // push starting node to frontier
    frontier.add(n, f);

    // init next node var and connection id var
    Node next;
    String connection;

    int count = 1;
    float total_time = 0;
    // driving loop:
    // continue searching until examining every node
    // or until we have reached the goal state
    while (!frontier.isEmpty()) {
      count++;

      // visit node with lowest f-value
      n = frontier.remove();
      reached.put(n.state, n);
      if (debug)
        System.out.println("\nvisiting " + n.toString());

      // if curr node is goal node, end algortihm
      if (n.state.equals(id2)) {
        System.out.println("\nnodes visited: " + count);
        return n;
      }

      // for every road the connects to current location,
      // add every connecting location to the fronter
      for (Road r : roads.get(n.state)) {
        // generate next state node
        connection = findConnection(r, n.state);
        g = n.g + g(n.state, connection, r.speedLimit);
        h = h(connection, id2);
        f = g + h;
        next = new Node(connection, n, f, g, h);

        // if child node has not already been examined
        // or if child node f value is less than the one
        // already examined, then add node
        if (!reached.containsKey(next.state) || (g < reached.get(next.state).g)) {
          reached.put(next.state, next);
          frontier.add(next, f);
          if (debug)
            System.out.println("  adding: " + next.toString());
        }
        else {
          if (debug)
            System.out.println("  skipping: " + next.toString());
        }
      }
    }
    // only returns null if goal state not found
    return null;
  }

  // find road:
  //   - takes start node and end node and
  //     determine which road you take to get
  //     between the locations of the two nodes
  Road findRoad (Node end, Node start) {
      for (Road r : roads.get(start.state)) {
        String connection = findConnection(r, start.state);
        if (connection.equals(end.state)) {
          return r;
        }
      }
      return null;
  }

  // route info:
  //   - takes goal state node and prints travel information
  void routeInfo(Node n) {
    routeInfoHelper(n, 0);
  }

  void routeInfoHelper(Node n, float time) {
    if (n.parent == null) {
      System.out.println("total travel time in seconds: " + time);
      System.out.println("\nRoute found is:");
      System.out.println("\n" + n.state + " (starting location)");
      return;
    }
    Road curr_road = findRoad(n.parent, n);
    float t =  g(n.state, n.parent.state, curr_road.speedLimit);
    routeInfoHelper(n.parent, time + t);
    System.out.println(n.state + " (" + curr_road.name + ")");
  }

   // degreesToDirection:
   //    - takes degree between 0-360 and returns
   //        direction as a string
  String degreesToDirection(float bearing) {
    if ((bearing <= 22.5) || (bearing >= 337.5))
      return "north";
    if ((bearing <= 112.5) && (bearing >= 67.5))
      return "east";
    if ((bearing <= 202.5) && (bearing >= 157.5))
      return "south";
    if ((bearing <= 292.5) && (bearing >= 247.5))
      return "west";
    if ((bearing > 22.5) && (bearing < 67.5))
      return "northeast";
    if ((bearing > 112.5) && (bearing < 157.5))
      return "southeast";
    if ((bearing > 202.5) && (bearing < 247.5))
      return "southwest";
    if ((bearing > 292.5) && (bearing < 337.5))
      return "northwest";
    return "error";
  }

  // get bearing:
  //    - get bearing between 0-360 based on
  //       lattitude and longitude of two locations
  float getBearing(Node n1, Node n2) {
    // access locations
    Location loc1 = locations.get(n1.state);
    Location loc2 = locations.get(n2.state);

    // get coordinates
    float lat1 = loc1.latitude;
    float lat2 = loc2.latitude;
    float long1 = loc1.longitude;
    float long2 = loc2.longitude;

    // convert to radians
    lat1*=Math.PI/180;
    lat2*=Math.PI/180;
    long1*=Math.PI/180;
    long2*=Math.PI/180;

    // math!
    float y = (float) (Math.sin(long2-long1) * Math.cos(lat2));
    float x = (float) (Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(long2-long1));
    float angle = (float) Math.atan2(y, x);
    float bearing = (float)(angle * 180/Math.PI + 360f) % 360;
    return bearing;
  }

  // based on change in rotation determine which direction to turn
  String turnDirection(float b1, float b2) {
    if (((b2-b1<0) && (Math.abs(b2-b1) < 180)) || ((b2-b1>0) && (Math.abs(b2-b1) >= 180)))
      return "left";
    else if (((b2-b1<0) && (Math.abs(b2-b1) >= 180)) || ((b2-b1>0) && (Math.abs(b2-b1) < 180)))
      return "right";
    return "error";
  }

  String displaySeconds(float s) {
    String display = "(";
    int minutes = (int) (s/60);
    int seconds = (int) (s%60);
    if (minutes > 0)
    display += minutes + " minutes and ";
    return display + seconds + " seconds)";
  }


  void gpsDirections(Node n) {
    gpsDirectionsHelper(n, 0f, 0f);
  }

  // prints GPS directions from init to goal node
  void gpsDirectionsHelper(Node n, float m, float t) {

    if (n.parent == null) {
      System.out.println("you have arrived!");
      return;
    }

    Road prev_road;
    float prev_bearing;
    Road curr_road = findRoad(n.parent, n);
    float curr_bearing = getBearing(n.parent, n);
    String bearing = degreesToDirection(curr_bearing);
    float timeOnStreet = g(n.state, n.parent.state, curr_road.speedLimit);
    float miles = locations.get(n.state).distanceinMiles(locations.get(n.parent.state));

    if (n.parent.parent == null) {
      // print starting info
      System.out.println("\nGPS directions: \nhead " + bearing + " on " + curr_road.name);
      // print road info
      System.out.println("   drive for " + (m + miles) + " miles " + displaySeconds(t + timeOnStreet));
      return;
    }

    curr_road = findRoad(n, n.parent);
    prev_road = findRoad(n.parent, n.parent.parent);
    curr_bearing = getBearing(n.parent, n);
    prev_bearing = getBearing(n.parent.parent, n.parent);

    // whenever there is a change in roads, reset
    if (!prev_road.name.equals(curr_road.name)) {
      gpsDirectionsHelper(n.parent, 0f, 0f);
      // print turning info
      System.out.println("turn " + turnDirection(prev_bearing, curr_bearing) + " onto " + curr_road.name);
      // print road info
      System.out.println("   drive for " + (m + miles) + " miles " + displaySeconds(t + timeOnStreet));
    }

    else {

      gpsDirectionsHelper(n.parent, m + miles, t + timeOnStreet);
    }
  }

}

// Driver class
public class GraphWarmup {
  public static void main(String[] args)
  {
      // process file information
      Graph g = new Graph();
      Scanner scan = new Scanner(System.in);
      String filename = "memphis-medium.txt";
      processInformation(filename, g);

      String startID;
      String endID;
      String debug;
      Node solution = null;
      // loop for user to search id's.
      // terminates when user enters "0"
      System.out.print("Starting location ID: ");
      startID = scan.nextLine();
      System.out.print("Ending location ID: ");
      endID = scan.nextLine();
      System.out.print("Do you want debugging information (y/n)? ");
      debug = scan.nextLine();

      // flip end and start for direction printing purposes
      if (debug.equals("y"))
        solution = g.aStar(startID, endID, true);
      else if (debug.equals("n"))
        solution = g.aStar(startID, endID, false);

      // print route information
      g.routeInfo(solution);
      g.gpsDirections(solution);

      scan.close();

  }


  /**
   * Read the file specified to add edges and vertices to graph
   */
  private static void processInformation(String filename, Graph g)
  {
    try {
      File file = new File(filename);
      Scanner scan = new Scanner(file);

      // for each line, determine if given info
      // is for location or road.
      while (scan.hasNextLine()) {
        String line = scan.nextLine();
        String[] info = line.split("\\|");

        //create location instance and add to graph
        if (info[0].equals("location")) {
          Location l = new Location(info[1], info[2], info[3]);
          g.addLocation(l);
        }
        //create road instance and add to graph
        else if (info[0].equals("road")) {
          Road r = new Road(info[1], info[2], info[3], info[4]);
          g.addRoad(r);
        }
      }
      scan.close();
    }
    catch (Exception e) {
      System.out.println("file does not exist");
    }
  }
}
