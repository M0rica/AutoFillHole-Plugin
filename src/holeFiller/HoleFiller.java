/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holeFiller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
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
    int runID;
    Material[] notPlaceMaterials = new Material[]{Material.AIR, Material.CAVE_AIR};
    List<Material> materialsToReplace = Arrays.asList(new Material[]{Material.AIR, Material.CAVE_AIR, Material.WATER, Material.LAVA, Material.GRASS, Material.DEAD_BUSH});
    List<Material> materialsToIgnore = Arrays.asList(new Material[]{Material.BEDROCK, Material.GRASS, Material.DEAD_BUSH});
    
    int maxDistance = 6;
    
    @Override
    public void onEnable(){
        world = Bukkit.getWorlds().get(0);
        log.info("Plugin enabled");
    }
    
    @Override
    public void onDisable(){
        log.info("Plugin disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        if(args.length == 3){
            fill(new Location(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])));
            return true;
        } else if(args[0].equalsIgnoreCase("stop")){
            Bukkit.getScheduler().cancelTask(runID);
            broadcastMsg("Stoped filling!");
            return true;
        }
        return false;
    }
    
    
    public void broadcastMsg(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[HoleFiller] " + ChatColor.LIGHT_PURPLE + msg);
            }
        });
    }
    
    public void broadcastErr(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "[HoleFiller] " + ChatColor.RED + msg);
            }
        });
    }
    
    
    private void fill(Location start){
        //log.info(materialsToReplace.toString());
        blocks = new ArrayList<>();
        blocks.add(start);
        broadcastMsg("Starting to fill");
        this.runID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            @Override
            public void run(){
                if(!blocks.isEmpty()){
                    fillBlock(blocks.get(0));
                } else {
                    Bukkit.getScheduler().cancelTask(runID);
                    broadcastMsg("Done!");
                }
            }
        }, 0L, 1L);
    }
    
    private void fillBlock(Location block){
        //log.info(block.toString());
        Material blockTyp = block.getBlock().getType();
        if(materialsToReplace.contains(blockTyp)){

            Location[] neighbours = getNeighbours(block);
            Material[] neighbourBlocks = new Material[6];

            for(int i=0; i<neighbours.length; i++){
                neighbourBlocks[i] = neighbours[i].getBlock().getType();
            }

            List<Material> mostCommonMaterial = findMostCommonMaterial(neighbourBlocks);
            if(mostCommonMaterial.isEmpty()){
                    mostCommonMaterial = findNearestBlocks();
                }
            //log.info("Block Material to fill with: " + mostCommonMaterial.toString());
            boolean placeBlock = true;
            if(mostCommonMaterial.isEmpty()){
                placeBlock = false;
            } else {
                for(Material m: notPlaceMaterials){
                    if(mostCommonMaterial.contains(m)){
                        placeBlock = false;
                    }
                }
            }
            if(placeBlock){
                block.getBlock().setType(mostCommonMaterial.get(rand.nextInt(mostCommonMaterial.size())));
                for(int i=0; i<neighbourBlocks.length; i++){
                    for(Material m: materialsToReplace){
                        if(neighbourBlocks[i] == m){
                            blocks.add(neighbours[i]);
                            break;
                        }
                    }
                }
            }
        }
        blocks.remove(0);
    }
    
    private Location[] getNeighbours(Location loc){
        Location[] neighbours = new Location[6];
        
        neighbours[0] = loc.clone().add(1, 0, 0);
        neighbours[1] = loc.clone().add(-1, 0, 0);
        neighbours[2] = loc.clone().add(0, 1, 0);
        neighbours[3] = loc.clone().add(0, -1, 0);
        neighbours[4] = loc.clone().add(0, 0, 1);
        neighbours[5] = loc.clone().add(0, 0, -1);
        
        return neighbours;
    }
    
    private List<Material> findMostCommonMaterial(Material[] materials){
        HashMap<Material, Integer> mostCommon = new HashMap<>();
        for(Material m: materials){
            if(materialsToIgnore.contains(m)){
                continue;
            }
            if(mostCommon.containsKey(m)){
                mostCommon.replace(m, mostCommon.get(m)+1);
            } else {
                mostCommon.put(m, 1);
            }
        }
        List<Material> mostCommonMaterials = new ArrayList<>();
        int max = 0;
        boolean shouldGetFilled = shouldFill();
        //log.info("Should get filled: " + shouldGetFilled);
        for(Material m: mostCommon.keySet()){
            int temp = mostCommon.get(m);
            /*if(m == Material.AIR){
                if(temp >= 4){
                    mostCommonMaterials.clear();
                    mostCommonMaterials.add(m);
                    return mostCommonMaterials;
                }*/
            if(materialsToReplace.contains(m)){
                if(temp >= 4 && !shouldGetFilled){
                    mostCommonMaterials.clear();
                    mostCommonMaterials.add(m);
                    return mostCommonMaterials;
                }
            } else if(shouldGetFilled){
                if(temp > max){
                    mostCommonMaterials.clear();
                    mostCommonMaterials.add(m);
                    max = temp;
                } else if(temp == max){
                    mostCommonMaterials.add(m);
                }
            }
        }
        return mostCommonMaterials;
    }
    
    private List<Material> findNearestBlocks(){
        Location loc = blocks.get(0);
        Location temp;
        Material tempBlock;
        boolean foundBlock = false;
        List<Material> nearestBlocks = new ArrayList<>();
        for(int i=1; i<maxDistance; i++){
            for(int j=0; j<6; j++){
                temp = loc.clone();
                switch(j){
                    case 0:
                        temp.add(i, 0, 0);
                    case 1:
                        temp.add(-i, 0, 0);
                    case 2:
                        temp.add(0, i, 0);
                    case 3:
                        temp.add(0, -i, 0);
                    case 4:
                        temp.add(0, 0, i);
                    case 5:
                        temp.add(0, 0, -i);
                }
                tempBlock = temp.getBlock().getType();
                if(!materialsToIgnore.contains(tempBlock) && !materialsToReplace.contains(tempBlock)){
                    nearestBlocks.add(tempBlock);
                    foundBlock = true;
                }
            }
            if(foundBlock){
                break;
            }
        }
        return findMostCommonMaterial(nearestBlocks.toArray(new Material[nearestBlocks.size()]));
    }
    
    private boolean shouldFill(){
        Location loc = blocks.get(0);
        int numOfBlocks = 0;
        Location temp;
        for(int j=0; j<6; j++){
            for(int i=1; i<maxDistance; i++){
                temp = loc.clone();
                switch(j){
                    case 0:
                        temp.add(i, 0, 0);
                    case 1:
                        temp.add(-i, 0, 0);
                    case 2:
                        temp.add(0, i, 0);
                    case 3:
                        temp.add(0, -i, 0);
                    case 4:
                        temp.add(0, 0, i);
                    case 5:
                        temp.add(0, 0, -i);
                }
                if(!materialsToReplace.contains(temp.getBlock().getType()) && !materialsToIgnore.contains(temp.getBlock().getType())){
                    numOfBlocks++;
                    break;
                }
            }
        }
        //log.info("Number of edges detected: " + String.valueOf(numOfBlocks));
        return numOfBlocks >= 3;
    }
    
}
