public class Node {
  String state;
  Node parent;
  float f;
  float g;
  float h;

  Node(String id, Node p, float f, float g, float h) {
    this.state = id;
    this.parent = p;
    this.f = f;
    this.g = g;
    this.h = h;
  }

  public String toString() {
    String p;
    if (parent == null)
      p = "null";
    else
      p = parent.state;
    return "[State=" + state + ", parent=" + p + ", g=" + g + ", h=" + h + ", f=" + f + "]";
  }
}
