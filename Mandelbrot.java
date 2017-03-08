import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Main extends Canvas {
	
	private BufferedImage buffer;
	
	private final int WIDTH = 800;
	private final int HEIGHT = 600;
	
	private final int ITERATIONS = 500;
	
	private int iteration = 0;
	
	private float ZOOM = 500;
	private float xOffset = 0f;
	private float yOffset = 0f;
	
	private float hueOffset = 0;
	
	private BufferStrategy bs = null;
	
	private boolean draw = true;
	
	private Timer timer = new Timer();
	
	private boolean[] keys = new boolean[65535];
	private boolean[] keyReleased = new boolean[65535];
	
	public Main(){
		
		buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		JFrame frame = new JFrame("Mandelbrot Set");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
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
		
		zoomLoop(); // Start loop
	}
	
	public void zoomLoop(){
		/*while(true){
			if(draw){
				iteration++;
				
				// Current zoom and transform operations
				//ZOOM += iteration; 
				//xOffset -= iteration * 1.45;
				//yOffset += 100/iteration;
				
				hueOffset += 0.01f;
				
				renderMandelbrot();
				
				drawSet(bs.getDrawGraphics());
				
				bs.show();
			}
		}*/
		
		
		// Allows for some sense of steady updating (not too steady, but it does the trick)
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / 60f;
		
		int ticks = 0;
		int frames = 0;
		long lastTimer = System.currentTimeMillis();
		
		// Constant render loop
		while(true){
			elapsedTime = timer.getElapsedTime();
			accumulator += elapsedTime;
			
			while(accumulator >= interval){
				ticks++;
				update();
				
				accumulator -= interval;
			}
			
			if(System.currentTimeMillis() - lastTimer > 1000){
				lastTimer += 1000;
				
				if(true)
					System.out.println("Updates per sec: " + ticks + ", Frames per sec: " + frames);
				
				ticks = 0;
				frames = 0;
			}
			
			hueOffset += 0.01f;
			renderMandelbrot(); // Render the set
			frames++;
		}
	}
	
	public void update(){
		if(keys[KeyEvent.VK_A]){
			xOffset -= .5 / ZOOM;
			System.out.println("Move Left");
		}else if(keys[KeyEvent.VK_D]){
			xOffset += .5 / ZOOM;
			System.out.println("Move Right");
		}
		
		if(keys[KeyEvent.VK_W]){
			yOffset -= .5 / ZOOM;
			System.out.println("Move Up");
		}else if(keys[KeyEvent.VK_S]){
			yOffset += .5 / ZOOM;
			System.out.println("Move Down");
		}
		
		if(keys[KeyEvent.VK_SPACE]){
			ZOOM /= 1.01;
			System.out.println("Zoom out");
		}else if(keys[KeyEvent.VK_SHIFT]){
			ZOOM *= 1.01;
			System.out.println("Zoom in");
		}
		
		if(keyReleased[KeyEvent.VK_P] && !keys[KeyEvent.VK_P]){
			draw = !draw;
			System.out.println("Drawing: " + draw);
			keyReleased[KeyEvent.VK_P] = false;
		}
	}
	
	/**
	 * Renders the set based on the pixel coordinate on the buffered image
	 */
	public void renderMandelbrot(){
		for(int x = 0; x < WIDTH; x++){
			for(int y = 0; y < HEIGHT; y++){
				
				int color = calculatePoint((x - WIDTH / 2f), (y - HEIGHT / 2f));
				
				buffer.setRGB(x, y, color);
			}
		}
		
		drawSet(bs.getDrawGraphics()); // Draws to canvas
		bs.show(); // Switches buffer (not the image buffer but the screen buffers for the canvas)
	}
	
	/**
	 * Calculates the function and applies the zoom and the x/y transofrmations
	 */
	public int calculatePoint(float x, float y){
		x /= ZOOM;
		x += xOffset;
		
		y /= ZOOM;
		y += yOffset;
		
		float cx = x;
		float cy = y;
		
		int i = 0;
		
		for(; i < ITERATIONS; i++){
			float nx = x*x - y*y + cx;
			float ny = 2 * x * y + cy;
			
			x = nx;
			y = ny;
			
			if(x * x + y * y > 4) break;
		}
		
		if(i == ITERATIONS) return 0x00000000; // Black if it does not diverge
		
		return Color.HSBtoRGB(((float) i / ITERATIONS + hueOffset) % 1, 0.5f, 1); // Color if it diverges
	}
	
	/**
	 * Actually draws the buffered image to the canvas
	 */
	public void drawSet(Graphics g){
		g.drawImage(buffer, 0, 0, null);
	}
	
	public static void main(String[] args){
		new Main();
	}

}
