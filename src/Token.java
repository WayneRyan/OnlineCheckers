import java.awt.Color;
import java.awt.Graphics;


public class Token {
	boolean hasCrown;
	Color myColor;
	
	public Token(Color c){
		myColor = c;
		hasCrown = false;
	}
	
	public void draw(Graphics g, int row, int col){
		int x = col*Client.SPACING;
		int y = row*Client.SPACING;
		g.setColor(myColor);
		g.fillOval(x, y, Client.SPACING, Client.SPACING);
		g.setColor(new Color(1,1,1,0.5f));
		if(hasCrown)g.fillOval(x+5, y+5, Client.SPACING-10, Client.SPACING-10);
	}
	
	public void drawSelected(Graphics g, int row, int col){
		g.setColor(Color.yellow);
		g.fillOval(col*Client.SPACING-2, row*Client.SPACING-2, Client.SPACING+4, Client.SPACING+4);
		draw(g,row,col);
	}
	
	public void addCrown(){
		hasCrown = true;
	}
	
	public boolean hasCrown(){
		return hasCrown;
	}

	public boolean isRed() {
		return myColor.equals(Color.red);
	}
	
	public Token(String s){
		String[] parts = s.split(" ");
		hasCrown = parts[0].equals("true");
		myColor = new Color(Integer.parseInt(parts[1]));
	}
	
	public String toString(){
		return ""+hasCrown+" "+myColor.getRGB();
	}

}
