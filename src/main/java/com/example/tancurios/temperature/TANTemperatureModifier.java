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

            // Évite le double comptage si la même pièce d'armure (par type)
            // est aussi équipée dans le slot vanilla correspondant : TAN
            // applique déjà son propre effet thermique pour celle-ci.
            if (stack.getItem() instanceof ArmorItem curiosArmor && isAlsoWornVanilla(player, curiosArmor)) {
                continue;
            }

            if (itemId.contains("wool") || itemId.contains("fur") || itemId.contains("hide") || itemId.contains("thermal")) {
                warmingPieces++;
            } else if (itemId.contains("leaf") || itemId.contains("cooling") || itemId.contains("straw")) {
                coolingPieces++;
            }
        }

        if (warmingPieces == 0 && coolingPieces == 0) return current;

        final int MAX_PIECES = 4;
        warmingPieces = Math.min(warmingPieces, MAX_PIECES);
        coolingPieces = Math.min(coolingPieces, MAX_PIECES);

        TanCuriosMod.LOGGER.debug("[TAN-Curios] Modificateur température: +chaud={} +froid={} base={}",
                warmingPieces, coolingPieces, current);

        // TAN ne décale pas de niveaux directement : il modifie le délai de
        // changement (ticks avant qu'un cran de TemperatureLevel change).
        // Notre IPlayerTemperatureModifier ne peut retourner qu'un niveau
        // cible, donc on simule un effet similaire en n'agissant QUE dans
        // les zones hostiles, comme le ferait une armure équipée normalement :
        //
        //  - Laine (warming) : pousse vers NEUTRAL uniquement si le joueur
        //    est en ICY ou COLD. En zone neutre ou chaude, aucun effet
        //    (on ne surchauffe pas avec une veste en laine par 30°C).
        //
        //  - Feuille (cooling) : pousse vers NEUTRAL uniquement si le joueur
        //    est en WARM ou HOT. En zone neutre ou froide, aucun effet.
        //
        // Le décalage reste limité à 1 niveau maximum (1-2 pièces) ou
        // 2 niveaux (3-4 pièces) pour éviter de sauter trop vite.

        TemperatureLevel[] values = TemperatureLevel.values();
        int coldEnd = findColdEndIndex(values);
        int hotEnd = (coldEnd == 0) ? values.length - 1 : 0;
        int directionTowardHot = (hotEnd > coldEnd) ? 1 : -1;
        int neutral = values.length / 2; // index NEUTRAL (2 sur 5)

        int ordinal = current.ordinal();
        int netWarmth = warmingPieces - coolingPieces;
        int magnitude = Math.abs(netWarmth);
        int maxShift = (magnitude <= 2) ? 1 : 2;

        if (netWarmth > 0) {
            // Pièces chauffantes : n'agir que si on est dans la moitié froide
            boolean inColdZone = (directionTowardHot > 0)
                ? ordinal < neutral
                : ordinal > neutral;
            if (!inColdZone) return current;
            ordinal += directionTowardHot * maxShift;
            // Ne pas dépasser NEUTRAL
            if (directionTowardHot > 0) ordinal = Math.min(ordinal, neutral);
            else ordinal = Math.max(ordinal, neutral);
        } else if (netWarmth < 0) {
            // Pièces refroidissantes : n'agir que si on est dans la moitié chaude
            boolean inHotZone = (directionTowardHot > 0)
                ? ordinal > neutral
                : ordinal < neutral;
            if (!inHotZone) return current;
            ordinal -= directionTowardHot * maxShift;
            // Ne pas dépasser NEUTRAL dans l'autre sens
            if (directionTowardHot > 0) ordinal = Math.max(ordinal, neutral);
            else ordinal = Math.min(ordinal, neutral);
        } else {
            return current; // pièces chaudes et froides s'annulent
        }

        ordinal = Math.max(0, Math.min(values.length - 1, ordinal));
        return values[ordinal];
    }

    /**
     * Retourne l'index, dans TemperatureLevel.values(), du niveau le plus
     * froid, en se basant sur le nom de la constante plutôt que sur sa
     * position. On ne fait aucune hypothèse sur l'ordre de déclaration côté
     * Tough As Nails, qui peut différer d'une version à l'autre.
     */
    private static int findColdEndIndex(TemperatureLevel[] values) {
        for (int i = 0; i < values.length; i++) {
            String name = values[i].name().toUpperCase();
            if (name.contains("ICY") || name.contains("COLD") || name.contains("FREEZ")) {
                return i;
            }
        }
        // Si aucun nom ne correspond à un repère froid connu, on suppose
        // (comme avant) que l'index 0 est le plus froid.
        return 0;
    }

    private boolean isAlsoWornVanilla(Player player, ArmorItem curiosArmor) {
        var equipmentSlot = switch (curiosArmor.getType()) {
            case HELMET     -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            case CHESTPLATE -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case LEGGINGS   -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case BOOTS      -> net.minecraft.world.entity.EquipmentSlot.FEET;
            default         -> null;
        };
        if (equipmentSlot == null) return false;

        ItemStack vanillaStack = player.getItemBySlot(equipmentSlot);
        if (vanillaStack.isEmpty() || !(vanillaStack.getItem() instanceof ArmorItem vanillaArmor)) return false;
        if (vanillaArmor.getType() != curiosArmor.getType()) return false;

        return isFromTAN(vanillaStack);
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
