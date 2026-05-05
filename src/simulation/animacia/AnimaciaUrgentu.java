package simulation.animacia;

import OSPAnimator.AnimShape;
import OSPAnimator.AnimShapeItem;
import OSPAnimator.AnimImageItem;
import OSPAnimator.AnimItem;
import entity.Ambulancia;
import entity.Lekar;
import entity.Pacient;
import entity.Sestra;
import simulation.MySimulation;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class AnimaciaUrgentu {
    private final MySimulation sim;

    private final Map<Integer, AnimImageItem> sestraItems = new HashMap<>();
    private final Map<Integer, AnimImageItem> lekarItems = new HashMap<>();

    private final Map<Integer, AnimImageItem> pacientItems = new HashMap<>();
    private final Map<Integer, Integer> cakarenSlotByPacient = new HashMap<>();
    private final PriorityQueue<Integer> volneCakarenSloty = new PriorityQueue<>();

    public AnimaciaUrgentu(MySimulation sim) {
        this.sim = sim;
    }

    public void prepareReplication() {
        pacientItems.clear();
        cakarenSlotByPacient.clear();
        volneCakarenSloty.clear();
        sestraItems.clear();
        lekarItems.clear();

        if (!sim.animatorExists()) {
            return;
        }

        for (int i = 0; i < 100; i++) {
            volneCakarenSloty.add(i);
        }

        sim.animator().clearAll();
        vytvorPozadie();
        registrujPersonalNaSanitkovomVstupe();
    }

    public void registrujPacientaNaVstupe(Pacient pacient) {
        if (!sim.animatorExists()) {
            return;
        }

        Point2D.Double start = pacient.getTyp() == Pacient.TypPacienta.PESO
                ? LayoutUrgentu.VSTUP_PESO
                : LayoutUrgentu.VSTUP_SANITKA;

        String imgPath = pacient.getTyp() == Pacient.TypPacienta.PESO
                ? "src/resources/pacient_peso.png"
                : "src/resources/pacient_sanitka.png";
        
        AnimImageItem item = new AnimImageItem(imgPath);
        item.setImageSize(24, 24);
        item.setPosition(sim.currentTime(), start.x, start.y);
        item.setToolTip("Pacient ID: " + pacient.id() + " (" + pacient.getTyp() + ")");

        sim.animator().register(pacient.id(), item);
        pacientItems.put(pacient.id(), item);
    }

    public void presunPacientaDoCakarne(Pacient pacient, double trvaniePresunu) {
        if (!sim.animatorExists()) {
            return;
        }

        AnimImageItem item = pacientItems.get(pacient.id());
        if (item == null) {
            registrujPacientaNaVstupe(pacient);
            item = pacientItems.get(pacient.id());
        }

        if (item == null) return;

        uvolniSlotCakarne(pacient);

        int slot = volneCakarenSloty.isEmpty() ? cakarenSlotByPacient.size() : volneCakarenSloty.poll();
        cakarenSlotByPacient.put(pacient.id(), slot);

        Point2D.Double ciel = LayoutUrgentu.cakarenSlot(slot);
        
        if (trvaniePresunu <= 0.0) {
            teleport(item, ciel);
        } else {
            item.moveTo(sim.currentTime(), trvaniePresunu, ciel.x, ciel.y);
        }
    }

    public void uvolniSlotCakarne(Pacient pacient) {
        Integer slot = cakarenSlotByPacient.remove(pacient.id());
        if (slot != null) {
            volneCakarenSloty.add(slot);
        }
    }

    private void vytvorPozadie() {
        // Celkove pozadie
        pridajObdlznik("", 20, 20, 1300, 900, new Color(245, 247, 250));

        // Vstupne zony
        pridajObdlznik("HLAVNÝ VCHOD (PESO)", LayoutUrgentu.VSTUP_PESO.x - 40, LayoutUrgentu.VSTUP_PESO.y - 30, 150, 80, new Color(232, 245, 233));
        pridajObdlznik("PRÍJEM SANITIEK", LayoutUrgentu.VSTUP_SANITKA.x - 40, LayoutUrgentu.VSTUP_SANITKA.y - 30, 150, 80, new Color(255, 243, 224));

        // Caren s ohranicenim
        pridajObdlznik("ČAKÁREŇ", LayoutUrgentu.CAKAREN_X - 5, LayoutUrgentu.CAKAREN_Y - 5,
                LayoutUrgentu.CAKAREN_W + 10, LayoutUrgentu.CAKAREN_H + 10, new Color(176, 190, 197));
        pridajObdlznik("", LayoutUrgentu.CAKAREN_X, LayoutUrgentu.CAKAREN_Y,
                LayoutUrgentu.CAKAREN_W, LayoutUrgentu.CAKAREN_H, Color.WHITE);

        vytvorAmbulancie();
    }

    private void pridajObdlznik(String label, double x, double y, double w, double h, Color color) {
        AnimShapeItem item = new AnimShapeItem(AnimShape.RECTANGLE);
        item.setSize(w, h);
        item.setColor(color);
        item.setPosition(x, y);
        item.setLabel(label);
        sim.animator().register(item);
    }

    private void vytvorAmbulancie() {
        int pocetA = sim.getPocetAmbulanciiA();
        int pocetB = sim.getPocetAmbulanciiB();
        int celkom = pocetA + pocetB;

        for (int i = 0; i < celkom; i++) {
            boolean jeA = i < pocetA;
            Point2D.Double p = LayoutUrgentu.ambulanciaPozicia(i);

            String label = jeA ? "AMB A" + (i + 1) : "AMB B" + (i - pocetA + 1);
            Color color = jeA ? new Color(232, 245, 233) : new Color(227, 242, 253);

            pridajObdlznik(label, p.x, p.y, LayoutUrgentu.AMBULANCIA_W, LayoutUrgentu.AMBULANCIA_H, color);
        }
    }

    private void registrujPersonalNaSanitkovomVstupe() {
        int index = 0;
        for (Sestra sestra : sim.agentSestier().getSestry()) {
            AnimImageItem item = new AnimImageItem("src/resources/sestra.png");
            item.setImageSize(24, 24);
            Point2D.Double p = LayoutUrgentu.zamestnanecStartPozicia(index++);
            item.setPosition(p.x, p.y);
            item.setToolTip("S" + sestra.id());
            sim.animator().register(sestra.id(), item);
            sestraItems.put(sestra.id(), item);
        }

        for (Lekar lekar : sim.agentLekarov().getLekari()) {
            AnimImageItem item = new AnimImageItem("src/resources/lekar.png");
            item.setImageSize(24, 24);
            Point2D.Double p = LayoutUrgentu.zamestnanecStartPozicia(index++);
            item.setPosition(p.x, p.y);
            item.setToolTip("L" + lekar.id());
            sim.animator().register(lekar.id(), item);
            lekarItems.put(lekar.id(), item);
        }
    }

    public void presunPacientaDoAmbulancie(Pacient pacient, Ambulancia ambulancia, double trvaniePresunu) {
        if (!sim.animatorExists()) return;
        if (pacient == null || ambulancia == null) return;
        AnimImageItem item = pacientItems.get(pacient.id());
        if (item == null) return;

        uvolniSlotCakarne(pacient);
        Point2D.Double ciel = LayoutUrgentu.ambulanciaPacientPozicia(ambulancia.getLayoutIndex());
        if (trvaniePresunu <= 0.0) teleport(item, ciel);
        else item.moveTo(sim.currentTime(), trvaniePresunu, ciel.x, ciel.y);
    }

    public void presunSestruDoAmbulancie(Sestra sestra, Ambulancia ambulancia, double trvaniePresunu) {
        if (!sim.animatorExists()) return;
        if (sestra == null || ambulancia == null) return;
        AnimImageItem item = sestraItems.get(sestra.id());
        if (item == null) return;

        Point2D.Double ciel = LayoutUrgentu.ambulanciaSestraPozicia(ambulancia.getLayoutIndex());
        if (trvaniePresunu <= 0.0) teleport(item, ciel);
        else item.moveTo(sim.currentTime(), trvaniePresunu, ciel.x, ciel.y);
    }

    public void presunLekaraDoAmbulancie(Lekar lekar, Ambulancia ambulancia, double trvaniePresunu) {
        if (!sim.animatorExists()) return;
        if (lekar == null || ambulancia == null) return;
        AnimImageItem item = lekarItems.get(lekar.id());
        if (item == null) return;

        Point2D.Double ciel = LayoutUrgentu.ambulanciaLekarPozicia(ambulancia.getLayoutIndex());
        if (trvaniePresunu <= 0.0) teleport(item, ciel);
        else item.moveTo(sim.currentTime(), trvaniePresunu, ciel.x, ciel.y);
    }

    public void odchodPacientaZAmbulancie(Pacient pacient, double trvaniePresunu) {
        if (!sim.animatorExists()) return;
        AnimImageItem item = pacientItems.get(pacient.id());
        if (item == null) return;
        Point2D.Double ciel = LayoutUrgentu.VSTUP_PESO;
        item.moveTo(sim.currentTime(), trvaniePresunu, ciel.x, ciel.y);
    }

    public void zmazPacienta(Pacient pacient) {
        if (!sim.animatorExists()) return;
        AnimImageItem item = pacientItems.get(pacient.id());
        if (item != null) {
            item.setVisible(sim.currentTime(), false);
            item.setPosition(sim.currentTime(), -100, -100);
            pacientItems.remove(pacient.id());
        }
    }

    private void teleport(AnimImageItem item, Point2D.Double ciel) {
        item.setPosition(sim.currentTime(), ciel.x, ciel.y);
    }
}
