import java.util.Random;

// helper class is a class to get random variables according to a certain distribution


public class Helper extends Simulation {
    public static double Exponential_distribution (double lambda, Random r,Boolean c){

        if(!c)
        {
            double j1 = (float) r.nextInt(1000+1)/1000;
            if (j1 == 0)
                j1 += 0.0001;
            double j2 = -Math.log(j1)/lambda;

            return j2;
        }
        else{
        double j1 = (float) r.nextInt(1000+1)/1000;
        j1 = 1-j1;
        if (j1 == 0)
            j1 += 0.0001;
        double j2 = -Math.log(j1)/lambda;

        return j2;}
    }

    public static int  Normal_distribution(double mean, double stdev, Random r,boolean c){   // TO MODEL BASED ON CUMULATIVE DENSITY FUNCTION OF NORMAL DISTRIBUTION BASED ON BOOK OF SHELDON ROSS, Simulation, The polar method, p80.
        double v1, v2, t;
        do{
            if (!c)
            {
                v1 = (float) r.nextInt(1000 + 1) * 2;
                v1 /= 1000;
                v1 -= 1;
                v2 = (float) r.nextInt(1000 + 1) * 2;
                v2 /= 1000;
                v2 -= 1;
            }
            else
            {
                v1 = (float) r.nextInt(1000 + 1) * 2;
                v1 /= 1000;
                v1 -= 1;
                v1 = 1-v1;
                v2 = (float) r.nextInt(1000 + 1) * 2;
                v2 /= 1000;
                v2 -= 1;
                v2 = 1-v2;
            }
            t=v1*v1+v2*v2;
        }
        while(t>=1||t==0);
        double multiplier = Math.sqrt(-2*Math.log(t)/t);
        return (int) (v1 * multiplier * stdev + mean);
    }

    public static int  Bernouilli_distribution(double prob, Random r, boolean c){     // INVERSION METHOD BERNOUILLI DISTRIBUTION
       if(!c)
       {
           double j1 = (float) r.nextInt(1000 + 1) / 1000;
           if (j1 < prob)
               return 0;
           else
               return 1;
       }
       else {
           double j1 = (float) r.nextInt(1000 + 1) / 1000;
           j1 = 1 - j1;
           if (j1 < prob)
               return 0;
           else
               return 1;
       }
    }


}
