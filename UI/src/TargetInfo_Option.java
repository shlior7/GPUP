import engine.Engine;

class TargetInfo_Option implements Option {
    @Override
    public String getText() {
        return "Displays target information";
    }

    @Override
    public void actOption() {
        if (!Engine.validateGraph()) {
            UI.error("no target graph found");
            return;
        }
        UI.printDivide(Engine.targetInfo(UI.prompt("please enter the targets name")));
    }
}
