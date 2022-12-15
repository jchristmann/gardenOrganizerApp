package com.garden.gardenorganizerapp;

import com.garden.gardenorganizerapp.dataobjects.*;
import com.garden.gardenorganizerapp.db.VarietyDAO;
import com.garden.gardenorganizerapp.viewcontrollers.GardenGridViewController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


public class GardenWidget extends Canvas {

    private Garden TheGarden;

    private GardenGridViewController controller;
    private PlantingArea area = new PlantingArea();

    private Point2D mouseDraggingStartCoord = null;
    private Point2D currentMouseCoord = null;
    private Point2D currentMouseMoveCoordStartRec = null;
    private Point2D currentMouseMoveCoordEndRec = null;

    private int hoverlength = 0;
    private int hoverwidth = 0;
    private boolean turned = false;

    public GardenWidget(Garden garden) {
        super(garden.getWidth(), garden.getHeight());
        this.TheGarden = garden;

        drawGarden();
    }

    public void setController(GardenGridViewController gardenGridViewController) {
        this.controller = gardenGridViewController;
    }


    public PlantingArea getCurrentPlantingArea() {
        return area;
    }

    public void setPlantingArea() {
        this.area = new PlantingArea();
    }

    public void setItemInPlantingArea(Item item) {
        if (this.area.getItem() == null && item != null) {
            activateGridActions();
        }
        this.area.setItem(item);
        hoverwidth = 0;
        hoverlength = 0;
    }

    public void activateGridActions() {
        setOnMouseMoved(e -> onMouseMoved(e.getX(), e.getY()));
        setOnMouseClicked(e -> onMouseClicked(e.getX(), e.getY()));
        setOnMousePressed(e -> onMousePressed(e.getX(), e.getY()));
        setOnMouseDragged(e -> onMouseDragged(e.getX(), e.getY()));
        setOnMouseReleased(e -> onMouseReleased());
        setOnScroll(e -> System.out.println("Mausrad gerdreht"));
    }

    private void onMouseMoved(double x, double y) {
        // Maus entered Canvas

        int normX = TheGarden.normalizeCoordToGrid(x);
        int normY = TheGarden.normalizeCoordToGrid(y);
        if (currentMouseMoveCoordStartRec == null) {
            this.currentMouseMoveCoordStartRec = new Point2D(normX, normY);
        } else if (currentMouseMoveCoordStartRec.getX() != normX || currentMouseMoveCoordStartRec.getY() != normY) {
            this.currentMouseMoveCoordStartRec = new Point2D(normX, normY);
        }
        drawGarden();
    }

    public void onMousePressed(double x, double y) {
        this.mouseDraggingStartCoord = new Point2D(x, y);
    }

    public void onMouseDragged(double x, double y) {
        this.currentMouseCoord = new Point2D(x, y);
        drawGarden();
    }

    public void onMouseClicked(double x, double y) {
        if (isAllowedToHandleClick()) {
            Point2D gridCoords =
                    toGridCoords(x, y);
            PlantingArea containingArea = getAreaContainingSpotCoords(gridCoords);
            if (containingArea != null && containingArea.removeSpot(gridCoords)) {
                controller.removeSpotFromDB(containingArea.getID(), new PlantingSpot(TheGarden.normalizeCoordToGrid(x), TheGarden.normalizeCoordToGrid(y)));

            } else {
                addSingleSpotToPlantingArea();
            }
        } else {
            enableHandleClick();
        }

        drawGarden();
    }

    public void onMouseReleased() {
        if (currentMouseCoord != null) {
            addSelectedSpotsToPlantingArea();
        }
        drawGarden();
    }


    private boolean isAllowedToHandleClick() {
        return this.currentMouseCoord == null;
    }

    private void enableHandleClick() {
        currentMouseCoord = null;
    }

    private PlantingArea getAreaContainingSpotCoords(Point2D gridCoords) {
        PlantingArea area = null;
        for (PlantingArea a : TheGarden.getAreas()) {
            if (a.containsSpotAt(gridCoords)) {
                area = a;
                break;
            }
        }
        if (null == area && this.area.containsSpotAt(gridCoords)) {
            area = this.area;
        }

        return area;
    }

    private Point2D toGridCoords(double x, double y) {
        return new Point2D(TheGarden.normalizeCoordToGrid(x), TheGarden.normalizeCoordToGrid(y));
    }

    private void addSingleSpotToPlantingArea() {
        assert currentMouseMoveCoordEndRec != null;
        for (int i = (int) currentMouseMoveCoordStartRec.getX(); i < currentMouseMoveCoordEndRec.getX(); i++) {
            for (int j = (int) currentMouseMoveCoordStartRec.getY(); j < currentMouseMoveCoordEndRec.getY(); j++) {
                area.addSpot(new Point2D(i, j));
            }
        }
    }

    private void addSelectedSpotsToPlantingArea() {
        double posStartX = mouseDraggingStartCoord.getX();
        double posStartY = mouseDraggingStartCoord.getY();

        double posEndX = this.currentMouseCoord.getX();
        double posEndY = this.currentMouseCoord.getY();

        if (posStartX > posEndX) {
            posStartX = this.currentMouseCoord.getX();
            posEndX = mouseDraggingStartCoord.getX();
        }
        if (posStartY > posEndY) {
            posStartY = this.currentMouseCoord.getY();
            posEndY = mouseDraggingStartCoord.getY();
        }

        for (int x = TheGarden.normalizeCoordToGrid(posStartX); x <= TheGarden.normalizeCoordToGrid(posEndX); ++x) {
            for (int y = TheGarden.normalizeCoordToGrid(posStartY); y <= TheGarden.normalizeCoordToGrid(posEndY); ++y) {
                area.addSpot(new PlantingSpot(x, y));
            }
        }
    }


    public void drawGarden() {
        drawGrid();
        drawSelectionRect();
        drawPlantingAreas();
        drawPlantingArea(area);
        if (currentMouseMoveCoordStartRec != null) {
            drawHoverRect();
        }
    }

    private void drawGrid() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.beginPath();

        gc.setFill(Paint.valueOf("#847743"));
        gc.fillRect(0, 0, this.TheGarden.getWidth(), this.TheGarden.getHeight());

        gc.setStroke(Paint.valueOf("#625932"));

        for (int i = 0; i <= this.TheGarden.getHeight(); i += TheGarden.getGridSize()) {
            gc.strokeLine(0, i, this.TheGarden.getWidth(), i);
        }

        for (int i = 0; i <= this.TheGarden.getWidth(); i += TheGarden.getGridSize()) {
            gc.strokeLine(i, 0, i, this.TheGarden.getHeight());
        }

    }

    private void drawSelectionRect() {
        if (shouldDrawSelectionRect()) {
            GraphicsContext gc = getGraphicsContext2D();
            gc.setFill(new Color(0.5, 0.5, 0.5, 0.5));

            gc.beginPath();
            double posStartX = mouseDraggingStartCoord.getX();
            double posStartY = mouseDraggingStartCoord.getY();

            double posEndX = this.currentMouseCoord.getX();
            double posEndY = this.currentMouseCoord.getY();

            if (posStartX > posEndX) {
                posStartX = this.currentMouseCoord.getX();
                posEndX = mouseDraggingStartCoord.getX();
            }
            if (posStartY > posEndY) {
                posStartY = this.currentMouseCoord.getY();
                posEndY = mouseDraggingStartCoord.getY();
            }
            gc.fillRect(posStartX, posStartY, posEndX - posStartX, posEndY - posStartY);
        }
    }

    private boolean shouldDrawSelectionRect() {
        return this.currentMouseCoord != null;
    }

    private void drawPlantingAreas() {
        for (PlantingArea area : TheGarden.getAreas()) {
            drawPlantingArea(area);
        }
    }

    private void drawPlantingArea(PlantingArea area) {
        if (area != null) {
            GraphicsContext gc = getGraphicsContext2D();
            double gSize = TheGarden.getGridSize();
            for (PlantingSpot s : area.getSpots()) {
                gc.setFill(area.getItem().getColor());
                gc.fillRect(s.getX() * gSize + 1, s.getY() * gSize + 1, gSize - 2, gSize - 2);
            }
        }
    }

    private void drawHoverRect() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(new Color(0.136, 0.232, 0.136, 0.67));
        gc.beginPath();

        double posStartX = currentMouseMoveCoordStartRec.getX();
        double posStartY = currentMouseMoveCoordStartRec.getY();
        int g = TheGarden.getGridSize();
        if (area.getItem().getVariety_ID() == null) {
            gc.fillRect(posStartX * g, posStartY * g, g, g);
            this.currentMouseMoveCoordEndRec = new Point2D(posStartX + 1, posStartY + 1);
        } else {
            VarietyDAO dao = new VarietyDAO();
            Variety v = dao.load(area.getItem().getVariety_ID());
            if (hoverwidth == 0 && hoverlength == 0){
                this.hoverlength = TheGarden.normalizeGrid(v.getPlantSpacing());
                this.hoverwidth = TheGarden.normalizeGrid(v.getRowSpacing());
            }
            gc.fillRect(posStartX * g, posStartY * g, hoverlength, hoverwidth);
            if (turned) {
                this.currentMouseMoveCoordEndRec = new Point2D(posStartX + normalizeCoordToArea(v.getRowSpacing()), posStartY + normalizeCoordToArea(v.getPlantSpacing()));
            } else {
                this.currentMouseMoveCoordEndRec = new Point2D(posStartX + normalizeCoordToArea(v.getPlantSpacing()), posStartY + normalizeCoordToArea(v.getRowSpacing()));
            }
        }
    }

    private int normalizeCoordToArea(double n) {
        if (n % TheGarden.getGridSize() == 0) {
            return (int) n / TheGarden.getGridSize();
        } else {
            return (int) n / TheGarden.getGridSize() + 1;
        }
    }

    public void turnHoverRect() {
        int turner = hoverlength;
        hoverlength = hoverwidth;
        hoverwidth = turner;
        if (turned) {
            turned = false;
        } else {
            turned = true;
        }
    }
}

// TODO Sobald Area ohne Spots, muss Area archiviert werden
