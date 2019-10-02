package test;

public class Node {
    public int x, y;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x:" + this.x + ",y:" + this.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Node node = (Node) obj;
        return node.x == this.x && node.y == this.y;
    }

    @Override
    public int hashCode() {
        return this.x + (this.y * 10000);
    }
}
