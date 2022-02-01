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

  float euclideanDist(Location other) {
    float lat1 = this.latitude;
    float long1 = this.longitude;
    float lat2 = other.latitude;
    float long2 = other.longitude;

    return (float) Math.sqrt(Math.pow(lat1-lat2, 2) + Math.pow(long1-long2, 2));
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
    else { System.out.println("id " + id + " doeas not exist"); }
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

  float g(String id1, String id2, float speedLimit) {
    float distance = locations.get(id1).distanceinMiles(locations.get(id2));
    float time = (distance/speedLimit)*3600;
    return time;
  }

  float h(String id1, String id2) {
    float distance = locations.get(id1).euclideanDist(locations.get(id2));
    float time = (distance/65)*3600;
    return time;
  }

  Node aStar(String id1, String id2) {
    PriQueue<Node, Float> frontier = new PriQueue<Node, Float>();
    Map<String, Node> reached = new HashMap<String, Node>();

    float h = h(id1, id2);
    float g = 0;
    Node n = new Node(id1, null, (g+h), g, h);
    frontier.add(n, (g+h));

    Node next;
    String connection;

    while (!frontier.isEmpty()) {

      n = frontier.remove();
      reached.put(n.state, n);
      if (n.state.equals(id2)) {
        System.out.println(n.state);
        return n;
      }


      for (Road r : roads.get(n.state)) {
        connection = findConnection(r, n.state);
        g = g(n.state, connection, r.speedLimit);
        h = h(connection, id2);
        next = new Node(connection, n, (n.g+g+h), (n.g + g), h);

        if (!reached.containsKey(next.state) || (n.g + g) < reached.get(next.state).g) {
          reached.put(next.state, next);
          frontier.add(next, (h+n.g+g));
        }
      }

    }
    return null;
  }

  Road findRoad (Node start, Node end) {
      for (Road r : roads.get(start.state)) {
        String connection = findConnection(r, start.state);
        if (connection.equals(end.state)) {
          return r;
        }
      }
      return null;
  }

  void routeInfo(Node n) {
    System.out.println(n.state + " (starting location)");
    int count = 1;
    float total_time = 0;
    Road curr_road;

    while (n.parent != null) {
      curr_road = findRoad(n, n.parent);
      total_time += g(n.state, n.parent.state, curr_road.speedLimit);
      System.out.println(n.state + " (" + curr_road.name + ")");
      n = n.parent;
    }

    System.out.println("total travel time in seconds: " + total_time);
    System.out.println("nodes visited: " + total_time);

  }

  void gpsDirections(Node n) {
    float timeOnStreet = 0;
    Road curr_road = findRoad(n, n.parent);
    Road next_road;

    while (n.parent != null) {
      curr_road = findRoad(n, n.parent);
      total_time += g(n.state, n.parent.state, curr_road.speedLimit);
      System.out.println(n.state + " (" + curr_road.name + ")");
      n = n.parent;
    }

    System.out.println("total travel time in seconds: " + total_time);
    System.out.println("nodes visited: " + total_time);

  }



}

// Driver class
public class GraphWarmup {
  public static void main(String[] args)
  {
      // process file information
      Graph g = new Graph();
      Scanner scan = new Scanner(System.in);
      System.out.print("What file do you want to read? ");
      String filename = scan.nextLine();
      processInformation(filename, g);

      String startID;
      String endID;
      // loop for user to search id's.
      // terminates when user enters "0"
      System.out.print("Starting location ID: ");
      startID = scan.nextLine();
      System.out.print("Ending location ID: ");
      endID = scan.nextLine();

      Node solution = g.aStar(endID, startID);

      g.routeInfo(solution);
      g.gpsDirections(solution);

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
