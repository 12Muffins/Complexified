package net.muffin.complexified.block.custom;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntityOld;
import net.muffin.complexified.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class BoilerHeaterStructuralBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<BoilerHeaterBlockEntityOld> {
    public static final Logger LOGGER = LogUtils.getLogger();


    public BoilerHeaterStructuralBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return ModBlocks.BOILER_HEATER_OLD.asStack();
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        LOGGER.info("has been placed");
        withBlockEntityDo(pLevel, pPos, BoilerHeaterBlockEntityOld::updateConnectivity);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();

        if (stillValid(level, clickedPos, state, false)) {
            BlockPos masterPos = getMaster(level, clickedPos, state);
            context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new BlockHitResult(context.getClickLocation(), context.getClickedFace(), masterPos,
                            context.isInside()));
            state = level.getBlockState(masterPos);
        }

        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();

        if (stillValid(level, clickedPos, state, false)) {
            BlockPos masterPos = getMaster(level, clickedPos, state);
            context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new BlockHitResult(context.getClickLocation(), context.getClickedFace(), masterPos,
                            context.isInside()));
            state = level.getBlockState(masterPos);
        }

        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        LOGGER.info("Called onRemove");
        if (stillValid(pLevel, pPos, pState, false)) {
            BlockPos master = getMaster(pLevel, pPos, pState);
            LOGGER.info("onRemove success on block: " + master.getX() + " " + master.getY() + " " + master.getZ());
            pLevel.destroyBlock(master, true);
        }
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (stillValid(pLevel, pPos, pState, false)) {
            BlockPos masterPos = getMaster(pLevel, pPos, pState);
            pLevel.destroyBlockProgress(masterPos.hashCode(), masterPos, -1);
            if (!pLevel.isClientSide() && pPlayer.isCreative())
                pLevel.destroyBlock(masterPos, false);
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
                                  BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (stillValid(pLevel, pCurrentPos, pState, false)) {
            BlockPos masterPos = getMaster(pLevel, pCurrentPos, pState);
            if (!pLevel.getBlockTicks()
                    .hasScheduledTick(masterPos, ModBlocks.BOILER_HEATER_OLD.get()))
                pLevel.scheduleTick(masterPos, ModBlocks.BOILER_HEATER_OLD.get(), 1);
            return pState;
        }
        if (!(pLevel instanceof Level level) || level.isClientSide())
            return pState;
        if (!level.getBlockTicks()
                .hasScheduledTick(pCurrentPos, this))
            level.scheduleTick(pCurrentPos, this, 1);
        return pState;
    }


    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state, boolean directlyAdjacent) {
        if (!state.is(this))
            return false;

        Direction direction = state.getValue(FACING);
        BlockPos targetedPos = pos.relative(direction);
        BlockState targetedState = level.getBlockState(targetedPos);

        if (!directlyAdjacent && stillValid(level, targetedPos, targetedState, true))
            return true;
        return targetedState.getBlock() instanceof BoilerHeaterBlockOld
//                && targetedState.getValue(BoilerHeaterBlock.HORIZONTAL_FACING) != direction.getAxis()
                ;
    }

    public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        BlockPos targetedPos = pos.relative(direction);
        BlockState targetedState = level.getBlockState(targetedPos);
        if (targetedState.is(ModBlocks.BOILER_HEATER_STRUCTURAL.get()))
            return getMaster(level, targetedPos, targetedState);
        return targetedPos;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!stillValid(pLevel, pPos, pState, false))
            pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
    }

    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new RenderProperties());
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public Class<BoilerHeaterBlockEntityOld> getBlockEntityClass() {
        return BoilerHeaterBlockEntityOld.class;
    }

    @Override
    public BlockEntityType<? extends BoilerHeaterBlockEntityOld> getBlockEntityType() {
        return ModBlockEntities.BOILER_HEATER_OLD.get();
    }

    public static class RenderProperties implements IClientBlockExtensions, MultiPosDestructionHandler {

        @Override
        public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
            return true;
        }

        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            if (target instanceof BlockHitResult bhr) {
                BlockPos targetPos = bhr.getBlockPos();
                BoilerHeaterStructuralBlock boilerHeaterStructuralBlock = ModBlocks.BOILER_HEATER_STRUCTURAL.get();
                if (boilerHeaterStructuralBlock.stillValid(level, targetPos, state, false))
                    manager.crack(BoilerHeaterStructuralBlock.getMaster(level, targetPos, state), bhr.getDirection());
                return true;
            }
            return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
        }

        @Override
        @Nullable
        public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState blockState, int progress) {
            BoilerHeaterStructuralBlock boilerHeaterStructuralBlock = ModBlocks.BOILER_HEATER_STRUCTURAL.get();
            if (!boilerHeaterStructuralBlock.stillValid(level, pos, blockState, false))
                return null;
            HashSet<BlockPos> set = new HashSet<>();
            set.add(BoilerHeaterStructuralBlock.getMaster(level, pos, blockState));
            return set;
        }
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return false;
    }
}

