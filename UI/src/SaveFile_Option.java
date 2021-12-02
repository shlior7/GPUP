
import javax.xml.transform.TransformerException;

class SaveFile_Option implements Option {
    @Override
    public String getText() {
        return "Save current graph";
    }

    @Override
    public void actOption() {
        String xmlPath = UI.prompt("Please enter the the path which to save the graph file\n(no need for file suffix .xml/.json/.txt etc)");
        try {
            FileHandler.saveToXML(Engine.getTargetGraph(),xmlPath);
        } catch (TransformerException e) {
            UI.error(e.getMessage());
        }
    }
}
