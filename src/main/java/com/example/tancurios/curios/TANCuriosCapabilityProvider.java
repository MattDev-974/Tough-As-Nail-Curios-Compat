package com.example.tancurios.curios;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class TANCuriosCapabilityProvider {

    public static boolean isTANArmorItem(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) return false;
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getNamespace().equals("toughasnails");
    }

    public static TANCuriosItem createCurio(ItemStack stack) {
        return new TANCuriosItem(stack);
    }
}
