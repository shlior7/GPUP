import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logic {
    private TargetGraph targetGraph = null;
    public TargetGraph getLastTargetGraph(){
        return targetGraph;
    }
    private Document loadFile(String xmlPath) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(xmlPath);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public void Load(String xmlPath) {
        List<Edge> targetsEdges = new ArrayList<>();
        List<Target> allTargets = new ArrayList<>();
        try {
            Document doc = loadFile(xmlPath);
            NodeList configurations = doc.getElementsByTagName("GPUP-Configuration");
            if (configurations.getLength() == 0 || configurations.item(0).getNodeType() != Node.ELEMENT_NODE)
                throw new Exception("no configurations");
            targetGraph = new TargetGraph();
            Element Configuration = (Element) doc.getElementsByTagName("GPUP-Configuration").item(0);
            String GraphsName = Configuration.getElementsByTagName("GPUP-Graph-Name").item(0).getTextContent();
            String WorkingDir = Configuration.getElementsByTagName("GPUP-Working-Directory").item(0).getTextContent();
//            System.out.println("GPUP-Graph-Name: " + Configuration.getElementsByTagName("GPUP-Graph-Name").item(0).getTextContent());
//            System.out.println("GPUP-Working-Directory: " + Configuration.getElementsByTagName("GPUP-Working-Directory").item(0).getTextContent());
//            System.out.println();
            NodeList targetsNodes = doc.getElementsByTagName("GPUP-Targets");
            if (targetsNodes.getLength() == 0 || targetsNodes.item(0).getNodeType() != Node.ELEMENT_NODE)
                throw new Exception("no Targets");

            NodeList targets = ((Element) targetsNodes.item(0)).getElementsByTagName("GPUP-Target");
            for (int itr = 0; itr < targets.getLength(); itr++) {
                Node targetNode = targets.item(itr);
                Target target = new Target(targetNode.getAttributes().getNamedItem("name").getTextContent(), itr);
//                System.out.println(targetNode.getAttributes().getNamedItem("name"));
                if (targetNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) targetNode;
//                System.out.println("Node: " + eElement.getTagName());
                    NodeList userData = eElement.getElementsByTagName("GPUP-User-Data");
                    if (userData.getLength() > 0)
                        target.setUserData(userData.item(0).getTextContent());
//                    System.out.println("userData : " + userData.item(0).getTextContent());
                    allTargets.add(target);
                    NodeList dependencies = eElement.getElementsByTagName("GPUG-Dependency");
                    for (int jtr = 0; jtr < dependencies.getLength(); jtr++) {
                        String type = dependencies.item(jtr).getAttributes().getNamedItem("type").getTextContent();
                        Edge newEdge = new Edge(target.name, dependencies.item(jtr).getTextContent(), type.equals("dependsOn"));
                        if (targetsEdges.stream().parallel().filter(e -> e.in == newEdge.in && e.out == newEdge.out).findFirst().isPresent())
                            throw new Exception("duplicate edge");
                        targetsEdges.add(new Edge(target.name, dependencies.item(jtr).getTextContent(), type.equals("dependsOn")));
//                        System.out.println(dependencies.item(jtr).getAttributes().getNamedItem("type"));
//                        System.out.println("name : " + dependencies.item(jtr).getTextContent());
                    }
                }
//                System.out.println();
            }
            targetGraph = new TargetGraph(GraphsName, WorkingDir, allTargets);
            targetGraph.connect(targetsEdges);
            System.out.println(targetGraph);
        } catch (IOException e) {
            System.out.println("error with loading file : " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("error with parsing file : " + e.getMessage());
        } catch (SAXException e) {
            System.out.println("error with xml file : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //5
    public void runTaskOnTargets(Task task) {
        targetGraph.runTask(task);
    }
}

