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

/**
 * 表示一个星形线
 *
 * @author Zoyn
 */
@SuppressWarnings("ALL")
public class Astroid extends ParticleObj implements Playable {

    private double radius;
    private double step;

    private double currentT = 0D;

    /**
     * 构造一个星形线
     *
     * @param origin 原点
     */
    public Astroid(Location origin, ParticleSpawner spawner) {
        this(1D, origin, spawner);
    }

    /**
     * 构造一个星形线
     *
     * @param radius 半径
     * @param origin 原点
     */
    public Astroid(double radius, Location origin, ParticleSpawner spawner) {
        super(spawner);
        this.radius = radius;
        setOrigin(origin);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        for (double t = 0.0D; t < 360.0D; t += step) {
            double radians = Math.toRadians(t);
            // 计算公式
            double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
            double z = Math.pow(this.radius * Math.sin(radians), 3.0D);

            Location showLocation = getOrigin().clone().add(x, 0, z);
            if (hasMatrix()) {
                Vector vector = new Vector(x, 0, z);
                Vector changed = getMatrix().applyVector(vector);

                showLocation = getOrigin().clone().add(changed);
            }

            showLocation.add(getIncrementX(), getIncrementY(), getIncrementZ());
            points.add(showLocation);
        }
        return points;
    }

    @Override
    public void show() {
        for (double t = 0.0D; t < 360.0D; t++) {
            double radians = Math.toRadians(t);
            // 计算公式
            double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
            double z = Math.pow(this.radius * Math.sin(radians), 3.0D);
            spawnParticle(getOrigin().clone().add(x, 0, z));
        }
    }

    @Override
    public void play() {
        RunnerDslKt.syncBukkitRunner(0, getPeriod(), new BukkitRunnable() {
            @Override
            public void run() {
                // 进行关闭
                // 重置
                if (currentT > 360D) {
                    cancel();
                    return ;
                }
                currentT += step;
                double radians = Math.toRadians(currentT);
                // 计算公式
                double x = Math.pow(getRadius() * Math.cos(radians), 3.0D);
                double z = Math.pow(getRadius() * Math.sin(radians), 3.0D);

                spawnParticle(getOrigin().clone().add(x, 0, z));
                return;
            }
        });
    }

    @Override
    public void playNextPoint() {
        currentT += step;
        double radians = Math.toRadians(currentT);
        // 计算公式
        double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
        double z = Math.pow(this.radius * Math.sin(radians), 3.0D);

        spawnParticle(getOrigin().clone().add(x, 0, z));
        // 重置
        if (currentT > 360D) {
            currentT = 0D;
        }
    }
}
