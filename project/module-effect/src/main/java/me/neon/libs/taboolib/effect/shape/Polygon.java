package me.neon.libs.taboolib.effect.shape;

import me.neon.libs.taboolib.effect.ParticleObj;
import me.neon.libs.taboolib.effect.ParticleSpawner;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示一个正多边形
 *
 * @author Zoyn
 */
public class Polygon extends ParticleObj {

    private final List<Location> locations;

    /**
     * 边数
     */
    private int side;
    private double step;

    private double radius;

    /**
     * 构造一个正多边形
     *
     * @param side   边数
     * @param origin 原点
     */
    public Polygon(int side, Location origin, ParticleSpawner spawner) {
        this(side, origin, 0.02, spawner);
    }

    /**
     * 构造一个正多边形
     *
     * @param side   边数
     * @param origin 原点
     * @param step   步长
     */
    public Polygon(int side, Location origin, double step, ParticleSpawner spawner) {
        super(spawner);
        if (side <= 2) {
            throw new IllegalArgumentException("side <= 2");
        }
        this.side = side;
        setOrigin(origin);
        this.step = step;
        this.locations = new ArrayList<>();
        resetLocations();
    }

    /**
     * 获取正多边形的边数
     *
     * @return 正多边形边数
     */
    public int getSide() {
        return side;
    }

    /**
     * 设置正多边形的边数
     *
     * @param side 边数
     * @return {@link Polygon}
     */
    public Polygon setSide(int side) {
        this.side = side;
        resetLocations();
        return this;
    }

    /**
     * 获取正多边形渲染粒子之间的间距
     *
     * @return 粒子之间的间距
     */
    public double getStep() {
        return step;
    }

    /**
     * 设置正多边形渲染粒子之间的间距
     *
     * @param step 给定的间距
     * @return {@link Polygon}
     */
    public Polygon setStep(double step) {
        this.step = step;
        resetLocations();
        return this;
    }

    /**
     * 获取正多边形的半径
     *
     * @return 正多边形的半径
     */
    public double getRadius() {
        return radius;
    }

    /**
     * 设置正多边形的半径
     *
     * @param radius 正多边形的半径
     * @return {@link Polygon}
     */
    public Polygon setRadius(double radius) {
        this.radius = radius;
        resetLocations();
        return this;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = new ArrayList<>();
        List<Location> temp = new ArrayList<>();

        for (double angle = 0; angle <= 360; angle += 360D / side) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians);
            double z = Math.sin(radians);

            temp.add(getOrigin().clone().add(x, 0, z));
        }
        for (int i = 0; i < temp.size(); i++) {
            if (i + 1 == temp.size()) {
                Vector vectorAB = temp.get(i).clone().subtract(temp.get(0)).toVector();
                double vectorLength = vectorAB.length();
                vectorAB.normalize();
                for (double j = 0; j < vectorLength; j += step) {
                    points.add(temp.get(0).clone().add(vectorAB.clone().multiply(j)));
                }
                break;
            }

            Vector vectorAB = temp.get(i + 1).clone().subtract(temp.get(i)).toVector();
            double vectorLength = vectorAB.length();
            vectorAB.normalize();
            for (double j = 0; j < vectorLength; j += step) {
                points.add(temp.get(i).clone().add(vectorAB.clone().multiply(j)));
            }
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
    public void show() {
        if (locations.isEmpty()) {
            return;
        }
        for (int i = 0; i < locations.size(); i++) {
            if (i + 1 == locations.size()) {
                buildLine(locations.get(i), locations.get(0), step);
                break;
            }
            buildLine(locations.get(i), locations.get(i + 1), step);
        }
    }

    /**
     * 重设渲染粒子的所有Location点位
     */
    public void resetLocations() {
        locations.clear();
        for (double angle = 0; angle <= 360; angle += 360D / side) {
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * radius;
            double z = Math.sin(radians) * radius;
            locations.add(getOrigin().clone().add(x, 0, z));
        }
    }

    /**
     * 此方法只用于 Polygon
     *
     * @param locA 点A
     * @param locB 点B
     * @param step 步长
     */
    private void buildLine(Location locA, Location locB, double step) {
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        for (double i = 0; i < vectorLength; i += step) {
            spawnParticle(locA.clone().add(vectorAB.clone().multiply(i)));
        }
    }
}
