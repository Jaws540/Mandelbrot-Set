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
	
	private final int ITERATIONS = 100;
	
	private int iteration = 0;
	
	private int ZOOM = 300;
	private int xOffset = 2;
	private int yOffset = 2;
	
	private float hueOffset = 0;
	
	private BufferStrategy bs = null;
	
	private boolean draw = true;
	
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
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE){
					draw = !draw;
					System.out.println("Drawing: " + draw);
				}
			}
		});
		
		this.createBufferStrategy(3);
		bs = this.getBufferStrategy();
		
		zoomLoop();
	}
	
	public void zoomLoop(){
		while(true){
			if(draw){
				iteration++;
				
				// Current zoom and transform operations
				ZOOM += iteration * 2;
				xOffset -= (int) (iteration * 2.75);
				//yOffset += 2;
				
				hueOffset += 0.01f;
				
				renderMandelbrot();
				
				drawSet(bs.getDrawGraphics());
				
				bs.show();
			}
		}
	}
	
	public void drawSet(Graphics g){
		g.drawImage(buffer, 0, 0, null);
	}
	
	public void renderMandelbrot(){
		for(int x = 0; x < WIDTH; x++){
			for(int y = 0; y < HEIGHT; y++){
				
				int color = calculatePoint(((x + xOffset) - WIDTH / 2f) / ZOOM, ((y + yOffset) - HEIGHT / 2f) / ZOOM);
				
				buffer.setRGB(x, y, color);
			}
		}
	}
	
	public int calculatePoint(float x, float y){
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
		
		if(i == ITERATIONS) return 0x00000000;
		
		return Color.HSBtoRGB(((float) i / ITERATIONS + hueOffset) % 1, 0.5f, 1);
	}
	
	public static void main(String[] args){
		new Main();
	}

}
