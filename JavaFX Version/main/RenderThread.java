package main;

import java.math.BigDecimal;

public class RenderThread extends Thread {
	
	private Mandelbrot set;
	
	private BigDecimal startX;
	private BigDecimal endX;
	
	private float percentComplete = 0f;
	
	private boolean rendering = false;
	
	public RenderThread(int startX, int endX, Mandelbrot set){
		this.set = set;
		this.startX = BigDecimal.valueOf(startX);
		this.endX = BigDecimal.valueOf(endX);
		rendering = true;
		setDaemon(true);
		start();
	}

	@Override
	public void run() {
		for(BigDecimal x = startX; x.compareTo(endX) < 0 && set.isRendering(); x = x.add(BigDecimal.ONE)){
		
			BigDecimal c_re = set.getMinRe().add(x.multiply(set.getReFactor(), set.getContext()));
			
			for(BigDecimal y = new BigDecimal("0"); y.compareTo(BigDecimal.valueOf(set.getHEIGHT())) < 0 && set.isRendering(); y = y.add(BigDecimal.ONE)){
				
				BigDecimal c_im = set.getMaxIm().subtract(y.multiply(set.getImFactor(), set.getContext()));
				
				set.setRGB(x.intValue(), y.intValue(), set.calculatePoint(c_re, c_im));
			}
			percentComplete = x.divide(endX.subtract(BigDecimal.ONE), set.getContext()).floatValue();
		}
		
		rendering = false;
	}

	public float getPercentComplete() {
		return percentComplete;
	}
	
	public boolean isRendering(){
		return rendering;
	}

}
