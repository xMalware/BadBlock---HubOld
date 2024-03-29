package fr.badblock.bukkit.hub.v1.effectlib.effect;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import fr.badblock.bukkit.hub.v1.effectlib.Effect;
import fr.badblock.bukkit.hub.v1.effectlib.EffectManager;
import fr.badblock.bukkit.hub.v1.effectlib.EffectType;
import fr.badblock.bukkit.hub.v1.effectlib.util.ParticleEffect;

public class LineEffect extends Effect {

	/**
	 * Should it do a zig zag?
	 */
	public boolean isZigZag = false;

	/**
	 * Length of arc A non-zero value here will use a length instead of the
	 * target endpoint
	 */
	public double length = 0;

	/**
	 * ParticleType of spawned particle
	 */
	public ParticleEffect particle = ParticleEffect.FLAME;

	/**
	 * Particles per arc
	 */
	public int particles = 100;

	/**
	 * Internal counter
	 */
	protected int step = 0;

	/**
	 * Internal boolean
	 */
	protected boolean zag = false;

	/**
	 * Direction of zig-zags
	 */
	public Vector zigZagOffset = new Vector(0, 0.1, 0);

	/**
	 * Number of zig zags in the line
	 */
	public int zigZags = 10;

	public LineEffect(EffectManager effectManager) {
		super(effectManager);
		type = EffectType.REPEATING;
		period = 1;
		iterations = 1;
	}

	@Override
	public void onRun() {
		Location location = getLocation();
		Location target = null;
		if (length > 0) {
			target = location.clone().add(location.getDirection().normalize().multiply(length));
		} else {
			target = getTarget();
		}
		double amount = particles / zigZags;
		if (target == null) {
			cancel();
			return;
		}
		Vector link = target.toVector().subtract(location.toVector());
		float length = (float) link.length();
		link.normalize();

		float ratio = length / particles;
		Vector v = link.multiply(ratio);
		Location loc = location.clone().subtract(v);
		for (int i = 0; i < particles; i++) {
			if (isZigZag) {
				if (zag) {
					loc.add(zigZagOffset);
				} else {
					loc.subtract(zigZagOffset);
				}
			}
			if (step >= amount) {
				if (zag) {
					zag = false;
				} else {
					zag = true;
				}
				step = 0;
			}
			step++;
			loc.add(v);
			display(particle, loc);
		}
	}

}
