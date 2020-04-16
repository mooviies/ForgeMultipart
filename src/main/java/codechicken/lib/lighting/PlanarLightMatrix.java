package codechicken.lib.lighting;

import codechicken.lib.render.CCRenderState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class PlanarLightMatrix extends PlanarLightModel {

    public static final int operationIndex = CCRenderState.registerOperation();
    public static PlanarLightMatrix instance = new PlanarLightMatrix();

    public ILightReader access;
    public BlockPos pos = BlockPos.ZERO;

    private int sampled = 0;
    public int[] brightness = new int[6];

    public PlanarLightMatrix() {
        super(PlanarLightModel.standardLightModel.colours);
    }

    public PlanarLightMatrix locate(ILightReader a, BlockPos bPos) {
        access = a;
        pos = bPos;
        sampled = 0;
        return this;
    }

    public int brightness(int side) {
        if ((sampled & 1 << side) == 0) {
            BlockState b = access.getBlockState(pos);
            brightness[side] = access.getCombinedLight(pos, b.getBlock().getLightValue(b, access, pos));
            sampled |= 1 << side;
        }
        return brightness[side];
    }

    @Override
    public boolean load(CCRenderState state) {
        state.pipeline.addDependency(state.sideAttrib);
        return true;
    }

    @Override
    public void operate(CCRenderState state) {
        super.operate(state);
        state.brightness = brightness(state.side);
    }

    @Override
    public int operationID() {
        return operationIndex;
    }
}
