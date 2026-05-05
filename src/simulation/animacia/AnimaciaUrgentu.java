package simulation.animacia;

import OSPAnimator.AnimImageItem;
import OSPAnimator.Animator;
import entity.Ambulancia;
import entity.Lekar;
import entity.Pacient;
import entity.Sestra;
import simulation.MySimulation;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class AnimaciaUrgentu {

    private final MySimulation sim;

    private final Map<Integer, AnimImageItem> sestraItems = new HashMap<>();
    private final Map<Integer, AnimImageItem> lekarItems = new HashMap<>();
    private final Map<Integer, AnimImageItem> pacientItems = new HashMap<>();

    private final Map<Integer, Point2D.Double> poziciaSestry = new HashMap<>();
    private final Map<Integer, Point2D.Double> poziciaLekara = new HashMap<>();
    private final Map<Integer, Point2D.Double> poziciaPacienta = new HashMap<>();

    private final Map<Integer, Integer> cakarenSlotByPacient = new HashMap<>();
    private final Map<Integer, Integer> ambulanciaByPacient = new HashMap<>();

    private final PriorityQueue<Integer> volneCakarenSloty = new PriorityQueue<>();

    /*
     * Offsets riešia prípad, keď pacient, sestra a lekár majú rovnaké ID.
     * Animator potom nedostane rovnaké ID pre rôzne entity.
     */
    private static final int ANIM_ID_PACIENT = 100_000;
    private static final int ANIM_ID_SESTRA = 200_000;
    private static final int ANIM_ID_LEKAR = 300_000;

    private static final String IMG_PACIENT_PESO = "resources/pacient_peso.png";
    private static final String IMG_PACIENT_SANITKA = "resources/pacient_sanitka.png";
    private static final String IMG_SESTRA = "resources/sestra.png";
    private static final String IMG_LEKAR = "resources/lekar.png";

    public AnimaciaUrgentu(MySimulation sim) {
        this.sim = sim;
    }

    public void prepareReplication() {
        pacientItems.clear();
        sestraItems.clear();
        lekarItems.clear();

        poziciaPacienta.clear();
        poziciaSestry.clear();
        poziciaLekara.clear();

        cakarenSlotByPacient.clear();
        ambulanciaByPacient.clear();
        volneCakarenSloty.clear();

        if (!sim.animatorExists() || sim.getDesiredSpeedDuration() == null) {
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
        if (!shouldAnimate()) {
            return;
        }

        Point2D.Double start = pacient.getTyp() == Pacient.TypPacienta.PESO
                ? LayoutUrgentu.VSTUP_PESO
                : LayoutUrgentu.VSTUP_SANITKA;

        String imgPath = pacient.getTyp() == Pacient.TypPacienta.PESO
                ? IMG_PACIENT_PESO
                : IMG_PACIENT_SANITKA;

        AnimImageItem item = new AnimImageItem(imgPath);
        item.setImageSize(24, 24);
        item.setPosition(sim.currentTime(), start.x, start.y);
        item.setToolTip("Pacient ID: " + pacient.id() + " (" + pacient.getTyp() + ")");

        sim.animator().register(ANIM_ID_PACIENT + pacient.id(), item);

        pacientItems.put(pacient.id(), item);
        poziciaPacienta.put(pacient.id(), start);
    }

    public void presunPacientaDoCakarne(Pacient pacient, double trvaniePresunu) {
        if (!shouldAnimate()) {
            return;
        }

        AnimImageItem item = pacientItems.get(pacient.id());

        if (item == null) {
            registrujPacientaNaVstupe(pacient);
            item = pacientItems.get(pacient.id());
        }

        if (item == null) {
            return;
        }

        uvolniSlotCakarne(pacient);

        int slot = volneCakarenSloty.isEmpty()
                ? cakarenSlotByPacient.size()
                : volneCakarenSloty.poll();

        cakarenSlotByPacient.put(pacient.id(), slot);

        Point2D.Double ciel = LayoutUrgentu.cakarenSlot(slot);

        boolean sanitka = pacient.getTyp() == Pacient.TypPacienta.SANITKA;

        movePoCeste(
                item,
                trvaniePresunu,
                LayoutUrgentu.cestaPacientVstupDoCakarne(sanitka, ciel)
        );

        poziciaPacienta.put(pacient.id(), ciel);
    }

    public void uvolniSlotCakarne(Pacient pacient) {
        Integer slot = cakarenSlotByPacient.remove(pacient.id());

        if (slot != null) {
            volneCakarenSloty.add(slot);
        }
    }

    public void presunPacientaDoAmbulancie(Pacient pacient, Ambulancia ambulancia, double trvaniePresunu) {
        if (!shouldAnimate()) {
            return;
        }

        if (pacient == null || ambulancia == null) {
            return;
        }

        AnimImageItem item = pacientItems.get(pacient.id());

        if (item == null) {
            return;
        }

        Point2D.Double start = poziciaPacienta.get(pacient.id());

        if (start == null) {
            Integer slot = cakarenSlotByPacient.get(pacient.id());
            start = slot == null
                    ? LayoutUrgentu.VSTUP_DO_CAKARNE
                    : LayoutUrgentu.cakarenSlot(slot);
        }

        uvolniSlotCakarne(pacient);

        int ambIndex = ambulancia.getLayoutIndex();
        Point2D.Double ciel = LayoutUrgentu.ambulanciaPacientPozicia(ambIndex);

        movePoCeste(
                item,
                trvaniePresunu,
                LayoutUrgentu.cestaCakarenDoAmbulancie(start, ambIndex, ciel)
        );

        poziciaPacienta.put(pacient.id(), ciel);
        ambulanciaByPacient.put(pacient.id(), ambIndex);
    }

    public void presunSestruDoAmbulancie(Sestra sestra, Ambulancia ambulancia, double trvaniePresunu) {
        if (!shouldAnimate()) {
            return;
        }

        if (sestra == null || ambulancia == null) {
            return;
        }

        AnimImageItem item = sestraItems.get(sestra.id());

        if (item == null) {
            return;
        }

        Point2D.Double start = poziciaSestry.get(sestra.id());

        if (start == null) {
            start = LayoutUrgentu.zamestnanecStartPozicia(0);
        }

        int ambIndex = ambulancia.getLayoutIndex();
        Point2D.Double ciel = LayoutUrgentu.ambulanciaSestraPozicia(ambIndex);

        movePoCeste(
                item,
                trvaniePresunu,
                LayoutUrgentu.cestaPersonalDoAmbulancie(start, ambIndex, ciel)
        );

        poziciaSestry.put(sestra.id(), ciel);
    }

    public void presunLekaraDoAmbulancie(Lekar lekar, Ambulancia ambulancia, double trvaniePresunu) {
        if (!shouldAnimate()) {
            return;
        }

        if (lekar == null || ambulancia == null) {
            return;
        }

        AnimImageItem item = lekarItems.get(lekar.id());

        if (item == null) {
            return;
        }

        Point2D.Double start = poziciaLekara.get(lekar.id());

        if (start == null) {
            start = LayoutUrgentu.zamestnanecStartPozicia(0);
        }

        int ambIndex = ambulancia.getLayoutIndex();
        Point2D.Double ciel = LayoutUrgentu.ambulanciaLekarPozicia(ambIndex);

        movePoCeste(
                item,
                trvaniePresunu,
                LayoutUrgentu.cestaPersonalDoAmbulancie(start, ambIndex, ciel)
        );

        poziciaLekara.put(lekar.id(), ciel);
    }


    public void odchodPacientaZAmbulancie(Pacient pacient, double trvaniePresunu) {
        if (!shouldAnimate()) {
            return;
        }

        if (pacient == null) {
            return;
        }

        AnimImageItem item = pacientItems.get(pacient.id());

        if (item == null) {
            return;
        }

        Integer ambIndex = ambulanciaByPacient.get(pacient.id());

        if (ambIndex == null) {
            ambIndex = 0;
        }

        movePoCeste(
                item,
                trvaniePresunu,
                LayoutUrgentu.cestaAmbulanciaNaVystup(ambIndex)
        );

        poziciaPacienta.put(pacient.id(), LayoutUrgentu.VSTUP_PESO);
        ambulanciaByPacient.remove(pacient.id());
    }

    public void zmazPacienta(Pacient pacient) {
        if (!shouldAnimate()) {
            return;
        }

        if (pacient == null) {
            return;
        }

        AnimImageItem item = pacientItems.get(pacient.id());

        if (item != null) {
            item.setVisible(sim.currentTime(), false);
            item.setPosition(sim.currentTime(), -100, -100);
            pacientItems.remove(pacient.id());
        }

        poziciaPacienta.remove(pacient.id());
        uvolniSlotCakarne(pacient);
        ambulanciaByPacient.remove(pacient.id());
    }

    private void vytvorPozadie() {
        if (sim.animator() instanceof Animator) {
            ((Animator) sim.animator()).setBackgroundImage(
                    LayoutUrgentu.BACKGROUND_IMAGE, 0, 0,
                    LayoutUrgentu.CANVAS_W, LayoutUrgentu.CANVAS_H
            );
        }
    }

    private void registrujPersonalNaSanitkovomVstupe() {
        int index = 0;

        for (Sestra sestra : sim.agentSestier().getSestry()) {
            AnimImageItem item = new AnimImageItem(IMG_SESTRA);
            item.setImageSize(24, 24);
            item.setZIndex(40);

            Point2D.Double p = LayoutUrgentu.zamestnanecStartPozicia(index++);

            item.setPosition(p.x, p.y);
            item.setToolTip("Sestra ID: " + sestra.id());

            sim.animator().register(ANIM_ID_SESTRA + sestra.id(), item);

            sestraItems.put(sestra.id(), item);
            poziciaSestry.put(sestra.id(), p);
        }

        for (Lekar lekar : sim.agentLekarov().getLekari()) {
            AnimImageItem item = new AnimImageItem(IMG_LEKAR);
            item.setImageSize(24, 24);
            item.setZIndex(40);

            Point2D.Double p = LayoutUrgentu.zamestnanecStartPozicia(index++);

            item.setPosition(p.x, p.y);
            item.setToolTip("Lekár ID: " + lekar.id());

            sim.animator().register(ANIM_ID_LEKAR + lekar.id(), item);

            lekarItems.put(lekar.id(), item);
            poziciaLekara.put(lekar.id(), p);
        }
    }

    private void movePoCeste(
            AnimImageItem item,
            double celkoveTrvanie,
            java.util.List<Point2D.Double> body
    ) {
        if (item == null || body == null || body.isEmpty()) {
            return;
        }

        if (celkoveTrvanie <= 0.0) {
            Point2D.Double posledny = body.get(body.size() - 1);
            teleport(item, posledny);
            return;
        }

        double trvanieUseku = celkoveTrvanie / body.size();
        double cas = sim.currentTime();

        for (Point2D.Double bod : body) {
            item.moveTo(cas, trvanieUseku, bod.x, bod.y);
            cas += trvanieUseku;
        }
    }

    private boolean shouldAnimate() {
        return sim.animatorExists()
                && sim.getDesiredSpeedDuration() != null
                && !sim.isMaxSpeed();
    }

    private void teleport(AnimImageItem item, Point2D.Double ciel) {
        item.setPosition(sim.currentTime(), ciel.x, ciel.y);
    }
}
