package au.id.jaysee.utils;



import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;

public class EnumUtils {
    public static boolean isSign(String blockName) {
        return Arrays.stream(Material.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equalsIgnoreCase(blockName) && blockName.endsWith("_SIGN"));
    }
    public static boolean isDye(String blockName) {
        return Arrays.stream(Material.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equalsIgnoreCase(blockName) && blockName.endsWith("_DYE"));
    }
    public static boolean isWallSign(String blockName) {
        return Arrays.stream(Material.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equalsIgnoreCase(blockName) && blockName.endsWith("_WALL_SIGN"));
    }
}