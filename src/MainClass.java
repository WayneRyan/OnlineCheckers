import java.awt.Color;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class MainClass extends JFrame implements ActionListener, Runnable {
	private static final long serialVersionUID = 1L;
	private ServerSocket server;
	private Socket redPlayer, blackPlayer;
	private BufferedReader redIn, blackIn;
	private BufferedWriter redOut, blackOut;

	private JButton startServer;
	private TextField servername;

	
	Token[][] allTokens;
	Token selected;
	int selectedRow,selectedCol;
	boolean redTurn, didJump;
	
	
	
	public MainClass() {
		didJump = false;
		redTurn = true;
		selected = null;
		allTokens = new Token[Client.NUM_SQUARES][Client.NUM_SQUARES];
		for (int col = 0; col < Client.NUM_SQUARES; col++) {
			for (int row = 0; row < 3; row++) {
				if ((row + col) % 2 != 0) {
					allTokens[row][col] = new Token(Color.black);
				}
			}
			for (int row = Client.NUM_SQUARES-3; row < Client.NUM_SQUARES; row++) {
				if ((row + col) % 2 != 0) {
					allTokens[row][col] = new Token(Color.red);
				}
			}
		}
		
		
		
		this.setLayout(null);
		this.setSize(400, 150);
		this.setResizable(false);
		this.getContentPane().setBackground(Color.BLACK);
		this.getContentPane().setForeground(Color.green);

		startServer = new JButton("Initialize Server");
		startServer.setBounds(10, 10, 150, 25);
		startServer.addActionListener(this);
		startServer.setEnabled(true);
		startServer.setBackground(Color.BLACK);
		startServer.setForeground(Color.GREEN);
		this.add(startServer);

		servername = new TextField("localhost");
		servername.setBounds(120, 90, 200, 25);
		servername.addActionListener(this);
		servername.setBackground(Color.black);
		servername.setForeground(Color.green);
		servername.setEnabled(true);
		this.add(servername);

		JLabel serverLabel = new JLabel("Server name: ");
		serverLabel.setForeground(Color.green);
		serverLabel.setBounds(10, 90, 100, 25);
		this.add(serverLabel);
	}

	public static void main(String[] args) throws Exception {
		MainClass mc = new MainClass();
		mc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mc.setVisible(true);
	}

	@Override
	public void run() {
		try {
			boolean servingRed = false;
			if (redPlayer == null) {
				redPlayer = server.accept();
				redIn = new BufferedReader(new InputStreamReader(
						redPlayer.getInputStream()));
				redOut = new BufferedWriter(new OutputStreamWriter(
						redPlayer.getOutputStream()));
				servingRed = true;
				System.out.println("Serving Red Player");
			} else {
				blackPlayer = server.accept();
				blackIn = new BufferedReader(new InputStreamReader(
						blackPlayer.getInputStream()));
				blackOut = new BufferedWriter(new OutputStreamWriter(
						blackPlayer.getOutputStream()));
				System.out.println("Serving Black Player");
			}
			while (redPlayer == null || blackPlayer == null) {
				Thread.sleep(30);
			}
			System.out.println("Starting game Loop");

			// create buffered reader and buffered writer

			while (true) {
				String message = "";
				for(Token[] row : allTokens){
					for(Token t : row){
						if(t==null)message += "null" + ",";
						else message += t.toString() + ",";
					}
					message = message.substring(0,message.length()-1)+":";
				}
				if(selected==null){
					message = message +  "null," + selectedRow + ","+selectedCol;
				}else{
					message = message + selected.toString() + "," + selectedRow + ","+selectedCol;
				}
				// send update to black and red
				redOut.write(message+"\n"); redOut.flush();
				blackOut.write(message + "\n"); blackOut.flush();
				if (servingRed) {
					// read input from redPlayer
					String input = redIn.readLine();
					System.out.println("input from red=" + input);
					// if not red turn ignore
					// else make red move
					if(redTurn)makeMove(input);
				} else {
					// read input from blackPlayer
					String input = blackIn.readLine();
					System.out.println("input from black=" + input);
					// if not black turn ignore
					// else make black move
					if(!redTurn)makeMove(input);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makeMove(String move){
		String[] coords = move.split(",");
		int row = Integer.parseInt(coords[0]);
		int col = Integer.parseInt(coords[1]);
		System.out.println("row="+row);
		System.out.println("col="+col);
		if(allTokens[row][col]==selected){
			// deselecting
			System.out.println("deselecting");
			selected = null;
			if(didJump){
				didJump = false;
				redTurn = !redTurn;
			}
		}
		else if(selected==null){
			System.out.println("selecting");
			// selecting
			if(redTurn != allTokens[row][col].isRed())return;
			selectedRow = row;
			selectedCol = col;
			selected = allTokens[selectedRow][selectedCol];
		}else{
			System.out.println("moving");
			// moving
			if(allTokens[row][col]!=null)return;
			if(!selected.hasCrown() && !isForward(row))return;
			if(didJump && !isJump(row,col))return;
			if(!didJump && !isJump(row,col) && !isAdjacent(row,col))return;
			allTokens[row][col] = selected;
			if(isBackRow(row))selected.addCrown();
			allTokens[selectedRow][selectedCol] = null;
			selectedRow = row;
			selectedCol = col;
			if(!didJump){
				selected = null;
				redTurn = !redTurn;
			}
		}
	}
	
	
	private boolean isAdjacent(int row, int col){
		int dx = Math.abs(row-selectedRow);
		int dy = Math.abs(col-selectedCol);
		return dx==1 && dy==1;
	}
	
	
	private boolean isForward(int row){
		int dy = row-selectedRow;  // positive if moving down
		return dy>0 != redTurn; // red moves up
	}
	
	private boolean isBackRow(int row){
		if(redTurn)return row==0;
		return row==Client.NUM_SQUARES-1;
	}

	private boolean isJump(int row, int col) {
		// make sure diagonally 2 away
		int dy = row-selectedRow;
		int dx = col-selectedCol;
		if( 4!=dy*dy || dx*dx!=4 )return false;
		
		// make sure token of other color exists between 
		int mX = selectedCol + dx /2;
		int mY = selectedRow +dy /2;
		if(allTokens[mY][mX] == null || allTokens[mY][mX].isRed() == redTurn)return false;
		
		// remove the jumped token, and set jump flag
		allTokens[mY][mX] = null;
		didJump = true;
		return true;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startServer) {
			try {
				server = new ServerSocket(4444);
				new Thread(this).start();
				Thread.sleep(30);
				Client c = new Client(new Socket("localhost", 4444));
				new Thread(this).start();
			} catch (Exception ex) {
				if (ex instanceof BindException)
					JOptionPane.showMessageDialog(null,
							"Server initialization error: " + ex.getMessage());
				else
					ex.printStackTrace();
			}
		}
		if (e.getSource() == servername) {
			try {
				Client c = new Client(new Socket(servername.getText().trim(),
						4444));
			} catch (Exception ex) {
				if (ex instanceof UnknownHostException) {
					JOptionPane.showMessageDialog(null, "Unknown host");
				}
			}
			servername.setEnabled(false);
		}
		startServer.setEnabled(false);
	}

}
