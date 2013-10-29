/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3dspline;

/**
 *
 * @author Akash
 */
public class Vector {
    private float[] vect;
    int index;
    public Vector(){
        vect = new float[3];
        index = 0;
    }
    public void setVector(float x, float y, float z){
            vect[0] = (index*(vect[0])+x)/(index+1);
            vect[1] = (index*(vect[1])+y)/(index+1);
            vect[2] = (index*(vect[2])+z)/(index+1);
            index++;
        
    }
    
    public float[] getVector(){
        return this.vect;
    }
    
    public static float dotProduct(Vector x, Vector y){
        float[] a = x.getVector();
        float[] b = y.getVector();
        return (a[0]*b[0]+a[1]*b[1]+a[2]*b[2]);
    }
    public static Vector crossProduct(Vector x, Vector y){
        float[] a = x.getVector();
        float[] b = y.getVector();
        Vector naya = new Vector();
        float i = a[1]*b[2] - a[2]*b[1];
        float j = a[2]*b[0] - a[0]*b[2];
        float k = a[0]*b[1] - a[1]*b[0];
        naya.setVector(i,j,k);
        return naya;
    }
    
    public Vector unitVector(){
        float d = (float) Math.sqrt(vect[0]*vect[0]+vect[1]*vect[1]+vect[2]*vect[2]);
        Vector v = new Vector();
        v.setVector(vect[0]/d, vect[1]/d, vect[2]/d);
        return v;
    }
    
}
