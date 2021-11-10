package flsim;

import java.awt.image.BufferedImage;

import io.github.owenbharrison.displib.image.ImageFilters;
import io.github.owenbharrison.displib.maths.Maths;

public class Gradient{
	public BufferedImage image;
	private int width;
	private int[][] pixels;

	public Gradient(BufferedImage image){
		this.image = image;
		
		this.width = this.image.getWidth();
		this.pixels = new int[this.width][3];
		
		int[] data = ImageFilters.imageToPixels(this.image);
		for(int x=0;x<this.width;x++){
			int ix = x*3;
			this.pixels[x] = new int[] {data[ix], data[ix+1], data[ix+2]};
		}
	}
	
	public int[] getAtPercent(double u){
		int index = (int)Maths.clamp(u*this.pixels.length, 0, this.pixels.length-1);
		return this.pixels[index];
	}
}
