package codechicken.lib.model.bakedmodels;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by covers1624 on 13/07/2017.
 */
public abstract class AbstractPerspectiveLayeredModel extends AbstractBakedPropertiesModel {

    //The layer that designates general quads.
    protected RenderType generalRenderType;

    public AbstractPerspectiveLayeredModel(ModelProperties properties) {
        this(properties, RenderType.getSolid());
    }

    public AbstractPerspectiveLayeredModel(ModelProperties properties, RenderType generalRenderType) {
        super(properties);
        this.generalRenderType = generalRenderType;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
        RenderType renderType = MinecraftForgeClient.getRenderLayer();
        if (renderType == null) {
            renderType = generalRenderType;
        }
        return getLayerQuads(state, side, renderType, rand, data);
    }

    @Override
    protected List<BakedQuad> getAllQuads(BlockState state, IModelData data) {
        List<BakedQuad> allQuads = new ArrayList<>();
        for (RenderType renderType : RenderType.getBlockRenderTypes()) {
            allQuads.addAll(getLayerQuads(state, null, renderType, new Random(0), data));
            for (Direction face : Direction.values()) {
                allQuads.addAll(getLayerQuads(state, face, renderType, new Random(0), data));
            }
        }
        return allQuads;
    }

    public abstract List<BakedQuad> getLayerQuads(BlockState state, Direction side, RenderType renderType, Random rand, IModelData data);

}
