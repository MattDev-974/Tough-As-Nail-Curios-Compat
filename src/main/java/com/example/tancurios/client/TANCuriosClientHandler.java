package com.example.tancurios.client;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Enregistre le renderer Curios pour les armures TAN côté client.
 */
@EventBusSubscriber(modid = TanCuriosMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TANCuriosClientHandler {

    @SubscribeEvent
    public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TanCuriosMod.LOGGER.info("[TAN-Curios] Enregistrement des renderers Curios pour armures TAN...");

            // Enregistrer le renderer pour chaque armure TAN
            BuiltInRegistries.ITEM.entrySet().stream()
                .filter(e -> e.getKey().location().getNamespace().equals("toughasnails"))
                .filter(e -> e.getValue() instanceof ArmorItem)
                .forEach(e -> {
                    CuriosRendererRegistry.register(e.getValue(), TANCuriosRenderer::new);
                    TanCuriosMod.LOGGER.debug("[TAN-Curios] Renderer enregistré pour {}",
                        e.getKey().location());
                });

            TanCuriosMod.LOGGER.info("[TAN-Curios] Renderers enregistrés.");
        });
    }
}
