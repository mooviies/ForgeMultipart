package codechicken.lib.texture;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

/**
 * Created by covers1624 on 25/11/2016.
 */
public interface IWorldBlockTextureProvider extends IItemBlockTextureProvider {

    /**
     * Gets the texture for the given face of an in world block.
     *
     * @param side  The side to get the texture for.
     * @param state The state of the block.
     * @param renderType The renderType needed.
     * @param world The world the block is in.
     * @param pos   The position of the block.
     * @return The texture, null if there is no texture for the arguments, {@link TextureUtils#getMissingSprite()} if the arguments are invalid.
     */
    TextureAtlasSprite getTexture(Direction side, BlockState state, RenderType renderType, ILightReader world, BlockPos pos);

}
