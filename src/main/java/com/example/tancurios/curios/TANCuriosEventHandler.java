package com.example.tancurios.curios;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;

/**
 * Enregistre la capability ICurio sur toutes les armures TAN.
 */
@EventBusSubscriber(modid = TanCuriosMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TANCuriosEventHandler {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        TanCuriosMod.LOGGER.info("[TAN-Curios] Enregistrement des capabilities Curios pour les armures TAN...");

        Item[] tanArmors = BuiltInRegistries.ITEM.entrySet().stream()
            .filter(e -> e.getKey().location().getNamespace().equals("toughasnails"))
            .filter(e -> e.getValue() instanceof ArmorItem)
            .map(java.util.Map.Entry::getValue)
            .toArray(Item[]::new);

        TanCuriosMod.LOGGER.info("[TAN-Curios] {} armures TAN trouvées.", tanArmors.length);

        if (tanArmors.length == 0) {
            TanCuriosMod.LOGGER.warn("[TAN-Curios] Aucune armure TAN trouvée ! Vérifiez que Tough As Nails est bien installé.");
            return;
        }

        event.registerItem(
            CuriosCapability.ITEM,
            (stack, ctx) -> new TANCuriosItem(stack),
            tanArmors
        );

        TanCuriosMod.LOGGER.info("[TAN-Curios] Enregistrement terminé.");
    }
}
