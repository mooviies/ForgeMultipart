package codechicken.lib.render.block;

import codechicken.lib.reflect.ObfMapping;
import codechicken.lib.reflect.ReflectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 8/09/2016.
 */
public class BlockRenderingRegistry {

    private static boolean initialized = false;

    private static List<ICCBlockRenderer> blockRenderers = new ArrayList<>();

    @OnlyIn (Dist.CLIENT)
    public static void init() {
        if (!initialized) {
            Minecraft mc = Minecraft.getInstance();
            BlockRendererDispatcher parentDispatcher = mc.getBlockRendererDispatcher();
            //mc.blockRenderDispatcher = new CCBlockRendererDispatcher(parentDispatcher, mc.getBlockColors());
            //TODO find solution for blockRendererDispatcher being private
            initialized = true;
        }
    }

    public static void registerRenderer(ICCBlockRenderer renderer) {
        blockRenderers.add(renderer);
    }

    static List<ICCBlockRenderer> getBlockRenderers() {
        return blockRenderers;
    }
}
