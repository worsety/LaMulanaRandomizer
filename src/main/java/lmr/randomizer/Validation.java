package lmr.randomizer;

import lmr.randomizer.node.*;
import lmr.randomizer.random.ItemRandomizer;
import lmr.randomizer.random.ShopRandomizationEnum;
import lmr.randomizer.random.TransitionGateRandomizer;
import lmr.randomizer.update.LocationCoordinateMapper;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class Validation {
    public static boolean validateCustomPlacements(Main.RandomizerUI randomizerUI) {
        CustomPlacementData customPlacementData = DataFromFile.getCustomPlacementData();

        List<String> locations = new ArrayList<>();
        List<String> items = new ArrayList<>();
        for (var weapon : customPlacementData.getStartingWeapons()) {
            if(Settings.getStartingItemsIncludingCustom().contains(weapon)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Custom starting weapon cannot be the same as starting item" + weapon + " not valid with current settings for starting item",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(customPlacementData.getRemovedItems().contains(weapon)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Cannot remove starting weapon",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(ItemRandomizer.ALL_SUBWEAPONS.contains(weapon)) {
                if(!Settings.isAllowSubweaponStart()) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom starting weapon not enabled",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else if(DataFromFile.MAIN_WEAPONS.contains(weapon)) {
                if("Whip".equals(weapon)) {
                    if(!Settings.isAllowWhipStart()) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Custom starting weapon not enabled",
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                else if(!Settings.isAllowMainWeaponStart()) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom starting weapon not enabled",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

            }
            else {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Invalid starting weapon: " + weapon,
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if(customPlacementData.getStartingLocation() != null) {
            if(!Settings.isRandomizeStartingLocation()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("Please enable \"%s\"", Translations.getText("randomization.randomizeStartingLocation")),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!DataFromFile.STARTING_LOCATIONS.contains(customPlacementData.getStartingLocation())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Invalid starting location: " + LocationCoordinateMapper.getStartingZoneName(customPlacementData.getStartingLocation()),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeTransitionGates()) {
                if(customPlacementData.getStartingLocation() == 11
                        || customPlacementData.getStartingLocation() == 14
                        || customPlacementData.getStartingLocation() == 16
                        || customPlacementData.getStartingLocation() == 21) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            String.format("Please enable \"%s\"", Translations.getText("randomization.randomizeTransitionGates")),
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        if(!customPlacementData.getCustomDoorPlacements().isEmpty()) {
            if(!Settings.isRandomizeBacksideDoors()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Please enable the setting \"" + Translations.getText("randomization.randomizeBacksideDoors") + "\"",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Map<String, String> placedDoorsAndDestinations = new HashMap<>();
            Map<String, Integer> placedDoorsAndBosses = new HashMap<>();
            for(CustomDoorPlacement customDoorPlacement : customPlacementData.getCustomDoorPlacements()) {
                if(!customDoorPlacement.getTargetDoor().startsWith("Door F") && !customDoorPlacement.getTargetDoor().startsWith("Door B")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Backside door " + customDoorPlacement.getTargetDoor() + " is invalid",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!customDoorPlacement.getDestinationDoor().startsWith("Door F") && !customDoorPlacement.getDestinationDoor().startsWith("Door B")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Backside door " + customDoorPlacement.getDestinationDoor() + " is invalid",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!Settings.isRandomizeNonBossDoors()) {
                    if(isNonBossDoor(customDoorPlacement.getTargetDoor())) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Please enable the setting \"" + Translations.getText("randomization.randomizeNonBossDoors") + "\"",
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if(isNonBossDoor(customDoorPlacement.getDestinationDoor())) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Please enable the setting \"" + Translations.getText("randomization.randomizeNonBossDoors") + "\"",
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                if(placedDoorsAndDestinations.keySet().contains(customDoorPlacement.getTargetDoor())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Backside door " + customDoorPlacement.getTargetDoor() + " cannot be assigned multiple destinations",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(placedDoorsAndDestinations.values().contains(customDoorPlacement.getDestinationDoor())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Multiple backside doors cannot lead to destination door " + customDoorPlacement.getDestinationDoor(),
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(customDoorPlacement.getTargetDoor().equals(customDoorPlacement.getDestinationDoor())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Backside door " + customDoorPlacement.getTargetDoor() + " cannot lead to itself",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(customDoorPlacement.getAssignedBoss() != null
                        && customDoorPlacement.getAssignedBoss() != 9 && (customDoorPlacement.getAssignedBoss() < 1 || customDoorPlacement.getAssignedBoss() > 7)) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Assigned boss for backside door " + customDoorPlacement.getTargetDoor() + " could not be processed; please use boss name or numbers 1-7",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(placedDoorsAndDestinations.values().contains(customDoorPlacement.getTargetDoor())) {
                    if(!customDoorPlacement.getTargetDoor().equals(placedDoorsAndDestinations.get(customDoorPlacement.getDestinationDoor()))) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Support for non-reversible backside door placement does not exist at this time; please update assignment for " + customDoorPlacement.getTargetDoor() + " or " + customDoorPlacement.getDestinationDoor(),
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                if(placedDoorsAndDestinations.keySet().contains(customDoorPlacement.getDestinationDoor())) {
                    if(!customDoorPlacement.getTargetDoor().equals(placedDoorsAndDestinations.get(customDoorPlacement.getDestinationDoor()))) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Support for non-reversible backside door placement does not exist at this time; please update assignment for " + customDoorPlacement.getTargetDoor() + " or " + customDoorPlacement.getDestinationDoor(),
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                Integer existingBoss = placedDoorsAndBosses.get(customDoorPlacement.getDestinationDoor());
                if(existingBoss != null && !existingBoss.equals(customDoorPlacement.getAssignedBoss())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "A pair of backside doors cannot be assigned two different bosses at this time; please update assignment for " + customDoorPlacement.getTargetDoor() + " or " + customDoorPlacement.getDestinationDoor(),
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if((customDoorPlacement.getAssignedBoss() != null && customDoorPlacement.getAssignedBoss() > 0 && customDoorPlacement.getAssignedBoss() < 9)
                        && (customDoorPlacement.getTargetDoor().endsWith("B8") || customDoorPlacement.getDestinationDoor().endsWith("B8"))) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Dimensional Corridor may not be paired with a Bronze Mirror door",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if((customDoorPlacement.getAssignedBoss() != null && customDoorPlacement.getAssignedBoss() > 0 && customDoorPlacement.getAssignedBoss() < 9)
                        && (customDoorPlacement.getTargetDoor().endsWith("B9") || customDoorPlacement.getDestinationDoor().endsWith("B9"))) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Gate of Time may not be paired with a Bronze Mirror door",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                placedDoorsAndDestinations.put(customDoorPlacement.getTargetDoor(), customDoorPlacement.getDestinationDoor());
                placedDoorsAndBosses.put(customDoorPlacement.getTargetDoor(), customDoorPlacement.getAssignedBoss());
                placedDoorsAndBosses.put(customDoorPlacement.getDestinationDoor(), customDoorPlacement.getAssignedBoss());
            }
        }
        if(!customPlacementData.getCustomTransitionPlacements().isEmpty()) {
            if(!Settings.isRandomizeTransitionGates()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Please enable the setting \"" + Translations.getText("randomization.randomizeTransitionGates") + "\"",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Map<String, String> placedTargetAndDestination = new HashMap<>();
            for(CustomTransitionPlacement customTransitionPlacement : customPlacementData.getCustomTransitionPlacements()) {
                if(!isValidTransition(customTransitionPlacement.getTargetTransition())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Transition " + customTransitionPlacement.getTargetTransition().replaceAll("^Transition:? ", "") + " is invalid",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!isValidTransition(customTransitionPlacement.getDestinationTransition())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Transition " + customTransitionPlacement.getDestinationTransition().replaceAll("^Transition:? ", "") + " is invalid",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!isValidTransitionDirection(customTransitionPlacement.getTargetTransition(), customTransitionPlacement.getDestinationTransition())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Transition " + customTransitionPlacement.getDestinationTransition().replaceAll("^Transition:? ", "") + " is the wrong direction for " + customTransitionPlacement.getTargetTransition().replaceAll("^Transition:? ", ""),
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!Settings.isRandomizeOneWayTransitions()) {
                    if(isOneWayTransition(customTransitionPlacement.getTargetTransition())) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Please enable the setting \"" + Translations.getText("randomization.randomizeOneWayTransitions") + "\"",
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if(isOneWayTransition(customTransitionPlacement.getDestinationTransition())) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Please enable the setting \"" + Translations.getText("randomization.randomizeOneWayTransitions") + "\"",
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                if(placedTargetAndDestination.keySet().contains(customTransitionPlacement.getTargetTransition())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Transition " + customTransitionPlacement.getTargetTransition().replaceAll("^Transition:? ", "") + " cannot be assigned multiple destinations",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(placedTargetAndDestination.values().contains(customTransitionPlacement.getDestinationTransition())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Multiple transitions cannot lead to destination transition " + customTransitionPlacement.getDestinationTransition().replaceAll("^Transition:? ", ""),
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(customTransitionPlacement.getTargetTransition().equals(customTransitionPlacement.getDestinationTransition())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Transition " + customTransitionPlacement.getTargetTransition().replaceAll("^Transition:? ", "") + " cannot lead to itself",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(placedTargetAndDestination.values().contains(customTransitionPlacement.getTargetTransition())) {
                    if(!customTransitionPlacement.getTargetTransition().equals(placedTargetAndDestination.get(customTransitionPlacement.getDestinationTransition()))) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Support for non-reversible transition placement does not exist at this time; please update assignment for " + customTransitionPlacement.getTargetTransition().replaceAll("^Transition:? ", "") + " or " + customTransitionPlacement.getDestinationTransition().replaceAll("^Transition:? ", ""),
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                if(placedTargetAndDestination.keySet().contains(customTransitionPlacement.getDestinationTransition())) {
                    if(!customTransitionPlacement.getTargetTransition().equals(placedTargetAndDestination.get(customTransitionPlacement.getDestinationTransition()))) {
                        JOptionPane.showMessageDialog(randomizerUI,
                                "Support for non-reversible backside door placement does not exist at this time; please update assignment for " + customTransitionPlacement.getTargetTransition().replaceAll("^Transition:? ", "") + " or " + customTransitionPlacement.getDestinationTransition().replaceAll("^Transition:? ", ""),
                                "Custom placement error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                placedTargetAndDestination.put(customTransitionPlacement.getTargetTransition(), customTransitionPlacement.getDestinationTransition());
            }
        }
        if(!customPlacementData.getCustomNPCPlacements().isEmpty()) {
            if(!Settings.isRandomizeNpcs()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Please enable the setting \"" + Translations.getText("randomization.randomizeNpcs") + "\"",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            for(CustomNPCPlacement customNPCPlacement : customPlacementData.getCustomNPCPlacements()) {
                if(!isValidNpc(customNPCPlacement.getNpcLocation())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "NPC " + customNPCPlacement.getNpcLocation().replaceAll("^NPCL?:? ", "") + " is invalid",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(!isValidNpc(customNPCPlacement.getNpcDoorContents())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "NPC " + customNPCPlacement.getNpcLocation().replaceAll("^NPCL?:? ", "") + " is invalid",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if("Tailor Dracuet".equals(customNPCPlacement.getNpcLocation()) && !Settings.isRandomizeDracuetShop()) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Please enable the setting \"" + Translations.getText("randomization.randomizeDracuetShop") + "\"",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if("Tailor Dracuet".equals(customNPCPlacement.getNpcDoorContents()) && !Settings.isRandomizeDracuetShop()) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Please enable the setting \"" + Translations.getText("randomization.randomizeDracuetShop") + "\"",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        if(Settings.isRequireFullAccess() && !customPlacementData.getRemovedItems().isEmpty()) {
            if(Settings.isRequireFullAccess()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Please disable the setting \"Require all items to be accessible\"",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        for(String customRemovedItem : customPlacementData.getRemovedItems()) {
            if(Settings.getStartingItems().contains(customRemovedItem)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Custom placement of " + customRemovedItem + " not valid with current settings for starting item",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(customRemovedItem.startsWith("Coin:")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Coin chests cannot be removed items",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(customRemovedItem.startsWith("Trap:")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Traps cannot be removed items",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(Settings.isRequireIceCapeForLava() && customRemovedItem.equals("Ice Cape")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Please disable the setting \"Require Ice Cape for swimming through lava\"",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(Settings.isRequireFlaresForExtinction() && customRemovedItem.equals("Flare Gun")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Please disable the setting \"Require Flare Gun for Chamber of Extinction\"",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!DataFromFile.getRandomRemovableItems().contains(customRemovedItem)
                    && !"Whip".equals(customRemovedItem)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        customRemovedItem + " cannot be a removed item",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(Settings.isHalloweenMode() && customRemovedItem.equals("Provocative Bathing Suit")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("Custom placement of \"%s\" cannot be used with this mode",
                                Translations.getText("items.ProvocativeBathingSuit")),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(!isValidContents(customRemovedItem)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Removed item not valid: " + customRemovedItem,
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        for(String customStartingItem : customPlacementData.getStartingItems()) {
            if (!isValidContents(customStartingItem)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Starting item not valid: " + customStartingItem,
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(Settings.isHalloweenMode() && customStartingItem.equals("Provocative Bathing Suit")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("Custom placement of \"%s\" cannot be used with this mode",
                                Translations.getText("items.ProvocativeBathingSuit")),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        for(String cursedChestLocation : customPlacementData.getCursedChests()) {
            if(!Settings.isRandomizeCursedChests()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Custom placement of cursed chest not valid with current settings for cursed chest randomization",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(DataFromFile.FLOATING_ITEM_LOCATIONS.contains(cursedChestLocation)
                    || DataFromFile.LOCATIONS_RELATED_TO_BLOCKS.contains(cursedChestLocation)
                    || "mantra.exe".equals(cursedChestLocation)
                    || "emusic.exe".equals(cursedChestLocation)
                    || "beolamu.exe".equals(cursedChestLocation)) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Non-chest location " + cursedChestLocation + " cannot be cursed",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        for(CustomItemPlacement customItemPlacement : customPlacementData.getCustomItemPlacements()) {
            if(Settings.getStartingItems().contains(customItemPlacement.getContents())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Custom placement of " + customItemPlacement.getContents() + " not valid with current settings for starting item",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(locations.contains(customItemPlacement.getLocation())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Location used for multiple items: " + customItemPlacement.getLocation(),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(items.contains(customItemPlacement.getContents())
                    && !"Weights".equals(customItemPlacement.getContents())
                    && !customItemPlacement.getContents().endsWith(" Ammo")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Item placed in multiple locations: " + customItemPlacement.getContents(),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(Settings.isHalloweenMode() && customItemPlacement.getContents().equals("Provocative Bathing Suit")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("Custom placement of \"%s\" cannot be used with this mode",
                                Translations.getText("items.ProvocativeBathingSuit")),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!isValidLocation(customItemPlacement.getLocation())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Location not valid: " + customItemPlacement.getLocation() + " for item " + customItemPlacement.getContents(),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!isValidContents(customItemPlacement.getContents())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Placed item not valid: " + customItemPlacement.getContents() + " at location " + customItemPlacement.getLocation(),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(DataFromFile.SHOP_ITEMS.contains(customItemPlacement.getLocation())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "To place " + customItemPlacement.getLocation() + " in a shop, please use the shop name and item number instead of the item name",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeTrapItems()) {
                if(customItemPlacement.getLocation().startsWith("Trap:")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement at location " + customItemPlacement.getLocation() + " not valid with current settings for randomized traps",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(customItemPlacement.getContents().startsWith("Trap:")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement of item " + customItemPlacement.getContents() + " not valid with current settings for randomized traps",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            if(!Settings.isRandomizeEscapeChest()) {
                if(customItemPlacement.getLocation().equals(DataFromFile.ESCAPE_CHEST_NAME) || customItemPlacement.getContents().startsWith(DataFromFile.ESCAPE_CHEST_NAME)) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement not valid with current settings for randomized escape chest",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            if(!Settings.isRandomizeCoinChests()) {
                if(customItemPlacement.getLocation().startsWith("Coin:")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement at location " + customItemPlacement.getLocation() + " not valid with current settings for randomized coin chests",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(customItemPlacement.getContents().startsWith("Coin:")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement of item " + customItemPlacement.getContents() + " not valid with current settings for randomized coin chests",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            if(!Settings.isRandomizeForbiddenTreasure()) {
                if(customItemPlacement.getLocation().equals("Provocative Bathing Suit")
                        || customItemPlacement.getContents().equals("Provocative Bathing Suit")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement not valid with current settings for Provocative Bathing Suit randomization",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else if(!Settings.isHTFullRandom()) {
                if(customItemPlacement.getLocation().equals("Provocative Bathing Suit")
                        && !customItemPlacement.getContents().startsWith("Map (")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement not valid with current settings for Provocative Bathing Suit randomization",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            if(customItemPlacement.getContents().startsWith("Coin:") || DataFromFile.EXPLODING_CHEST_NAME.equals(customItemPlacement.getContents())) {
                if(DataFromFile.FLOATING_ITEM_LOCATIONS.contains(customItemPlacement.getLocation())
                        || DataFromFile.LOCATIONS_RELATED_TO_BLOCKS.contains(customItemPlacement.getLocation())
                        || "mantra.exe".equals(customItemPlacement.getLocation())
                        || "emusic.exe".equals(customItemPlacement.getLocation())
                        || "beolamu.exe".equals(customItemPlacement.getLocation())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Item " + customItemPlacement.getContents() + " cannot be placed at non-chest location " + customItemPlacement.getLocation(),
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            if(!Settings.isRandomizeDracuetShop() && customItemPlacement.getLocation().startsWith("Shop 23 (HT)")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Custom placement not valid with current settings for Dracuet shop randomization",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if(ShopRandomizationEnum.CATEGORIZED.equals(Settings.getShopRandomization())
                    && customItemPlacement.getLocation().startsWith("Shop ")) {
                if(!DataFromFile.CATEGORIZED_SHOP_ITEM_LOCATIONS.contains(customItemPlacement.getLocation())) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement of item at " + customItemPlacement.getLocation() + " not valid with current settings for shop randomization",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else if("Weights".equals(customItemPlacement.getContents())
                        || customItemPlacement.getContents().endsWith(" Ammo")) {
                    JOptionPane.showMessageDialog(randomizerUI,
                            "Custom placement of " + customItemPlacement.getContents() + " not valid with current settings for shop randomization",
                            "Custom placement error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            if(!Settings.isRandomizeStartingLocation() && customItemPlacement.getLocation().startsWith("Shop 0")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("Please enable the setting \"%s\"", Translations.getText("randomization.randomizeStartingLocation")),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            locations.add(customItemPlacement.getLocation());
            items.add(customItemPlacement.getContents());
        }
        return true;
    }

    public static boolean validateRemovedItems(Main.RandomizerUI randomizerUI) {
        Set<String> manuallyRemovedItems = new HashSet<>(DataFromFile.getCustomPlacementData().getRemovedItems());
        if(Settings.isRemoveMainWeapons()) {
            manuallyRemovedItems.addAll(DataFromFile.MAIN_WEAPONS);
            manuallyRemovedItems.remove("Whip"); // Whip gets special treatment.

            if(!Settings.isAlternateMotherAnkh()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s \" is required when removing Main Weapons", Translations.getText("gameplay.alternateMotherAnkh")),
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isAllowSubweaponStart()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        "Starting with subweapon is required when removing Main Weapons",
                        "Custom placement error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        int ankhJewelsRemoved = 0;
        for (String item : manuallyRemovedItems) {
            if(item.contains("Ankh Jewel")) {
                ankhJewelsRemoved += 1;
            }
        }
        if(!Settings.isFoolsGameplay() && ankhJewelsRemoved > 0) {
            JOptionPane.showMessageDialog(randomizerUI,
                    "Current settings do not allow Ankh Jewels to be removed",
                    "Custom placement error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(Settings.isFoolsGameplay() && ankhJewelsRemoved > 4) {
            JOptionPane.showMessageDialog(randomizerUI,
                    "Current settings do not allow more than 4 Ankh Jewels to be removed",
                    "Custom placement error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if((Settings.getMinRandomRemovedItems() + manuallyRemovedItems.size() > 99)) {
            JOptionPane.showMessageDialog(randomizerUI,
                    "Minimum removed item count is too high with custom placement settings. A minimum of " + (99 - manuallyRemovedItems.size()) + " will be used instead.",
                    "Randomizer error", JOptionPane.WARNING_MESSAGE);
            Settings.setMinRandomRemovedItems(99 - manuallyRemovedItems.size(), false);
        }
        if((Settings.getMaxRandomRemovedItems() + manuallyRemovedItems.size() > 99)) {
            JOptionPane.showMessageDialog(randomizerUI,
                    "Maximum removed item count is too high with custom placement settings. A maximum of " + (99 - manuallyRemovedItems.size()) + " will be used instead.",
                    "Randomizer error", JOptionPane.WARNING_MESSAGE);
            Settings.setMaxRandomRemovedItems(99 - manuallyRemovedItems.size(), false);
        }
        return true;
    }


    public static boolean validateInstallDir(Main.RandomizerUI randomizerUI) {
        if(Settings.getLaMulanaBaseDir() != null && !Settings.getLaMulanaBaseDir().isEmpty()
                && new File(Settings.getLaMulanaBaseDir()).exists()) {
            return true;
        }
        JOptionPane.showMessageDialog(randomizerUI,
                "Unable to find La-Mulana install directory",
                "Randomizer error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public static boolean validateSaveDir(Main.RandomizerUI randomizerUI) {
        if(Settings.getLaMulanaSaveDir() != null && !Settings.getLaMulanaSaveDir().isEmpty()
                        && new File(Settings.getLaMulanaSaveDir()).exists()) {
            return true;
        }
        JOptionPane.showMessageDialog(randomizerUI,
                "Unable to find La-Mulana save directory",
                "Randomizer error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public static boolean validateGraphicsPack(Main.RandomizerUI randomizerUI) {
        if("HALLOWEEN".equals(Settings.getGraphicsPack())) {
            JOptionPane.showMessageDialog(randomizerUI,
                    String.format("HALLOWEEN cannot be used as %s. Please select a folder from which the HALLOWEEN graphics should be created.", Translations.getText("settings.graphicsPack")),
                    "Randomizer error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if("FOOLS2020".equals(Settings.getGraphicsPack())) {
            JOptionPane.showMessageDialog(randomizerUI,
                    String.format("FOOLS2020 cannot be used as %s. Please select a folder from which the FOOLS2020 graphics should be created.", Translations.getText("settings.graphicsPack")),
                    "Randomizer error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static boolean validateSettingCombinations(Main.RandomizerUI randomizerUI) {
        if(!Settings.isAllowWhipStart() && !Settings.isAllowMainWeaponStart() && !Settings.isAllowSubweaponStart()) {
            JOptionPane.showMessageDialog(randomizerUI,
                    "Starting without a weapon is not currently enabled",
                    "Randomizer error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(Settings.isRandomizeEnemies() && Settings.getEnabledDamageBoosts().contains("Enemy")) {
            JOptionPane.showMessageDialog(randomizerUI,
                    String.format("The setting \"%s\" cannot be used with the setting \"%s\"",
                            Translations.getText("enemies.randomizeEnemies"),
                            Translations.getText("dboost.Enemy")),
                    "Randomizer error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(Settings.isRequireFullAccess() && Settings.isRemoveMainWeapons()) {
            JOptionPane.showMessageDialog(randomizerUI,
                    "The setting \"Require all items to be accessible\" cannot be used when removing Main Weapons",
                    "Randomizer error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(Settings.isRandomizeStartingLocation()) {
            if(!ShopRandomizationEnum.EVERYTHING.equals(Settings.getShopRandomization())) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("Please enable %s %s when using %s", Translations.getText("randomization.randomizeShops"), Translations.getText("randomization.randomizeShops.everything"), Translations.getText("randomization.randomizeStartingLocation")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public static boolean validateHalloween(Main.RandomizerUI randomizerUI) {
        if(Settings.isHalloweenMode()){
            if(Settings.isRequireFullAccess()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" cannot be used with this mode",
                                Translations.getText("logic.requireFullAccess.short")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
//                if(!Settings.isRandomizeEnemies()) {
//                    JOptionPane.showMessageDialog(randomizerUI,
//                            String.format("The setting \"%s\" is required for this mode",
//                                    Translations.getText("enemies.randomizeEnemies")),
//                            "Randomizer error", JOptionPane.ERROR_MESSAGE);
//                    return false;
//                }
//                if(!Settings.isRandomizeStartingLocation()) {
//                    JOptionPane.showMessageDialog(randomizerUI,
//                            String.format("The setting \"%s\" is required for this mode",
//                                    Translations.getText("randomization.randomizeStartingLocation")),
//                            "Randomizer error", JOptionPane.ERROR_MESSAGE);
//                    return false;
//                }
            if(Settings.getEnabledDamageBoosts().contains("Enemy")) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" cannot be used with this mode",
                                Translations.getText("dboost.Enemy")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public static boolean validateFools2020(Main.RandomizerUI randomizerUI) {
        if(Settings.isFools2020Mode()) {
            if(Settings.isRequireFullAccess()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" cannot be used with this mode",
                                Translations.getText("logic.requireFullAccess.short")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeTrapItems()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeTrapItems")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeCoinChests()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeCoinChests")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public static boolean validateFools2021(Main.RandomizerUI randomizerUI) {
        if(Settings.isFools2021Mode()) {
            if(Settings.isRequireFullAccess()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" cannot be used with this mode",
                                Translations.getText("logic.requireFullAccess.short")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeCoinChests()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeCoinChests")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeTransitionGates()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeTransitionGates")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeOneWayTransitions()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeOneWayTransitions")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeBacksideDoors()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeBacksideDoors")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(!Settings.isRandomizeNonBossDoors()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is required for this mode",
                                Translations.getText("randomization.randomizeNonBossDoors")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(Settings.isAlternateMotherAnkh()) {
                JOptionPane.showMessageDialog(randomizerUI,
                        String.format("The setting \"%s\" is cannot be used in this mode",
                                Translations.getText("gameplay.alternateMotherAnkh")),
                        "Randomizer error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private static boolean isValidLocation(String location) {
        if(DataFromFile.getAllItems().contains(location)) {
            return true;
        }
        if(DataFromFile.getAllCoinChests().contains(location)
                || DataFromFile.ESCAPE_CHEST_NAME.equals(location)) {
            return true;
        }
        if(DataFromFile.TRAP_ITEMS.contains(location)) {
            return true;
        }
        if(location.startsWith("Shop ")
                && (location.endsWith(" Item 1") || location.endsWith(" Item 2") || location.endsWith(" Item 3"))) {
            for(String shopName : DataFromFile.getAllShops()) {
                if(location.startsWith(shopName)) {
                    return !location.startsWith("Shop 2 Alt (Surface)") || location.endsWith("1");
                }
            }
            if(Settings.isRandomizeStartingLocation() && location.startsWith(DataFromFile.CUSTOM_SHOP_NAME)) {
                // Assumes random starting location that isn't the Surface.
                return true;
            }
        }
        return false;
    }

    private static boolean isValidContents(String contents) {
        if(DataFromFile.getAllItems().contains(contents)
                || "Whip".equals(contents) || "Ankh Jewel (Extra)".equals(contents)) {
            return true;
        }
        if(DataFromFile.getAllCoinChests().contains(contents)
                || DataFromFile.ESCAPE_CHEST_NAME.equals(contents)) {
            return true;
        }
        if(DataFromFile.TRAP_ITEMS.contains(contents)) {
            return true;
        }
        if(contents.equals("Weights") || contents.equals("Shuriken Ammo") || contents.equals("Rolling Shuriken Ammo")
                || contents.equals("Caltrops Ammo") || contents.equals("Chakram Ammo") || contents.equals("Flare Gun Ammo")
                || contents.equals("Earth Spear Ammo") || contents.equals("Pistol Ammo") || contents.equals("Bomb Ammo")) {
            return true;
        }
        return false;
    }

    private static boolean isValidTransition(String transition) {
        String formattedTransition = transition.replace("Transition ", "Transition: ");
        return "Transition: Goddess W1".equals(formattedTransition)
                || "Transition: Inferno W1".equals(formattedTransition)
                || (TransitionGateRandomizer.getTransitionList().contains(formattedTransition)
                && !formattedTransition.startsWith("Transition: Sun R")
                && !formattedTransition.startsWith("Transition: Extinction L"));
    }

    private static boolean isValidNpc(String npcName) {
        return "Nebur".equals(npcName)
                || "Sidro".equals(npcName)
                || "Modro".equals(npcName)
                || "Penadvent of ghost".equals(npcName)
                || "Greedy Charlie".equals(npcName)
                || "Shalom III".equals(npcName)
                || "Usas VI".equals(npcName)
                || "Kingvalley I".equals(npcName)
                || "Mr. Fishman (Original)".equals(npcName)
                || "Mr. Fishman (Alt)".equals(npcName)
                || "Operator Combaker".equals(npcName)
                || "Yiegah Kungfu".equals(npcName)
                || "Arrogant Metagear".equals(npcName)
                || "Arrogant Sturdy Snake".equals(npcName)
//                || "Yiear Kungfu".equals(npcName)
                || "Affected Knimare".equals(npcName)
                || "Mover Athleland".equals(npcName)
                || "Giant Mopiran".equals(npcName)
                || "Kingvalley II".equals(npcName)
                || "Energetic Belmont".equals(npcName)
                || "Mechanical Efspi".equals(npcName)
                || "Mud Man Qubert".equals(npcName)
                || "Hot-blooded Nemesistwo".equals(npcName)
                || "Hiner".equals(npcName)
                || "Moger".equals(npcName)
//                || "Former Mekuri Master".equals(npcName)
                || "Priest Zarnac".equals(npcName)
                || "Priest Xanado".equals(npcName)
                || "Philosopher Giltoriyo".equals(npcName)
                || "Priest Hidlyda".equals(npcName)
                || "Priest Romancis".equals(npcName)
                || "Priest Aramo".equals(npcName)
//                || "Priest Triton".equals(npcName)
                || "Priest Jaguarfiv".equals(npcName)
                || "The Fairy Queen".equals(npcName)
//                || "Mr. Slushfund".equals(npcName)
//                || "Priest Alest".equals(npcName)
//                || "Stray fairy".equals(npcName)
                || "Giant Thexde".equals(npcName)
                || "Philosopher Alsedana".equals(npcName)
                || "Philosopher Samaranta".equals(npcName)
                || "Priest Laydoc".equals(npcName)
                || "Priest Ashgine".equals(npcName)
                || "Philosopher Fobos".equals(npcName)
                || "8bit Elder".equals(npcName)
                || "duplex".equals(npcName)
                || "Samieru".equals(npcName)
                || "Naramura".equals(npcName)
//                || "8bit Fairy".equals(npcName)
                || "Priest Madomono".equals(npcName)
                || "Priest Gailious".equals(npcName)
                || "Tailor Dracuet".equals(npcName);
    }

    private static boolean isOneWayTransition(String transition) {
        String formattedTransition = transition.replace("Transition ", "Transition: ");
        return !"Transition: Goddess L1".equals(formattedTransition)
                && !"Transition: Illusion R1".equals(formattedTransition)
                && !"Transition: Twin U3".equals(formattedTransition)
                && !"Transition: Dimensional D1".equals(formattedTransition)
                && !"Transition: Shrine U1".equals(formattedTransition)
                && !"Transition: Endless D1".equals(formattedTransition)
                && !"Transition: Extinction U3".equals(formattedTransition)
                && !"Transition: Inferno W1".equals(formattedTransition)
                && !"Transition: Retromausoleum D1".equals(formattedTransition)
                && !"Transition: Goddess W1".equals(formattedTransition)
                && !"Transition: Twin U2".equals(formattedTransition)
                && !"Transition: Shrine D3".equals(formattedTransition)
                && !"Transition: Endless U1".equals(formattedTransition)
                && !"Transition: Shrine D2".equals(formattedTransition);
    }

    private static boolean isNonBossDoor(String door) {
        return door.endsWith("8") || door.endsWith("9");
    }

    private static boolean isValidTransitionDirection(String transitionTarget, String transitionDestination) {
        char transitionDirection1 = transitionTarget.charAt(transitionTarget.length() - 2);
        char transitionDirection2 = transitionDestination.charAt(transitionDestination.length() - 2);

        // Handle special transitions
        if(transitionDirection1 == 'W' && transitionTarget.contains("Goddess W1")) {
            transitionDirection1 = 'U';
        }
        if(transitionDirection2 == 'W' && transitionDestination.contains("Goddess W1")) {
            transitionDirection2 = 'U';
        }
        if(transitionDirection1 == 'W' && transitionTarget.contains("Inferno W1")) {
            transitionDirection1 = 'D';
        }
        if(transitionDirection2 == 'W' && transitionDestination.contains("Inferno W1")) {
            transitionDirection2 = 'D';
        }

        // Check direction
        if (transitionDirection1 == 'U') {
            return transitionDirection2 == 'D';
        }
        else if (transitionDirection1 == 'R') {
            return transitionDirection2 == 'L';
        }
        else if (transitionDirection1 == 'D') {
            return transitionDirection2 == 'U';
        }
        else if (transitionDirection1 == 'L') {
            return transitionDirection2 == 'R';
        }
        return false;
    }
}
