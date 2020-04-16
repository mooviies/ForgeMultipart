package codechicken.lib.render;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.IModelTransform;

/**
 * Created by covers1624 on 5/16/2016.
 * Same as a SimpleModelState except copy's the input map, saves BS when creating IModelStates.
 */
public final class CCModelTransform implements IModelTransform
{
    public static final CCModelTransform IDENTITY = new CCModelTransform(TransformationMatrix.identity());

    private final ImmutableMap<?, TransformationMatrix> map;
    private final TransformationMatrix base;

    public CCModelTransform(ImmutableMap<?, TransformationMatrix> map)
    {
        this(map, TransformationMatrix.identity());
    }

    public CCModelTransform(TransformationMatrix base)
    {
        this(ImmutableMap.of(), base);
    }

    public CCModelTransform(ImmutableMap<?, TransformationMatrix> map, TransformationMatrix base)
    {
        this.map = ImmutableMap.copyOf(map);
        this.base = base;
    }

    @Override
    public TransformationMatrix getRotation()
    {
        return base;
    }

    @Override
    public TransformationMatrix getPartTransformation(Object part)
    {
        return map.getOrDefault(part, TransformationMatrix.identity());
    }
}
