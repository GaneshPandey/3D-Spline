/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3dspline;

/**
 *
 * @author Akash
 */
public class Matrix {
    public static float[][] multiply(float a[][], float b[][]) {
        int aRows = a.length,
            aColumns = a[0].length,
            bRows = b.length,
            bColumns = b[0].length;

        if ( aColumns != bRows ) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        float[][] resultant = new float[aRows][bColumns];

        for(int i = 0; i < aRows; i++) { // aRow
            for(int j = 0; j < bColumns; j++) { // bColumn
            for(int k = 0; k < aColumns; k++) { // aColumn
                resultant[i][j] += a[i][k] * b[k][j];
            }
            }  
        }
        return resultant;
    }
    
    public static float[][] transpose(float a[][]) {
        int aRows = a.length,
            aColumns = a[0].length;

        float[][] resultant = new float[aColumns][aRows];
        
        for(int i=0; i<aRows; i++)
            for (int j=0; j<aColumns; j++)
                resultant[j][i] = a[i][j];
        return resultant;
    }
    
    public static float[][] normalize(float a[][]){
        int aRows = a.length,
            aColumns = a[0].length;
        float[][] resultant = new float[aRows-1][aColumns];
        for (int i=0; i<aRows-1; i++){
            for (int j=0; j<aColumns; j++)
                resultant[i][j] = a[i][j]/a[aRows-1][j];
        }
        return resultant;
    }

}
