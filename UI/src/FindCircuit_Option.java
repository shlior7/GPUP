import java.util.LinkedList;

class FindCircuit_Option implements Option {
    @Override
    public String getText() {
        return "Find out if a target is in a circuit in the graph";
    }

    @Override
    public void actOption() {
//        String targetName = UI.prompt("Please enter the targets name", engine.Engine::NoSuchTarget, "No such target in the graph");
////        LinkedList<String> circuit = engine.Engine.findCircuit(targetName);
//        if (circuit.size() == 0) {
//            UI.printDivide("the target " + targetName + " was not found to be in a circuit");
//            return;
//        }
//        UI.printPath(circuit);
    }
}
