package com.example.tancurios;

import com.example.tancurios.temperature.TANTemperatureModifier;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import toughasnails.api.temperature.TemperatureHelper;

@Mod(TanCuriosMod.MOD_ID)
public class TanCuriosMod {

    public static final String MOD_ID = "tancurios";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TanCuriosMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[TAN-Curios] Initialisation...");
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Enregistrer le modifier de température
            TemperatureHelper.registerPlayerTemperatureModifier(new TANTemperatureModifier());
            LOGGER.info("[TAN-Curios] Modificateur de température TAN enregistré.");
        });
    }
}
