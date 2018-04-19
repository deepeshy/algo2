import edu.princeton.cs.algs4.Picture;

import java.awt.*;

public class SeamCarver {
    private Picture picture;
    private double[][] energy;

    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Null Picture");
        }
        this.picture = picture;
        energy = new double[width()][height()];
        calcEnergy();
    }

    private void calcEnergy() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (isOnborder(i, j)) {
                    this.energy[i][j] = 1000.0;
                } else {
                    this.energy[i][j] = getPixelEnergy(i, j);
                }
            }
        }
    }

    private double getPixelEnergy(int i, int j) {
        if (isOnborder(i, j)) throw new IllegalArgumentException("Trying to calculate energy for pixels at border");
        // Rx, Gx, Bx
        // R(x, y) = R(x+1, y) - R(x-1, y)
        Color leftCell = picture.get(i - 1, j);
        Color rightCell = picture.get(i + 1, j);
        double DeltaSqX = Math.pow(leftCell.getRed() - rightCell.getRed(), 2) +
                Math.pow(leftCell.getBlue() - rightCell.getBlue(), 2) +
                Math.pow(leftCell.getGreen() - rightCell.getGreen(), 2);

        // Ry, Gy, By
        Color topCell = picture.get(i, j - 1);
        Color bottomCell = picture.get(i, j + 1);

        double DeltaSqY = Math.pow(topCell.getRed() - bottomCell.getRed(), 2) +
                Math.pow(topCell.getBlue() - bottomCell.getBlue(), 2) +
                Math.pow(topCell.getGreen() - bottomCell.getGreen(), 2);

        double energyForPixel = 1.000 * Math.sqrt(DeltaSqX + DeltaSqY);
        return energyForPixel;
    }


    private boolean isOnborder(int x, int y) {
        return x == 0 || x == width() - 1 || y == 0 || y == height() - 1;
    }

    public Picture picture() {
        return this.picture;
    }

    // width of current picture
    public int width() {
        return picture().width();
    }

    // height of current picture
    public int height() {
        return picture().height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            throw new IllegalArgumentException("x, y outside picture boundary");
        }
        return this.energy[x][y];
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return null;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return null;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("Null horizontal seam");
        }

        if (picture().width() <= 1) {
            throw new IllegalArgumentException("Picture too small");
        }
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("Null horizontal seam");
        }

        if (picture().height() <= 1) {
            throw new IllegalArgumentException("Picture too small");
        }
    }


}
