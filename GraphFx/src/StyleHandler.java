/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.scene.shape.Shape;


public class StyleHandler implements StyledElement {

    private final Shape client;
    
    public StyleHandler(Shape client) {
        this.client = client;
    }
    @Override
    public void setStyle(String css) {
        client.setStyle(css);
    }
    @Override

    public void setStyleClass(String cssClass) {
        client.getStyleClass().clear();
        client.setStyle(null);
        client.getStyleClass().add(cssClass);
    }
    @Override
    public void addStyleClass(String cssClass) {
        client.getStyleClass().add(cssClass);
    }
    @Override
    public boolean removeStyleClass(String cssClass) {
        return client.getStyleClass().remove(cssClass);
    }
    
}
