package codechicken.lib.raytracer;

import codechicken.lib.vec.Vector3;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

//TODO Copyable.
public class DistanceRayTraceResult extends RayTraceResult implements Comparable<DistanceRayTraceResult> {

    /**
     * The square distance from the start of the raytrace.
     */
    public double dist;
    private final Direction face;
    private final BlockPos pos;
    private final boolean isMiss;
    private final boolean inside;
    private final Object data;

    public DistanceRayTraceResult(Vector3 hitVec, Direction faceIn, BlockPos posIn, boolean isInside, Object data, double dist) {
        this(false, hitVec.vec3(), faceIn, posIn, isInside, data, dist);
    }

    protected DistanceRayTraceResult(boolean isMissIn, Vec3d hitVec, Direction faceIn, BlockPos posIn, boolean isInside, Object data, double dist) {
        super(hitVec);
        setData(data);
        this.dist = dist;
        this.isMiss = isMissIn;
        this.face = faceIn;
        this.pos = posIn;
        this.inside = isInside;
        this.data = data;
    }

    public void setData(Object data) {
        if (data instanceof Integer) {
            subHit = (Integer) data;
        }
        hitInfo = data;
    }

    public DistanceRayTraceResult withFace(Direction newFace) {
        return new DistanceRayTraceResult(getType() == Type.MISS, getHitVec(), getFace(), getPos(), isInside(), hitInfo, dist);
    }

    public DistanceRayTraceResult offsetHit(BlockPos pos) {
        return new DistanceRayTraceResult(this.isMiss, hitResult.add(pos.getX(), pos.getY(), pos.getZ()), this.face, this.pos, this.inside, this.data, this.dist);
    }

    @Override
    public int compareTo(DistanceRayTraceResult o) {
        return Double.compare(dist, o.dist);
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", subHit=" + subHit + ", sqDist: " + dist + "}";
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getFace() {
        return this.face;
    }

    public Type getType() {
        return this.isMiss ? Type.MISS : Type.BLOCK;
    }

    public boolean isInside() {
        return this.inside;
    }
}
