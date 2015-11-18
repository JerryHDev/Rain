package com.jerryhuang.rain;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import com.jerryhuang.rain.graphics.Screen;
import com.jerryhuang.rain.input.Keyboard;

public class Game extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;
	
	// Resolution variables
	public static int width = 300;
	public static int height = width / 16 * 9;
	public static int scale = 3;
	public static String title = "Rain";
	
	private Thread thread;
	private JFrame frame;
	private Keyboard key;
	private boolean running = false;
	
	private Screen screen;

	/**Creates image and accesses image*/
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // creates an image with a buffer
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData(); // converting image object into an array of integers
	
	// constructor
	public Game() { 
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);
		
		screen = new Screen(width, height);
		frame = new JFrame();
		key = new Keyboard();
		
		addKeyListener(key); // listens to key inputs
	}
	
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0; // update screen 60 times/sec.
		double delta = 0;
		int frames = 0;
		int updates = 0;
		while (running) { // game loop
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates + " ups, " + frames + " fps");
				frame.setTitle(title + "  |  " + updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
		}
		stop();
	}
	int x = 0, y = 0;
	
	public void update() {
		key.update(); // gets which key is being pressed
		/**How much to move by*/
		if (key.up) y--; 
		if (key.down) y++;
		if (key.left) x--;
		if (key.right) x++;
	}
	
	/**Creates graphics behind-the-scenes*/
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3); // creates 3 buffer strategies
			return;
		}
		
		screen.clear();
		screen.render(x, y);
		
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = screen.pixels[i];
		}

		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose(); // removes current graphics
		bs.show(); // shows next available buffer
	}
	
	public static void main(String[] args) {
		Game game = new Game();
		game.frame.setResizable(false);
		game.frame.setTitle(Game.title);
		game.frame.add(game);
		game.frame.pack();
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closes *program*
		game.frame.setLocationRelativeTo(null); // centers game window
		game.frame.setVisible(true); // shows game window
		
		game.start(); // starts the game
	}
}