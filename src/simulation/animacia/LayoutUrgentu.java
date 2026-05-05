package simulation.animacia;

import java.awt.geom.Point2D;

public final class LayoutUrgentu {
    public static final Point2D.Double VSTUP_PESO = new Point2D.Double(80, 180);
    public static final Point2D.Double VSTUP_SANITKA = new Point2D.Double(80, 320);

    public static final double AMBULANCIA_W = 160;
    public static final double AMBULANCIA_H = 44;

    public static final double CAKAREN_X = 550; 
    public static final double CAKAREN_Y = 120;
    public static final double CAKAREN_W = 360;
    public static final double CAKAREN_H = 300;

    private static final double SLOT_START_X = 590; 
    private static final double SLOT_START_Y = 170;
    private static final double SLOT_GAP_X = 34;
    private static final double SLOT_GAP_Y = 34;
    private static final int SLOT_COLUMNS = 8;

    private LayoutUrgentu() {}

    public static Point2D.Double cakarenSlot(int index) {
        int col = index % SLOT_COLUMNS;
        int row = index / SLOT_COLUMNS;

        return new Point2D.Double(
                SLOT_START_X + col * SLOT_GAP_X,
                SLOT_START_Y + row * SLOT_GAP_Y
        );
    }

    public static Point2D.Double ambulanciaPacientPozicia(int index) {
        Point2D.Double p = ambulanciaPozicia(index);
        return new Point2D.Double(p.x + 28, p.y + 22);
    }

    public static Point2D.Double ambulanciaSestraPozicia(int index) {
        Point2D.Double p = ambulanciaPozicia(index);
        return new Point2D.Double(p.x + 78, p.y + 22);
    }

    public static Point2D.Double ambulanciaLekarPozicia(int index) {
        Point2D.Double p = ambulanciaPozicia(index);
        return new Point2D.Double(p.x + 128, p.y + 22);
    }

    public static Point2D.Double zamestnanecStartPozicia(int index) {
        int col = index % 5;
        int row = index / 5;

        return new Point2D.Double(
                VSTUP_SANITKA.x + 105 + col * 24,
                VSTUP_SANITKA.y - 35 + row * 24
        );
    }

    public static Point2D.Double ambulanciaPozicia(int index) {
        return new Point2D.Double(1080, 80 + index * 58);
    }
}
