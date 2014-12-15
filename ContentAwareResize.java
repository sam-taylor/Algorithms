import java.awt.Color;

//usage: ContentAwareResize file.png [horizontal pixels to remove] [vert to remove]

public class ContentAwareResize
{
    private static final double INFINITY = Double.MAX_VALUE;
    private Picture picture;
    private static final double borderEnergy = 195075;
    
// create a ContentAwareResize object based on the given picture
    public ContentAwareResize(Picture picture)
    {
        this.picture = new Picture(picture);
    }
    
    // current picture
    public Picture picture()
    {
        return this.picture;
    }
    
    // width of current picture
    public     int width()  
    {
        return this.picture.width();
    }
    
    // height of current picture
    public     int height()    
    {
        return this.picture.height();
    }
    
    // energy of pixel at column x and row y
    public  double energy(int x, int y)   
    {
        if (x > width() - 1 || y > height() - 1 || x < 0 || y < 0) 
            throw new java.lang.IndexOutOfBoundsException();
        else if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1)
            return this.borderEnergy;
        else 
        {
            Color left = this.picture.get(x - 1, y);
            Color right = this.picture.get(x + 1, y);
            Color up = this.picture.get(x, y - 1);
            Color down = this.picture.get(x, y + 1);
            return gradient(left, right) + gradient(up, down);
        }
    }
    
    private double gradient(Color a, Color b)
    {
        double dRed = a.getRed() - b.getRed();
        double dBlu = a.getBlue() - b.getBlue();
        double dGrn = a.getGreen() - b.getGreen();
        return dRed*dRed + dBlu*dBlu + dGrn*dGrn;
    }
    
    private double[][] energies()
    {
        double[][] energies = new double[height()][width()];
        //enrg[row][column]
        for (int i = 0; i < height(); i++)
        {
            for (int j = 0; j < width(); j++)
            {
                energies[i][j] = energy(j, i);
            } 
        }
        //towardsTopEnergySmear(energies);
        return energies;
    }
    
    private double[][] transEnergies(double[][] energies)
    {
        int h = energies.length;
        int w = energies[0].length;
        double[][] tEnergies = new double[w][h];
        for (int row = 0; row < h; row++)
        {
            for (int col = 0; col < w; col++)
            {
                tEnergies[col][row] = energies[row][col];
            }
        }
        return tEnergies;
    }
    
    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() 
    {
        double[][] transEnergies = transEnergies(energies());
        return minVertPath(transEnergies);
    }
    // sequence of indices for vertical seam
    public int[] findVerticalSeam() 
    {
        return minVertPath(energies());
    }
    
    private static void towardsTopEnergySmear(double[][] energies)
    {
        int h = energies.length, w = energies[0].length;
        for (int row = h - 2; row >= 0; row--) 
        {
            for (int col = 0; col < w; col++)
            {
                double downCenter = energies[row + 1][col];
                double min = (col == 0) ?
                    downCenter : Math.min(downCenter, energies[row + 1][col-1]);
                min = (col == (w -1)) ? 
                    min : Math.min(min, energies[row + 1][col + 1]);
                energies[row][col] += min;
            }
        }
    }
    
    private static int[] minVertPath (double[][] energies)
    {
        towardsTopEnergySmear(energies);
        int h = energies.length;
        int w = energies[0].length;
        int[] minVertPath = new int[h]; //for each row this will designate the column to remove
        minVertPath[1] = 0;
        for (int j = 1; j < w; j++) //find lowest energy in top row
        {
            if (energies[1][j] < energies[1][minVertPath[1]])
                minVertPath[1] = j;
        }
        minVertPath[0] = minVertPath[1];
        for (int i = 2; i < h; i++) 
        {
            int targ = minVertPath[i - 1];
            minVertPath[i] = targ;
            //StdOut.println("target is:" + targ);
            //StdOut.println(energies[i][targ]);
            if (targ > 0 && energies[i][targ - 1] < energies[i][targ])
            {
                minVertPath[i] = targ - 1;
                //StdOut.println(energies[i][targ - 1]);
            }
            if (targ < w && energies[i][targ + 1] < energies[i][minVertPath[i]])
            {
                minVertPath[i] = targ + 1;
               // StdOut.println(energies[i][targ + 1]);
            }
        }
        return minVertPath;
    }
    
    
    
    
    public    void removeHorizontalSeam(int[] seam)  // remove horizontal seam from current picture
    {
        Picture pic = new Picture(width(), height() - 1);
        if (height() < 1 || !isValidSeam(seam, width(), height() - 1))
            throw new java.lang.IllegalArgumentException();
        for (int col = 0; col < width(); col++)
        {
            for (int row = 0; row < seam[col]; row++)
                pic.set(col, row, this.picture.get(col, row));
            for (int row = seam[col] + 1; row < height(); row++)
                pic.set(col, row - 1, this.picture.get(col, row));
        }     
        this.picture = pic;
    }
    
    
    public    void removeVerticalSeam(int[] seam)     // remove vertical seam from current picture
    {
        if (width() < 1 || !isValidSeam(seam, height(), width()))
            throw new java.lang.IllegalArgumentException();
        Picture pic = new Picture(width() - 1, height());
        for (int row = 0; row < height(); row++)
        {
            for (int col = 0; col < seam[row]; col++)
                pic.set(col, row, this.picture.get(col, row));
            for (int col = seam[row] + 1; col < width(); col++)
                pic.set(col - 1, row, this.picture.get(col, row));
        }     
        this.picture = pic;
    }
    
    private boolean isValidSeam(int[] seam, int size, int range)
    {
        if (seam.length != size || seam[0] > range || seam[0] < 0)
            return false;
        for (int i = 1; i < size; i++)
        {
            if (seam[i] > Math.min(range, seam[i - 1] + 1) || seam[i] < Math.max(0, seam[i - 1] - 1))
                return false;
        }
        return true;
    }
}
