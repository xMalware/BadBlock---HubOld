package fr.badblock.bukkit.hub.v1.effectlib.effect;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import fr.badblock.bukkit.hub.v1.effectlib.Effect;
import fr.badblock.bukkit.hub.v1.effectlib.EffectManager;
import fr.badblock.bukkit.hub.v1.effectlib.EffectType;
import fr.badblock.bukkit.hub.v1.effectlib.util.ParticleEffect;
import fr.badblock.bukkit.hub.v1.effectlib.util.VectorUtils;

public class DonutEffect extends Effect {

	/**
	 * Amount of circles to build the torus
	 */
	public int circles = 36;

	/**
	 * ParticleType of spawned particle
	 */
	public ParticleEffect particle = ParticleEffect.FLAME;

	/**
	 * Amount of particles inside of a single vertical circle
	 */
	public int particlesCircle = 10;

	/**
	 * Radius of the torus
	 */
	public float radiusDonut = 2;

	/**
	 * Radius of the tube (the circles on the outside.
	 */
	public float radiusTube = .5f;

	/**
	 * Rotation of the torus.
	 */
	public double xRotation, yRotation, zRotation = 0;

	public DonutEffect(EffectManager effectManager) {
		super(effectManager);
		type = EffectType.REPEATING;
		period = 10;
		iterations = 20;
	}

	@Override
	public void onRun() {
		Location location = getLocation();
		Vector v = new Vector();
		for (int i = 0; i < circles; i++) {
			double theta = 2 * Math.PI * i / circles;
			for (int j = 0; j < particlesCircle; j++) {
				double phi = 2 * Math.PI * j / particlesCircle;
				double cosPhi = Math.cos(phi);
				v.setX((radiusDonut + radiusTube * cosPhi) * Math.cos(theta));
				v.setY((radiusDonut + radiusTube * cosPhi) * Math.sin(theta));
				v.setZ(radiusTube * Math.sin(phi));

				VectorUtils.rotateVector(v, xRotation, yRotation, zRotation);

				display(particle, location.add(v));
				location.subtract(v);
			}
		}
	}
}
