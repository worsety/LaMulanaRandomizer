package lmr.randomizer.random;

import lmr.randomizer.DataFromFile;
import lmr.randomizer.ItemPriceCount;
import lmr.randomizer.Settings;
import lmr.randomizer.dat.Block;
import lmr.randomizer.dat.shop.ShopBlock;
import lmr.randomizer.node.CustomItemPlacement;
import lmr.randomizer.node.MoneyChecker;
import lmr.randomizer.update.GameDataTracker;
import lmr.randomizer.update.GameObjectId;
import lmr.randomizer.update.LocationCoordinateMapper;

import java.util.*;

/**
 * Created by thezerothcat on 8/2/2017.
 *
 * Currently only randomizes unique items. Should probably eventually randomize ammo options also.
 */
public class EverythingShopRandomizer extends ShopRandomizer {
    public EverythingShopRandomizer() {
        randomizedShops = new ArrayList<>(DataFromFile.getAllShops());

        for(String shop : DataFromFile.getAllShops()) {
            if(MSX_SHOP_NAME.equals(shop)) {
                unassignedShopItemLocations.add(String.format("%s Item 1", shop));
            }
            else if(Settings.isFools2021Mode() && FISH_FAIRY_SHOP_NAME.equals(shop)) {
                unassignedShopItemLocations.add(String.format("%s Item 3", shop));
            }
            else {
                for (int i = 0; i < 3; i++) {
                    unassignedShopItemLocations.add(String.format("%s Item %d", shop, i + 1));
                }
            }
        }

        shopsWithTransformations = new ArrayList<>();
        shopsWithTransformations.add(NON_MSX_SHOP_NAME);
        shopsWithTransformations.add(MSX_SHOP_NAME);
    }

    @Override
    public ShopRandomizer copy() {
        EverythingShopRandomizer other = new EverythingShopRandomizer();
        other.randomizedShops = new ArrayList<>(this.randomizedShops);
        other.unassignedShopItemLocations = new ArrayList<>(this.unassignedShopItemLocations);
        other.shopsWithTransformations = new ArrayList<>(this.shopsWithTransformations);
        other.mapOfShopInventoryItemToContents = new HashMap<>(this.mapOfShopInventoryItemToContents);
        return other;
    }

    @Override
    public void placeNonRandomizedItems() {
        List<String> originalShopContents;
        String originalShopItem;
        String shopItemLocation;

        List<String> nonRandomizedItems = DataFromFile.getNonRandomizedItems();
        for(String shop : randomizedShops) {
            originalShopContents = DataFromFile.getMapOfShopNameToShopOriginalContents().get(shop);
            for (int i = 0; i < 3; i++) {
                shopItemLocation = String.format("%s Item %d", shop, i + 1);
                originalShopItem = originalShopContents.get(i);
                if(originalShopItem.equals("Weights") || originalShopItem.endsWith("Ammo")) {
                    continue;
                }
                else if(Settings.getStartingItemsIncludingCustom().contains(originalShopItem)) {
                    continue;
                }
                else if(MSX_SHOP_NAME.equals(shop) && "Mobile Super X2".equals(originalShopItem) && nonRandomizedItems.contains(originalShopItem)) {
                    mapOfShopInventoryItemToContents.put(shopItemLocation, originalShopItem);
                    unassignedShopItemLocations.remove(shopItemLocation);
                    itemRandomizer.removeItemFromUnplacedItems(originalShopItem);
                }
                else if(FISH_FAIRY_SHOP_NAME.equals(shop) && !"Shell Horn".equals(originalShopItem) && !"guild.exe".equals(originalShopItem) && nonRandomizedItems.contains(originalShopItem)) {
                    mapOfShopInventoryItemToContents.put(shopItemLocation, originalShopItem);
                    unassignedShopItemLocations.remove(shopItemLocation);
                    itemRandomizer.removeItemFromUnplacedItems(originalShopItem);
                }
                else if(nonRandomizedItems.contains(originalShopItem)){
                    mapOfShopInventoryItemToContents.put(shopItemLocation, originalShopItem);
                    unassignedShopItemLocations.remove(shopItemLocation);
                    itemRandomizer.removeItemFromUnplacedItems(originalShopItem);
                }
            }
        }
        if(LocationCoordinateMapper.isSurfaceStart() || Settings.getCurrentStartingLocation() == 23 || Settings.getCurrentStartingLocation() == 24) {
            int i = 1;
            for(String shopContents : DataFromFile.getMapOfShopNameToShopOriginalContents().get(DataFromFile.CUSTOM_SHOP_NAME)) {
                shopItemLocation = String.format("%s Item %d", DataFromFile.CUSTOM_SHOP_NAME, i++);
                mapOfShopInventoryItemToContents.put(shopItemLocation, shopContents);
                unassignedShopItemLocations.remove(shopItemLocation);
            }
        }

        for(CustomItemPlacement customItemPlacement : DataFromFile.getCustomPlacementData().getCustomItemPlacements()) {
            String customLocation = customItemPlacement.getLocation();
            if(customLocation != null && customLocation.startsWith("Shop ")) {
                if(!customLocation.startsWith(DataFromFile.CUSTOM_SHOP_NAME) || !LocationCoordinateMapper.isSurfaceStart()) {
                    mapOfShopInventoryItemToContents.put(customLocation, customItemPlacement.getContents());
                    unassignedShopItemLocations.remove(customLocation);
                    if(!Settings.isFools2021Mode() || !"Spaulder".equals(customItemPlacement.getContents())) {
                        itemRandomizer.removeItemFromUnplacedItems(customItemPlacement.getContents());
                    }
                    if(customItemPlacement.getContents().contains("Sacred Orb")) {
                        shopsWithTransformations.add(customLocation.substring(0, customLocation.indexOf(')') + 1));
                    }
                }
            }
        }
    }

    @Override
    public void placeSpecialSubweaponAmmo(Random random) {
        if(LocationCoordinateMapper.isSurfaceStart()) {
            List<String> guaranteedAmmoShopLocations = new ArrayList<>();
            for(String location : unassignedShopItemLocations) {
                if(location.contains("Surface") && !location.equals(MSX_SHOP_NAME + " Item 1")) {
                    guaranteedAmmoShopLocations.add(location);
                }
            }
            if(!guaranteedAmmoShopLocations.isEmpty()) {
                String specialAmmoLocation = guaranteedAmmoShopLocations.get(random.nextInt(guaranteedAmmoShopLocations.size()));
                mapOfShopInventoryItemToContents.put(specialAmmoLocation, Settings.getCurrentStartingSubWeapons().get(0) + " Ammo");
                unassignedShopItemLocations.remove(specialAmmoLocation);
            }
        }
        else {
            String shopItem = DataFromFile.CUSTOM_SHOP_NAME + " Item 1";
            String shopContents = mapOfShopInventoryItemToContents.get(shopItem);
            if(shopContents == null) {
                mapOfShopInventoryItemToContents.put(shopItem, Settings.getCurrentStartingSubWeapons().get(0) + " Ammo");
                unassignedShopItemLocations.remove(shopItem);
                return;
            }
            shopItem = DataFromFile.CUSTOM_SHOP_NAME + " Item 2";
            shopContents = mapOfShopInventoryItemToContents.get(shopItem);
            if(shopContents == null) {
                mapOfShopInventoryItemToContents.put(shopItem, Settings.getCurrentStartingSubWeapons().get(0) + " Ammo");
                unassignedShopItemLocations.remove(shopItem);
                return;
            }
            shopItem = DataFromFile.CUSTOM_SHOP_NAME + " Item 3";
            shopContents = mapOfShopInventoryItemToContents.get(shopItem);
            if(shopContents == null) {
                mapOfShopInventoryItemToContents.put(shopItem, Settings.getCurrentStartingSubWeapons().get(0) + " Ammo");
                unassignedShopItemLocations.remove(shopItem);
                return;
            }
        }
    }

    @Override
    public List<String> getPlacedShopItems() {
        List<String> placedItems = new ArrayList<>();
        for(Map.Entry<String, String> locationAndItem : mapOfShopInventoryItemToContents.entrySet()) {
            placedItems.add(locationAndItem.getValue());
        }
        return placedItems;
    }

    @Override
    public void determineItemTypes(Random random) {
        assignWeights(random);
        assignSubweaponAmmoLocations(random);
    }

    private void assignWeights(Random random) {
        List<String> shopsWithNoWeights = new ArrayList<>(randomizedShops);
        shopsWithNoWeights.remove(LITTLE_BROTHER_SHOP_NAME);

        Map<String, Integer> mapOfShopNameToAvailableItems = new HashMap<>();
        for(String shopName : randomizedShops) {
            mapOfShopNameToAvailableItems.put(shopName, 3);
        }

        for(Map.Entry<String, String> shopLocationAndItem : mapOfShopInventoryItemToContents.entrySet()) {
            String shopInventoryLocation = shopLocationAndItem.getKey();
            String shopName = shopInventoryLocation.substring(0, shopInventoryLocation.indexOf(")") + 1);
            if(shopLocationAndItem.getKey().contains("Surface") && shopLocationAndItem.getValue().equals("Weights")) {
                shopsWithNoWeights.remove(shopName);
                if(MSX_SHOP_NAME.equals(shopName)) {
                    shopsWithNoWeights.remove(NON_MSX_SHOP_NAME);
                    mapOfShopNameToAvailableItems.remove(shopName);
                }
                else if(NON_MSX_SHOP_NAME.equals(shopName)) {
                    shopsWithNoWeights.remove(MSX_SHOP_NAME);
                    mapOfShopNameToAvailableItems.remove(shopName);
                }
                break;
            }
            mapOfShopNameToAvailableItems.put(shopName, mapOfShopNameToAvailableItems.get(shopName) - 1);
            if(mapOfShopNameToAvailableItems.get(shopName) < 1) {
                mapOfShopNameToAvailableItems.remove(shopName);
            }
            if(shopLocationAndItem.getValue().equals("Weights")) {
                shopsWithNoWeights.remove(shopName);
            }
        }

        String shop;
        String location;
        List<Integer> shopItemNumbers;
        int shopItemNumberIndex;
        int maxAdditionalWeights = getWeightCount(random, mapOfShopNameToAvailableItems.size());
        int weightsPlaced = 0;
        while(weightsPlaced < maxAdditionalWeights) {
            location = null;
            shop = shopsWithNoWeights.get(random.nextInt(shopsWithNoWeights.size()));
            shopItemNumbers = new ArrayList<>(Arrays.asList(1, 2, 3));
            while(!shopItemNumbers.isEmpty()) {
                shopItemNumberIndex = random.nextInt(shopItemNumbers.size());
                location = String.format("%s Item %d", shop, shopItemNumbers.get(shopItemNumberIndex));
                if(location.equals(MSX_SHOP_NAME + " Item 1")) {
                    // Don't put weights/ammo where MSX2 was.
                    shopItemNumbers.remove(shopItemNumberIndex);
                    continue;
                }

                if(unassignedShopItemLocations.contains(location)) {
                    mapOfShopInventoryItemToContents.put(location, "Weights");
                    unassignedShopItemLocations.remove(location);
                    ++weightsPlaced;
                    break;
                }
                shopItemNumbers.remove(shopItemNumberIndex);
            }
            if(location != null) {
                shopsWithNoWeights.remove(shop);
                if(MSX_SHOP_NAME.equals(shop) && (location.contains("Item 2") || location.contains("Item 3"))) {
                    shopsWithNoWeights.remove(NON_MSX_SHOP_NAME);
                }
                else if(NON_MSX_SHOP_NAME.equals(shop) && (location.contains("Item 2") || location.contains("Item 3"))) {
                    shopsWithNoWeights.remove(MSX_SHOP_NAME);
                }
            }

            if(shopsWithNoWeights.isEmpty()) {
                // We've assigned to put weights in more shops than we actually have available. Logic for this should be cleaned up, but it's probably harmless as is.
//                FileUtils.log("Unable to place " + maxAdditionalWeights + " weights");
                break;
            }
        }
    }

    public void placeGuaranteedWeights(Random random) {
        // Guarantee weight shop on the Surface
        List<String> guaranteedWeightShopLocations = new ArrayList<>();
        if(LocationCoordinateMapper.isSurfaceStart()) {
            for(String location : unassignedShopItemLocations) {
                if(location.contains("Surface") && !location.contains(MSX_SHOP_NAME)) {
                    guaranteedWeightShopLocations.add(location);
                }
            }
            if(!guaranteedWeightShopLocations.isEmpty()) {
                String surfaceWeightsLocation = guaranteedWeightShopLocations.get(random.nextInt(guaranteedWeightShopLocations.size()));
                mapOfShopInventoryItemToContents.put(surfaceWeightsLocation, "Weights");
                unassignedShopItemLocations.remove(surfaceWeightsLocation);
            }
        }
        else {
            String shopItem = DataFromFile.CUSTOM_SHOP_NAME + " Item 1";
            String shopContents = mapOfShopInventoryItemToContents.get(shopItem);
            if(shopContents == null) {
                mapOfShopInventoryItemToContents.put(shopItem, "Weights");
                unassignedShopItemLocations.remove(shopItem);
            }
            else {
                shopItem = DataFromFile.CUSTOM_SHOP_NAME + " Item 2";
                shopContents = mapOfShopInventoryItemToContents.get(shopItem);
                if(shopContents == null) {
                    mapOfShopInventoryItemToContents.put(shopItem, "Weights");
                    unassignedShopItemLocations.remove(shopItem);
                }
                else {
                    shopItem = DataFromFile.CUSTOM_SHOP_NAME + " Item 3";
                    shopContents = mapOfShopInventoryItemToContents.get(shopItem);
                    if(shopContents == null) {
                        mapOfShopInventoryItemToContents.put(shopItem, "Weights");
                        unassignedShopItemLocations.remove(shopItem);
                        return;
                    }
                }
            }
        }

        if(!Settings.isFools2021Mode()) {
            // Guarantee weights at Little Brother's shop so there's a guaranteed way to unlock Big Brother's shop.
            guaranteedWeightShopLocations.clear();
            for(String location : unassignedShopItemLocations) {
                if(location.contains(LITTLE_BROTHER_SHOP_NAME)) {
                    guaranteedWeightShopLocations.add(location);
                }
            }
            if(!guaranteedWeightShopLocations.isEmpty()) {
                String littleBrotherShopWeightsLocation = guaranteedWeightShopLocations.get(random.nextInt(guaranteedWeightShopLocations.size()));
                mapOfShopInventoryItemToContents.put(littleBrotherShopWeightsLocation, "Weights");
                unassignedShopItemLocations.remove(littleBrotherShopWeightsLocation);
            }
        }

        // Guarantee weights at the shop in Graveyard of the Giants, as mercy for people on long journeys across the backside areas
        guaranteedWeightShopLocations.clear();
        for(String location : unassignedShopItemLocations) {
            if(location.contains(GRAVEYARD_SHOP_NAME)) {
                guaranteedWeightShopLocations.add(location);
            }
        }
        if(!guaranteedWeightShopLocations.isEmpty()) {
            String graveyardShopWeightsLocation = guaranteedWeightShopLocations.get(random.nextInt(guaranteedWeightShopLocations.size()));
            mapOfShopInventoryItemToContents.put(graveyardShopWeightsLocation, "Weights");
            unassignedShopItemLocations.remove(graveyardShopWeightsLocation);
        }
    }

    private int getWeightCount(Random random, int usableShops) {
        int maxAdditionalWeights = Math.min(usableShops - 4,
                unassignedShopItemLocations.size() - ItemRandomizer.ALL_SUBWEAPONS.size() - itemRandomizer.getTotalShopItems()); // Must have enough room for all shop items plus one of each ammo type plus one weight. The remaining ammo to weights ratio can be random.
        if(maxAdditionalWeights < 0) {
            maxAdditionalWeights = 0;
        }
        return maxAdditionalWeights == 0 ? 0 : random.nextInt(maxAdditionalWeights);
    }

    private void assignSubweaponAmmoLocations(Random random) {
        List<String> unassignedSubweapons = new ArrayList<>(ItemRandomizer.ALL_SUBWEAPONS);
        unassignedSubweapons.removeAll(Settings.getRemovedItems());
        unassignedSubweapons.removeAll(Settings.getCurrentRemovedItems());
        int totalSubweaponLocations = unassignedShopItemLocations.size() - itemRandomizer.getTotalShopItems();

        List<String> availableSubweapons = new ArrayList<>(unassignedSubweapons);

        String location;
        String subweapon;
        int shopLocationIndex;
        for(int i = 0; i < totalSubweaponLocations; i++) {
            shopLocationIndex = random.nextInt(unassignedShopItemLocations.size());
            location = unassignedShopItemLocations.get(shopLocationIndex);
            while(location.equals(MSX_SHOP_NAME + " Item 1")) {
                shopLocationIndex = random.nextInt(unassignedShopItemLocations.size());
                location = unassignedShopItemLocations.get(shopLocationIndex);
            }

            if(availableSubweapons.isEmpty()) {
                mapOfShopInventoryItemToContents.put(location, "Weights");
                unassignedShopItemLocations.remove(location);
                continue;
            }
            else if(unassignedSubweapons.isEmpty()) {
                subweapon = availableSubweapons.get(random.nextInt(availableSubweapons.size()));
            }
            else {
                int unassignedSubweaponIndex = random.nextInt(unassignedSubweapons.size());
                subweapon = unassignedSubweapons.get(unassignedSubweaponIndex);
                unassignedSubweapons.remove(unassignedSubweaponIndex);
            }
            mapOfShopInventoryItemToContents.put(location, subweapon + " Ammo");
            unassignedShopItemLocations.remove(location);
        }
    }

    @Override
    public void updateFiles(List<Block> blocks, boolean subweaponOnly, MoneyChecker moneyChecker, Random random) {
        String shopItem1;
        String shopItem2;
        String shopItem3;
        ShopBlock shopBlock;
        ShopItemPriceCountRandomizer shopItemPriceCountRandomizer = new ShopItemPriceCountRandomizer(subweaponOnly, moneyChecker, random);
        ItemPriceCount itemPriceCountMsxShop2 = null;
        ItemPriceCount itemPriceCountMsxShop3 = null;
        for(String shopName : randomizedShops) {
            if(DataFromFile.CUSTOM_SHOP_NAME.equals(shopName) && (LocationCoordinateMapper.isSurfaceStart() || Settings.getCurrentStartingLocation() == 23 || Settings.getCurrentStartingLocation() == 24)) {
                continue;
            }
            if(Settings.isFools2020Mode() && "Shop 7 (Graveyard)".equals(shopName)) {
                // Angel Shield shop text for Graveyard
                shopBlock = (ShopBlock) blocks.get(273);
            }
            else {
                shopBlock = (ShopBlock) blocks.get(DataFromFile.getMapOfShopNameToShopBlock().get(shopName));
            }

            if(MSX_SHOP_NAME.equals(shopName)) {
                shopItem1 = Settings.getUpdatedContents(mapOfShopInventoryItemToContents.get(String.format("%s Item 1", shopName)));
                shopItem2 = Settings.getUpdatedContents(mapOfShopInventoryItemToContents.get(String.format("%s Item 2", NON_MSX_SHOP_NAME)));
                shopItem3 = Settings.getUpdatedContents(mapOfShopInventoryItemToContents.get(String.format("%s Item 3", NON_MSX_SHOP_NAME)));

                // No need to worry about flag replacement because MSX2 can't be a removed item.
                Map<String, GameObjectId> nameToDataMap = DataFromFile.getMapOfItemToUsefulIdentifyingRcdData();
                GameObjectId itemNewContentsData = nameToDataMap.get(shopItem1);
                GameDataTracker.writeLocationContents("Mobile Super X2", shopItem1,
                        nameToDataMap.get("Mobile Super X2"), itemNewContentsData, itemNewContentsData.getWorldFlag(), random, false);
            }
            else {
                shopItem1 = Settings.getUpdatedContents(mapOfShopInventoryItemToContents.get(String.format("%s Item 1", shopName)));
                shopItem2 = Settings.getUpdatedContents(mapOfShopInventoryItemToContents.get(String.format("%s Item 2", shopName)));
                shopItem3 = Settings.getUpdatedContents(mapOfShopInventoryItemToContents.get(String.format("%s Item 3", shopName)));
            }

            if(Settings.getCurrentRemovedItems().contains(shopItem1)
                    || Settings.getRemovedItems().contains(shopItem1)
                    || Settings.getStartingItemsIncludingCustom().contains(shopItem1)
                    || (Settings.isReplaceMapsWithWeights() && shopItem1.startsWith("Map (") && !"Map (Shrine of the Mother)".equals(shopItem1))) {
                shopItem1 = "Weights";
            }
            if(Settings.getCurrentRemovedItems().contains(shopItem2)
                    || Settings.getRemovedItems().contains(shopItem2)
                    || Settings.getStartingItemsIncludingCustom().contains(shopItem2)
                    || (Settings.isReplaceMapsWithWeights() && shopItem2.startsWith("Map (") && !"Map (Shrine of the Mother)".equals(shopItem2))) {
                shopItem2 = "Weights";
            }
            if(Settings.getCurrentRemovedItems().contains(shopItem3)
                    || Settings.getRemovedItems().contains(shopItem3)
                    || Settings.getStartingItemsIncludingCustom().contains(shopItem3)
                    || (Settings.isReplaceMapsWithWeights() && shopItem3.startsWith("Map (") && !"Map (Shrine of the Mother)".equals(shopItem3))) {
                shopItem3 = "Weights";
            }

            if(Settings.isFools2021Mode() && FISH_FAIRY_SHOP_NAME.equals(shopName)) {
                shopItem1 = "Shell Horn";
                shopItem2 = "guild.exe";
            }

            if(NON_MSX_SHOP_NAME.equals(shopName) || MSX_SHOP_NAME.equals(shopName)) {
                if(itemPriceCountMsxShop2 == null) {
                    itemPriceCountMsxShop2 = shopItemPriceCountRandomizer.getItemPriceAndCount(String.format("%s Item 2", shopName), shopItem2);
                }
                if(itemPriceCountMsxShop3 == null) {
                    itemPriceCountMsxShop3 = shopItemPriceCountRandomizer.getItemPriceAndCount(String.format("%s Item 3", shopName), shopItem3);
                }
                GameDataTracker.writeShopInventory(shopBlock, shopItem1, shopItem2, shopItem3, blocks,
                        shopItemPriceCountRandomizer.getItemPriceAndCount(String.format("%s Item 1", shopName), shopItem1),
                        itemPriceCountMsxShop2,
                        itemPriceCountMsxShop3,
                        shopName, false, npcRandomizer, random);
            }
            else {
                GameDataTracker.writeShopInventory(shopBlock, shopItem1, shopItem2, shopItem3, blocks,
                        shopItemPriceCountRandomizer.getItemPriceAndCount(String.format("%s Item 1", shopName), shopItem1),
                        shopItemPriceCountRandomizer.getItemPriceAndCount(String.format("%s Item 2", shopName), shopItem2),
                        shopItemPriceCountRandomizer.getItemPriceAndCount(String.format("%s Item 3", shopName), shopItem3),
                        shopName, false, npcRandomizer, random);
            }
        }
    }
}
