import processing.core.*;

public class Main extends PApplet{
  public static int N = 210;
  public static int SCALE = 4;
  public static final int iter = 3;
  public Fluid fluid;
  public float diffusion = 0;
  public float viscosity = 0.00000005f;

  public static void main(String[] args) {
  	PApplet.main(new String[] {"Main"});
  }
  
  public void settings() {
  	size(N*SCALE, N*SCALE);
  }
  
  public void setup() {
  	fluid = new Fluid(this, diffusion, viscosity, 0.1f);
  	colorMode(HSB);
  }
  
  public void keyPressed() {
  	if(key==CODED) {
  		if(keyCode==UP&&SCALE>1) {//more intensive
  			SCALE--;
  			N = 840/SCALE;
  			System.out.println("======================");
  			System.out.println("added particles to sim");
  			System.out.println("======================");
  		}
      if(keyCode==DOWN&&SCALE<8) {//less intensive
      	SCALE++;
      	N = 840/SCALE;
      	System.out.println("======================");
      	System.out.println("took particles from sim");
      	System.out.println("======================");
  		}
      if(keyCode==LEFT) {
      	viscosity /= 5;
      	System.out.println("======================");
      	System.out.println("sim viscosity decreased: "+viscosity);
      	System.out.println("======================");
      }
      if(keyCode==RIGHT) {
      	viscosity *= 5;
      	System.out.println("======================");
      	System.out.println("sim viscosity increased: "+viscosity);
      	System.out.println("======================");
      }
      fluid = new Fluid(this, diffusion, viscosity, 0.1f);
  	}
  }
  
  public void mouseDragged() {
  	if(mouseX>0&&mouseX<width&&mouseY>0&&mouseY<height) {
  	  fluid.addDensity(mouseX/SCALE, mouseY/SCALE, 100);
  	  fluid.addVelocity(mouseX/SCALE, mouseY/SCALE, (mouseX-pmouseX), (mouseY-pmouseY));
  	}
  }
  
  public void draw() {
  	background(0);
  	fluid.timeStep();
  	fluid.renderD();
  	fluid.fadeD();
  	surface.setTitle("Fluid Simulation ... rendering "+(N*N)+" fluid particles at "+round(frameRate)+"fps");
  }
}
