import javafx.geometry.Point2D;

public class UtilitiesPoint2D {

    public static Point2D rotate(final Point2D point, final Point2D pivot, double angle_degrees) {
        double angle = Math.toRadians(angle_degrees);

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        Point2D result = point.subtract(pivot);

        // rotate point
        Point2D rotatedOrigin = new Point2D(
                result.getX() * cos - result.getY() * sin,
                result.getX() * sin + result.getY() * cos);

        result = rotatedOrigin.add(pivot);

        return result;
    }

    public static Point2D attractiveForce(Point2D from, Point2D to, int globalCount, double force, double scale) {

        double distance = from.distance(to);

        Point2D vec = to.subtract(from).normalize();

        double factor = attractiveFunction(distance, globalCount, force, scale);
        return vec.multiply(factor);
    }

    static double attractiveFunction(double distance, int globalCount, double force, double scale) {
        if (distance < 1) {
            distance = 1;
        }

        return force * Math.log(distance / scale) * 0.1;
    }


    public static Point2D repellingForce(Point2D from, Point2D to, double scale) {
        double distance = from.distance(to);

        Point2D vec = to.subtract(from).normalize();

        double factor = -repellingFunction(distance, scale);

        return vec.multiply(factor);
    }

    static double repellingFunction(double distance, double scale) {
        if (distance < 1) {
            distance = 1;
        }
        return scale / (distance * distance);
    }

}
