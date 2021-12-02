class FindPath_Option implements Option {
    @Override
    public String getText() {
        return "Find path between two targets";
    }

    @Override
    public void actOption() {
        String target1 = UI.prompt("Please enter the `First` targets name", Engine::NoSuchTarget, "No such target in the graph");
        String target2 = UI.prompt("Please enter the `Second` targets name", Engine::NoSuchTarget, "No such target in the graph");
        String dependOrRequired = UI.prompt("Please enter if `Depends` or `Required` relation", "depends", "required");

        UI.printAllPaths(Engine.findPaths(target1, target2, dependOrRequired.equals("depends")));
    }
}
