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

public class FileHandler {
    private Document document;
    private Element config;
    private Element targetsElement;
    public static String logLibPath;

    public static boolean log(String Data, String targetName) throws IOException {
        if (logLibPath.equals("")) {
            UI.error("no library path provided");
            return false;
        }
        String filePath = logLibPath + "/" + targetName + ".log";
        new File(filePath).createNewFile();
        FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        out.println(Data + "\n");
        out.close();
        return true;
    }

    public TargetGraph loadGPUPXMLFile(String xmlPath) throws Exception {
        createDocument(xmlPath);
        Map<String, String> config = readConfigurations();
        Map<String, List> targets = readTargets();

        return new TargetGraph(config.get("name"), config.get("directory"), targets.get("targets"), targets.get("edges"));
    }

    private void createDocument(String xmlPath) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(xmlPath);
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();
    }

    private Map<String, String> readConfigurations() throws Exception {
        NodeList configurations = document.getElementsByTagName("GPUP-Configuration");
        if (configurations.getLength() == 0 || configurations.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("no configurations");

        config = (Element) configurations.item(0);
        String GraphsName = config.getElementsByTagName("GPUP-Graph-Name").item(0).getTextContent();
        String WorkingDir = config.getElementsByTagName("GPUP-Working-Directory").item(0).getTextContent();
        return new HashMap<String, String>() {{
            put("name", GraphsName);
            put("directory", WorkingDir);
        }};
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

    private HashMap<String, List> readTargets() throws Exception {
        List<Edge> targetsEdges = new ArrayList<>();
        List<Target> allTargets = new ArrayList<>();

        NodeList targetsNodes = document.getElementsByTagName("GPUP-Targets");
        if (targetsNodes.getLength() == 0 || targetsNodes.item(0).getNodeType() != Node.ELEMENT_NODE)
            throw new Exception("no Targets");

        targetsElement = (Element) targetsNodes.item(0);
        List<Element> targets = nodeListToElements(targetsElement.getElementsByTagName("GPUP-Target"));
        targets.forEach(targetNode -> {
            Target target = new Target(targetNode.getAttributes().getNamedItem("name").getTextContent());
            Element eElement = (Element) targetNode;
            NodeList userData = eElement.getElementsByTagName("GPUP-User-Data");
            if (userData.getLength() != 0)
                target.setUserData(userData.item(0).getTextContent());
            allTargets.add(target);

            List<Element> dependencies = nodeListToElements(eElement.getElementsByTagName("GPUG-Dependency"));
            dependencies.forEach(edge -> {
                String type = edge.getAttributes().getNamedItem("type").getTextContent();
                Edge newEdge = new Edge(target.name, edge.getTextContent(), type.equals("dependsOn"));
                if (targetsEdges.stream().allMatch(e -> !e.in.equals(newEdge.in) || !e.out.equals(newEdge.out)))
                    targetsEdges.add(newEdge);
            });
        });

        return new HashMap<String, List>() {{
            put("edges", targetsEdges);
            put("targets", allTargets);
        }};
    }


    public void saveToXML(TargetGraph tg, String xmlFilePath) throws TransformerException {

        nodeListToElements(targetsElement.getChildNodes()).forEach(targetNode ->
        {
            try {
                String targetName = targetNode.getAttributes().getNamedItem("name").getTextContent();
                Optional<Target> OptionalTarget = tg.getTarget(targetName);
                if (!OptionalTarget.isPresent())
                    throw new Exception("no target in the name of " + targetName);
                Target target = OptionalTarget.get();
                Element status = document.createElement("Status");
                status.appendChild(document.createTextNode(tg.getStatus(targetName).toString()));
                Element result = document.createElement("Result");
                result.appendChild(document.createTextNode(target.getResult().toString()));
                targetNode.appendChild(status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(xmlFilePath));
        transformer.transform(domSource, streamResult);
        UI.printDivide("done");
    }
}
