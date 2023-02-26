package me.yellowstrawberry.privatestorage;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class InventoryParser {
    public String asBase64(Inventory inv) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BukkitObjectOutputStream data = new BukkitObjectOutputStream(out);

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack itm = inv.getItem(i);
            if(itm != null) data.writeObject(itm.serializeAsBytes());
            else data.writeObject(null);
        }

        data.close();
        return Base64Coder.encodeLines(out.toByteArray());
    }

    public Inventory parse(String base64) throws IOException, ClassNotFoundException {
        Inventory inv = Bukkit.createInventory(null, 27);

        ByteArrayInputStream ipt = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
        BukkitObjectInputStream data = new BukkitObjectInputStream(ipt);

        for (int i = 0; i < inv.getSize(); i++) {
            byte[] itm = (byte[]) data.readObject();

            if (itm != null) inv.setItem(i, ItemStack.deserializeBytes(itm));
        }

        data.close();
        return inv;
    }


}
