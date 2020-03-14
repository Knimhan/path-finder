package de.camundahometask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Diagram {
    Map<Node, ArrayList<Node>> adjacentNodes;

    public Diagram() {
        adjacentNodes = new HashMap<>();
    }

    public Map<Node, ArrayList<Node>> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void addEdge(Node fromNode, Node toNode) {
        adjacentNodes.get(fromNode).add(toNode);
    }

    public void addNode(Node node) {
        adjacentNodes.putIfAbsent(node, new ArrayList<>());
    }


    public ArrayList<Node> getAdjacentNodes(Node node) {
        return adjacentNodes.getOrDefault(node, new ArrayList<>());
    }

}
