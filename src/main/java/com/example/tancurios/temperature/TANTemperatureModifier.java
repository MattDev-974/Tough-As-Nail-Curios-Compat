package com.example.tancurios.temperature;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import toughasnails.api.temperature.IPlayerTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Modifie la température du joueur en tenant compte des armures TAN
 * équipées dans les slots Curios.
 */
public class TANTemperatureModifier implements IPlayerTemperatureModifier {

    @Override
    public TemperatureLevel modify(Player player, TemperatureLevel current) {
        // Récupérer les armures TAN dans les slots Curios
        var curiosHandler = CuriosApi.getCuriosInventory(player);
        if (curiosHandler.isEmpty()) return current;

        int warmingPieces = 0;  // armures qui réchauffent (wool, fur, hide)
        int coolingPieces = 0;  // armures qui refroidissent (leaf, cooling)

        var stacks = curiosHandler.get().findCurios(stack ->
            stack.getItem() instanceof ArmorItem &&
            isFromTAN(stack)
        );

        for (var slotResult : stacks) {
            ItemStack stack = slotResult.stack();
            String itemId = getItemId(stack);

            if (itemId.contains("wool") || itemId.contains("fur") || itemId.contains("hide") || itemId.contains("thermal")) {
                warmingPieces++;
            } else if (itemId.contains("leaf") || itemId.contains("cooling") || itemId.contains("straw")) {
                coolingPieces++;
            }
        }

        if (warmingPieces == 0 && coolingPieces == 0) return current;

        TanCuriosMod.LOGGER.debug("[TAN-Curios] Modificateur température: +chaud={} +froid={} base={}",
                warmingPieces, coolingPieces, current);

        // Appliquer les modifications de température
        // Chaque pièce chaude décale d'un niveau vers le chaud
        // Chaque pièce froide décale d'un niveau vers le froid
        int ordinal = current.ordinal();
        ordinal += warmingPieces;
        ordinal -= coolingPieces;

        // Clamp entre les valeurs valides de TemperatureLevel
        TemperatureLevel[] values = TemperatureLevel.values();
        ordinal = Math.max(0, Math.min(values.length - 1, ordinal));

        return values[ordinal];
    }

    private boolean isFromTAN(ItemStack stack) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getNamespace().equals("toughasnails");
    }

    private String getItemId(ItemStack stack) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null ? key.getPath() : "";
    }
}
