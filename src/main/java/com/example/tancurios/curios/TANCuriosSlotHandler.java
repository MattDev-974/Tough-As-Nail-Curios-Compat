package com.example.tancurios.curios;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class TANCuriosSlotHandler {

    public static boolean canFitInSlot(ItemStack stack, String slotId) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) return false;
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null || !key.getNamespace().equals("toughasnails")) return false;

        return switch (armorItem.getType()) {
            case HELMET   -> slotId.equals("head");
            case CHESTPLATE -> slotId.equals("body");
            case LEGGINGS -> slotId.equals("legs");
            case BOOTS    -> slotId.equals("feet");
            default       -> false;
        };
    }
}
