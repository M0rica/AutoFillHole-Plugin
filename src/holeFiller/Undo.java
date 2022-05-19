package holeFiller;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class Undo extends Command implements TabExecutor {
    List<Location> locs = new ArrayList<>();
    List<Material> typs = new ArrayList<>();

    protected Undo() {
        super("undo");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (int i = 0; i < locs.size(); i++) {
            Location loc = locs.get(i);
            Material typ = typs.get(i);

            loc.getBlock().setType(typ);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

    public void addBlock(Location location, Material material) {
        locs.add(location);
        typs.add(material);
    }
    public void clear() {
        locs.clear();
        typs.clear();
    }
}
