import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Logic {
    TargetGraph targetGraph;
    FileHandler fileHandler = new FileHandler();

    public void load(String xmlPath) {
        try {
            targetGraph = fileHandler.loadGPUPXMLFile(xmlPath);
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

            Element Configuration = (Element) configurations.item(0);
            String GraphsName = Configuration.getElementsByTagName("GPUP-Graph-Name").item(0).getTextContent();
            String WorkingDir = Configuration.getElementsByTagName("GPUP-Working-Directory").item(0).getTextContent();

            NodeList targetsNodes = doc.getElementsByTagName("GPUP-Targets");
            if (targetsNodes.getLength() == 0 || targetsNodes.item(0).getNodeType() != Node.ELEMENT_NODE)
                throw new Exception("no Targets");

            NodeList targets = ((Element) targetsNodes.item(0)).getElementsByTagName("GPUP-Target");
            for (int itr = 0; itr < targets.getLength(); itr++) {
                Node targetNode = targets.item(itr);
                Target target = new Target(targetNode.getAttributes().getNamedItem("name").getTextContent());
                if (targetNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) targetNode;
                    NodeList userData = eElement.getElementsByTagName("GPUP-User-Data");
                    if (userData.getLength() > 0)
                        target.setUserData(userData.item(0).getTextContent());
                    allTargets.add(target);
                    NodeList dependencies = eElement.getElementsByTagName("GPUG-Dependency");
                    for (int jtr = 0; jtr < dependencies.getLength(); jtr++) {
                        String type = dependencies.item(jtr).getAttributes().getNamedItem("type").getTextContent();
                        Edge newEdge = new Edge(target.name, dependencies.item(jtr).getTextContent(), type.equals("dependsOn"));
                        if (targetsEdges.stream().allMatch(e -> !e.in.equals(newEdge.in) || !e.out.equals(newEdge.out)))
                            targetsEdges.add(new Edge(target.name, dependencies.item(jtr).getTextContent(), type.equals("dependsOn")));
                    }
                }
            }
            targetGraph = new TargetGraph(GraphsName, WorkingDir, allTargets, targetsEdges);
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


    public void loadGraphFromLastPoint(String pathName) {
        File f = new File(pathName);
        File[] match = f.listFiles(((dir, name) -> {
            return name.startsWith("Simulation");
        }));
        findLastRunnedTaskFolder(match);
//        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").parse(currentTime).toString()
    }

    public String findLastRunnedTaskFolder(File[] folders) {
        if (folders.length == 0) {
            UI.print("no runned tasks");
        }
        String s = folders[0].getName().substring(folders[0].getName().lastIndexOf(" "));
        UI.print(s);
//        Arrays.stream(folders).reduce()
        return s;
    }

    public void runTaskOnTargets(Task task) {
        targetGraph.runTaskFromScratch(task);
    }

    public void runTaskOnTargetsAgain(Task task) {
        targetGraph.runTaskFromLastTime(task);
    }

    public void save(String xmlPath) throws TransformerException {
        fileHandler.saveToXML(targetGraph, xmlPath);
    }
}

