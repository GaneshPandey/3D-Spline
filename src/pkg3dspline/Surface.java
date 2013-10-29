/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3dspline;

/**
 *
 * @author Akash
 */
public class Surface {
    private Point[] points;
    private int index;
    private Vector normal;
    public Surface(){
        points = new Point[3];
        index = 0;
    }
    public void setNormal(float a, float b, float c){
        normal = new Vector();
        normal.setVector(a,b,c);
    }
    public Vector getNormal(){
        return this.normal;
    }
    public void addPoint(Point p){
        if (index > 2)
            return;
        points[index] = p;
        index++;
    }
    
    public Point[] getPoint(){
        return this.points;
    }
}
