package simulation.animacia;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public final class LayoutUrgentu {

    public static final double CANVAS_W = 1600;
    public static final double CANVAS_H = 900;

    public static final String BACKGROUND_IMAGE = "resources/pozadie.png";

    // Vstupy
    public static final Point2D.Double VSTUP_PESO = new Point2D.Double(260, 205);
    public static final Point2D.Double VSTUP_SANITKA = new Point2D.Double(260, 400);

    // Ľavá chodba
    public static final Point2D.Double CHODBA_PESO_1 = new Point2D.Double(350, 205);
    public static final Point2D.Double CHODBA_PESO_2 = new Point2D.Double(470, 205);

    public static final Point2D.Double CHODBA_SANITKA_1 = new Point2D.Double(350, 400);
    public static final Point2D.Double CHODBA_SANITKA_2 = new Point2D.Double(470, 400);

    // Vstup do čakárne
    public static final Point2D.Double VSTUP_DO_CAKARNE = new Point2D.Double(555, 400);

    // Čakáreň
    public static final double CAKAREN_X = 525;
    public static final double CAKAREN_Y = 90;
    public static final double CAKAREN_W = 540;
    public static final double CAKAREN_H = 515;

    // Sloty v čakárni
    private static final double SLOT_START_X = 610;
    private static final double SLOT_START_Y = 150;
    private static final double SLOT_GAP_X = 38;
    private static final double SLOT_GAP_Y = 52;
    private static final int SLOT_COLUMNS = 5;

    // Výstup z čakárne smerom k ambulanciám
    public static final Point2D.Double CAKAREN_VYCHOD_AMB = new Point2D.Double(1065, 330);

    // Pravá chodba pred ambulanciami
    public static final Point2D.Double CHODBA_PRED_CAKARNOU = new Point2D.Double(1145, 330);

    public static final double CHODBA_AMB_X = 1185;

    // Dolná chodba
    public static final double DOLNA_CHODBA_Y = 775;

    public static final double AMBULANCIA_X = 1255;
    public static final double AMBULANCIA_W = 220;
    public static final double AMBULANCIA_H = 57;
    public static final double AMBULANCIA_GAP_Y = 62;

    private static final int AMBULANCIA_A_COUNT = 5;
    private static final double AMBULANCIA_A_START_Y = 25;
    private static final double AMBULANCIA_B_START_Y = 360;

    /*
     * Zázemie personálu.
     * Predtým sa používali 4 stĺpce a posledné ikony vychádzali mimo miestnosti.
     */
    public static final double PERSONAL_X = 315;
    public static final double PERSONAL_Y = 605;
    private static final int PERSONAL_COLUMNS = 3;
    private static final double PERSONAL_GAP_X = 34;
    private static final double PERSONAL_GAP_Y = 38;

    private LayoutUrgentu() {
    }

    public static Point2D.Double cakarenSlot(int index) {
        int col = index % SLOT_COLUMNS;
        int row = index / SLOT_COLUMNS;

        return new Point2D.Double(
                SLOT_START_X + col * SLOT_GAP_X,
                SLOT_START_Y + row * SLOT_GAP_Y
        );
    }

    public static Point2D.Double ambulanciaPozicia(int index) {
        double y = index < AMBULANCIA_A_COUNT
                ? AMBULANCIA_A_START_Y + index * AMBULANCIA_GAP_Y
                : AMBULANCIA_B_START_Y + (index - AMBULANCIA_A_COUNT) * AMBULANCIA_GAP_Y;

        return new Point2D.Double(AMBULANCIA_X, y);
    }

    public static Point2D.Double ambulanciaDvere(int index) {
        Point2D.Double p = ambulanciaPozicia(index);

        return new Point2D.Double(
                CHODBA_AMB_X,
                p.y + AMBULANCIA_H / 2.0
        );
    }

    public static Point2D.Double ambulanciaPacientPozicia(int index) {
        Point2D.Double p = ambulanciaPozicia(index);

        return new Point2D.Double(
                p.x + 35,
                p.y + 32
        );
    }

    public static Point2D.Double ambulanciaSestraPozicia(int index) {
        Point2D.Double p = ambulanciaPozicia(index);

        return new Point2D.Double(
                p.x + 95,
                p.y + 32
        );
    }

    public static Point2D.Double ambulanciaLekarPozicia(int index) {
        Point2D.Double p = ambulanciaPozicia(index);

        return new Point2D.Double(
                p.x + 155,
                p.y + 32
        );
    }

    public static Point2D.Double zamestnanecStartPozicia(int index) {
        int col = index % PERSONAL_COLUMNS;
        int row = index / PERSONAL_COLUMNS;

        return new Point2D.Double(
                PERSONAL_X + col * PERSONAL_GAP_X,
                PERSONAL_Y + row * PERSONAL_GAP_Y
        );
    }

    public static List<Point2D.Double> cestaPacientVstupDoCakarne(boolean sanitka, Point2D.Double cielSlot) {
        List<Point2D.Double> cesta = new ArrayList<>();

        if (sanitka) {
            cesta.add(CHODBA_SANITKA_1);
            cesta.add(CHODBA_SANITKA_2);
        } else {
            cesta.add(CHODBA_PESO_1);
            cesta.add(CHODBA_PESO_2);
            cesta.add(new Point2D.Double(470, 400));
        }

        cesta.add(VSTUP_DO_CAKARNE);
        cesta.add(new Point2D.Double(585, cielSlot.y));
        cesta.add(cielSlot);

        return cesta;
    }

    public static List<Point2D.Double> cestaCakarenDoAmbulancie(
            Point2D.Double start,
            int ambulanciaIndex,
            Point2D.Double ciel
    ) {
        List<Point2D.Double> cesta = new ArrayList<>();

        Point2D.Double dvere = ambulanciaDvere(ambulanciaIndex);

        cesta.add(new Point2D.Double(start.x, 330));
        cesta.add(CAKAREN_VYCHOD_AMB);
        cesta.add(CHODBA_PRED_CAKARNOU);
        cesta.add(new Point2D.Double(CHODBA_AMB_X, 330));
        cesta.add(new Point2D.Double(CHODBA_AMB_X, dvere.y));
        cesta.add(dvere);
        cesta.add(ciel);

        return cesta;
    }

    public static List<Point2D.Double> cestaPersonalDoAmbulancie(
            Point2D.Double start,
            int ambulanciaIndex,
            Point2D.Double ciel
    ) {
        List<Point2D.Double> cesta = new ArrayList<>();

        Point2D.Double dvere = ambulanciaDvere(ambulanciaIndex);

        if (start.x >= AMBULANCIA_X - 20) {
            int aktualnaAmb = najblizsiaAmbulanciaPodlaY(start.y);
            Point2D.Double aktualneDvere = ambulanciaDvere(aktualnaAmb);

            cesta.add(aktualneDvere);
            cesta.add(new Point2D.Double(CHODBA_AMB_X, aktualneDvere.y));
        } else {
            cesta.add(new Point2D.Double(start.x, DOLNA_CHODBA_Y));
        }

        cesta.add(new Point2D.Double(1145, DOLNA_CHODBA_Y));
        cesta.add(new Point2D.Double(CHODBA_AMB_X, DOLNA_CHODBA_Y));
        cesta.add(new Point2D.Double(CHODBA_AMB_X, dvere.y));
        cesta.add(dvere);
        cesta.add(ciel);

        return cesta;
    }

    public static List<Point2D.Double> cestaAmbulanciaNaVystup(int ambulanciaIndex) {
        List<Point2D.Double> cesta = new ArrayList<>();

        Point2D.Double dvere = ambulanciaDvere(ambulanciaIndex);

        cesta.add(dvere);
        cesta.add(new Point2D.Double(CHODBA_AMB_X, dvere.y));
        cesta.add(new Point2D.Double(CHODBA_AMB_X, DOLNA_CHODBA_Y));
        cesta.add(new Point2D.Double(470, DOLNA_CHODBA_Y));
        cesta.add(new Point2D.Double(470, 205));
        cesta.add(CHODBA_PESO_1);
        cesta.add(VSTUP_PESO);

        return cesta;
    }

    private static int najblizsiaAmbulanciaPodlaY(double y) {
        double aMidY = AMBULANCIA_A_START_Y
                + (AMBULANCIA_A_COUNT - 1) * AMBULANCIA_GAP_Y
                + AMBULANCIA_H / 2.0;

        double bMidY = AMBULANCIA_B_START_Y + AMBULANCIA_H / 2.0;
        double hranica = (aMidY + bMidY) / 2.0;

        int index;

        if (y < hranica) {
            index = (int) Math.round((y - AMBULANCIA_A_START_Y) / AMBULANCIA_GAP_Y);
            index = Math.max(0, Math.min(AMBULANCIA_A_COUNT - 1, index));
        } else {
            index = AMBULANCIA_A_COUNT
                    + (int) Math.round((y - AMBULANCIA_B_START_Y) / AMBULANCIA_GAP_Y);

            index = Math.max(AMBULANCIA_A_COUNT, Math.min(AMBULANCIA_A_COUNT + 6, index));
        }

        return index;
    }
}