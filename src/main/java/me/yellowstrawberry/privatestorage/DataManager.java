package me.yellowstrawberry.privatestorage;

import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.sql.*;

public class DataManager {

    private final Connection con;
    private final InventoryParser inventoryParser = new InventoryParser();

    public DataManager() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/server",
                    "root", "root"
            );
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveInventory(Inventory inv, String uuid) {
        if(isInventoryExist(uuid)) {
            updateInventory(inv, uuid);
        }else {
            addInventory(inv, uuid);
        }
    }

    private void updateInventory(Inventory inv, String uuid) {
        try(PreparedStatement statement = con.prepareStatement("UPDATE inventories SET data=? WHERE id=?;")){
            statement.setString(1, inventoryParser.asBase64(inv));
            statement.setString(2, uuid);
            statement.executeUpdate();
        }catch (SQLException | IOException e){
            throw new RuntimeException(e);
        }
    }

    private void addInventory(Inventory inv, String uuid) {
        try(PreparedStatement statement = con.prepareStatement("INSERT INTO inventories(id, data) VALUES (?, ?);")){
            statement.setString(1, uuid);
            statement.setString(2, inventoryParser.asBase64(inv));
            statement.executeUpdate();
        }catch (SQLException | IOException e){
            throw new RuntimeException(e);
        }
    }

    private boolean isInventoryExist(String uuid) {
        try(PreparedStatement statement = con.prepareStatement("SELECT 1 FROM inventories WHERE id=?;")) {
            statement.setString(1, uuid);
            ResultSet set = statement.executeQuery();
            return set.first();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Inventory loadInventory(String id) {
        try(PreparedStatement statement = con.prepareStatement("SELECT data FROM inventories WHERE id=?;")){
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.first()) return null;
            return inventoryParser.parse(resultSet.getString("data"));
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
