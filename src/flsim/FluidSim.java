package flsim;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.github.owenbharrison.displib.Display;
import io.github.owenbharrison.displib.image.ImageFilters;
import io.github.owenbharrison.displib.maths.Maths;

public class FluidSim extends Display{
	private static final long serialVersionUID = -3597022308285520820L;
	
	private boolean showVelocity = false;
	private boolean showDensity = true;
	
	private Gradient stressGrad, coolGrad, tempGrad, rainbowGrad, grayGrad, binaryGrad, currGrad;
	
	private int flSize = 60;
	private double flDiff = 0.00015, flVisc = 0.00004;
	
	public double[] fls;
	public double[] flDensity;
		
	public double[] flVx;
	public double[] flVy;

	public double[] flVx0;
	public double[] flVy0;
	
	private FluidSimControlPanel fscp;
	
	public static void main(String[] args) {
		FluidSim fs = new FluidSim();
		fs.setPreferredSize(new Dimension(800, 800));
		fs.start();
	}
	
	@Override
	protected void setup() {
		flReset();
		
		try {
			stressGrad = new Gradient(ImageIO.read(FluidSim.class.getResourceAsStream("res/stressGradient.jpg")));
			coolGrad = new Gradient(ImageIO.read(FluidSim.class.getResourceAsStream("res/coolGradient.jpg")));
			tempGrad = new Gradient(ImageIO.read(FluidSim.class.getResourceAsStream("res/tempGradient.jpg")));
			rainbowGrad = new Gradient(ImageIO.read(FluidSim.class.getResourceAsStream("res/rainbowGradient.jpg")));
			grayGrad = new Gradient(ImageIO.read(FluidSim.class.getResourceAsStream("res/grayGradient.jpg")));
			binaryGrad = new Gradient(ImageIO.read(FluidSim.class.getResourceAsStream("res/binaryGradient.jpg")));
			currGrad = stressGrad;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fscp = new FluidSimControlPanel();
	}
	
	private void flReset() {
		fls = new double[flSize*flSize];
		flDensity = new double[flSize*flSize];
		
		flVx = new double[flSize*flSize];
		flVy = new double[flSize*flSize];
		
		flVx0 = new double[flSize*flSize];
		flVy0 = new double[flSize*flSize];
	}
	
	@Override
	protected void update(double dt) {
		if(leftMouseButton) {
			if(mouseX>0&&mouseY>0&&mouseX<width&&mouseY<height) {
				int i = (int)Maths.map(mouseX, 0, width, 0, flSize);
				int j = (int)Maths.map(mouseY, 0, height, 0, flSize);
				double xs = (mouseX-prevMouseX);
				double ys = (mouseY-prevMouseY);
				double ds = Math.sqrt(xs*xs+ys*ys)+0.1;
				addDensity(i, j, 200.0*ds);
				addVelocity(i, j, xs, ys);
			}
		}
		
		if(fscp.sliderChange) {
			fscp.sliderChange = false;
			flSize = fscp.slider.getValue();
			flReset();
		}
		
		if(fscp.renderChange) {
			fscp.renderChange = false;
			String str = (String)fscp.render.getSelectedItem();
			if(str.equals(FluidSimControlPanel.densRenderMask)) {
				showDensity = true;
				showVelocity = false;
			}
			else if(str.equals(FluidSimControlPanel.velRenderMask)) {
				showVelocity = true;
				showDensity = false;
			}
			else if(str.equals(FluidSimControlPanel.bothRenderMask)) {
				showDensity = showVelocity = true;
			}
		}
		
		if(fscp.gradientChange) {
			fscp.gradientChange = false;
			String str = (String)fscp.gradient.getSelectedItem();
			if(str.equals(FluidSimControlPanel.stressGradMask)) currGrad = stressGrad;
			else if(str.equals(FluidSimControlPanel.coolGradMask)) currGrad = coolGrad;
			else if(str.equals(FluidSimControlPanel.tempGradMask)) currGrad = tempGrad;
			else if(str.equals(FluidSimControlPanel.rainbowGradMask)) currGrad = rainbowGrad;
			else if(str.equals(FluidSimControlPanel.grayGradMask)) currGrad = grayGrad;
			else if(str.equals(FluidSimControlPanel.binaryGradMask)) currGrad = binaryGrad;
		}
		
		diffuse(1, flVx0, flVx, flVisc, dt);
		diffuse(2, flVy0, flVy, flVisc, dt);
		
		project(flVx0, flVy0, flVx, flVy);
		
		advect(1, flVx, flVx0, flVx0, flVy0, dt);
		advect(2, flVy, flVy0, flVx0, flVy0, dt);
		
		project(flVx, flVy, flVx0, flVy0);
		
		diffuse(0, fls, flDensity, flDiff, dt);
		advect(0, flDensity, fls, flVx, flVy, dt);
		
		setTitle("FluidSim "+flSize+"x"+flSize+" @ "+getFps()+"fps");
	}

	@Override
	protected void draw(Graphics2D g) {
		if(showDensity) {
			int[] data = new int[flSize*flSize*3];
			for(int i=0;i<flSize;i++) {
				for(int j=0;j<flSize;j++) {
					int flix = index(i, j);
					double pct = Maths.clamp(flDensity[flix], 0.0, 255.0)/255.0;
					int[] gc = currGrad.getAtPercent(pct);
					int dtix = (i+j*flSize)*3;
					data[dtix] = gc[0];
					data[dtix+1] = gc[1];
					data[dtix+2] = gc[2];
					if(i==0||j==0||i==flSize-1||j==flSize-1) {
						data[dtix] = 0;
						data[dtix+1] = 0;
						data[dtix+2] = 0;
					}
				}
			}
			background(g, ImageFilters.pixelsToImage(data, flSize, flSize));
		}
		if(showVelocity) {
			float sz = (float)((double)width/(double)flSize/4.0);
			g.setStroke(new BasicStroke(sz));
			antialiasOn(g);
			if(!showDensity) {
				background(g, 0);
			}
			for(int i=0;i<flSize;i++) {
				for(int j=0;j<flSize;j++) {
					double x = i/(double)flSize*(double)width + width/(double)flSize/2.0;
					double y = j/(double)flSize*(double)height + height/(double)flSize/2.0;
					
					int index = index(i, j);
					double pct = Maths.clamp(flDensity[index], 0.0, 255.0)/255.0;
					int[] gc = currGrad.getAtPercent(pct);
					
					if(showDensity) stroke(g, 0);
					else stroke(g, gc[0], gc[1], gc[2]);
					
					double mx = (double)width/(double)flSize*1.5;
					double my = (double)height/(double)flSize*1.5;
					g.draw(new Line2D.Double(x, y, x+mx*flVx[index], y+my*flVy[index]));
				}
			}
		}
	}
	
	public void addDensity(int x, int y, double amount) {
		flDensity[index(x, y)] += amount;
	}
	
	public void addVelocity(int x, int y, double amountX, double amountY){
		int z = index(x, y);
		flVx[z] += amountX;
		flVy[z] += amountY;
	}
	
	public int index(int x, int y) {
		return (int)Maths.clamp(x, 0, flSize-1)+(int)Maths.clamp(y, 0, flSize-1)*flSize;
	}

	public void diffuse(int b, double[] x, double[] x0, double diff, double dt) {
		double a = dt*diff*(flSize-2)*(flSize-2);
		lin_solve(b,x,x0,a,1+6*a);
	}

	public void lin_solve(int b, double[] x, double[] x0, double a, double c){
		double cRecip = 1.0f / c;
		int iter = 3;
		for(int k=0;k<iter;k++) {
			for(int j=1;j<flSize-1;j++) {
				for(int i=1;i<flSize-1;i++) {
					x[index(i, j)] =
						(x0[index(i, j)]
						+a*(x[index(i+1,j)]
						+x[index(i-1,j)]
						+x[index(i,j+1)]
						+x[index(i,j-1)]
					)) * cRecip;
				}
			}
			set_bnd(b, x);
		}
	}

	public void project(double[] velocX, double[] velocY, double[] p, double[] div){
		for(int j=1;j<flSize-1;j++) {
			for(int i=1;i<flSize-1;i++) {
				div[index(i,j)] = -0.5f*(
					velocX[index(i+1,j)]
					-velocX[index(i-1,j)]
					+velocY[index(i,j+1)]
					-velocY[index(i,j-1)]
				)/flSize;
				p[index(i, j)] = 0;
			}
		}
		set_bnd(0, div); 
		set_bnd(0, p);
		lin_solve(0, p, div, 1, 6);
		for(int j=1;j<flSize-1;j++) {
			for (int i=1;i<flSize-1;i++) {
				velocX[index(i,j)] -= 0.5f*(p[index(i+1,j)]-p[index(i-1,j)])*flSize;
				velocY[index(i,j)] -= 0.5f*(p[index(i,j+1)]-p[index(i,j-1)])*flSize;
			}
		}
		set_bnd(1, velocX);
		set_bnd(2, velocY);
	}

	public void set_bnd(int b, double[] x) {
		for(int i=1;i<flSize-1;i++){
			x[index(i,0)] = b==2?-x[index(i,1)]:x[index(i,1)];
			x[index(i,flSize-1)] = b==2?-x[index(i,flSize-2)]:x[index(i,flSize-2)];
		}
		for(int j=1;j<flSize-1;j++) {
			x[index(0,j)] = b==1?-x[index(1,j)]:x[index(1,j)];
			x[index(flSize-1,j)] = b==1?-x[index(flSize-2,j)]:x[index(flSize-2,j)];
		}

		x[index(0,0)] = 0.5f*(x[index(1,0)]+x[index(0,1)]);
		x[index(0,flSize-1)] = 0.5f*(x[index(1,flSize-1)]+x[index(0,flSize-2)]);
		x[index(flSize-1,0)] = 0.5f*(x[index(flSize-2,0)]+x[index(flSize-1,1)]);
		x[index(flSize-1,flSize-1)] = 0.5f*(x[index(flSize-2,flSize-1)]+x[index(flSize-1,flSize-2)]);
	}
	
	public void advect(int b, double[] d, double[] d0, double[] velocX, double[] velocY, double dt) {
		double i0, i1, j0, j1;

		double dtx = dt*(flSize-2);
		double dty = dt*(flSize-2);

		double s0, s1, t0, t1;
		double tmp1, tmp2, x, y;

		double Ndouble = flSize;
		double idouble, jdouble;
		int i, j;

		for(j=1,jdouble=1;j<flSize-1;j++,jdouble++) { 
			for(i=1,idouble=1;i<flSize-1;i++,idouble++) {
				tmp1 = dtx*velocX[index(i,j)];
				tmp2 = dty*velocY[index(i,j)];
				x = idouble-tmp1;
				y = jdouble-tmp2;

				if (x<0.5f)x=0.5f;
				if (x>Ndouble+0.5f)x=Ndouble+0.5f;
				i0 = (double)Math.floor(x);
				i1 = i0+1.0f;
				if(y<0.5f)y=0.5f;
				if(y>Ndouble+0.5f)y=Ndouble+0.5f;
				j0 = (double)Math.floor(y);
				j1 = j0 + 1.0f;

				s1 = x-i0; 
				s0 = 1.0f-s1; 
				t1 = y-j0; 
				t0 = 1.0f-t1;

				int i0i = (int)i0;
				int i1i = (int)i1;
				int j0i = (int)j0;
				int j1i = (int)j1;
				d[index(i,j)] = s0*(t0*d0[index(i0i,j0i)]+t1*d0[index(i0i,j1i)])+s1*(t0*d0[index(i1i,j0i)]+t1*d0[index(i1i,j1i)]);
			}
		}

		set_bnd(b, d);
	}
}