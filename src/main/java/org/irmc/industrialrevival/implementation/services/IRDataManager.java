package org.irmc.industrialrevival.implementation.services;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.irmc.industrialrevival.api.menu.MachineMenu;
import org.irmc.industrialrevival.api.menu.MachineMenuPreset;
import org.irmc.industrialrevival.api.data.runtime.IRBlockData;
import org.irmc.industrialrevival.api.data.sql.BlockRecord;
import org.irmc.industrialrevival.core.services.IIRDataManager;
import org.irmc.industrialrevival.dock.IRDock;
import org.irmc.industrialrevival.utils.Debug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IRDataManager implements IIRDataManager {
    private final Map<Location, IRBlockData> blockDataMap;

    public IRDataManager() {
        this.blockDataMap = new HashMap<>();

        loadData();
    }

    private void loadData() {
        List<BlockRecord> records =
                IRDock.getPlugin().getSQLDataManager().getAllBlockRecords();
        Debug.log("List<BlockRecord> records: " + records.size());
        for (BlockRecord record : records) {
            Location loc = record.getLocation();

            blockDataMap.put(loc, IRBlockData.warp(record));
        }
    }

    public IRBlockData getBlockData(Location location) {
        return blockDataMap.get(location);
    }

    public void placeBlock(Location loc, NamespacedKey machineId) {
        Debug.log("handleBlockPlacing");
        YamlConfiguration configuration = new YamlConfiguration();
        MachineMenuPreset preset = IRDock.getPlugin().getRegistry().getMenuPresets().get(machineId);

        MachineMenu menu = null;
        if (preset != null) {
            menu = new MachineMenu(loc, preset);
            preset.newInstance(loc.getBlock(), menu);
        }

        IRBlockData blockData = new IRBlockData(machineId, loc, configuration, menu);
        blockDataMap.put(loc, blockData);
    }

    @CanIgnoreReturnValue
    public IRBlockData breakBlock(Location loc) {
        return blockDataMap.remove(loc);
    }

    public void saveAllData() {
        Debug.log("blockDataMap: " + blockDataMap.size());
        for (IRBlockData data : blockDataMap.values()) {
            BlockRecord blockRecord = BlockRecord.warp(data);

            IRDock.getPlugin().getSQLDataManager().saveBlockRecord(blockRecord);
        }
        blockDataMap.clear();
    }

    public Map<Location, IRBlockData> getBlockDataMap() {
        return new HashMap<>(blockDataMap);
    }

    private void restoreMenuData(MachineMenu menu, MachineMenuPreset preset) {
    }
}
