/*
 * Copyright (C) 2021 M0rica
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package holeFiller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 *
 * @author M0rica
 */
public class TerrainRuleChecker {
    
    private class Ore{
        
        public int minHeight;
        public int maxHeight;
        public int maxDepositSize;
        
        public Ore(int minH, int maxH, int maxSize){
            
            minHeight = minH;
            maxHeight = maxH;
            maxDepositSize = maxSize;
            
        }
        
    }
    
    HashMap<Material, Ore> ores;
    List<Location> blocksToCheck;
    
    public TerrainRuleChecker(){
        
        ores = new HashMap<>();
        
        Ore coal_ore = new Ore(0, 127, 37);
        Ore diamond_ore = new Ore(1, 16, 10);
        Ore gold_ore = new Ore(0, 32, 13);
        Ore iron_ore = new Ore(0, 63, 13);
        Ore lapis_ore = new Ore(0, 30, 10);
        Ore redstone_ore = new Ore(1, 16, 10);
        
        ores.put(Material.COAL_ORE, coal_ore);
        ores.put(Material.DIAMOND_ORE, diamond_ore);
        ores.put(Material.GOLD_ORE, gold_ore);
        ores.put(Material.IRON_ORE, iron_ore);
        ores.put(Material.LAPIS_ORE, lapis_ore);
        ores.put(Material.REDSTONE_ORE, redstone_ore);
    }
    
    public boolean blockFollowsRules(Location loc, Material block){
        boolean followsRules = true;
        
        if(ores.containsKey(block)){
            followsRules = canPlaceOre(loc, block);
        }
        
        return followsRules;
    }
    
    private boolean canPlaceOre(Location loc, Material block){
        
        Ore ore = ores.get(block);
        int height = loc.getBlockY();
        
        if(height < ore.minHeight || height > ore.maxHeight){
            return false;
        }
        
        List<Location> oreDeposit = new ArrayList<>();
        blocksToCheck = new ArrayList<>();
        oreDeposit.add(loc);
        
        while(!blocksToCheck.isEmpty()){
            Location temp = blocksToCheck.get(0);
            blocksToCheck.remove(0);
            if(temp.getBlock().getType() == block){
                oreDeposit.add(temp);
                blocksToCheck.addAll(getNeighbours(temp, block));
            }
        }
        
        return oreDeposit.size() <= ore.maxDepositSize;
        
    }
    
    private List<Location> getNeighbours(Location loc, Material block){
        Location[] neighbours = new Location[6];
        //direct neighbours
        neighbours[0] = loc.clone().add(1, 0, 0);
        neighbours[1] = loc.clone().add(-1, 0, 0);
        neighbours[2] = loc.clone().add(0, 1, 0);
        neighbours[3] = loc.clone().add(0, -1, 0);
        neighbours[4] = loc.clone().add(0, 0, 1);
        neighbours[5] = loc.clone().add(0, 0, -1);
        
        List<Location> checkedNeighbours = new ArrayList<>();
        
        for(Location l: neighbours){
            if(l.getBlock().getType() == block && !blocksToCheck.contains(l)){
                checkedNeighbours.add(l);
            }
        }
        return checkedNeighbours;
    }
    
}
