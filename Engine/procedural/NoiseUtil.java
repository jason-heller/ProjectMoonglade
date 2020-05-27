package procedural;

public class NoiseUtil {
	//TODO: Remove this class
	public static int seed = 0;
	
	/*public static void reseed() {
		Random r = new Random();
		seed = r.nextInt()*19990303;
	}*/
	
	public static double noise1d(int x) {
		x += seed;
		//x = (x >> 13);//(int) Math.pow(, x);
		int nn = (x * (x * x * 60493 + 19990303) + 1376312589) & 0x7fffffff;
	  return 1.0 - ((double)nn / 1073741824.0);
	}
	
	public static double noise1d(int x, long s) {
		x += s;
		//x = (x >> 13);//(int) Math.pow(, x);
		int nn = (x * (x * x * 60493 + 19990303) + 1376312589) & 0x7fffffff;
	  return 1.0 - ((double)nn / 1073741824.0);
	}
	
	public static float noise2d(long x, long y) {
		return noise2d(x, y, seed);
	}
	
	public static float noise2d(long x, long y, long s) {
		x *= s;
		y *= s*60493;
		/* mix around the bits in x: */
		x = x * 3266489917l + 374761393;
		x = (x << 17) | (x >> 15);

		/* mix around the bits in y and mix those into x: */
		x += y * 3266489917l;

		/* Give x a good stir: */
		x *= 668265263;
		x ^= x >> 15;
		x *= 2246822519l;
		x ^= x >> 13;
		x *= 3266489917l;
		x ^= x >> 16;

		/* trim the result and scale it to a float in [0,1): */
		return (x & 0x00ffffff) * (1.0f / 0x1000000);
	}
	
	public static double noiseLerp1d(double x) {
		int intX = (int) (Math.floor(x));
		double n0 = noise1d(intX);
		double n1 = noise1d(intX + 1);
		double weight = x - Math.floor(x);
		double noise = lerp(n0, n1, curve(weight));
		return noise;
	}
	
	public static double noiseLerp2d(double x, double y) {
		x += (x%2==0) ? 1451 : 804;
		y += (y%2==1) ? 7057 : 6046;
		
		return noiseLerp1d(x/1.0)/2.0 + noiseLerp1d(y/1.0)/2.0;
	}
	
	private static double curve(double weight) {
		return -2*(weight*weight*weight) + 3*(weight*weight);
	}
	
	public static double lerp(double start, double end, double amount) {
		return start + amount * (end - start);
	}

	public static void setSeed(String terrainSeed) {
		seed = terrainSeed.hashCode();
	}

	public static long szudzik(int x, int y) {
		// NXN -> N pairing funct
		return y > x ? y * y + x : x * x + x + y;
	}
}

