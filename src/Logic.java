import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;

public class Logic {
    private static TargetGraph targetGraph;
    private static final FileHandler fileHandler = new FileHandler();


    public static void showMenu() {
        Menu menu = new Menu("GPUP",
                Arrays.asList(
                        new Load_Option(),
                        new GraphInfo_Option(),
                        new TargetInfo_Option(),
                        new FindPath_Option(),
                        new RunTask_Option(),
                        new FindCircle_Option())
        );
        menu.spawnMenu();
    }

    public static void load(String xmlPath) {
        try {
            targetGraph = fileHandler.loadGPUPXMLFile(xmlPath);
        } catch (IOException e) {
            UI.error("with loading file : " + e.getMessage());
        } catch (ParserConfigurationException e) {
            UI.error("with parsing file : " + e.getMessage());
        } catch (SAXException e) {
            UI.error("with xml file : " + e.getMessage());
        } catch (Exception e) {
            UI.error(e.toString());
        }
    }

    public static boolean validateGraph() {
        if (targetGraph == null) {
            UI.error("no target graph found");
            return false;
        }
        return true;
    }

    public static void graphInfo() {
        if (validateGraph())
            UI.printDivide(targetGraph.toString());
    }

    public static void targetInfo(String name) {
        if (validateGraph()) {
            Optional<Target> target = targetGraph.getTarget(name);
            if (target.isPresent())
                UI.printDivide(targetGraph.getTargetInfo(name));
            else {
                UI.error("No target by this name: " + name);
            }
        }
    }

    public static void runTaskOnTargets(Task task, boolean startFromLastPoint) {
        if (startFromLastPoint) {
            targetGraph.runTaskFromLastTime(task);
        } else {
            targetGraph.runTaskFromScratch(task);
        }
    }

    public static void save(String xmlPath) throws TransformerException {
        fileHandler.saveToXML(targetGraph, xmlPath);
    }

    public static void findCircle(String targetName) {
        UI.printPath(targetGraph.findCircuit(targetName));
    }

    public static void findPath(String targetName1, String targetName2, boolean dependsOn) {
        targetGraph.printAllPaths(dependsOn ? targetName1 : targetName2, dependsOn ? targetName2 : targetName1);
    }
}

