import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main extends Canvas {
	
	private BufferedImage buffer;
	
	private final BigDecimal WIDTH = new BigDecimal("1280");
	private final BigDecimal HEIGHT = new BigDecimal("720");
	
	private int ITERATIONS = 50;
	
	private double ZOOM = 1;
	private double xOffset = 0;
	private double yOffset = 0;
	
	private double zoomFactor = 1.01;
	private double offsetFactor = 0.001 / ZOOM;
	
	private float hueOffset = 0;
	
	private BufferStrategy bs = null;
	
	private boolean draw = true;
	
	private Timer timer = new Timer();
	
	private boolean[] keys = new boolean[65535];
	private boolean[] keyReleased = new boolean[65535];
	
	private BigDecimal MinRe = new BigDecimal("-2");
	private BigDecimal MaxRe = new BigDecimal("2");
	private BigDecimal MinIm = new BigDecimal("-1.125");
	private BigDecimal MaxIm = MinIm.add(MaxRe.subtract(MinRe)).multiply(HEIGHT.divide(WIDTH));
	private BigDecimal ReFactor = MaxRe.subtract(MinRe).divide(WIDTH); // (MaxRe - MinRe) / (WIDTH - 1)
	private BigDecimal ImFactor = MaxIm.subtract(MinIm).divide(HEIGHT); // (MaxIm - MinIm) / (HEIGHT - 1)
	
	public Main(){
		
		buffer = new BufferedImage(WIDTH.intValue(), HEIGHT.intValue(), BufferedImage.TYPE_INT_RGB);
		
		JFrame frame = new JFrame("Mandelbrot Set");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH.intValue(), HEIGHT.intValue());
		frame.setResizable(false);
		frame.add(this);
		frame.setVisible(true);
		
		this.addKeyListener(new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				keys[e.getKeyCode()] = true;
				keyReleased[e.getKeyCode()] = true;
			}
			@Override
			public void keyReleased(KeyEvent e) {
				keys[e.getKeyCode()] = false;
			}
		});
		
		this.createBufferStrategy(3);
		bs = this.getBufferStrategy();
		
		timer.init();
		zoomLoop();
	}
	
	public void zoomLoop(){
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / 60f;
		
		long lastTimer = System.currentTimeMillis();
		
		while(true){
			elapsedTime = timer.getElapsedTime();
			accumulator += elapsedTime;
			
			while(accumulator >= interval){
				update();
				
				accumulator -= interval;
			}
			
			if(draw){
				if(System.currentTimeMillis() - lastTimer > 1000){
					lastTimer += 1000;
					
					System.out.println("ZOOM: " + ZOOM + ", Hue offset: " + hueOffset);
				}
				
				hueOffset += 0.01f;
				renderMandelbrot();
			}
		}
	}
	
	public void update(){
		if(keys[KeyEvent.VK_A]){
			xOffset -= offsetFactor;
		}else if(keys[KeyEvent.VK_D]){
			xOffset += offsetFactor;
		}
		
		if(keys[KeyEvent.VK_S]){
			yOffset -= offsetFactor;
		}else if(keys[KeyEvent.VK_W]){
			yOffset += offsetFactor;
		}
		
		if(keys[KeyEvent.VK_SPACE]){
			ZOOM /= zoomFactor;
		}else if(keys[KeyEvent.VK_SHIFT]){
			ZOOM *= zoomFactor;
		}
		
		if(keyReleased[KeyEvent.VK_P] && !keys[KeyEvent.VK_P]){
			draw = !draw;
			System.out.println("Drawing: " + draw);
			keyReleased[KeyEvent.VK_P] = false;
		}
		
		if(keyReleased[KeyEvent.VK_I] && !keys[KeyEvent.VK_I]){
			ITERATIONS = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter number of iterations required", ITERATIONS));
			keyReleased[KeyEvent.VK_I] = false;
		}
	}
	
	/**
	 * 
	 */
	public void renderMandelbrot(){
		for(int y = 0; y < HEIGHT.intValue(); y++){
			BigDecimal c_im = MaxIm.subtract(new BigDecimal(y).multiply(ImFactor)); // MaxIm - y * ImFactor
			for(int x = 0; x < WIDTH.intValue(); x++){
				BigDecimal c_re = MaxRe.add(new BigDecimal(x).multiply(ReFactor)); // MaxRe + x * ReFactor
				
										   // c_re / ZOOM + xOffset					c_im / ZOOM + yOffset
				int color = calculatePoint(c_re.divide(BigDecimal.valueOf(ZOOM)).add(BigDecimal.valueOf(xOffset)).negate().setScale(100, 0), c_im.divide(BigDecimal.valueOf(ZOOM)).add(BigDecimal.valueOf(yOffset)).setScale(100, 0));
				
				buffer.setRGB(x, y, color);
			}
		}
		
		drawSet(bs.getDrawGraphics());
		bs.show();
	}
	
	public int calculatePoint(BigDecimal x, BigDecimal y){
		BigDecimal cx = x;
		BigDecimal cy = y;
		
		int i = 0;
		
		for(; i < ITERATIONS; i++){
			BigDecimal nx = x.multiply(x).subtract(y.multiply(y)).add(cx); // x * x - y * y + cx
			BigDecimal ny = BigDecimal.valueOf(2).multiply(x).multiply(y).add(cy); // 2 * x * y + cy
			
			x = nx;
			y = ny;
			
			if(x.multiply(x).add(y.multiply(y)).compareTo(BigDecimal.valueOf(4)) > 0) break;
		}
		
		if(i == ITERATIONS) return 0x00000000;
		
		return Color.HSBtoRGB(((float) (i / ITERATIONS + hueOffset)) % 1, 0.5f, 1);
	}
	
	public void drawSet(Graphics g){
		g.drawImage(buffer, 0, 0, null);
		
		g.setColor(Color.RED);
		g.drawLine(WIDTH.intValue() / 2, HEIGHT.intValue() / 2 - 100, WIDTH.intValue() / 2, HEIGHT.intValue() / 2 + 100);
		g.drawLine(WIDTH.intValue() / 2 - 100, HEIGHT.intValue() / 2, WIDTH.intValue() / 2 + 100, HEIGHT.intValue() / 2);
	}
	
	/*@Override
	public void paint(Graphics g){
		drawSet(g);
	}*/
	
	public static void main(String[] args){
		new Main();
	}

}
