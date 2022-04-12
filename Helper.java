import java.util.Random;

public class Helper extends Simulation {
    // variables for helper class
    //none

    // methods for Helper class

    /* COUNTER */

    public int i6;
    private double j1, j2, j3;


    public static Random rnd;

    public Helper(long seed){
        rnd = new Random(seed);
    }

    public int poissonDistribution(double lambda){
        double k, L;
        int p;
        j1 = (Math.round(rnd.nextDouble()*1000))/1000.0;
        k = 0;
        L = Math.exp(-lambda);
        j3 = 0;
        do
        {
            j2 = L * Math.pow(lambda, k);
            p = 1;
            for (i6 = 0; i6 <= k; i6++)
            {   if (i6 == 0)
                p = 1;
            else
                p *= i6;

            }
            j2 /= p;
            j3 += j2;
            k++;
        } while (j1 >= j3);

        return (int) (k-1);

    }
    int normal_distribution(double mean, double stdev)
    {   // TO MODEL BASED ON CUMULATIVE DENSITY FUNCTION OF NORMAL DISTRIBUTION BASED ON BOOK OF SHELDON ROSS, Simulation, The polar method, p80.

        double v1, v2, t;
        int x;
        do
        {
            v1 = (double) (rnd.nextDouble())*2;
            v1 /= 1000;
            v1 -= 1;
            v2 = (double) (rnd.nextDouble())*2;
            v2 /= 1000;
            v2 -= 1;
            t=v1*v1+v2*v2;
        }
        while(t>=1||t==0);
        double multiplier = Math.sqrt(-2*Math.log(t)/t);
        x = (int) (v1 * multiplier * stdev + mean);
        return x;


    }

    int bernouilli_distribution(double prob)     // INVERSION METHOD BERNOUILLI DISTRIBUTION
    {   j1 = (Math.round(rnd.nextDouble()*1000))/1000.0;;
        if (j1 < prob)
            return 0;
        else
            return 1;


    }

    int uniform_distribution(double a, double b) // INVERSION METHOD UNIFORM DISTRIBUTION
    {   int x;
        j1 = (Math.round(rnd.nextDouble()*1000))/1000.0;
        x = (int) (a + (b-a) * j1);

        return x;
    }


    int triangular_distribution(int a, int b, int c) // INVERSION METHOD TRIANGULAR DISTRIBUTION
    {   double mean, stdev;
        double x, L;

        mean = (a+b+c)/3;
        stdev = (Math.pow(a,2)+Math.pow(b,2)+Math.pow(c,2)-a*b-a*c-b*c)/18;
        stdev = Math.sqrt(stdev);
        j1 = (Math.round(rnd.nextDouble()*1000))/1000.0;;
        x = a;

        do
        {   if (x <= b)
            L = Math.pow((x-a),2)/((c-a)*(b-a));
        else
            L = 1-(Math.pow(c-x,2)/((c-a)*(c-b)));
            x++;
        } while (j1 >= L);

        return (int) (x-1);

    }


}
