import TargetGraph.Edge;
import TargetGraph.Target;
import TargetGraph.TargetGraph;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileHandler {
    private static Document document;
    private static Element targetsElement;
    public static String logLibraryPath;

    public static void createLogLibrary(String taskName) {
        String currentTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());
        String path = taskName + " - " + currentTime;
        new File(path).mkdir();
        logLibraryPath = path;
    }

    public static void log(String Data, String targetName) throws IOException {
        String filePath = logLibraryPath + "/" + targetName + ".log";
        new File(filePath).createNewFile();
        FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        out.println(Data + "\n");
        out.close();
    }

    public static TargetGraph loadGPUPXMLFile(String xmlPath) throws Exception {
        createDocument(xmlPath);
        Map<String, String> config = readConfigurations();
        Map<String, List> targets = readTargets();

        return new TargetGraph(config.get("name"), config.get("directory"), targets.get("targets"), targets.get("edges"));
    }

    private static void createDocument(String xmlPath) throws ParserConfigurationException, IOException, SAXException {
        if (!xmlPath.endsWith("xml"))
            xmlPath = xmlPath + ".xml";
        File file = new File(xmlPath);
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();
    }

    private static Map<String, String> readConfigurations() throws Exception {
        NodeList configurations = document.getElementsByTagName("GPUP-Configuration");
        if (configurations.getLength() == 0 || configurations.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("no configurations");

        Element config = (Element) configurations.item(0);
        String GraphsName = config.getElementsByTagName("GPUP-graph.Graph-Name").item(0).getTextContent();
        String WorkingDir = config.getElementsByTagName("GPUP-Working-Directory").item(0).getTextContent();
        return new HashMap<String, String>() {{
            put("name", GraphsName);
            put("directory", WorkingDir);
        }};
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
            throw new Exception("no Targets");

        targetsElement = (Element) targetsNodes.item(0);
        List<Element> targets = nodeListToElements(targetsElement.getElementsByTagName("GPUP-TargetGraph.Target"));
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


    public static void saveToXML(TargetGraph targetGraph, String xmlFilePath) throws TransformerException {
        nodeListToElements(targetsElement.getChildNodes()).forEach(targetNode ->
        {
            String targetName = targetNode.getAttributes().getNamedItem("name").getTextContent();
            Optional<Target> OptionalTarget = targetGraph.getTarget(targetName);
            if (OptionalTarget.isPresent()) {
                Target target = OptionalTarget.get();
                if (target.getResult() != null) {
                    Node resultNode = targetNode.getElementsByTagName("TargetGraph.Result").item(0);
                    if (resultNode != null) {
                        resultNode.setTextContent(target.getResult().toString());
                    } else {
                        Element result = document.createElement("TargetGraph.Result");
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
