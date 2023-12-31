package io.github.lightman314.lightmanscurrency.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {

	public static Vector3f XP() { return new Vector3f(1f,0f,0f); }
	public static Vector3f YP() { return new Vector3f(0f,1f,0f); }
	public static Vector3f ZP() { return new Vector3f(0f,0f,1f); }

	public static Quaternionf getRotationDegrees(float angleDegrees) { return new Quaternionf().fromAxisAngleDeg(YP(), angleDegrees); }
	public static Quaternionf getRotationDegrees(Vector3f direction, float angleDegrees) { return new Quaternionf().fromAxisAngleDeg(direction, angleDegrees); }


	/**
	 * Multiplies all parts of a Vector3f by a float
	 */
	public static Vector3f VectorMult(Vector3f vector, float num)
	{
		return new Vector3f(vector.x() * num, vector.y() * num, vector.z() * num);
	}
	
	/**
	 * Sum all of the Vector3f's together
	 */
	public static Vector3f VectorAdd(Vector3f... vectors)
	{
		float x = 0f;
		float y = 0f;
		float z = 0f;
		
		for(Vector3f vector : vectors)
		{
			x += vector.x();
			y += vector.y();
			z += vector.z();
		}
		
		return new Vector3f(x, y, z);
	}
	
	/**
	 * Restricts an integer between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static int clamp(int value, int min, int max)
	{
		if(min > max)
		{
			int temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}
	
	/**
	 * Restricts a float between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static float clamp(float value, float min, float max)
	{
		if(min > max)
		{
			float temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}
	
	/**
	 * Restricts a double between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static double clamp(double value, double min, double max)
	{
		if(min > max)
		{
			double temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}
	
	/**
	 * Restricts a long between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static long clamp(long value, long min, long max)
	{
		if(min > max)
		{
			long temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}

	public static int DivideByAndRoundUp(int a, int b)
	{
		int result = a/b;
		if(a%b != 0)
			result++;
		return result;
	}
	
}