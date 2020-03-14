package de.camundahometask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Diagram {
    Map<Node, ArrayList<Node>> adjacentNodes;

    public Diagram() {
        adjacentNodes = new HashMap<>();
    }

    public Diagram(Map<Node, ArrayList<Node>> adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }

    public Map<Node, ArrayList<Node>> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void setAdjacentNodes(Map<Node, ArrayList<Node>> adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }

    public void addNode(String nodeLabel) {
        adjacentNodes.putIfAbsent(new Node(nodeLabel), new ArrayList<>());
    }

    public void addEdge(String fromNode, String toNode) {
        adjacentNodes.get(new Node(fromNode)).add(new Node(toNode)); //TODO:null checks make it optional
    }

    public void addNode(Node node) {
        adjacentNodes.putIfAbsent(node, new ArrayList<>());
    }

    public ArrayList<Node> getAdjacentNodes(Node node) {
        return adjacentNodes.getOrDefault(node, new ArrayList<>()) ;
    }

}
