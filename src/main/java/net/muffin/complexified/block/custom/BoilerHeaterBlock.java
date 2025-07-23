package net.muffin.complexified.block.custom;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntity;
import net.muffin.complexified.block.entity.ModBlockEntities;
import net.muffin.complexified.foundation.block.behaviour.ConnectivityHandlerShort;
import net.muffin.complexified.foundation.block.behaviour.movement.ComplexifiedMovementChecks;
import net.muffin.complexified.foundation.lang.ComplexifiedLang;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.IntStream;

public class BoilerHeaterBlock extends Block implements IWrenchable, IBE<BoilerHeaterBlockEntity> {
    private static final VoxelShape BLOCK_SHAPE = Block.box(0,0,0,16,15,16);

    public static final EnumProperty<GrateCorner> HEATER_GRATE = EnumProperty.create("grate", GrateCorner.class);

    public static boolean isHeater(BlockState state) {
        return state.getBlock() instanceof BoilerHeaterBlock;
    }

    @Override
    public Class<BoilerHeaterBlockEntity> getBlockEntityClass() {
        return BoilerHeaterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BoilerHeaterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BOILER_HEATER.get();
    }

    public static BlockMovementChecks.CheckResult isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, Direction direction) {
        if (state.getBlock() instanceof BoilerHeaterBlock)
            return BlockMovementChecks.CheckResult.of(ConnectivityHandlerShort.isConnected(world, pos, pos.relative(direction)));
        else return BlockMovementChecks.CheckResult.PASS;
    }

    public enum GrateCorner implements StringRepresentable {
        n(Direction.NORTH),
        e(Direction.EAST),
        s(Direction.SOUTH),
        w(Direction.WEST),
        ne(Direction.NORTH, Direction.EAST),
        se(Direction.EAST, Direction.SOUTH),
        sw(Direction.SOUTH, Direction.WEST),
        nw(Direction.WEST, Direction.NORTH),
        none,
        all(Iterate.horizontalDirections),
        ;

        public static final GrateCorner[] GRATE_SIDES = {nw, n, ne, w, none, e, sw, s, se};

        private final Edge[] edgeSide;
        private final Direction[] directions;

        GrateCorner(Direction... directions) {
            this.directions = directions;
            this.edgeSide = this.directions.length == 2 ? new Edge[]{Edge.LEFT, Edge.RIGHT} :
                    Edge.fill(Edge.middle, directions.length);
        }

        public Direction[] getDirections() {
            return directions;
        }

        public Pair<Direction, Edge>[] getGrateData() {
            return IntStream.range(0, this.directions.length)
                    .mapToObj(i -> Pair.of(this.directions[i], this.edgeSide[i]))
                    .toArray(Pair[]::new);
        }
        @Override
        public String getSerializedName() {
            return ComplexifiedLang.asId(name());
        }

        public enum Edge implements StringRepresentable {
            close,
            middle,
            far;

            public static final Edge NORTH = close;
            public static final Edge WEST = close;
            public static final Edge SOUTH = far;
            public static final Edge EAST = far;

            public static final Edge RIGHT = far;
            public static final Edge LEFT = close;

            private static Edge[] fill(Edge edge, int amount) {
                Edge[] edgeArr = new Edge[amount];
                Arrays.fill(edgeArr, edge);
                return edgeArr;
            }

            public static Edge getEdge(int offset, int width) {
                return offset != 0 ? offset == width - 1 ? far : middle : close;
            }

            @Override
            public String getSerializedName() {
                return ComplexifiedLang.asId(
                        switch (this) {
                            case far -> "right";
                            case middle ->  "middle";
                            case close -> "left";
                        }
                );
            }
        }

        public static GrateCorner mergeEdge(Edge xEdge, Edge zEdge) {
            int index = zEdge.ordinal() * 3 + xEdge.ordinal();
            return GRATE_SIDES[index];
        }
    }

    public BoilerHeaterBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState()
                .setValue(HEATER_GRATE, GrateCorner.all));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState>   pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(HEATER_GRATE);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        withBlockEntityDo(world, pos, BoilerHeaterBlockEntity::updateConnectivity);
    }


    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof BoilerHeaterBlockEntity heaterBE))
                return;
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(heaterBE);
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return BLOCK_SHAPE;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public static float getHeat(Level level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof BoilerHeaterBlockEntity heater)) {
            return BoilerHeater.NO_HEAT;
        }
        return heater.getHeatValue();
    }

}
