
class GraphInfo_Option implements Option {
    @Override
    public String getText() {
        return "Displays general information on the target graph";
    }

    @Override
    public void actOption() {
        if (!Engine.validateGraph()) {
            UI.error("no target graph found");
            return;
        }
        UI.printDivide(Engine.graphInfo());
    }
}
