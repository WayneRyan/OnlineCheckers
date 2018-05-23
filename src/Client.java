import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.JFrame;


public class Client extends JFrame implements Runnable, MouseListener{
	private static final long serialVersionUID = 1L;
	public static final int SIZE = 800;
	public static final int NUM_SQUARES = 8;
	public static final int SPACING = SIZE / NUM_SQUARES;
	
	private BufferedReader in;
	private BufferedWriter out;
	
	private BufferedImage offscreen;
	private Graphics bg;
	Token[][] allTokens;
	Token selected;
	int selectedRow,selectedCol;
	
	public Client(Socket s){
		try {
			allTokens = new Token[NUM_SQUARES][NUM_SQUARES];
			offscreen = new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_INT_RGB);
			bg = offscreen.getGraphics();
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			new Thread(this).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setSize(SIZE, SIZE + 30);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		this.addMouseListener(this);
	}
	
	public void paint(Graphics g){
		drawBoard(bg);
		for (int col = 0; col < NUM_SQUARES; col++) {
			for (int row = 0; row < NUM_SQUARES; row++) {
				if(allTokens[row][col]!=null)allTokens[row][col].draw(bg, row, col);
			}
		}
		if(selected!=null)selected.drawSelected(bg, selectedRow, selectedCol);
		g.drawImage(offscreen, 0, 30, null);
		// draw all the tokens
	}

	private void drawBoard(Graphics g) {
		for (int row = 0; row < NUM_SQUARES; row++) {
			for (int col = 0; col < NUM_SQUARES; col++) {
				if ((row + col) % 2 == 0)
					g.setColor(new Color(0x8C0000));
				else
					g.setColor(new Color(0x616161));
				g.fillRect(col * SPACING, row * SPACING, SPACING, SPACING);
			}
		}
	}
	

	@Override
	public void run() {
		while(true){
			// listen for message from server
			try {
				int r=0;
				for(String row : in.readLine().split(":")){
					if(r==NUM_SQUARES){
						String[] parts = row.split(",");
						if(parts[0].equals("null"))selected = null;
						else{
							selected = new Token(parts[0]);
							selectedRow = Integer.parseInt(parts[1]);
							selectedCol = Integer.parseInt(parts[2]);
						}
						break;
					}
					int c=0;
					for(String tok : row.split(",")){
						if(tok.equals("null")){
							allTokens[r][c] = null;
						}else{
							allTokens[r][c] = new Token(tok);
						}
						c++;
					}
					r++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// update all tokens
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int col = e.getX()/SPACING;
		int row = (e.getY()-30)/SPACING;
		// send message to server
		try {
			out.write(row + "," + col + "\n");
			out.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	

}
