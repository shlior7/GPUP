import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;


public class ActionButton extends Button {
    public ActionButton() {
        super();
        init();
    }

    public ActionButton(String label, EventHandler<ActionEvent> eventHandler) {
        super(label);
        setOnAction(eventHandler);
        init();
    }

    public void init() {
        setMinHeight(50);
        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.GOLD);
        borderGlow.setWidth(20);
        borderGlow.setHeight(20);
        setEffect(borderGlow);
    }
}
