package me.neon.libs.taboolib.effect.shape;

import kotlin.Unit;
import me.neon.libs.taboolib.effect.ParticleObj;
import me.neon.libs.taboolib.effect.ParticleSpawner;
import me.neon.libs.taboolib.effect.Playable;
import me.neon.libs.util.RunnerDslKt;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代表一个射线
 *
 * @author Zoyn IceCold
 */
public class Ray extends ParticleObj implements Playable {

    private Vector direction;
    private double maxLength;
    private double step;
    private double range;
    private RayStopType stopType;
    private double currentStep = 0D;

    public Ray(Location origin, Vector direction, double maxLength , ParticleSpawner spawner) {
        this(origin, direction, maxLength, 0.2D , spawner);
    }

    public Ray(Location origin, Vector direction, double maxLength , double step , ParticleSpawner spawner) {
        this(origin, direction, maxLength, step, 0.5D, RayStopType.MAX_LENGTH , 20L , spawner);
    }

    public Ray(Location origin, Vector direction, double maxLength, double step, double range, RayStopType stopType , long period , ParticleSpawner spawner) {
        super(spawner);
        setOrigin(origin);
        this.direction = direction;
        this.maxLength = maxLength;
        this.step = step;
        this.range = range;
        this.stopType = stopType;
        setPeriod(period);
    }

    @Override
    public void show() {
        for (double i = 0; i < maxLength; i += step) {
            Vector vectorTemp = direction.clone().multiply(i);
            Location spawnLocation = getOrigin().clone().add(vectorTemp);

            spawnParticle(spawnLocation);

        }
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();

        for (double i = 0; i < maxLength; i += step) {
            Vector vectorTemp = direction.clone().multiply(i);
            Location spawnLocation = getOrigin().clone().add(vectorTemp);

            points.add(spawnLocation);
        }

        // 做一个对 Matrix 和 Increment 的兼容
        return points.stream().map(location -> {
            Location showLocation = location;
            if (hasMatrix()) {
                Vector v = new Vector(location.getX() - getOrigin().getX(), location.getY() - getOrigin().getY(), location.getZ() - getOrigin().getZ());
                Vector changed = getMatrix().applyVector(v);

                showLocation = getOrigin().clone().add(changed);
            }

            showLocation.add(getIncrementX(), getIncrementY(), getIncrementZ());
            return showLocation;
        }).collect(Collectors.toList());
    }

    @Override
    public void play() {
        RunnerDslKt.syncBukkitRunner(0 , getPeriod() , new BukkitRunnable() {
            @Override
            public void run() {
                // 进行关闭
                if (currentStep > maxLength) {
                    cancel();
                }
                currentStep += step;
                Vector vectorTemp = direction.clone().multiply(currentStep);
                Location spawnLocation = getOrigin().clone().add(vectorTemp);

                spawnParticle(spawnLocation);
            }
        });
    }

    @Override
    public void playNextPoint() {
        currentStep += step;
        Vector vectorTemp = direction.clone().multiply(currentStep);
        Location spawnLocation = getOrigin().clone().add(vectorTemp);

        spawnParticle(spawnLocation);

        if (currentStep > maxLength) {
            currentStep = 0D;
        }
    }

    public Vector getDirection() {
        return direction;
    }

    public Ray setDirection(Vector direction) {
        this.direction = direction;
        return this;
    }

    public double getMaxLength() {
        return maxLength;
    }

    public Ray setMaxLength(double maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public double getStep() {
        return step;
    }

    public Ray setStep(double step) {
        this.step = step;
        return this;
    }

    public double getRange() {
        return range;
    }

    public Ray setRange(double range) {
        this.range = range;
        return this;
    }

    public RayStopType getStopType() {
        return stopType;
    }

    public Ray setStopType(RayStopType stopType) {
        this.stopType = stopType;
        return this;
    }

    public enum RayStopType {
        /**
         * 固定长度(同时也是最大长度)
         */
        MAX_LENGTH,
    }

}
