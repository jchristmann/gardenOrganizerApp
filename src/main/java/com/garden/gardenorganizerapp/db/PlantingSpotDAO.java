package com.garden.gardenorganizerapp.db;

import com.garden.gardenorganizerapp.GardenApplication;
import com.garden.gardenorganizerapp.dataobjects.PlantingSpot;
import javafx.scene.paint.Color;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class PlantingSpotDAO {

    public boolean store(PlantingSpot s)
    {
        if(DBConnection.isIdValid(s.getID())) {
            return updateExistingSpot(s);
        }
        else {
            return insertNewSpot(s);
        }
    }

    private boolean updateExistingSpot(PlantingSpot s) {
        DBConnection c = GardenApplication.getDBConnection();
        String sql = "UPDATE PlantingSpot SET x = " + s.getX() + ", y = " + s.getY() + ", Color = '" + s.getColor().toString() + "' WHERE ID = " + s.getID();
        if(c.query(sql))
        {
            return true;
        }
        return false;
    }

    private boolean insertNewSpot(PlantingSpot s)
    {
        DBConnection c = GardenApplication.getDBConnection();
        String sql = "INSERT INTO PlantingSpot (x, y, Color, PlantingArea_ID) VALUES (" + s.getX() + ", " + s.getY() + ", '" + s.getColor().toString() + "', " + s.getPlantingAreaId() +");";

        int id = c.insertQuery(sql);
        s.setID(id);
        return c.isIdValid(id);
    }

    public PlantingSpot load(int spotId)
    {
        DBConnection c = GardenApplication.getDBConnection();

        PlantingSpot spot = null;

        String sql = "SELECT PlantingArea_ID, x, y, Color FROM PlantingSpot WHERE ID = " + spotId + ";";
        try {
            Statement s = c.getConnection().createStatement();
            ResultSet res = s.executeQuery(sql);

            if(res.next())
            {
                spot = new PlantingSpot(res.getInt("x"), res.getInt("y"), Color.valueOf(res.getString("Color")));
                spot.setPlantingAreaId(res.getInt("PlantingArea_ID"));
                spot.setID(spotId);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return spot;
    }

    public Vector<PlantingSpot> loadForArea(int areaId)
    {
        DBConnection c = GardenApplication.getDBConnection();

        Vector<PlantingSpot> spots = new Vector<PlantingSpot>();

        String sql = "SELECT ID FROM PlantingSpot WHERE PlantingArea_ID = " + areaId + ";";
        try {
            Statement s = c.getConnection().createStatement();
            ResultSet res = s.executeQuery(sql);

            while(res.next())
            {
                spots.add(load(res.getInt("ID")));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return spots;
    }
}
