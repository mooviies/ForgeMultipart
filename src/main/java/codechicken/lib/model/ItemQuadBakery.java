package codechicken.lib.model;

import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.LambdaUtils;
import codechicken.lib.util.TransformUtils;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.client.model.ItemLayerModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by covers1624 on 13/02/2017.
 */
public class ItemQuadBakery {

    public static List<BakedQuad> bakeItem(List<TextureAtlasSprite> sprites) {
        return bakeItem(sprites, DefaultVertexFormats.ITEM, TransformUtils.DEFAULT_ITEM);
    }

    public static List<BakedQuad> bakeItem(List<TextureAtlasSprite> sprites, IModelTransform modelTransform) {
        return bakeItem(sprites, DefaultVertexFormats.ITEM, modelTransform);
    }

    public static List<BakedQuad> bakeItem(List<TextureAtlasSprite> sprites, VertexFormat format) {
        return bakeItem(sprites, format, TransformUtils.DEFAULT_ITEM);
    }

    public static List<BakedQuad> bakeItem(List<TextureAtlasSprite> sprites, VertexFormat format, IModelTransform modelTransform) {
        return bakeItem(format, modelTransform, sprites.toArray(new TextureAtlasSprite[0]));
    }

    public static List<BakedQuad> bakeItem(TextureAtlasSprite... sprites) {
        return bakeItem(TransformUtils.DEFAULT_ITEM, sprites);
    }

    public static List<BakedQuad> bakeItem(IModelTransform modelTransform, TextureAtlasSprite... sprites) {
        return bakeItem(DefaultVertexFormats.ITEM, modelTransform, sprites);
    }

    public static List<BakedQuad> bakeItem(VertexFormat format, TextureAtlasSprite... sprites) {
        return bakeItem(format, TransformUtils.DEFAULT_ITEM, sprites);
    }

    public static List<BakedQuad> bakeItem(VertexFormat format, IModelTransform modelTransform, TextureAtlasSprite... sprites) {

        LambdaUtils.checkArgument(sprites, "Sprites must not be Null or empty!", ArrayUtils::isNullOrContainsNull);

        List<BakedQuad> quads = new LinkedList<>();
        Optional<TRSRTransformation> transform = modelTransform.apply(Optional.empty());
        for (int i = 0; i < sprites.length; i++) {
            TextureAtlasSprite sprite = sprites[i];
            quads.addAll(ItemLayerModel.getQuadsForSprite(i, sprite, format, transform));
        }
        return quads;
    }

}
