package codechicken.lib.render;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.lighting.LC;
import codechicken.lib.lighting.LightMatrix;
import codechicken.lib.render.buffer.BakingVertexBuffer;
import codechicken.lib.render.pipeline.CCRenderPipeline;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.render.pipeline.IVertexSource;
import codechicken.lib.render.pipeline.VertexAttribute;
import codechicken.lib.render.pipeline.attribute.*;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.Vertex5;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

/**
 * The core of the CodeChickenLib render system.
 * Where possible assign a local var of CCRenderState to avoid millions of calls to instance();
 * Uses a ThreadLocal system to assign each thread their own CCRenderState so we can use it in Multithreaded chunk batching.
 * TODO, proper piping of BakedQuads and CCBakedQuads.
 */
public class CCRenderState {

    private static int nextOperationIndex;

    public static int registerOperation() {
        return nextOperationIndex++;
    }

    public static int operationCount() {
        return nextOperationIndex;
    }

    //Each attrib needs to be assigned in this order to have a valid operation index.
    public final VertexAttribute<Vector3[]> normalAttrib = new NormalAttribute();
    public final VertexAttribute<int[]> colourAttrib = new ColourAttribute();
    public final VertexAttribute<int[]> lightingAttrib = new LightingAttribute();
    public final VertexAttribute<int[]> sideAttrib = new SideAttribute();
    public final VertexAttribute<LC[]> lightCoordAttrib = new LightCoordAttribute();

    private static final ThreadLocal<CCRenderState> instances = ThreadLocal.withInitial(CCRenderState::new);

    //pipeline state
    public IVertexSource model;
    public int firstVertexIndex;
    public int lastVertexIndex;
    public int vertexIndex;
    public CCRenderPipeline pipeline;
    @OnlyIn (Dist.CLIENT)
    public BufferBuilder r;
    @OnlyIn (Dist.CLIENT)
    public VertexFormat fmt;

    //context
    public int baseColour;
    public int alphaOverride;
    public boolean computeLighting;
    public LightMatrix lightMatrix = new LightMatrix();

    //vertex outputs
    public final Vertex5 vert = new Vertex5();
    public final Vector3 normal = new Vector3();
    public int colour;
    public int brightness;

    //attribute storage
    public int side;
    public LC lc = new LC();
    @OnlyIn (Dist.CLIENT)
    public TextureAtlasSprite sprite;

    private CCRenderState() {
        pipeline = new CCRenderPipeline(this);
    }

    public static CCRenderState instance() {
        return instances.get();
    }

    public void reset() {
        model = null;
        pipeline.reset();
        computeLighting = true;
        baseColour = alphaOverride = -1;
    }

    public void preRenderWorld(ILightReader world, BlockPos pos) {
        this.reset();
        this.colour = 0xFFFFFFFF;
        this.setBrightness(world, pos);
    }

    public void setPipeline(IVertexOperation... ops) {
        pipeline.setPipeline(ops);
    }

    public void setPipeline(IVertexSource model, int start, int end, IVertexOperation... ops) {
        pipeline.reset();
        pipeline.forceFormatAttributes = false;
        setModel(model, start, end);
        pipeline.forceFormatAttributes = true;
        pipeline.setPipeline(ops);
    }

    public void bindModel(IVertexSource model) {
        if (this.model != model) {
            this.model = model;
            pipeline.rebuild();
        }
    }

    public void setModel(IVertexSource source) {
        setModel(source, 0, source.getVertices().length);
    }

    public void setModel(IVertexSource source, int start, int end) {
        bindModel(source);
        setVertexRange(start, end);
    }

    public void setVertexRange(int start, int end) {
        firstVertexIndex = start;
        lastVertexIndex = end;
    }

    public void render(IVertexOperation... ops) {
        setPipeline(ops);
        render();
    }

    public void render() {
        Vertex5[] verts = model.getVertices();
        for (vertexIndex = firstVertexIndex; vertexIndex < lastVertexIndex; vertexIndex++) {
            model.prepareVertex(this);
            vert.set(verts[vertexIndex]);
            runPipeline();
            writeVert();
        }
    }

    public void runPipeline() {
        pipeline.operate();
    }

    public void writeVert() {
        if (r instanceof BakingVertexBuffer) {
            ((BakingVertexBuffer) r).setSprite(sprite);
        }
        for (int e = 0; e < fmt.getElementCount(); e++) {
            VertexFormatElement fmte = fmt.getElement(e);
            switch (fmte.getUsage()) {
                case POSITION:
                    r.pos(vert.vec.x, vert.vec.y, vert.vec.z);
                    break;
                case UV:
                    if (fmte.getIndex() == 0) {
                        r.tex(vert.uv.u, vert.uv.v);
                    } else {
                        r.lightmap(brightness >> 16 & 65535, brightness & 65535);
                    }
                    break;
                case COLOR:
                    r.color(colour >>> 24, colour >> 16 & 0xFF, colour >> 8 & 0xFF, alphaOverride >= 0 ? alphaOverride : colour & 0xFF);
                    break;
                case NORMAL:
                    r.normal((float) normal.x, (float) normal.y, (float) normal.z);
                    break;
                case PADDING:
                    break;
                default:
                    throw new UnsupportedOperationException("Generic vertex format element");
            }
        }
        r.endVertex();
    }

    public void pushColour() {
        GlStateManager.color4f((colour >>> 24) / 255F, (colour >> 16 & 0xFF) / 255F, (colour >> 8 & 0xFF) / 255F, (alphaOverride >= 0 ? alphaOverride : colour & 0xFF) / 255F);
    }

    public void setBrightness(ILightReader world, BlockPos pos) {
        brightness = world.getBlockState(pos).getLightValue(world, pos);
    }

    public void pullLightmap() {
        brightness = (int) GLX.lastBrightnessY << 16 | (int) GLX.lastBrightnessX;
    }

    public void pushLightmap() {
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, brightness & 0xFFFF, brightness >>> 16);
    }

    public void setFluidColour(FluidStack fluidStack) {
        setFluidColour(fluidStack, 0xFF);
    }

    public void setFluidColour(FluidStack fluidStack, int alpha) {
        this.baseColour = fluidStack.getFluid().getAttributes().getColor(fluidStack) << 8 | alpha;
    }

    public void setColour(Colour colour) {
        this.colour = colour.rgba();
    }

    public ColourRGBA getColour() {
        return new ColourRGBA(colour);
    }

    @OnlyIn (Dist.CLIENT)
    public BufferBuilder startDrawing(int mode, VertexFormat format) {
        BufferBuilder r = Tessellator.getInstance().getBuffer();
        r.begin(mode, format);
        bind(r);
        return r;
    }

    @OnlyIn (Dist.CLIENT)
    public BufferBuilder startDrawing(int mode, VertexFormat format, BufferBuilder buffer) {
        buffer.begin(mode, format);
        bind(buffer);
        return buffer;
    }

    @OnlyIn (Dist.CLIENT)
    public void bind(BufferBuilder r) {
        this.r = r;
        fmt = r.getVertexFormat();
    }

    @OnlyIn (Dist.CLIENT)
    public BufferBuilder getBuffer() {
        return r;
    }

    @OnlyIn (Dist.CLIENT)
    public VertexFormat getVertexFormat() {
        return fmt;
    }

    public void draw() {
        Tessellator.getInstance().draw();
    }
}