package net.muffin.complexified.block.custom;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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
import net.muffin.complexified.foundation.block.wrenchInterface.IWrenchableWithHeatingElement;
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater.HeatingElementBlockEntityBehaviour;
import net.muffin.complexified.foundation.lang.ComplexifiedLang;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class BoilerHeaterBlock extends Block implements IWrenchableWithHeatingElement, IBE<BoilerHeaterBlockEntity> {
    private static final VoxelShape BLOCK_SHAPE = Block.box(0,0,0,16,15,16);

    public static final EnumProperty<GrateCorner> HEATER_GRATE = EnumProperty.create("grate", GrateCorner.class);
    public static final EnumProperty<HeaterElement> HEATING_ELEMENT = EnumProperty.create("heating_element", HeaterElement.class);

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

    @Override
    public Optional<ItemStack> removeElement(BlockGetter world, BlockPos pos, boolean inOnReplacedContext) {
        HeatingElementBlockEntityBehaviour behaviour =
                HeatingElementBlockEntityBehaviour.get(world, pos, HeatingElementBlockEntityBehaviour.TYPE);
        if (behaviour == null)
            return Optional.empty();
        BlockState bracket = behaviour.removeElement(inOnReplacedContext);
        if (bracket == null)
            return Optional.empty();
        return Optional.of(new ItemStack(bracket.getBlock()));
    }

    public enum HeaterElement implements StringRepresentable {
        none,
        copper
        ;

        @Override
        public @NotNull String getSerializedName() {
            return ComplexifiedLang.asId(
                    switch (this) {
                        case none -> "none";
                        case copper -> "copper";
                    }
            );
        }
    }

    public enum GrateCorner implements StringRepresentable {
        nw(Direction.WEST, Direction.NORTH),
        n(Direction.NORTH),
        ne(Direction.NORTH, Direction.EAST),
        w(Direction.WEST),
        none,
        e(Direction.EAST),
        sw(Direction.SOUTH, Direction.WEST),
        s(Direction.SOUTH),
        se(Direction.EAST, Direction.SOUTH),
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

        public GrateCorner mirror(Mirror mirror) {
            if (this == all || this == none || mirror == Mirror.NONE) return this;

            boolean x = mirror == Mirror.FRONT_BACK;

            int index = this.ordinal();
            int row = index % 3; // x
            int col = index / 3; // z

            if (x)
                row = 2 - row;
            else
                col = 2 - col;

            return values()[col * 3 + row];
        }

        public GrateCorner rotate(Rotation rotation) {
            if (this == all || this == none) return this;

            int index = this.ordinal();
            int row = index % 3 - 1; // x
            int col = index / 3 - 1; // z

            int newCol;
            int newRow;
            switch (rotation) {
                case CLOCKWISE_90 -> {
                    newCol = row;
                    newRow = -col;
                }
                case COUNTERCLOCKWISE_90 -> {
                    newCol = -row;
                    newRow = col;
                }
                case CLOCKWISE_180 -> {
                    newCol = -col;
                    newRow = -row;
                }
                case NONE -> {
                    newCol = col;
                    newRow = row;
                }
                default -> {
                    newCol = 0;
                    newRow = 0;
                }
            }

            return values()[(1 + newCol) * 3 + (1 + newRow)];
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
            return values()[index];
        }
    }

    public BoilerHeaterBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState()
                .setValue(HEATER_GRATE, GrateCorner.all)
                .setValue(HEATING_ELEMENT, HeaterElement.none));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState>   pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(HEATER_GRATE);
        pBuilder.add(HEATING_ELEMENT);
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
        return IWrenchableWithHeatingElement.super.onSneakWrenched(state, context);
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

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        GrateCorner grate = state.getValue(BoilerHeaterBlock.HEATER_GRATE);
        return state.setValue(HEATER_GRATE, grate.mirror(mirror));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        GrateCorner grate = state.getValue(BoilerHeaterBlock.HEATER_GRATE);
        return state.setValue(HEATER_GRATE, grate.rotate(rotation));
    }
}
