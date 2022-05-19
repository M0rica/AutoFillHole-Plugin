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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author M0rica
 */
public class HoleFiller extends JavaPlugin{
    
    Logger log = this.getLogger();
    Random rand = new Random();
    
    World world;
    
    List<Location> blocks;
    List<Location> newBlocks;
    int runID;
    
    int blocksPlaced;
    boolean isRunning = false;
    int allBlocksPlaced;

    Undo undocmd = new Undo();
    
    Material[] notPlaceMaterials = new Material[]{Material.AIR, Material.CAVE_AIR};
    List<Material> materialsToReplace = Arrays.asList(new Material[]{Material.AIR, Material.CAVE_AIR, Material.WATER, Material.LAVA, Material.GRASS, Material.TALL_GRASS, Material.DEAD_BUSH,
                    Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.OXEYE_DAISY, Material.CORNFLOWER, 
                    Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, 
                    Material.PEONY, Material.WHITE_TULIP, Material.ORANGE_TULIP, Material.RED_TULIP, Material.PINK_TULIP, Material.SEAGRASS, Material.KELP, Material.SNOW});
    List<Material> materialsToIgnore = Arrays.asList(new Material[]{Material.AIR, Material.CAVE_AIR, Material.BEDROCK, Material.GRASS, Material.TALL_GRASS, 
                    Material.DEAD_BUSH, Material.WATER, Material.LAVA, Material.CAVE_AIR, Material.DANDELION, Material.POPPY, 
                    Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.OXEYE_DAISY, Material.CORNFLOWER, 
                    Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, 
                    Material.PEONY, Material.WHITE_TULIP, Material.ORANGE_TULIP, Material.RED_TULIP, Material.PINK_TULIP, Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, 
                    Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE, 
                    Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES,
                    Material.DARK_OAK_LEAVES, Material.VINE, Material.SNOW, Material.SEAGRASS, Material.KELP});
    
    HashSet<Material> materialsToIgnoreFindingStart = new HashSet<>(materialsToIgnore);
    
    int maxDistance = 6;
    int blocksPerTick = 10;
    
    BlockAnalyzer ba;
    
    @Override
    public void onEnable(){
        world = Bukkit.getWorlds().get(0);
        log.info("Plugin enabled");
        ba = new BlockAnalyzer(log, this);
        this.getCommand("undofillhole").setExecutor(undocmd);
    }
    
    @Override
    public void onDisable(){
        log.info("Plugin disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] args) {
        if (cmd.getName().equals("fillhole")) {
            if (args.length == 3) {
                fill(new Location(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])));
                return true;
            } else if (args.length == 0) {
                Player player = (Player) cs;
                Location loc = player.getTargetBlock(materialsToIgnoreFindingStart, 10).getLocation();
                loc.setY(loc.getY() + 1);
                if (isRunning) {
                    stop();
                }
                fill(loc);
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (isRunning) {
                    stop();
                }
                return true;
            }
        }
        return false;
    }
    
    public void stop(){
        Bukkit.getScheduler().cancelTask(runID);
        isRunning = false;
        if(allBlocksPlaced == 0){
            broadcastMsg("No hole detected, nothing was filled.");
        } else {
            broadcastMsg(String.format("Done! (%d blocks filled)", allBlocksPlaced));
        }
    }
    
    
    public void broadcastMsg(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[AutoFillHole] " + ChatColor.LIGHT_PURPLE + msg);
            }
        });
    }
    
    public void broadcastErr(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "[AutoFillHole] " + ChatColor.RED + msg);
            }
        });
    }
    
    public void addNewBlocks(List<Location> blocks){
        newBlocks.addAll(blocks);
    }
    
    
    private void fill(Location start){
        //log.info(materialsToReplace.toString());
        isRunning = true;
        blocks = new ArrayList<>();
        blocks.add(start);
        allBlocksPlaced = 0;
        broadcastMsg("Starting to fill");
        undocmd.clear();
        this.runID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            @Override
            public void run(){
                blocksPlaced = 0;
                newBlocks = new ArrayList<>();
                if(!blocks.isEmpty()){
                    while(!blocks.isEmpty() && blocksPlaced < blocksPerTick){
                        fillBlock(getNextBlock());
                        blocksPlaced++;
                    }
                    blocks.addAll(newBlocks);
                } else {
                    stop();
                }
            }
        }, 20L, 1L);
    }
    
    private Location getNextBlock(){
        Location lowestBlock = blocks.get(0);
        int y = lowestBlock.getBlockY();
        
        for(int i=1; i<blocks.size(); i++){
            Location temp = blocks.get(i);
            if(temp.getBlockY() < y){
                lowestBlock = temp;
            }
        }
        blocks.remove(lowestBlock);
        return lowestBlock;
    }
    
    private void fillBlock(Location block){
        //log.info(block.toString());
        Material blockTyp = block.getBlock().getType();
        if(materialsToReplace.contains(blockTyp)){

            //log.info("Block Material to fill with: " + mostCommonMaterial.toString());
            Material m = ba.analyzeBlock(block);
            if(m != null){
                undocmd.addBlock(block, blockTyp);
                block.getBlock().setType(m);
                allBlocksPlaced++;
            }
            //blocks.remove(0);
        } else {
            //blocks.remove(0);
            if(!blocks.isEmpty()){
                fillBlock(getNextBlock());
            }
        }
    }
    
    
}
