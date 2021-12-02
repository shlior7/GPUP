
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class Load_Option implements Option {

    @Override
    public String getText() {
        return "Load the `Target Graph`";
    }

    @Override
    public void actOption() {
        try {
            Engine.load(FileHandler.loadGPUPXMLFile(UI.prompt("Please enter the graphs XML full path that you want to load\n(A graph that was saved is acceptable as well and in that case no need for file suffix .xml/.json/.txt etc)")));
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
}
