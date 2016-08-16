import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class CG_hw2
{

	int TOP = 0;
	int BOTTOM = 1;
	int LEFT = 2;
	int RIGHT = 3;
	int clip_boundary;
	float intersect_x, intersect_y;
	int world_x1 = 0, world_y1 = 0, world_x2 = 499, world_y2 = 499;
	float scaling_factor = 1.0f;
	int width = (world_x2 - world_x1) + 1;
	int height = (world_y2 - world_y1) + 1;
	int rotation = 0, translation_x = 0, translation_y = 0;
	int pixels [][];
	String input = "hw2_a.ps";
	List<List<Integer>> points = new ArrayList<List<Integer>>();
	List<List<Float>> transformed_lines = new ArrayList<List<Float>>();
	List<List<Float>> clipped_lines = new ArrayList<List<Float>>();



	public void transfer_points()
	{
		transformed_lines.clear();
		for(int i = 0; i < clipped_lines.size(); i++)
		{
			List<Float> row = new ArrayList<Float>();

			float x = clipped_lines.get(i).get(0);
			float y = clipped_lines.get(i).get(1);

			row.add(x);
			row.add(y);

			transformed_lines.add(row);

		}

		if(clip_boundary != LEFT)
			clipped_lines.clear();

	}
	public void clipping()
	{
		//Sutherland-Hodgman clipping

		clip_boundary = TOP;
		clipper();
		transfer_points();

		clip_boundary = RIGHT;
		clipper();
		transfer_points();

		clip_boundary = BOTTOM;
		clipper();
		transfer_points();

		clip_boundary = LEFT;
		clipper();
		transfer_points();
	}

	public void output_vertex(float x, float y, int index)
	{
		List<Float> row = new ArrayList<Float>();
		row.add(x);
		row.add(y);

		clipped_lines.add(row);

	}
	public void clipper()
	{
		for(int j=0; j<transformed_lines.size() - 1; j++)
		{
			float x1 = transformed_lines.get(j).get(0);
			float y1 = transformed_lines.get(j).get(1);
			float x2 = transformed_lines.get(j+1).get(0);
			float y2 = transformed_lines.get(j+1).get(1);

			if(inside(x1, y1))
			{	
				if(j == 0)
					output_vertex(x1, y1, j);

				if(inside(x2, y2))
				{
					output_vertex(x2, y2, j+1);

				}
				else
				{
					intersection(x1, y1, x2, y2, clip_boundary);
					output_vertex(intersect_x, intersect_y, j+1);
				}
			}
			else
			{
				if(inside(x2, y2))
				{
					intersection(x2, y2, x1, y1, clip_boundary);
					output_vertex(intersect_x, intersect_y, j);

					output_vertex(x2, y2, j + 1);
				}
			}
		}



	}

	public void intersection(float x1, float y1, float x2, float y2, int clip_boundary)
	{
		float dx = x2 - x1;
		float dy = y2 - y1;

		float slope = dy/dx;

		//Vertical line condition
		if(dx == 0 || dy == 0)
		{

			if(clip_boundary == TOP)
			{
				intersect_x = x1;
				intersect_y = world_y2;
			}
			else if(clip_boundary == LEFT)
			{
				intersect_x = world_x1;
				intersect_y = y1;
			}
			else if(clip_boundary == BOTTOM)
			{
				intersect_x = x1;
				intersect_y = world_y1;
			}
			else if(clip_boundary == RIGHT)
			{
				intersect_x = world_x2;
				intersect_y = y1;
			}

			return;
		}

		if(clip_boundary == LEFT)
		{
			intersect_x = world_x1;
			intersect_y = slope * (world_x1 - x1) + y1;
		}
		if(clip_boundary == RIGHT)
		{
			intersect_x = world_x2;
			intersect_y = slope * (world_x2 - x1) + y1;
		}
		if(clip_boundary == TOP)
		{
			intersect_x = (world_y2 - y1)/slope + x1;
			intersect_y = world_y2;
		}
		if(clip_boundary == BOTTOM)
		{
			intersect_x = (world_y1 - y1)/slope + x2;
			intersect_y = world_y1;
		}
	}

	public boolean inside(float x, float y)
	{

		if(clip_boundary == TOP && y < world_y2)
			return true;

		else if(clip_boundary == LEFT && x > world_x1)
			return true;

		else if(clip_boundary == BOTTOM && y > world_y1)
			return true;

		else if(clip_boundary == RIGHT && x < world_x2)
			return true;

		return false;

	}


	public void drawing()
	{
		for (int i=0; i<height; i++)
		{
			for (int j=0; j<width; j++)
			{
				pixels[i][j] = 0;
			}	
		}
		for (int i=0; i<clipped_lines.size() - 1; i++)
		{
			float x1 = clipped_lines.get(i).get(0);
			float y1 = clipped_lines.get(i).get(1);
			float x2 = clipped_lines.get(i + 1).get(0);
			float y2 = clipped_lines.get(i + 1).get(1);


			//DDA

			float dx,dy,steps;
			float xc,yc;
			float x,y;

			dx = x2 - x1;
			dy = y2 - y1;

			if(Math.abs(dx) > Math.abs(dy))
				steps = Math.abs(dx);

			else
				steps = Math.abs(dy);

			if(x1 == x2 && dy < 0)
				steps = Math.abs(dy);


			xc = dx/steps;

			yc = dy/steps;

			//if(x1 == x2 && dy < 0)
			//yc = Math.abs(dy)/steps;

			x = (int)x1;

			y = (int)y1;



			//	pixels[Math.round(y-world_y1)][Math.round(x-world_x1)] = 1;

			for (int j=0; j<steps; j++)
			{
				x = x + xc;
				y = y + yc;

				if(!(x < world_x1 || y < world_y1 || x > world_x2 || y > world_y2))
					pixels[Math.round(y-world_y1)][Math.round(x-world_x1)] = 1;
			}
		}
	}

	public void output() throws FileNotFoundException, UnsupportedEncodingException
	{
		System.out.println("/*XPM*/");
		System.out.println("static char *sco100[] = { ");
		System.out.println("/* width height num_colors chars_per_pixel */ ");
		System.out.println("\""+ width + " " + height + " " + "2" + " " + "1" + "\"" + ",");
		System.out.println("/*colors*/");
		System.out.println("\""+ "0" + " " + "c" + " " + "#" + "ffffff" + "\"" + "," );
		System.out.println("\""+ "1" + " " + "c" + " " + "#" + "000000" + "\"" + "," );
		System.out.println("/*pixels*/");
		for (int i=0; i<height; i++)
		{
			System.out.print("\"");
			for(int j=0; j<width; j++)
			{
				System.out.print(pixels[height-i-1][j]);
			}
			if(i == height - 1)
				System.out.print("\"");
			else
				System.out.print("\"" + ",");

			System.out.println();
		}

		System.out.println("};");
	}

	public void transformation()
	{
		//Scaling

		List<List<Float>> scaled_lines = new ArrayList<List<Float>>();

		for (int i=0; i<points.size(); i++)
		{
			List<Float> row = new ArrayList<Float>();

			for(int j=0; j<2; j++)
			{

				float temp = points.get(i).get(j);
				temp = temp * scaling_factor;
				row.add(temp);
			}
			scaled_lines.add(row);
		}

		//Rotation
		List<List<Float>> rotated_lines = new ArrayList<List<Float>>();

		for(int i=0; i<scaled_lines.size(); i++)
		{
			List<Float> row1 = new ArrayList<Float>();

			float x = scaled_lines.get(i).get(0);
			float y = scaled_lines.get(i).get(1);
			double x_prime = x * Math.cos(Math.toRadians(rotation)) - y * Math.sin(Math.toRadians(rotation));
			double y_prime = x * Math.sin(Math.toRadians(rotation)) + y * Math.cos(Math.toRadians(rotation));

			row1.add((float)x_prime);
			row1.add((float)y_prime);

			rotated_lines.add(row1);
		}

		//Translation
		for(int i=0; i<rotated_lines.size(); i++)
		{
			List<Float> row2 = new ArrayList<Float>();

			float x = rotated_lines.get(i).get(0);
			float y = rotated_lines.get(i).get(1);
			x = x + translation_x;
			y = y + translation_y;
			row2.add(x);
			row2.add(y);

			transformed_lines.add(row2);
		}
	}
	public void read_file(String input) throws FileNotFoundException
	{
		File file = new File(input);
		Scanner sc = new Scanner(file);
		while(sc.hasNextLine())
		{

			if(sc.nextLine().equals("%%%BEGIN"))
			{	

				while(sc.hasNextLine())
				{
					String line = sc.nextLine();

					if(line.equals("%%%END") || line.equals("stroke"))
					{
						break;
					}

					String parse[] = line.split(" ");
					int x1 = Integer.parseInt(parse[0]);
					int y1 = Integer.parseInt(parse[1]);

					List<Integer> row = new ArrayList<Integer>();
					row.add(x1); 
					row.add(y1); 


					points.add(row);	
				}
			}
		}
		sc.close();   
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		CG_hw2 obj = new CG_hw2();

		for (int i=0; i<args.length; i+=2)
		{
			if(args[i].equals("-f"))
			{

				obj.input = args[i+1];
			}

			if(args[i].equals("-a"))
			{
				obj.world_x1 = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-b"))
			{
				obj.world_y1 = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-c"))
			{
				obj.world_x2 = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-d"))
			{
				obj.world_y2 = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-r"))
			{
				obj.rotation = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-m"))
			{
				obj.translation_x = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-n"))
			{
				obj.translation_y = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-s"))
			{
				obj.scaling_factor = Float.parseFloat(args[i+1]);
			}
		}

		obj.read_file(obj.input);
		obj.width = (obj.world_x2 - obj.world_x1) + 1;
		obj.height = (obj.world_y2 - obj.world_y1) + 1;
		obj.pixels = new int[obj.height][obj.width];
		obj.transformation();
		obj.clipping();
		obj.drawing();
		obj.output();

		/*
		for(int i=0; i< obj.clipped_lines.size(); i++)
		{
			for(int j=0; j<2; j++)
			{
				System.out.print(obj.clipped_lines.get(i).get(j) + " ");
			}
			System.out.println();
		}
		 */
	}

}
