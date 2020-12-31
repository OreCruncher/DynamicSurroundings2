/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.footsteps;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.orecruncher.mobeffects.config.Config;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.effects.particles.Collections;
import org.orecruncher.mobeffects.footsteps.accents.FootstepAccents;
import org.orecruncher.mobeffects.library.Constants;
import org.orecruncher.mobeffects.library.FootstepLibrary;
import org.orecruncher.sndctrl.api.acoustics.AcousticEvent;
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.audio.acoustic.AcousticCompiler;

@OnlyIn(Dist.CLIENT)
public class Generator {

	protected static final IModLog LOGGER = MobEffects.LOGGER.createChild(Generator.class);

	public static final double PROBE_DEPTH = 1F / 16F;

	protected static final Random RANDOM = XorShiftRandom.current();
	protected static final int BRUSH_INTERVAL = 2;

	protected final Variator VAR;

	protected float dmwBase;
	protected float dwmYChange;
	protected double yPosition;

	protected double prevX = Double.MIN_VALUE;
	protected double prevY = Double.MIN_VALUE;
	protected double prevZ = Double.MIN_VALUE;

	protected boolean didJump;
	protected boolean isFlying;
	protected float fallDistance;

	protected float lastReference;
	protected boolean isImmobile;
	protected long timeImmobile;

	protected boolean isRightFoot;
	protected boolean isOnLadder;
	protected boolean isInWater;
	protected boolean isSneaking;
	protected boolean isJumping;

	protected double xMovec;
	protected double zMovec;
	protected boolean scalStat;
	protected boolean stepThisFrame;

	protected BlockPos messyPos = BlockPos.ZERO;
	protected long brushesTime;

	// We calc our own because of inconsistencies with Minecraft
	protected double distanceWalkedOnStepModified;
	protected int pedometer;

	protected static final ObjectArray<IAcoustic> accents = new ObjectArray<>();

	public Generator(@Nonnull final Variator var) {
		this.VAR = var;
	}

	public int getPedometer() {
		return this.pedometer;
	}

	public void generateFootsteps(@Nonnull final LivingEntity entity) {

		// If an entity is a passenger or is sleeping then no footsteps to process
		if (entity.isOnePlayerRiding() || entity.isSleeping())
			return;

		// No footstep or print effects for spectators
		if ((entity instanceof PlayerEntity) && entity.isSpectator())
			return;

		// Clear starting state
		this.didJump = false;
		this.stepThisFrame = false;

		this.isOnLadder = entity.isOnLadder();
		this.isInWater = entity.isInWater();
		this.isSneaking = entity.isSneaking();
		this.isJumping = entity.isJumping;

		simulateFootsteps(entity);
		simulateAirborne(entity);
		simulateBrushes(entity);

		if (this.stepThisFrame)
			this.pedometer++;

		if (Constants.FOOTSTEPS.getVolumeScale() > 0) {
			entity.nextStepDistance = Float.MAX_VALUE;
		} else {
			final float dist = entity.nextStepDistance;
			if (dist == Float.MAX_VALUE)
				entity.nextStepDistance = 0;
		}
	}

	protected boolean stoppedImmobile(float reference) {
		final long current = TickCounter.getTickCount();
		final float diff = this.lastReference - reference;
		this.lastReference = reference;
		if (!this.isImmobile && diff == 0) {
			this.timeImmobile = current;
			this.isImmobile = true;
		} else if (this.isImmobile && diff != 0) {
			this.isImmobile = false;
			return current - this.timeImmobile > this.VAR.IMMOBILE_DURATION;
		}

		return false;
	}

	protected void updateWalkedOnStep(@Nonnull final LivingEntity entity) {

		float distance = 0F;

		// First time initialization
		if (Double.compare(this.prevX, Double.MIN_VALUE) == 0) {
			this.prevX = entity.getPosX();
			this.prevY = entity.getPosY();
			this.prevZ = entity.getPosZ();
		} else {
			final double dX = entity.getPosX() - this.prevX;
			final double dY = entity.getPosY() - this.prevY;
			final double dZ = entity.getPosZ() - this.prevZ;

			this.prevX = entity.getPosX();
			this.prevY = entity.getPosY();
			this.prevZ = entity.getPosZ();

			// The amount of distance added is dependent on whether the player
			// is on the ground (moving x/z) or not (moving x/y/z)
			final double sqrt;
			if (entity.isOnGround())
				sqrt = Math.sqrt(dX * dX + dZ * dZ);
			else
				sqrt = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
			distance = (float) sqrt * 0.6F;
		}

		this.distanceWalkedOnStepModified += distance;
	}

	protected void simulateFootsteps(@Nonnull final LivingEntity entity) {

		updateWalkedOnStep(entity);

		final float distanceReference = (float) this.distanceWalkedOnStepModified;

		if (this.dmwBase > distanceReference) {
			this.dmwBase = 0;
			this.dwmYChange = 0;
		}

		final double movX = entity.getMotion().x;
		final double movZ = entity.getMotion().z;

		final double scal = movX * this.xMovec + movZ * this.zMovec;
		if (this.scalStat != scal < 0.001f) {
			this.scalStat = !this.scalStat;

			if (this.scalStat && this.VAR.PLAY_WANDER && !hasSpecialStoppingConditions(entity)) {
				playSinglefoot(entity, 0d, Constants.WANDER, this.isRightFoot);
			}
		}

		this.xMovec = movX;
		this.zMovec = movZ;

		if (entity.isOnGround() || this.isInWater || this.isOnLadder) {
			AcousticEvent event = null;

			float dwm = distanceReference - this.dmwBase;
			final boolean immobile = stoppedImmobile(distanceReference);
			if (immobile && !this.isOnLadder) {
				dwm = 0;
				this.dmwBase = distanceReference;
			}

			float distance = 0f;

			if (entity.isOnLadder() && !entity.isOnGround()) {
				distance = this.VAR.STRIDE_LADDER;
			} else if (!this.isInWater && MathStuff.abs(this.yPosition - entity.getPosY()) > 0.4d) {
				// This ensures this does not get recorded as landing, but as a
				// step
				if (this.yPosition < entity.getPosY()) { // Going upstairs
					distance = this.VAR.STRIDE_STAIR;
					event = speedDisambiguator(entity, Constants.UP, Constants.UP_RUN);
				} else if (!this.isSneaking) { // Going downstairs
					distance = -1f;
					event = speedDisambiguator(entity, Constants.DOWN, Constants.DOWN_RUN);
				}

				this.dwmYChange = distanceReference;

			} else {
				distance = this.VAR.STRIDE;
			}

			if (event == null) {
				event = speedDisambiguator(entity, Constants.WALK, Constants.RUN);
			}

			distance = reevaluateDistance(event, distance);

			if (dwm > distance) {
				produceStep(entity, event, 0F);
				stepped(entity, event);
				this.dmwBase = distanceReference;
			}
		}

		// This fixes an issue where the value is evaluated
		// while the player is between two steps in the air
		// while descending stairs
		if (entity.isOnGround()) {
			this.yPosition = entity.getPosY();
		}
	}

	protected void stepped(@Nonnull final LivingEntity entity, @Nonnull final AcousticEvent event) {
	}

	protected float reevaluateDistance(@Nonnull final AcousticEvent event, final float distance) {
		return distance;
	}

	protected void produceStep(@Nonnull final LivingEntity entity, @Nonnull final AcousticEvent event) {
		produceStep(entity, event, 0d);
	}

	protected void produceStep(@Nonnull final LivingEntity entity, @Nullable AcousticEvent event,
			final double verticalOffsetAsMinus) {
		if (!playSpecialStoppingConditions(entity)) {
			if (event == null)
				event = speedDisambiguator(entity, Constants.WALK, Constants.RUN);
			playSinglefoot(entity, verticalOffsetAsMinus, event, this.isRightFoot);
			this.isRightFoot = !this.isRightFoot;
		}

		this.stepThisFrame = true;
	}

	protected void simulateAirborne(@Nonnull final LivingEntity entity) {
		if ((entity.isOnGround() || this.isOnLadder) == this.isFlying) {
			this.isFlying = !this.isFlying;
			simulateJumpingLanding(entity);
		}

		if (this.isFlying)
			this.fallDistance = entity.fallDistance;
	}

	protected void simulateJumpingLanding(@Nonnull final LivingEntity entity) {
		if (hasSpecialStoppingConditions(entity))
			return;

		if (this.isFlying && this.isJumping) {
			if (this.VAR.EVENT_ON_JUMP) {
				// If climbing stairs motion will be negative
				if (entity.getMotion().y > 0) {
					this.didJump = true;
					final double speed = entity.getMotion().x * entity.getMotion().x + entity.getMotion().z * entity.getMotion().z;

					if (speed < this.VAR.SPEED_TO_JUMP_AS_MULTIFOOT) {
						// STILL JUMP
						playMultifoot(entity, 0.4d, Constants.JUMP);
					} else {
						// RUNNING JUMP
						playSinglefoot(entity, 0.4d, Constants.JUMP, this.isRightFoot);
					}
				}
			}
		} else if (!this.isFlying && this.fallDistance > 0.01F) {
			if (this.fallDistance > this.VAR.LAND_HARD_DISTANCE_MIN) {
				playMultifoot(entity, 0d, Constants.LAND);
			} else if (!this.stepThisFrame && !this.isSneaking) {
				playSinglefoot(entity, 0d, speedDisambiguator(entity, Constants.CLIMB, Constants.CLIMB_RUN),
						this.isRightFoot);
				this.isRightFoot = !this.isRightFoot;
			}
		}
	}

	protected AcousticEvent speedDisambiguator(@Nonnull final LivingEntity entity, @Nonnull final AcousticEvent walk,
			@Nonnull final AcousticEvent run) {
		final double velocity = entity.getMotion().x * entity.getMotion().x + entity.getMotion().z * entity.getMotion().z;
		return velocity > this.VAR.SPEED_TO_RUN ? run : walk;
	}

	protected void simulateBrushes(@Nonnull final LivingEntity entity) {
		final long current = TickCounter.getTickCount();
		if (current >= this.brushesTime) {
			this.brushesTime = current + BRUSH_INTERVAL;
			if (proceedWithStep(entity) && (entity.getMotion().x != 0d || entity.getMotion().z != 0d)) {
				final int yy = MathStuff
						.floor(entity.getPosY() - PROBE_DEPTH - entity.getYOffset() - (entity.isOnGround() ? 0d : 0.25d));
				final BlockPos pos = new BlockPos(entity.getPosX(), yy, entity.getPosZ());
				if (!this.messyPos.equals(pos)) {
					this.messyPos = pos;
					final Association assos = findAssociationMessyFoliage(entity, pos);
					if (assos != null)
						playAssociation(assos, Constants.WALK);
				}
			}
		}
	}

	protected boolean proceedWithStep(@Nonnull final LivingEntity entity) {
		return !this.isSneaking;
	}

	protected void playSinglefoot(@Nonnull final LivingEntity entity, final double verticalOffsetAsMinus,
			@Nonnull final AcousticEvent eventType, final boolean foot) {
		if (proceedWithStep(entity)) {
			final Association assos = findAssociation(entity, verticalOffsetAsMinus, foot);
			playAssociation(assos, eventType);
		}
	}

	protected void playMultifoot(@Nonnull final LivingEntity entity, final double verticalOffsetAsMinus,
			final AcousticEvent eventType) {

		if (proceedWithStep(entity)) {
			// STILL JUMP
			final Association leftFoot = findAssociation(entity, verticalOffsetAsMinus, false);
			final Association rightFoot = findAssociation(entity, verticalOffsetAsMinus, true);
			playAssociation(leftFoot, eventType);
			playAssociation(rightFoot, eventType);
		}
	}

	/**
	 * Play an association.
	 */
	protected void playAssociation(@Nullable final Association assoc, @Nonnull final AcousticEvent eventType) {
		if (assoc != null) {
			assoc.play(eventType);
		}
	}

	protected boolean shouldProducePrint(@Nonnull final LivingEntity entity) {
		return this.VAR.HAS_FOOTPRINT
				&& Config.CLIENT.footsteps.get_enableFootprintParticles()
				&& (entity.isOnGround() || !(this.isJumping || entity.isAirBorne))
				&& !entity.isInvisibleToPlayer(GameUtils.getPlayer());
	}

	/**
	 * Find an association for an entities particular foot. This will fetch the
	 * player angle and use it as a basis to find out what block is below their feet
	 * (or which block is likely to be below their feet if the player is walking on
	 * the edge of a block when walking over non-emitting blocks like air or water).
	 */
	@Nullable
	protected Association findAssociation(@Nonnull final LivingEntity entity,
			final double verticalOffsetAsMinus, final boolean isRightFoot) {

		final float rotDegrees = MathStuff.wrapDegrees(entity.rotationYaw);
		final double rot = MathStuff.toRadians(rotDegrees);
		final float feetDistanceToCenter = isRightFoot ? -this.VAR.DISTANCE_TO_CENTER : this.VAR.DISTANCE_TO_CENTER;

		final double xx = entity.getPosX() + MathStuff.cos(rot) * feetDistanceToCenter;
		final double zz = entity.getPosZ() + MathStuff.sin(rot) * feetDistanceToCenter;
		final double minY = entity.getBoundingBox().minY;
		final FootStrikeLocation loc = new FootStrikeLocation(entity, xx, minY - PROBE_DEPTH - verticalOffsetAsMinus,
				zz);

		final AcousticResolver resolver = new AcousticResolver(entity.getEntityWorld(), loc,
				this.VAR.DISTANCE_TO_CENTER);

		final Association result = addFootstepAccent(entity, resolver.findAssociationForEvent());

		// It is possible that the association has no position, so it
		// needs to be checked.
		if (result != null && shouldProducePrint(entity)) {
			final Vector3d printPos = result.getStrikeLocation().footprintPosition();
			if (printPos != null) {
				FootprintStyle style = this.VAR.FOOTPRINT_STYLE;

				if (entity instanceof PlayerEntity) {
					style = Config.CLIENT.footsteps.get_playerFootprintStyle();
				}

				final Footprint print = Footprint.produce(
						style, entity, printPos, rotDegrees,
						this.VAR.FOOTPRINT_SCALE,
						isRightFoot);

				final Vector3d stepLocation = print.getStepLocation();
				final World world = print.getEntity().getEntityWorld();
				Collections.addFootprint(print.getStyle(), world, stepLocation, print.getRotation(), print.getScale(),
						print.isRightFoot());
			}
		}
		return result;
	}

	/**
	 * Play special sounds that must stop the usual footstep figuring things out
	 * process.
	 */
	protected boolean playSpecialStoppingConditions(@Nonnull final LivingEntity entity) {
		if (entity.isInWater()) {
			if (proceedWithStep(entity)) {
				final FluidState fs = entity.getEntityWorld().getFluidState(new BlockPos(entity.getEyePosition(1F)));
				final AcousticEvent evt = fs.isEmpty() ? Constants.WALK : Constants.SWIM;
				FootstepLibrary.getSwimAcoustic().playAt(entity.getPositionVec(), evt);
			}
			return true;
		}

		return false;
	}

	/**
	 * Tells if footsteps can be played.
	 */
	protected boolean hasSpecialStoppingConditions(@Nonnull final LivingEntity entity) {
		return entity.isInWater();
	}

	@Nullable
	protected Association findAssociationMessyFoliage(@Nonnull final LivingEntity entity, @Nonnull final BlockPos pos) {
		Association result = null;
		final BlockPos up = pos.up();
		final BlockState above = entity.getEntityWorld().getBlockState(up);

		if (above.getMaterial() != Material.AIR) {
			IAcoustic acoustics = FootstepLibrary.getBlockAcoustics(above, Substrate.MESSY);
			if (acoustics == Constants.MESSY_GROUND) {
				acoustics = FootstepLibrary.getBlockAcoustics(above, Substrate.FOLIAGE);
				if (acoustics != Constants.NOT_EMITTER) {
					result = new Association(entity, acoustics);
				}
			}
		}
		return result;
	}

	/**
	 * Adds additional sound overlays to the acoustic based on other environment
	 * aspects, such as armor being worn.
	 */
	@Nullable
	protected Association addFootstepAccent(@Nonnull final LivingEntity entity, @Nullable Association assoc) {
		// Don't apply overlays if the entity is not on the ground
		if (Config.CLIENT.footsteps.get_enableFootstepAccents() && entity.isOnGround()) {
			accents.clear();
			final BlockPos pos = assoc != null ? assoc.getStepPos() : entity.getPosition();
			FootstepAccents.provide(entity, pos, accents);
			if (accents.size() > 0) {
				if (assoc == null) {
					final IAcoustic acoustic = AcousticCompiler.combine(accents);
					assoc = new Association(entity, acoustic);
				} else {
					assoc.merge(accents.toArray(new IAcoustic[0]));
				}
			}
		}

		return assoc;
	}

	@Override
	public String toString() {
		return "didJump: " + this.didJump + ' ' +
				"onLadder: " + this.isOnLadder + ' ' +
				"flying: " + this.isFlying + ' ' +
				"immobile: " + this.isImmobile + ' ' +
				"steps: " + this.pedometer;
	}

}