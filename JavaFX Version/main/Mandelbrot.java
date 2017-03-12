package main;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Mandelbrot extends Task<Void> {
	
	private ImageView buffer;
	private Task task;
	
	private int WIDTH = 1920;
	private int HEIGHT = 1080;
	private int newWIDTH = 0;
	private int newHEIGHT = 0;
	private boolean imageAspectChanged = false;

	private WritableImage img = new WritableImage(WIDTH, HEIGHT);
	private int[] rgbArray = new int[WIDTH * HEIGHT];
	
	private long ITERATIONS = 50;
	private long changedIterations = 0;
	private boolean iterationChange = false;
	
	private final MathContext context = new MathContext(100);
	
	private BigDecimal ZOOM = new BigDecimal("1");
	private BigDecimal changedZoom = null;
	private boolean zoomChange = false;
	
	private BigDecimal xOffset = new BigDecimal("-1.74995768370609350360221450607069970727110579726252077930242837820286008082972804887218672784431700831100544507655659531379747541999999995");
	private BigDecimal yOffset = new BigDecimal("0.00000000000000000278793706563379402178294753790944364927085054500163081379043930650189386849765202169477470552201325772332454726999999995");
	private BigDecimal changedXOffset = null;
	private BigDecimal changedYOffset = null;
	private boolean xyChange = false;
	
	/*From video
	 * -1.74995768370609350360221450607069970727110579726252077930242837820286008082972804887218672784431700831100544507655659531379747541999999995
	 * 0.00000000000000000278793706563379402178294753790944364927085054500163081379043930650189386849765202169477470552201325772332454726999999995
	 */
	
	private BigDecimal MaxRe = new BigDecimal("2.0");
	private BigDecimal MinRe = MaxRe.negate();
	private BigDecimal MaxIm = MaxRe.multiply(BigDecimal.valueOf((double) (HEIGHT) / (double) (WIDTH)), context);
	private BigDecimal MinIm = MaxIm.negate();
	private BigDecimal ReFactor = MaxRe.subtract(MinRe).divide(BigDecimal.valueOf(WIDTH - 1), context);
	private BigDecimal ImFactor = MaxIm.subtract(MinIm).divide(BigDecimal.valueOf(HEIGHT - 1), context);
	
	private boolean rendering = false;
	
	private int numberOfThreads = 16;
	private int changedNumberOfThreads = 0;
	private boolean numberOfThreadsChanged = false;
	private RenderThread[] threads = new RenderThread[numberOfThreads];
	
	private boolean autoSave = false;
	private final String autoSaveFileName = "Mandelbrot_Render_";
	private int fileNumber = 1;
	private String defaultSaveFolder = FileSystemView.getFileSystemView().getParentDirectory(FileSystemView.getFileSystemView().getDefaultDirectory()).getAbsolutePath() + "\\Pictures";
	
	/**
	 * Renders the set based on the pixel coordinate on the buffered image
	 */
	@Override
	public Void call(){
		updateProgress(0, 1);
		rendering = true;
		
		long startTime = System.currentTimeMillis();
		
		for(int i = 0; i < numberOfThreads; i++){
			threads[i] = new RenderThread(i * (WIDTH / numberOfThreads), (i + 1) * (WIDTH / numberOfThreads), this);
		}
		
		while(rendering){
			int threadsRunning = numberOfThreads;
			float percentComplete = 0f;

			for(RenderThread t : threads){
				if(!t.isRendering()){
					threadsRunning -= 1;
					percentComplete += 1f / numberOfThreads;
				}else{
					float percent = t.getPercentComplete();
					percentComplete += (percent / numberOfThreads);
				}
			}
			
			updateProgress((double) (percentComplete), 1);
			
			if(threadsRunning == 0){
				rendering = false;
			}
		}
		
		long renderTime = System.currentTimeMillis() - startTime;
		
		rendering = false;
		
		long hours = TimeUnit.MILLISECONDS.toHours(renderTime);
		renderTime -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(renderTime);
		renderTime -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(renderTime);
		
		String time = String.format("Render Time: %d hours, %d minutes, %d seconds", hours, minutes, seconds);
		Toolkit.getDefaultToolkit().beep();
		
		System.out.println(time);
		
		drawSet();
		
		if(autoSave){
			autoSave();
		}
		
		if(iterationChange){
			ITERATIONS = changedIterations;
			iterationChange = false;
		}
		
		if(zoomChange){
			ZOOM = changedZoom;
			zoomChange = false;
			changedZoom = null;
		}
		
		if(numberOfThreadsChanged){
			numberOfThreads = changedNumberOfThreads;
			numberOfThreadsChanged = false;
			changedNumberOfThreads = 0;
		}
		
		if(xyChange){
			xOffset = changedXOffset;
			yOffset = changedYOffset;
			xyChange = false;
			changedXOffset = null;
			changedYOffset = null;
		}
		
		if(imageAspectChanged){
			WIDTH = newWIDTH;
			HEIGHT = newHEIGHT;
			imageAspectChanged = false;
			newWIDTH = 0;
			newHEIGHT = 0;
			img = new WritableImage(WIDTH, HEIGHT);
			rgbArray = new int[WIDTH * HEIGHT];
			setMaxAndMins();
		}
		
		return null;
	}
	
	public int calculatePoint(BigDecimal x, BigDecimal y){
		x = x.divide(ZOOM, context).add(xOffset); // x = x / ZOOM + xOffset
		y = y.divide(ZOOM, context).add(yOffset); // y = y / ZOOM + yOffset
		
		BigDecimal cx = x;
		BigDecimal cy = y;
		
		int i = 0;
		
		boolean isInside = true;
		
		for(; i < ITERATIONS; i++){
			
			// Check if its outside first
			if(x.multiply(x, context).add(y.multiply(y, context)).compareTo(BigDecimal.valueOf(4)) > 0){ // x * x + y * y > 4
				isInside = false;
				break;
			}
			
			BigDecimal nx = x.multiply(x, context).subtract(y.multiply(y, context)).add(cx); // x*x - y*y + cx
			BigDecimal ny = BigDecimal.valueOf(2).multiply(x, context).multiply(y, context).add(cy); // 2 * x * y + cy
			
			x = nx;
			y = ny;
		}
		
		if(isInside) return  0xFFFFFFFF; // Black if it does not diverge
		
		/*
		 * Coloring values (hue, saturation, brightness)
		 * 1) i / 256f, 1, 1 / (1 + 8f)
		 * 2) i / 256f, 1, i / 256f --- RENDERED 150,000,000,000 ZOOM
		 * 3) i / 256f, 1, i / (i + 100f)
		 * 4) i / 256f, 1, (float) (i / (float) (ITERATIONS)) --- Gold Dragon
		 */
		return Color.HSBtoRGB(i / 256f, 1, i / 256f);
	}
	
	public void setRGB(int x, int y, int color){
		rgbArray[(y * WIDTH) + x] = color;
	}
	
	private void drawSet(){
		PixelWriter pw = img.getPixelWriter();
		pw.setPixels(0, 0, WIDTH, HEIGHT, PixelFormat.getIntArgbInstance(), rgbArray, 0, WIDTH);
		
		buffer.setImage(img);
	}
	
	private void setMaxAndMins(){
		MaxRe = new BigDecimal("2.0");
		MinRe = MaxRe.negate();
		MaxIm = MaxRe.multiply(BigDecimal.valueOf((double) (HEIGHT) / (double) (WIDTH)), context);
		MinIm = MaxIm.negate();
		ReFactor = MaxRe.subtract(MinRe).divide(BigDecimal.valueOf(WIDTH - 1), context);
		ImFactor = MaxIm.subtract(MinIm).divide(BigDecimal.valueOf(HEIGHT - 1), context);
	}
	
	private void autoSave(){
		File imgFile = new File(defaultSaveFolder + "\\" + autoSaveFileName + fileNumber + ".png");
		
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(img, new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)), "png", imgFile);
		} catch (IOException e1) {
			// TODO: Error messege from autosave
		}
	}

	public void setImage(ImageView img) {
		this.buffer = img;
	}

	public int getWIDTH() {
		return WIDTH;
	}

	public int getHEIGHT() {
		return HEIGHT;
	}

	public MathContext getContext() {
		return context;
	}

	public BigDecimal getxOffset() {
		return xOffset;
	}

	public BigDecimal getyOffset() {
		return yOffset;
	}

	public BigDecimal getMaxRe() {
		return MaxRe;
	}

	public BigDecimal getMinRe() {
		return MinRe;
	}

	public BigDecimal getMaxIm() {
		return MaxIm;
	}

	public BigDecimal getMinIm() {
		return MinIm;
	}

	public BigDecimal getReFactor() {
		return ReFactor;
	}
	
	public BigDecimal getImFactor() {
		return ImFactor;
	}

	public boolean isRendering() {
		return rendering;
	}

}
