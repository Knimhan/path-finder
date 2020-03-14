package de.camundahometask;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.FlowNodeRef;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class App {

    public static final String target_url = "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";
    public static final String source_url = "src/main/resources/invoice.xml";

    public static void main(String[] args) {
        if (args.length == 0 || StringUtils.isEmpty(args[0]) || StringUtils.isEmpty(args[1])) {
            System.exit(-1);
        }
        try {
            //fetch invoice xml
            HttpURLConnection httpURLConnection = getConnection();
            InputStream xmlStream = httpURLConnection.getInputStream();

            //parse xml
            BpmnModelInstance modelInstance = parse(xmlStream);
            httpURLConnection.disconnect();

            //convert to traversable data structure
            Diagram diagram = map(modelInstance);

            //find and print path
            findPathDepthFirst(diagram, args[0], args[1]);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static Diagram map(BpmnModelInstance modelInstance) {
        Diagram diagram = new Diagram();
        Collection<SequenceFlow> sequenceFlows = modelInstance.getModelElementsByType(SequenceFlow.class);
        for (SequenceFlow se : sequenceFlows) {
            Node fromNode = new Node(se.getSource().getId());
            Node toNode = new Node(se.getTarget().getId());
            diagram.addNode(fromNode);
            diagram.addNode(toNode);
            diagram.addEdge(fromNode, toNode);
        }
        return diagram;
    }

    private static void findPathDepthFirst(Diagram diagram, String start, String end) {
        Set<Node> visited = new LinkedHashSet<>();
        Stack<Node> stack = new Stack<>();
        Node endNode = new Node(end);
        stack.push(new Node(start));
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.equals(endNode)) {
                visited.add(node);
                for (Node print : visited) System.out.println(print.getName());
                System.exit(0);
            }
            if (!visited.contains(node)) {
                visited.add(node);
                for (Node adjacentNode : diagram.getAdjacentNodes(node)) {
                    stack.push(adjacentNode);
                }
            }
        }
        System.exit(-1);
    }

    private static List<Node> map(ArrayList<FlowNodeRef> flowNodeRefArrayList) {
        List<Node> nodes = new ArrayList<Node>();
        for (FlowNodeRef flowNodeRef : flowNodeRefArrayList) {
            nodes.add(new Node(flowNodeRef.getDomElement().getTextContent()));
        }
        return nodes;
    }

    private static BpmnModelInstance parse(InputStream xmlStream) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(
                new BufferedReader(new InputStreamReader((xmlStream))));
        String xml = (String) jsonObject.get("bpmn20Xml");
        File targetFile = new File(source_url);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(xml.getBytes());
        outStream.close();
        return Bpmn.readModelFromFile(targetFile);
    }

    private static HttpURLConnection getConnection() throws IOException {
        URL url = new URL(target_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        if (httpURLConnection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpURLConnection.getResponseCode());
        }
        return httpURLConnection;
    }
}
