package com.nisovin.magicspells.util.ai.vanilla;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.nisovin.magicspells.MagicSpells;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class FollowOwnerGoal implements Goal<Mob> {

    public static final GoalKey<@NotNull Mob> KEY = GoalKey.of(
            Mob.class,
            new NamespacedKey(MagicSpells.getInstance(), "follow_owner")
    );

    private final Entity owner;
    private final Mob entity;

    private final float moveSpeed;
    private final double followRange;
    private final double teleportIfDistance;

    public FollowOwnerGoal(Entity owner, Mob entity, float moveSpeed, double followRange, double teleportIfDistance) {
        this.owner = owner;
        this.entity = entity;
        this.moveSpeed = moveSpeed;
        this.followRange = followRange;
        this.teleportIfDistance = teleportIfDistance;
    }

    @Override
    public boolean shouldActivate() {
        return owner.isValid() && entity.isValid() && entity.getTarget() == null;
    }

    @Override
    public void tick() {
        if (!owner.isValid() || !entity.isValid()) return;

        Location ownerLocation = owner.getLocation();
        if (!ownerLocation.getWorld().equals(entity.getWorld())) {
            if (Double.isFinite(teleportIfDistance)) {
                entity.getPathfinder().stopPathfinding();
                entity.teleport(ownerLocation);
            }
            return;
        }

        double distanceFromOwner = entity.getLocation().distance(ownerLocation);
        if (distanceFromOwner > teleportIfDistance) {
            entity.getPathfinder().stopPathfinding();
            entity.teleport(ownerLocation);
            return;
        }

        Location followLocation = ownerLocation.clone();
        followLocation.add(followLocation.getDirection().setY(0).normalize().multiply(followRange));
        entity.getPathfinder().moveTo(followLocation, moveSpeed);
    }

    @Override
    public @NotNull GoalKey<@NotNull Mob> getKey() {
        return KEY;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
}
