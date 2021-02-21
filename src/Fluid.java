import processing.core.*;

import java.lang.Math;

public class Fluid {
	public PApplet papplet;
  public int size;
  public float dt;
  public float diff;
  public float visc;
    
  public float[] s;
  public float[] density;
    
  public float[] Vx;
  public float[] Vy;

  public float[] Vx0;
  public float[] Vy0;
  
  public Fluid(PApplet papp, float diffusion, float viscosity, float dt_) {
    papplet = papp;
  	
  	size = Main.N;
    dt = dt_;
    diff = diffusion;
    visc = viscosity;
    
    s = new float[Main.N*Main.N];
    density = new float[Main.N*Main.N];
    
    Vx = new float[Main.N*Main.N];
    Vy = new float[Main.N*Main.N];
    
    Vx0 = new float[Main.N*Main.N];
    Vy0 = new float[Main.N*Main.N];
  }
  
  public void timeStep() {
    diffuse(1, Vx0, Vx, visc, dt);
    diffuse(2, Vy0, Vy, visc, dt);
    
    project(Vx0, Vy0, Vx, Vy);
    
    advect(1, Vx, Vx0, Vx0, Vy0, dt);
    advect(2, Vy, Vy0, Vx0, Vy0, dt);
    
    project(Vx, Vy, Vx0, Vy0);
    
    diffuse(0, s, density, diff, dt);
    advect(0, density, s, Vx, Vy, dt);
  }
  
  public void renderD() {
  	papplet.loadPixels();
  	for(int x=0;x<papplet.width;x++) {
  		for(int y=0;y<papplet.height;y++) {
  			float d = density[IX(PApplet.round(x/Main.SCALE), PApplet.round(y/Main.SCALE))];
		    papplet.pixels[x+y*papplet.width] = papplet.color(d, 255, 255);
    	}
  	}
  	papplet.updatePixels();
  }
  
  public void fadeD() {
  	for(int i=0;i<density.length;i++) {
  		density[i] = PApplet.constrain(density[i]-0.4f, 0, 255);
  	}
  }
  
  public void addDensity(int x, int y, float amount) {
    density[IX(x, y)] += amount;
  }
  
  public void addVelocity(int x, int y, float amountX, float amountY){
    int index = IX(x, y);
    Vx[index] += amountX;
    Vy[index] += amountY;
  }

  public int IX(int x, int y) {
  	if(x<0)x = 0;
  	if(x>Main.N-1)x = Main.N-1;
  	if(y<0)y = 0;
  	if(y>Main.N-1)y = Main.N-1;
	  return x+y*Main.N;
  }

  public void diffuse(int b, float[] x, float[] x0, float diff, float dt) {
    float a = dt*diff*(Main.N-2)*(Main.N-2);
    lin_solve(b,x,x0,a,1+6*a);
  }

  public void lin_solve(int b, float[] x, float[] x0, float a, float c){
    float cRecip = 1.0f / c;
    for(int k=0;k<Main.iter;k++) {
      for(int j=1;j<Main.N-1;j++) {
        for(int i=1;i<Main.N-1;i++) {
          x[IX(i, j)] =
            (x0[IX(i, j)]
            +a*(x[IX(i+1,j)]
            +x[IX(i-1,j)]
            +x[IX(i,j+1)]
            +x[IX(i,j-1)]
          )) * cRecip;
        }
      }
      set_bnd(b, x);
    }
  }

  public void project(float[] velocX, float[] velocY, float[] p, float[] div){
    for(int j=1;j<Main.N-1;j++) {
      for(int i=1;i<Main.N-1;i++) {
        div[IX(i,j)] = -0.5f*(
          velocX[IX(i+1,j)]
          -velocX[IX(i-1,j)]
          +velocY[IX(i,j+1)]
          -velocY[IX(i,j-1)]
        )/Main.N;
        p[IX(i, j)] = 0;
      }
    }
    set_bnd(0, div); 
    set_bnd(0, p);
    lin_solve(0, p, div, 1, 6);
    for(int j=1;j<Main.N-1;j++) {
      for (int i=1;i<Main.N-1;i++) {
        velocX[IX(i,j)] -= 0.5f*(p[IX(i+1,j)]-p[IX(i-1,j)])*Main.N;
        velocY[IX(i,j)] -= 0.5f*(p[IX(i,j+1)]-p[IX(i,j-1)])*Main.N;
      }
    }
    set_bnd(1, velocX);
    set_bnd(2, velocY);
  }

  public void set_bnd(int b, float[] x) {
    for (int i=1;i<Main.N-1;i++){
      x[IX(i,0)] = b==2?-x[IX(i,1)]:x[IX(i,1)];
      x[IX(i,Main.N-1)] = b==2?-x[IX(i,Main.N-2)]:x[IX(i,Main.N-2)];
    }
    for (int j=1;j<Main.N-1;j++) {
      x[IX(0,j)] = b==1?-x[IX(1,j)]:x[IX(1,j)];
      x[IX(Main.N-1,j)] = b==1?-x[IX(Main.N-2,j)]:x[IX(Main.N-2,j)];
    }

    x[IX(0,0)] = 0.5f*(x[IX(1,0)]+x[IX(0,1)]);
    x[IX(0,Main.N-1)] = 0.5f*(x[IX(1,Main.N-1)]+x[IX(0,Main.N-2)]);
    x[IX(Main.N-1,0)] = 0.5f*(x[IX(Main.N-2,0)]+x[IX(Main.N-1,1)]);
    x[IX(Main.N-1,Main.N-1)] = 0.5f*(x[IX(Main.N-2,Main.N-1)]+x[IX(Main.N-1,Main.N-2)]);
  }
  
  public void advect(int b, float[] d, float[] d0, float[] velocX, float[] velocY, float dt) {
    float i0, i1, j0, j1;

    float dtx = dt*(Main.N-2);
    float dty = dt*(Main.N-2);

    float s0, s1, t0, t1;
    float tmp1, tmp2, x, y;

    float Nfloat = Main.N;
    float ifloat, jfloat;
    int i, j;

    for(j=1,jfloat=1;j<Main.N-1;j++,jfloat++) { 
      for(i=1,ifloat=1;i<Main.N-1;i++,ifloat++) {
        tmp1 = dtx*velocX[IX(i,j)];
        tmp2 = dty*velocY[IX(i,j)];
        x = ifloat-tmp1;
        y = jfloat-tmp2;

        if (x<0.5f)x=0.5f;
        if (x>Nfloat+0.5f)x=Nfloat+0.5f;
        i0 = (float)Math.floor(x);
        i1 = i0+1.0f;
        if(y<0.5f)y=0.5f;
        if(y>Nfloat+0.5f)y=Nfloat+0.5f;
        j0 = (float)Math.floor(y);
        j1 = j0 + 1.0f;

        s1 = x-i0; 
        s0 = 1.0f-s1; 
        t1 = y-j0; 
        t0 = 1.0f-t1;

        int i0i = (int)i0;
        int i1i = (int)i1;
        int j0i = (int)j0;
        int j1i = (int)j1;
        d[IX(i,j)] = 
          s0*(t0*d0[IX(i0i,j0i)]+t1*d0[IX(i0i,j1i)]) +
          s1*(t0*d0[IX(i1i,j0i)]+t1*d0[IX(i1i,j1i)]);
      }
    }

    set_bnd(b, d);
  }
}
