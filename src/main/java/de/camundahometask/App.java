package de.camundahometask;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.FlowNodeRef;
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

    public static void main(String[] args) {
        if (StringUtils.isEmpty(args[0]) || StringUtils.isEmpty(args[1])) {
            System.exit(-1);
        }
        try {
            //fetch invoice xml
            HttpURLConnection httpURLConnection = fetchInvoiceXml();
            InputStream xmlStream = httpURLConnection.getInputStream();
            httpURLConnection.disconnect();

            //parse xml
            BpmnModelInstance modelInstance = parse(xmlStream);
            ArrayList<FlowNodeRef> flowNodeRefCollection = (ArrayList<FlowNodeRef>)
                    modelInstance.getModelElementsByType(FlowNodeRef.class);

            //map to traversable data structure graph
            Diagram diagram = new Diagram();
            for (Node node : map(flowNodeRefCollection)) {
                diagram.addNode(node);
            }
            addEdges(diagram);

            //find and print path from start to end node
            findPathBreadthFirst(diagram, args[0], args[1]); //TODO: which one to use? depth first or breadth first

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

    private static void addEdges(Diagram diagram) {
        //TODO: add edges --> this is static not derived from this bpmn model instance
        diagram.addEdge("approveInvoice", "invoice_approved");
        diagram.addEdge("invoice_approved", "prepareBankTransfer");
        diagram.addEdge("invoice_approved", "reviewInvoice");
        diagram.addEdge("reviewSuccessful_gw", "invoiceNotProcessed");
        diagram.addEdge("reviewSuccessful_gw", "approveInvoice");
        diagram.addEdge("assignApprover", "approveInvoice");
        diagram.addEdge("StartEvent_1", "assignApprover");
        diagram.addEdge("reviewInvoice", "reviewSuccessful_gw");
        diagram.addEdge("prepareBankTransfer", "ServiceTask_1");
        diagram.addEdge("ServiceTask_1", "invoiceProcessed");
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
                for (Node print : visited) {
                    System.out.println(print.getName() + " ");
                }
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


    private static void findPathBreadthFirst(Diagram diagram, String start, String end) {
        Set<Node> visited = new LinkedHashSet<>();
        Queue<Node> queue = new LinkedList<>();
        Node endNode = new Node(end);
        queue.add(new Node(start));

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (node.equals(endNode)) {
                visited.add(node);
                for (Node print : visited) {
                    System.out.println(print.getName() + " ");
                }
                System.exit(0);
            }
            if (!visited.contains(node)) {
                visited.add(node);
                for (Node adjacentNode : diagram.getAdjacentNodes(node)) {
                    queue.add(adjacentNode);
                }
            }
        }
        //path not found
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

        File targetFile = new File("src/main/resources/invoice.xml");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(xml.getBytes());
        return Bpmn.readModelFromFile(targetFile);
    }

    private static HttpURLConnection fetchInvoiceXml() throws IOException {
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
