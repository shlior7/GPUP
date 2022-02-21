package utils;

import TargetGraph.Edge;
import TargetGraph.Target;
import TargetGraph.TargetGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import types.Task;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {
//    private Document document;
//    private Element targetsElement;

    public FileHandler() {

    }

    public TargetGraph loadGPUPXMLFile(InputStream file) throws Exception {
        Document document = createDocument(file);
        return extractTargetGraph(document);
    }

    public TargetGraph loadGPUPXMLFile(File file) throws Exception {
        Document document = createDocument(file);
        return extractTargetGraph(document);
    }

    private Document createDocument(File file) throws ParserConfigurationException, IOException, SAXException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();
        return document;
    }

    private Document createDocument(InputStream file) throws ParserConfigurationException, IOException, SAXException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();
        return document;
    }

    private TargetGraph extractTargetGraph(Document document) throws Exception {
        Map<String, Object> config = readConfigurations(document);
        Map<String, List> targets = readTargets(document);


        return new TargetGraph(String.valueOf(config.get("name")), targets.get("targets"), targets.get("edges"), (Map<Class<? extends Task>, Integer>) config.get("prices"));
    }


    private Map<String, Object> readConfigurations(Document document) throws Exception {
        NodeList configurations = document.getElementsByTagName("GPUP-Configuration");
        if (configurations.getLength() == 0 || configurations.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("no configurations");

        Element config = (Element) configurations.item(0);
        String GraphsName = validateTextContent(config, "GPUP-Graph-Name");
        List<Element> taskPrices = nodeListToElements(config.getElementsByTagName("GPUP-GPUP-Pricing"));
        Map<Class<? extends Task>, Integer> TaskPrices = new HashMap<>();
        List<String> errors = new ArrayList<>();
        taskPrices.forEach(taskPrice -> {
            try {
                String name = taskPrice.getAttributes().getNamedItem("name").getTextContent();
                Class<? extends Task> taskClass = (Class<? extends Task>) Class.forName(name);
                String price = taskPrice.getAttributes().getNamedItem("price-per-target").getTextContent();
                TaskPrices.put(
                        taskClass,
                        Integer.parseInt(price));

            } catch (NumberFormatException e) {
                errors.add("price-per-target was not a number");
            } catch (ClassNotFoundException e) {
                errors.add("task class name is not a known task");
            }
        });
        return new HashMap<String, Object>() {{
            put("name", GraphsName);
            put("prices", TaskPrices);
        }};
    }

    public String validateTextContent(Element elem, String tagName) throws Exception {
        NodeList nl = elem.getElementsByTagName(tagName);
        if (nl.getLength() == 1)
            return nl.item(0).getTextContent();

        throw new Exception("Wrong xml input, Expected the tag name:" + tagName);
    }

    private List<Element> nodeListToElements(NodeList nl) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                elements.add((Element) nl.item(i));
        }
        return elements;
    }


    private HashMap<String, List> readTargets(Document document) throws Exception {
        List<Edge> targetsEdges = new ArrayList<>();
        List<Target> allTargets = new ArrayList<>();

        NodeList targetsNodes = document.getElementsByTagName("GPUP-Targets");
        if (targetsNodes.getLength() == 0 || targetsNodes.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("Bad Format: No Targets");

        Element targetsElement = (Element) targetsNodes.item(0);
        List<Element> targets = nodeListToElements(targetsElement.getElementsByTagName("GPUP-Target"));
        targets.forEach(targetNode -> {
            Target target = new Target(targetNode.getAttributes().getNamedItem("name").getTextContent());


            NodeList result = targetNode.getElementsByTagName("Result");
            if (result.getLength() != 0) {
                target.setResultFromStr(result.item(0).getTextContent());
            }

            NodeList userData = targetNode.getElementsByTagName("GPUP-User-Data");
            if (userData.getLength() != 0)
                target.setUserData(userData.item(0).getTextContent());
            allTargets.add(target);

            List<Element> dependencies = nodeListToElements(targetNode.getElementsByTagName("GPUG-Dependency"));
            dependencies.forEach(edge -> {
                String type = edge.getAttributes().getNamedItem("type").getTextContent();
                Edge newEdge = new Edge(target.name, edge.getTextContent(), type.equals("dependsOn"));
                if (targetsEdges.stream().allMatch(e -> !e.getIn().equals(newEdge.getIn()) || !e.getOut().equals(newEdge.getOut())))
                    targetsEdges.add(newEdge);
            });
        });

        return new HashMap<String, List>() {{
            put("edges", targetsEdges);
            put("targets", allTargets);
        }};
    }

//    public void saveToXML(TargetGraph targetGraph, String xmlFilePath) throws TransformerException {
//        nodeListToElements(targetsElement.getChildNodes()).forEach(targetNode ->
//        {
//            String targetName = targetNode.getAttributes().getNamedItem("name").getTextContent();
//            Optional<Target> OptionalTarget = targetGraph.getTarget(targetName);
//            if (OptionalTarget.isPresent()) {
//                Target target = OptionalTarget.get();
//                if (target.getResult() != Result.NULL) {
//                    Node resultNode = targetNode.getElementsByTagName("Result").item(0);
//                    if (resultNode != null) {
//                        resultNode.setTextContent(target.getResult().toString());
//                    } else {
//                        Element result = document.createElement("Result");
//                        result.appendChild(document.createTextNode(target.getResult().toString()));
//                        targetNode.appendChild(result);
//                    }
//                }
//            }
//        });
//
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//        DOMSource domSource = new DOMSource(document);
//        StreamResult streamResult = new StreamResult(new File(xmlFilePath + ".xml"));
//        transformer.transform(domSource, streamResult);
//    }
}
