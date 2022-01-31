public class Node {
  State state;
  Node parent;
  float f;
  float g;
  float h;

  Node(String id, Node p, float f, float g) {
    this.state = id;
    this.parent = p;
    this.f = f;
    this.g = g;
    this.h = f + g;
  }
}
