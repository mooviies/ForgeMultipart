package codechicken.microblock

import java.util.{Random, LinkedList => JLinkedList, List => JList}

import codechicken.lib.render.CCRenderState
import codechicken.lib.render.pipeline.{ColourMultiplier, IVertexOperation}
import codechicken.lib.texture.TextureUtils
import codechicken.lib.vec.uv.{IconTransformation, MultiIconTransformation, UVTransformation}
import codechicken.lib.vec.{Cuboid6, Vector3}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{RenderType, RenderTypeLookup}
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{Explosion, IWorldReader}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.ForgeHooks

object MaterialRenderHelper {
    private val instances = new ThreadLocal[MaterialRenderHelper] {
        override def initialValue() = new MaterialRenderHelper
    }

    def instance = instances.get()
}

class MaterialRenderHelper {
    private var renderType: RenderType = null
    private var builder = Seq.newBuilder[IVertexOperation]

    def start(pos: Vector3, renderType: RenderType, uvt: UVTransformation) = {
        this.renderType = renderType
        builder.clear()
        builder += pos.translation()
        builder += uvt
        this
    }

    def blockColour(colour: Int) = {
        builder += ColourMultiplier.instance(colour)
        this
    }

    def lighting() = {
        if (renderType != null) {
            builder += CCRenderState.instance().lightMatrix
        }
        this
    }

    def result() = builder.result()
}

/**
 * Standard micro material class suitable for most blocks.
 */
class BlockMicroMaterial(val state: BlockState, val materialID: String) extends IMicroMaterial {
    val blockProperties = state.getProperties
    val random = new Random()

    @OnlyIn(Dist.CLIENT)
    var icont: MultiIconTransformation = _

    @OnlyIn(Dist.CLIENT)
    var pIconT: IconTransformation = _

    @OnlyIn(Dist.CLIENT)
    override def loadIcons() {
        @OnlyIn(Dist.CLIENT)
        def getSideIcon(state: BlockState, s: Int): TextureAtlasSprite = {
            val side = Direction.byIndex(s)
            val model = Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state)
            var winner = if(model.getParticleTexture == null) TextureUtils.getMissingSprite else model.getParticleTexture
            if (model != null) {
                val quads = new JLinkedList[BakedQuad]
                quads.addAll(model.getQuads(state, side, random))
                quads.addAll(model.getQuads(state, null, random).filter((quad: BakedQuad) => quad.getFace eq side))
                if (quads.size > 0) {
                    val list = new JLinkedList[TextureAtlasSprite]

                    for (quad <- quads) {
                        val sprite: TextureAtlasSprite = quad.getSprite
                        list.add(sprite)
                    }
                    if (!list.isEmpty) winner = list.get(0)
                }
            }
            winner
        }

        try {
            icont = new MultiIconTransformation(Array.tabulate(6)(side => getSideIcon(state, side)): _*)
            pIconT = new IconTransformation(TextureUtils.getParticleIconForBlock(state))
        } catch {
            case e: RuntimeException =>
                logger.error(s"unable to load microblock icons for block ${state.getBlock} with state $state")
        }
    }

    override def getMicroRenderOps(pos: Vector3, side: Int, renderType: RenderType, bounds: Cuboid6): Seq[Seq[IVertexOperation]] = {
        Seq(MaterialRenderHelper.instance.start(pos, renderType, icont).blockColour(getColour(renderType)).lighting().result())
    }

    def getColour(renderType: RenderType) = {
        renderType match {
            case null =>
                Minecraft.getInstance.getBlockColors.getColor(state, null, null, 0) << 8 | 0xFF
            case _ =>
                Minecraft.getInstance.getBlockColors.getColor(state,
                    CCRenderState.instance().lightMatrix.access, CCRenderState.instance().lightMatrix.pos, 0) << 8 | 0xFF
        }
    }

    override def canRenderInLayer(renderType: RenderType) = RenderTypeLookup.canRenderInLayer(state, renderType)

    @OnlyIn(Dist.CLIENT)
    def getBreakingIcon(side: Int) = pIconT.icon

    def getItem = new ItemStack(Item.getItemFromBlock(state.getBlock), 1, state.getBlock.getMetaFromState(state))

    def getLocalizedName = getItem.getDisplayName

    def getStrength(player: PlayerEntity) =
        ForgeHooks.blockStrength(state, player, player.world, new BlockPos(0, -1, 0))

    def isTransparent = !state.isOpaqueCube

    def getLightValue = state.getLightValue

    def toolClasses = Seq("axe", "pickaxe", "shovel")

    def getCutterStrength = state.getBlock.getHarvestLevel(state)

    def getSound = state.getSoundType

    def explosionResistance(world: IWorldReader, pos: BlockPos, entity: Entity, explosion: Explosion): Float = state.getExplosionResistance(world, pos, entity, explosion)

    override def getMaterialID: String = materialID

    override def compare(o: IMicroMaterial): Int = {
        o match {
            case other: BlockMicroMaterial =>
                if (state.getBlock == other.state.getBlock) {
                    state.getBlock.getMetaFromState(state) - other.state.getBlock.getMetaFromState(other.state)
                } else {
                    Block.getIdFromBlock(state.getBlock) - Block.getIdFromBlock(other.state.getBlock)
                }
            case _ => getMaterialID.compareTo(o.getMaterialID)
        }
    }
}

/**
 * Utility functions for cleaner registry code
 */
object BlockMicroMaterial {
    def materialKey(block: Block): String =
        materialKey(block.getDefaultState)

    def materialKey(state: BlockState): String = {
        var key: String = state.getBlock.getRegistryName.toString
        val numOfProps = state.getProperties.size
        if (numOfProps > 0) {
            key += "["
            import scala.collection.JavaConversions._
            val properties = state.getProperties.iterator
            while (properties.hasNext) {
                val (p, v) = properties.next()
                key += p.getName + "=" + v.toString
                if (properties.hasNext) {
                    key += ","
                }
            }
            key += "]"
        }
        key
    }

    def createAndRegister(block: Block) {
        createAndRegister(block.getDefaultState)
    }

    def createAndRegister(state: BlockState) {
        val id = materialKey(state)
        MicroMaterialRegistry.registerMaterial(new BlockMicroMaterial(state, id), id)
    }

    def createAndRegister(states: Seq[BlockState]) {
        states.foreach(createAndRegister)
    }

    def createAndRegister(block: Block, metas: Seq[Int]) {
        createAndRegister(metas map block.getStateFromMeta)
    }
}
