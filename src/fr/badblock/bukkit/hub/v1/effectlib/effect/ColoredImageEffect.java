package fr.badblock.bukkit.hub.v1.effectlib.effect;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import fr.badblock.bukkit.hub.v1.effectlib.Effect;
import fr.badblock.bukkit.hub.v1.effectlib.EffectManager;
import fr.badblock.bukkit.hub.v1.effectlib.EffectType;
import fr.badblock.bukkit.hub.v1.effectlib.util.MathUtils;
import fr.badblock.bukkit.hub.v1.effectlib.util.ParticleEffect;
import fr.badblock.bukkit.hub.v1.effectlib.util.VectorUtils;

public class ColoredImageEffect extends Effect {

	public enum Plane {

		X, XY, XYZ, XZ, Y, YZ, Z;
	}

	/**
	 * Turns the cube by this angle each iteration around the x-axis
	 */
	public double angularVelocityX = Math.PI / 200;

	/**
	 * Turns the cube by this angle each iteration around the y-axis
	 */
	public double angularVelocityY = Math.PI / 170;

	/**
	 * Turns the cube by this angle each iteration around the z-axis
	 */
	public double angularVelocityZ = Math.PI / 155;

	/**
	 * Delay between steps
	 */
	protected int delay = 0;

	/**
	 * Should it rotate?
	 */
	public boolean enableRotation = true;

	/**
	 * For configuration-driven files
	 */
	public String fileName = null;

	/**
	 * File of the gif if needed
	 */
	protected File gifFile = null;

	/**
	 * Image as BufferedImage
	 */
	protected BufferedImage image = null;

	/**
	 * Is this a gif image?
	 */
	protected boolean isGif = false;

	/**
	 * Particle to draw the image
	 */
	public ParticleEffect particle = ParticleEffect.REDSTONE;

	/**
	 * What plane should it rotate?
	 */
	public Plane plane = Plane.XYZ;

	/**
	 * Rotation step counter
	 */
	protected int rotationStep = 0;

	/**
	 * Scale the image down
	 */
	public float size = (float) 1 / 40;

	/**
	 * Step counter
	 */
	protected int step = 0;

	/**
	 * Each stepX pixel will be shown. Saves packets for high resolutions.
	 */
	public int stepX = 10;

	/**
	 * Each stepY pixel will be shown. Saves packets for high resolutions.
	 */
	public int stepY = 10;

	public ColoredImageEffect(EffectManager effectManager) throws IOException {
		super(effectManager);
		type = EffectType.REPEATING;
		period = 2;
		iterations = 200;
	}

	// Reads gifs
	private BufferedImage getImg(int s) throws IOException {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		ImageReader reader = ImageIO.getImageReadersBySuffix("GIF").next();
		ImageInputStream in = ImageIO.createImageInputStream(gifFile);
		reader.setInput(in);
		for (int i = 0, count = reader.getNumImages(true); i < count; i++) {
			BufferedImage image = reader.read(i);
			images.add(image);
		}
		if (step >= reader.getNumImages(true)) {
			step = 0;
			return images.get(s - 1);
		}
		return images.get(s);
	}

	public void loadFile(File file) {
		try {
			image = ImageIO.read(file);
			this.isGif = file.getName().endsWith(".gif");
			this.gifFile = file;
		} catch (Exception ex) {
			ex.printStackTrace();
			image = null;
		}
	}

	@Override
	public void onRun() {
		if (image == null && fileName != null) {
			File file;
			if (!fileName.startsWith(File.pathSeparator)) {
				file = new File(effectManager.getOwningPlugin().getDataFolder(), fileName);
			} else {
				file = new File(fileName);
			}
			loadFile(file);
		}
		if (image == null) {
			cancel();
			return;
		}
		if (isGif) {
			try {
				image = getImg(step);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (delay == 5) {
				step++;
				delay = 0;
			}
			delay++;
		}
		Location location = getLocation();
		for (int y = 0; y < image.getHeight(); y += stepY) {
			for (int x = 0; x < image.getWidth(); x += stepX) {
				Vector v = new Vector((float) image.getWidth() / 2 - x, (float) image.getHeight() / 2 - y, 0)
						.multiply(size);
				VectorUtils.rotateAroundAxisY(v, -location.getYaw() * MathUtils.degreesToRadians);
				if (enableRotation) {
					double rotX = 0;
					double rotY = 0;
					double rotZ = 0;
					switch (plane) {
					case X:
						rotX = angularVelocityX * rotationStep;
						break;
					case Y:
						rotY = angularVelocityY * rotationStep;
						break;
					case Z:
						rotZ = angularVelocityZ * rotationStep;
						break;
					case XY:
						rotX = angularVelocityX * rotationStep;
						rotY = angularVelocityY * rotationStep;
						break;
					case XZ:
						rotX = angularVelocityX * rotationStep;
						rotZ = angularVelocityZ * rotationStep;
						break;
					case XYZ:
						rotX = angularVelocityX * rotationStep;
						rotY = angularVelocityY * rotationStep;
						rotZ = angularVelocityZ * rotationStep;
						break;
					case YZ:
						rotY = angularVelocityY * rotationStep;
						rotZ = angularVelocityZ * step;
						break;
					}
					VectorUtils.rotateVector(v, rotX, rotY, rotZ);
				}
				int r = (new Color(image.getRGB(x, y))).getRed();
				int g = (new Color(image.getRGB(x, y))).getGreen();
				int b = (new Color(image.getRGB(x, y))).getBlue();
				display(particle, location.add(v), org.bukkit.Color.fromRGB(r, g, b));
				location.subtract(v);
			}
		}
		rotationStep++;
	}

}
