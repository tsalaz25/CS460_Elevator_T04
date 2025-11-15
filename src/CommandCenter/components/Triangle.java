package CommandCenter.components;

import javafx.scene.shape.Polygon;

public class Triangle extends Polygon {

    public Triangle(){
        this(0, 0);
    }

    public Triangle(double xOffset, double yOffset){
        this.getPoints().addAll(new Double[]{
            5.0+xOffset, 0.0+yOffset,
            0.0+xOffset, 8.0+yOffset,
            10.0+xOffset, 8.0+yOffset
        });
        this.setScaleY(2);
        this.setScaleX(2);
    }
}
