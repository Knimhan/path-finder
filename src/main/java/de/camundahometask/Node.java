package de.camundahometask;

public class Node {
    private String name;

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if( ! (obj instanceof Node) ) return false;
        Node node = (Node) obj;
        return node.name.equals(this.name);
    }
}
