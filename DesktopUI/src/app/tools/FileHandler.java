package app.tools;

import TargetGraph.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class FileHandler {
    private static Document document;
    private static Element targetsElement;

    public static TargetGraph loadGPUPXMLFile(File file) throws Exception {
        createDocument(file);
        Map<String, String> config = readConfigurations();
        Map<String, List> targets = readTargets();
        List<SerialSet> serialSets = readSerialSets();

        int parallelism = Integer.parseInt(config.get("parallelism"));
        return new TargetGraph(config.get("name"), config.get("directory"), parallelism, targets.get("targets"), targets.get("edges"), serialSets);
    }

    private static void createDocument(File file) throws ParserConfigurationException, IOException, SAXException {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();
    }

    private static Map<String, String> readConfigurations() throws Exception {
        NodeList configurations = document.getElementsByTagName("GPUP-Configuration");
        if (configurations.getLength() == 0 || configurations.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("no configurations");

        Element config = (Element) configurations.item(0);
        String GraphsName = validateTextContent(config, "GPUP-Graph-Name");
        String WorkingDir = validateTextContent(config, "GPUP-Working-Directory");
        String maxParallelism = validateTextContent(config, "GPUP-Max-Parallelism");

        return new HashMap<String, String>() {{
            put("name", GraphsName);
            put("directory", WorkingDir);
            put("parallelism", maxParallelism);
        }};
    }

    public static String validateTextContent(Element elem, String tagName) throws Exception {
        NodeList nl = elem.getElementsByTagName(tagName);
        if (nl.getLength() == 1)
            return nl.item(0).getTextContent();

        throw new Exception("Wrong xml input, Expected the tag name:" + tagName);
    }

    private static List<Element> nodeListToElements(NodeList nl) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                elements.add((Element) nl.item(i));
        }
        return elements;
    }


    private static HashMap<String, List> readTargets() throws Exception {
        List<Edge> targetsEdges = new ArrayList<>();
        List<Target> allTargets = new ArrayList<>();

        NodeList targetsNodes = document.getElementsByTagName("GPUP-Targets");
        if (targetsNodes.getLength() == 0 || targetsNodes.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("Bad Format: No Targets");

        targetsElement = (Element) targetsNodes.item(0);
        List<Element> targets = nodeListToElements(targetsElement.getElementsByTagName("GPUP-Target"));
        targets.forEach(targetNode -> {
            Target target = new Target(targetNode.getAttributes().getNamedItem("name").getTextContent());


            NodeList result = targetNode.getElementsByTagName("TargetGraph.Result");
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

    private static List<SerialSet> readSerialSets() {
        List<SerialSet> serialSetsList = new ArrayList<>();

        NodeList serialSets = document.getElementsByTagName("GPUP-Serial-Sets");
        if (serialSets.getLength() == 0 || serialSets.item(0).getNodeType() != Node.ELEMENT_NODE)
            return new ArrayList<>();

        Element serialSetsElement = (Element) serialSets.item(0);
        List<Element> allSerialSets = nodeListToElements(serialSetsElement.getElementsByTagName("GPUP-Serial-set"));
        allSerialSets.forEach(setNode -> {
            String name = setNode.getAttributes().getNamedItem("name").getTextContent();
            HashSet<String> targets = Arrays.stream(setNode.getAttributes().getNamedItem("targets").getTextContent().split(",")).collect(Collectors.toCollection(HashSet::new));

            serialSetsList.add(new SerialSet(name, targets));
        });

        return serialSetsList;
    }


    public static void saveToXML(TargetGraph targetGraph, String xmlFilePath) throws TransformerException {
        nodeListToElements(targetsElement.getChildNodes()).forEach(targetNode ->
        {
            String targetName = targetNode.getAttributes().getNamedItem("name").getTextContent();
            Optional<Target> OptionalTarget = targetGraph.getTarget(targetName);
            if (OptionalTarget.isPresent()) {
                Target target = OptionalTarget.get();
                if (target.getResult() != Result.NULL) {
                    Node resultNode = targetNode.getElementsByTagName("Result").item(0);
                    if (resultNode != null) {
                        resultNode.setTextContent(target.getResult().toString());
                    } else {
                        Element result = document.createElement("Result");
                        result.appendChild(document.createTextNode(target.getResult().toString()));
                        targetNode.appendChild(result);
                    }
                }
            }
        });

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(xmlFilePath + ".xml"));
        transformer.transform(domSource, streamResult);
    }
}
