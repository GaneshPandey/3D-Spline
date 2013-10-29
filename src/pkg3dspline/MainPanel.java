/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3dspline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Akash
 */
public class MainPanel extends javax.swing.JPanel {
    private float xprp=0,yprp=100,zprp = -150, zvp = 200;
    private Point[][] controlPoints;
    private ArrayList<Surface> triangles;
//    private Point[][] historyControl;
    int radiusControl = 10; // radius of the control point circle in show
    private ArrayList<Point> lightSources;
    private ArrayList<Point> points;
    private float wireframeCount; // total no of division to produce each surface
    private boolean wireframe; // wireframe or not
    private int shX, shY; // total shift required in X & Y to make it display converge at the center of screen
    private Point2D draggableControl; //actually holds the index of controlPoint that needs to be dragged
    private int tempX, tempY; // used in mouse dragging module as temp variables... but wanted to be static var so used here
    private boolean showControl; // show control points or not
    private boolean showLight; // show lightSources or not
    private boolean Shade; // what shade? true=fillShade else fillFlat
    
    public MainPanel(){
        /*Initialize control points*/
        float x[][][] = {{{-200,0,1},{-100,0,1},{100,0,1},{200,0,1}},
                         {{-200,0,75},{-100,0,75},{100,0,75},{200,0,75}},
                         {{-200,0,125},{-100,0,125},{100,0,125},{200,0,125}},
                         {{-200,0,200},{-100,0,200},{100,0,200},{200,0,200}}};
        controlPoints = new Point[4][4];
        for (int row=0; row<4; row++)
            for (int col=0; col<4;col++){
                controlPoints[row][col] = new Point(x[row][col][0],x[row][col][1],x[row][col][2]);
            }
        
        draggableControl = new Point2D();
        draggableControl.x=-1;draggableControl.y=-1;
        points = new ArrayList<>();
        lightSources = new ArrayList<>();
        Point ha = new Point(0,-100,70);
        ha.setColor(new Color(255,2,25));
        lightSources.add(ha);
        Point hb = new Point(-100,-150,20);
        hb.setColor(new Color(100,152,123));
        lightSources.add(hb);
        this.wireframeCount = (float) 0.02;
        this.triangles = new ArrayList<>();
        
        this.evalSurface(); // evaluate the surface coordinates from the control points
        
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e){
                int newX=e.getX(), newY=e.getY();
                if (tempX==0 || tempY==0){ tempX=newX;tempY=newY; } /*initial case*/
                if (showControl){
                    // Poll..for which control has been dragging
                    Point2D mouse = new Point2D();
                    mouse.x = e.getX();
                    mouse.y = e.getY();
                    if(draggableControl.x==-1 && draggableControl.y==-1){
                        for(int row=0;row<4;row++)
                            for (int col=0;col<4;col++){
                                float[] light = controlPoints[row][col].getPlotPoints();                    
        //                        System.out.println("mouseclicked: " + mouse.x + " " + mouse.y);
                                if(mouse.x<=(light[0]+radiusControl+shX) && mouse.x>=(light[0]-radiusControl+shX) && mouse.y<=(light[1]+radiusControl+shY) && mouse.y>=(light[1]-radiusControl+shY)){
                                    draggableControl.x = row;
                                    draggableControl.y = col;
                                    break;
                                }
                            }
                    }

                    // update coordinate of that control point                    
                    int dx = (newX-tempX)/5, dy=(newY-tempY)/5;
//                    System.out.println(dx + "dx, dy" + dy);
                    if(draggableControl.x!=-1 && draggableControl.y!=-1){
                        float[] ppp = controlPoints[draggableControl.x][draggableControl.y].getTruePoints();
                        controlPoints[draggableControl.x][draggableControl.y].setPoints(ppp[0]+dx*(ppp[2]+zvp)/20, ppp[1]+dy*(ppp[2]+zvp)/20, ppp[2]);
//                        controlPoints[draggableControl.x][draggableControl.y].setPoints(, , ppp[2]);
//                        System.out.println((ppp[0]-dx) + " " + (ppp[1]+dy));
                        evalSurface();
                        repaint();
                    }
                }
//                rotation((tempX-shX)/25, (tempY-shY)/25, (newX-shX)/25, (newY-shY)/25);
                
                
                tempX=newX; tempY=newY;
            }
        });
        this.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseReleased(MouseEvent e){
                tempX = 0; tempY = 0;
                draggableControl.x = -1;
                draggableControl.y = -1;
            }
            @Override
            public void mouseClicked(MouseEvent e){
                if (showLight){
                    Point2D mouse = new Point2D();
                    mouse.x = e.getX();
                    mouse.y = e.getY();
                        for(int i=0;i<lightSources.size();i++){
                                float[] light = lightSources.get(i).getPlotPoints();                    
        //                        System.out.println("mouseclicked: " + mouse.x + " " + mouse.y);
                                if(mouse.x<=(light[0]+radiusControl+shX) && mouse.x>=(light[0]-radiusControl+shX) && mouse.y<=(light[1]+radiusControl+shY) && mouse.y>=(light[1]-radiusControl+shY)){
                                    lightSources.get(i).setSelected();
                                    repaint();
                                    return;
                                }
                                else
                                    lightSources.get(i).notSelected();
                            }
                }
                if (e.isMetaDown())
                    zvp+=5;
                else
                    zvp-=5;
//                rotation(0, 5, 0, 0);
                repaint();
            }
        });
    }
    
    
    public void toggleWireframe(){
        wireframe = !wireframe;
        repaint();
    }
    public void toggleShowControl(){
        showControl = !showControl;
        repaint();
    }
    public int getWireframeDivision(){
        return (int)Math.ceil(1/this.wireframeCount);
    }
    public void setWireframeDivision(int x){
        if (x>0 && x<100){
            this.wireframeCount = (float)1/x;
            this.evalSurface();
            repaint();
        }  
    }
    public void toogleLight(){
        this.showLight = !this.showLight;
        repaint();
    }
    public void toggleShade(){
        this.Shade = !this.Shade;
        repaint();
    }
    public boolean getShade(){
        /*Status of toggleShade button*/
        return this.Shade;
    }
    public static float[] minmax(float[] a){
        Arrays.sort(a);
        return new float[] {a[0],a[a.length-1]};
    }
    public void setLightColor(Color c){
        for(int i=0; i<lightSources.size();i++){
            Point eachLight = lightSources.get(i);
            if(eachLight.isSelected()){
                eachLight.setColor(c);
                repaint();
                return;
            }
        }
    }
    public void moveLightX(int x){
        for(int i=0; i<lightSources.size();i++){
            Point eachLight = lightSources.get(i);
            if(eachLight.isSelected()){
                float[] plotted = eachLight.getTruePoints();
                eachLight.setPoints(plotted[0]+x, plotted[1], plotted[2]);
                repaint();
                return;
            }
        }
    }
    public void moveLightY(int y){
        for(int i=0; i<lightSources.size();i++){
            Point eachLight = lightSources.get(i);
            if(eachLight.isSelected()){
                float[] plotted = eachLight.getTruePoints();
                eachLight.setPoints(plotted[0], plotted[1]+y, plotted[2]);
                repaint();
                return;
            }
        }
    }
    public void moveLightZ(int z){
        for(int i=0; i<lightSources.size();i++){
            Point eachLight = lightSources.get(i);
            if(eachLight.isSelected()){
                float[] plotted = eachLight.getTruePoints();
                eachLight.setPoints(plotted[0], plotted[1], plotted[2]+z);
                repaint();
                return;
            }
        }
    }
    public void newLight(){
        Point x = new Point(0,0,0);
        x.setColor(Color.red);
        lightSources.add(x);
        repaint();
    }
    public void delLight(){
        for(int i=0; i<lightSources.size();i++){
            Point eachLight = lightSources.get(i);
            if(eachLight.isSelected()){
                lightSources.remove(i);
                repaint();
            }
        }
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        this.evalColor();
//        System.out.println(this.getWireframeDivision());
//        System.out.println(this.wireframeCount);
        shX = this.getWidth()/2;
        shY = this.getHeight()/2;
        setBackground(Color.black);
        
        this.evalPsSurface();
//        for (int i=0;i<points.size();i++)
//            System.out.println(Arrays.toString(points.get(i).getTruePoints()));
        if(this.wireframe){
        g.setColor(Color.green);
            for(int i=0; i<points.size(); i++){
                float[] x = points.get(i).getPlotPoints();
                g.drawLine((int)(x[0]+shX), (int)(x[1]+shY), (int)(x[0]+shX), (int)(x[1]+shY));
            }
        }
        
        if (!this.wireframe){
            if (Shade)
                this.fillShade2(g);
            else
                this.fillFlat(g);
        }
        
        if (showLight){
            g.setColor(Color.DARK_GRAY);
            this.evalPsLight();
            Point[] p = new Point[lightSources.size()];
            lightSources.toArray(p);
            for (Point eachLight : p){
                g.setColor(eachLight.getColor());
                float[] x = eachLight.getPlotPoints();
                if (eachLight.isSelected())
                    g.drawOval((int)(x[0]+shX), (int)(x[1]+shY), 15,15);
                else
                    g.fillOval((int)(x[0]+shX), (int)(x[1]+shY), 15,15);
            }
        }
        
        if (showControl){
            this.evalPsControl();
            g.setColor(Color.red);
            for (Point a[] : controlPoints)
                for (Point b : a){
                    float[] x = b.getPlotPoints();
                    //System.out.println(Arrays.toString(x));
                    g.drawOval((int)(x[0]+shX-radiusControl), (int)(x[1]+shY-radiusControl), radiusControl, radiusControl);
                }
        }        
    }
    private void evalColor(){
        /**
         * Evaluates color at each vertex
         */
        for(int i=0; i<points.size();i++){
            Point vertex = points.get(i);
            Vector v1; // Orientation of point(average of surface normals)
            //Vector normal = new Vector(); // viewer's orientation at that vertex
            v1 = vertex.getNormal();
            float[] xyz = vertex.getTruePoints();
            //normal.setVector(xyz[0]-this.xprp, xyz[1]-this.yprp, xyz[2]-zprp);
            
            float tDist=0;
            for(int l=0; l<this.lightSources.size(); l++){ // this loop, just to calculate the total distances of each light sources form that vertex
                Point light = this.lightSources.get(l);
                tDist+=Math.pow(Point.distance(light, vertex),2);
            }
            Color c;
            float r=0,g=0,b=0;
            for(int l=0; l<this.lightSources.size(); l++){ //this loop to calculate lightSource's contribution to makeup color at vertex
                Point light = this.lightSources.get(l);
                float[] lightCord = light.getTruePoints();
                float dist = (float) Math.pow(Point.distance(vertex, light),2);
                Color lightC = light.getColor();
                float rC = lightC.getRed();
                float gC = lightC.getGreen();
                float bC = lightC.getBlue();
                Vector vL = new Vector();
//                vL.setVector(lightCord[0]-xyz[0],lightCord[1]-xyz[1],lightCord[2]-xyz[2]);
                vL.setVector(xyz[0]-lightCord[0],xyz[1]-lightCord[1],xyz[2]-lightCord[2]);
//                System.out.println(Arrays.toString(vL.getVector()));
//                System.out.println("Angle: " + Vector.dotProduct(vL.unitVector(), v1.unitVector()));
                if (Math.floor(tDist)==Math.floor(dist)){
                    dist=0;
//                    System.out.println(""+tDist + " " + dist);
                }
                if(tDist!=0) {
                    r+=rC*((tDist-dist)/tDist)/2*Math.pow(Vector.dotProduct(vL.unitVector(), v1.unitVector()),3);
                    g+=gC*((tDist-dist)/tDist)/2*Math.pow(Vector.dotProduct(vL.unitVector(), v1.unitVector()),3);
                    b+=bC*((tDist-dist)/tDist)/2*Math.pow(Vector.dotProduct(vL.unitVector(), v1.unitVector()),3);
                }
            }
//            System.out.println("rgb=" + r + "," + g + ","+b);
//            if (r<0) r=1;
//            else r=r%255;
//            if (g<0) g=1;
//            else g=g%255;
//            if (b<0) b=1;
//            else b=b%255;
            vertex.setColor(new Color((int)Math.abs(r)%255, (int)Math.abs(g)%255,(int)Math.abs(b)%255));
//            vertex.setColor(new Color((int)Math.abs(r), (int)Math.abs(g),(int)Math.abs(b)));
            
        }
    }
    private void fillFlat(Graphics g){
        for(int i=0; i<this.triangles.size(); i++){
            Point[] vertex = this.triangles.get(i).getPoint().clone();
            float[] p1 = vertex[0].getPlotPoints();
            float[] p2 = vertex[1].getPlotPoints();
            float[] p3 = vertex[2].getPlotPoints();
            int[] xPoints = new int[] {(int)p1[0]+shX, (int)p2[0]+shX, (int)p3[0]+shX};
            int[] yPoints = new int[] {(int)p1[1]+shY, (int)p2[1]+shY, (int)p3[1]+shY};
            
            Vector normal = new Vector(); // viewer's orientation at that vertex
            Vector v1;
            v1 = this.triangles.get(i).getNormal();
            float[] xyz = vertex[0].getTruePoints();
            normal.setVector(xyz[0]-this.xprp, xyz[1]-this.yprp, xyz[2]-zprp);
//            normal.setVector(this.xprp-xyz[0], this.yprp-xyz[1], zprp-xyz[2]);
            if (Vector.dotProduct(v1.unitVector(), normal.unitVector()) > -0.5){
                g.setColor(vertex[0].getColor());
                g.fillPolygon(xPoints, yPoints, 3);
            }
        }
    }
    private void fillShade(Graphics g){
        /* Fills the surface pieces with calculated color */
        for(int i=0; i<this.triangles.size();i++){
            Point[] vertex = this.triangles.get(i).getPoint().clone();
            //sort the points 
            float[] p1 = vertex[0].getPlotPoints();
            float[] p2 = vertex[1].getPlotPoints();
            float[] p3 = vertex[2].getPlotPoints();
            if (p1[1]<=p2[1] && p1[1]<=p3[1]){
                if (p2[1]>=p3[1]){
                    Point temp = vertex[1];
                    vertex[1] = vertex[2];
                    vertex[2] = temp;
                }
            }
            else if(p2[1]<=p1[1] && p2[1]<=p3[1]){
                if (p1[1]<=p3[1]){
                    Point temp = vertex[0];
                    vertex[0] = vertex[1];
                    vertex[1] = temp;
                }
                else{
                    Point temp = vertex[0];
                    vertex[0] = vertex[1];
                    vertex[1] = vertex[2];
                    vertex[2] = temp;
                }
            }
            else if(p3[1]<=p1[1] && p3[1]<=p2[1]){
                if (p2[1]<=p1[1]){
                    Point temp = vertex[0];
                    vertex[0] = vertex[2];
                    vertex[2] = temp;
                }
                else{
                    Point temp = vertex[0];
                    vertex[0] = vertex[2];
                    vertex[2] = vertex[1];
                    vertex[1] = temp;
                }
            }
            float minX = 0,maxX = 0;
            if (p1[0]<=p2[0] && p1[0]<=p3[0])
                minX = p1[0];
            else if (p2[0]<=p1[0] && p2[0]<=p3[0])
                minX = p2[0];
            else
                minX = p3[0];
            if (p1[0]>=p2[0] && p1[0]>=p3[0])
                maxX = p1[0];
            else if (p2[0]>=p1[0] && p2[0]>=p3[0])
                maxX = p2[0];
            else
                maxX = p3[0];
            /* Sorting Done */
            
            p1 = vertex[0].getPlotPoints();
            p2 = vertex[1].getPlotPoints();
            p3 = vertex[2].getPlotPoints();
            
//            System.out.println("new set");
//            System.out.println(Arrays.toString(p1));
//            System.out.println(Arrays.toString(p2));
//            System.out.println(Arrays.toString(p3));
            
            /* Slope calculations */
            float x1 = p1[0];
            float x2 = p1[0];
            int y = (int) p1[1];
            float m1 = 0, m2 = 0, m3=0;
            if((p2[1]-p1[1]) != 0)
                m1 = (p2[0]-p1[0])/(p2[1]-p1[1]);
            if ((p3[1]-p1[1])!=0)
                m2 = (p3[0]-p1[0])/(p3[1]-p1[1]);
            if ((p3[1]-p2[1])!=0)
                m3 = (p3[0]-p2[0])/(p3[1]-p2[1]);
            boolean switchSlope = false;
            if (Math.round(m1)==0){
                switchSlope = true;
                x1 = p2[0];
                x2 = p1[0];
            }
            
            float c1R = vertex[0].getColor().getRed();
            float c1G = vertex[0].getColor().getGreen();
            float c1B = vertex[0].getColor().getBlue();
            float c2R = vertex[1].getColor().getRed();
            float c2G = vertex[1].getColor().getGreen();
            float c2B = vertex[1].getColor().getBlue();
            float c3R = vertex[2].getColor().getRed();
            float c3G = vertex[2].getColor().getGreen();
            float c3B = vertex[2].getColor().getBlue();
            
            for (float u=p1[1]; u<=p3[1]; u++){
                /* prevention from being out of bound */
                if (!(x1<=maxX && x1>=minX))
                    x1 = minX;
                if (!(x2<=maxX && x2>=minX))
                    x2 = minX;
                /* Should not be out of bound */
                
                /* Intensity calculation */
                float reflectivity=1;
                float clR2 = (p3[1]-p1[1]!=0)?(c3R*(u-p1[1])/(p3[1]-p1[1])+c1R*(p3[1]-u)/(p3[1]-p1[1])):(c3R+c1R)/2;
                float clR1 = 0;
                float clB2 = (p3[1]-p1[1]!=0)?(c3B*(u-p1[1])/(p3[1]-p1[1])+c1B*(p3[1]-u)/(p3[1]-p1[1])):(c3B+c1B)/2;
                float clB1 = 0;
                float clG2 = (p3[1]-p1[1]!=0)?(c3G*(u-p1[1])/(p3[1]-p1[1])+c1G*(p3[1]-u)/(p3[1]-p1[1])):(c3G+c1G)/2;
                float clG1 = 0;
                if(Math.round(p2[1]-p1[1])!=0){
                    if (!switchSlope){
                        clR1 = c2R*(u-p1[1])/(p2[1]-p1[1])+c2R*(p2[1]-u)/(p2[1]-p1[1]);
                        clG1 = c2G*(u-p1[1])/(p2[1]-p1[1])+c2G*(p2[1]-u)/(p2[1]-p1[1]);
                        clB1 = c2B*(u-p1[1])/(p2[1]-p1[1])+c2B*(p2[1]-u)/(p2[1]-p1[1]);
                    }
                    else{
                        clR1 = c3R*(u-p2[1])/(p3[1]-p2[1])+c2R*(p3[1]-u)/(p3[1]-p2[1]);
                        clG1 = c3G*(u-p2[1])/(p3[1]-p2[1])+c2G*(p3[1]-u)/(p3[1]-p2[1]);
                        clB1 = c3B*(u-p2[1])/(p3[1]-p2[1])+c2B*(p3[1]-u)/(p3[1]-p2[1]);
                    }
                }
 
                int a = (int) ((x1<=x2)? x1:x2);
                int b = (int) ((x1<x2)? x2:x1);
                if (Math.floor(u)==Math.floor(p2[1])){
                    switchSlope = true;
                    x1 = p2[0];
                }
                for (float v=a-1; v<=b+1; v++){
                    float cR=0,cG=0,cB=0;
//                    if (b-a!=0){
                        cR = (clR1*(b-v)/(b-a) + clR2*(v-a)/(b-a));
                        cG = (clG1*(b-v)/(b-a) + clG2*(v-a)/(b-a));
                        cB = (clB1*(b-v)/(b-a) + clB2*(v-a)/(b-a));
//                    }
//                    else{
//                        cR=clR1;cG=clG1;cB=clB1;
//                    }
                    if (Float.isNaN(cR))
                        cR = (clR1+clR2)/2;
                    if (Float.isNaN(cG))
                        cG = (clG1+clG2)/2;
                    if (Float.isNaN(cB))
                        cB = (clB1+clB2)/2;
                    
                    Color fd = new Color(Math.abs((int)(cR)%255), Math.abs((int)(cG)%255), Math.abs((int)(cB)%255));
//                    System.out.println(fd.toString());
                    g.setColor(fd);
                    g.drawLine((int)v+shX, (int)u+shY, (int)v+shX, (int)u+shY);
                }
                if (!switchSlope)
                    x1 = x1 + m1;
                else
                    x1 = x1 + m3;
                x2 = x2 + m2;
            }
            
        }
    }
    private void fillShade2(Graphics g){
        /* Fills the surface pieces with calculated color */
        for(int i=0; i<this.triangles.size();i++){
            Point[] vertex = this.triangles.get(i).getPoint().clone();
            float[] p1 = vertex[0].getPlotPoints();
            float[] p2 = vertex[1].getPlotPoints();
            float[] p3 = vertex[2].getPlotPoints();
            if (i%2==0)
                g.setColor(Color.BLUE);
            else{
//                g.setColor(Color.red);
                p1 = vertex[2].getPlotPoints();
                p2 = vertex[1].getPlotPoints();
                p3 = vertex[0].getPlotPoints();
            }
            
            //sort the points            
            
            float[] temp = minmax(new float[]{p1[0],p2[0],p3[0]});
            int minX = (int)temp[0],maxX = (int)temp[1];
            temp = minmax(new float[]{p1[1],p2[1],p3[1]});
            int minY = (int)temp[0],maxY = (int)temp[1];
            /* Sorting Done */
            
            /* Slope calculations */
            float m1 = 0, m2 = 0, m3=0;
            if((p2[1]-p1[1]) != 0)
                m1 = (p2[0]-p1[0])/(p2[1]-p1[1]);
            if ((p3[1]-p1[1])!=0)
                m2 = (p3[0]-p1[0])/(p3[1]-p1[1]);
            if ((p3[1]-p2[1])!=0)
                m3 = (p3[0]-p2[0])/(p3[1]-p2[1]);
            boolean switchSlope = false;
            if (Math.round((p2[0]-p1[0])/(p2[1]-p1[1]))==0){
                switchSlope = true;
//                x1 = p2[0];
//                x2 = p1[0];
            }
            
            float c1R = vertex[0].getColor().getRed();
            float c1G = vertex[0].getColor().getGreen();
            float c1B = vertex[0].getColor().getBlue();
            float c2R = vertex[1].getColor().getRed();
            float c2G = vertex[1].getColor().getGreen();
            float c2B = vertex[1].getColor().getBlue();
            float c3R = vertex[2].getColor().getRed();
            float c3G = vertex[2].getColor().getGreen();
            float c3B = vertex[2].getColor().getBlue();
            
            for (int y=minY; y<=maxY;y++){
                float clR2 = (p3[1]-p1[1]!=0)?(c3R*(y-p1[1])/(p3[1]-p1[1])+c1R*(p3[1]-y)/(p3[1]-p1[1])):(c3R+c1R)/2;
                float clR1 = 0;
                float clB2 = (p3[1]-p1[1]!=0)?(c3B*(y-p1[1])/(p3[1]-p1[1])+c1B*(p3[1]-y)/(p3[1]-p1[1])):(c3B+c1B)/2;
                float clB1 = 0;
                float clG2 = (p3[1]-p1[1]!=0)?(c3G*(y-p1[1])/(p3[1]-p1[1])+c1G*(p3[1]-y)/(p3[1]-p1[1])):(c3G+c1G)/2;
                float clG1 = 0;
                if(Math.round(p2[1]-p1[1])!=0){
                    if (!switchSlope){
                        clR1 = c2R*(y-p1[1])/(p2[1]-p1[1])+c2R*(p2[1]-y)/(p2[1]-p1[1]);
                        clG1 = c2G*(y-p1[1])/(p2[1]-p1[1])+c2G*(p2[1]-y)/(p2[1]-p1[1]);
                        clB1 = c2B*(y-p1[1])/(p2[1]-p1[1])+c2B*(p2[1]-y)/(p2[1]-p1[1]);
                    }
                    else{
                        clR1 = c3R*(y-p2[1])/(p3[1]-p2[1])+c2R*(p3[1]-y)/(p3[1]-p2[1]);
                        clG1 = c3G*(y-p2[1])/(p3[1]-p2[1])+c2G*(p3[1]-y)/(p3[1]-p2[1]);
                        clB1 = c3B*(y-p2[1])/(p3[1]-p2[1])+c2B*(p3[1]-y)/(p3[1]-p2[1]);
                    }
                }
                for (int x=minX; x<=maxX;x++){
                    int b = (int) (y*m2), a=(int) (y*m1);
                    if(switchSlope)
                        a=(int) (y*m3);
                    if (b==0) b=maxX;
                    if (b==0) a=minX;
                    if(((p2[0]-p1[0])*(y-p1[1])-(p2[1]-p1[1])*(x-p1[0]))>0 && ((p3[0]-p2[0])*(y-p2[1])-(p3[1]-p2[1])*(x-p2[0]))>0 && ((p1[0]-p3[0])*(y-p3[1])-(p1[1]-p3[1])*(x-p3[0]))>0){
                      float cR=0,cG=0,cB=0;
                    if (b-a!=0){
                        cR = (clR1*(b-x)/(b-a) + clR2*(x-a)/(b-a));
                        cG = (clG1*(b-x)/(b-a) + clG2*(x-a)/(b-a));
                        cB = (clB1*(b-x)/(b-a) + clB2*(x-a)/(b-a));
                    }
//                    float d1 = (float) Math.sqrt(Math.pow(p1[0]-x,2)+Math.pow(p1[1]-y,2));
//                    float d2 = (float) Math.sqrt(Math.pow(p2[0]-x,2)+Math.pow(p2[1]-y,2));
//                    float d3 = (float) Math.sqrt(Math.pow(p3[0]-x,2)+Math.pow(p3[1]-y,2));
//                    float d = d1+d2+d3;
//                    cR = ((d1)/d)*c1R+((d2)/d)*c2R+((d3)/d)*c3R;
//                    cG = ((d1)/d)*c1G+((d2)/d)*c2G+((d3)/d)*c3G;
//                    cB = ((d1)/d)*c1B+((d2)/d)*c2B+((d3)/d)*c3B;
                    
                    Color fd = new Color(Math.abs((int)(cR)%255), Math.abs((int)(cG)%255), Math.abs((int)(cB)%255));
//                    System.out.println(fd.toString());
                    g.setColor(fd);
                    g.drawLine((int)x+shX, (int)y+shY, (int)x+shX, (int)y+shY);
                    }
//                    System.out.println("fuck");
                }
                if (y==Math.round(p2[1]))
                    switchSlope=true;
            }
        }
    }
    private void evalTriangle(){
        /**
         * This method evaluates the surface coordinates and form an ArrayList of surfaces.. triangles in each surfaces.
         * Also evaluates the normal vector of each surface and assigns them to corresponding vertices.
         */
        this.triangles.clear();
        int n=this.getWireframeDivision();
        if ((n+1)*(n+1)-1 > this.points.size()) // sometimes the points.size() is less than the index that will be requested below... and (n+1)^2-1 is the max index that below requests
            n = n-1;
        for(int v=n;v>0;v--)
            for(int u=0; u<n; u++){
                //System.out.println((v*(n+1)+u) + " " + (v*(n+1)+u+1) + " " + ((v-1)*(n+1)+u));
                //System.out.println(((v-1)*(n+1)+u) + " " + ((v-1)*(n+1)+u+1) + " " + ((v)*(n+1)+u+1));
                
                float[] p1 = points.get((v*(n+1)+u)).getTruePoints();
                float[] p2 = points.get((v*(n+1)+u+1)).getTruePoints();
                float[] p3 = points.get((v-1)*(n+1)+u).getTruePoints();
                
                float a = p1[1]*(p2[2]-p3[2]) + p2[1]*(p3[2]-p1[2]) + p3[1]*(p1[2]-p2[2]);
                float b = p1[2]*(p2[0]-p3[0]) + p2[2]*(p3[0]-p1[0]) + p3[2]*(p1[0]-p2[0]);
                float c = p1[0]*(p2[1]-p3[1]) + p2[0]*(p3[1]-p1[1]) + p3[0]*(p1[1]-p2[1]);

                points.get((v*(n+1)+u)).setNormal(a,b,c); // the setNormal method automatically evaluates average if the vector already exists
                points.get(v*(n+1)+u+1).setNormal(a,b,c);
                points.get((v-1)*(n+1)+u).setNormal(a,b,c);
                
                Surface c1 = new Surface();
                c1.setNormal(a,b,c);
                c1.addPoint(points.get((v*(n+1)+u)));
                c1.addPoint(points.get(v*(n+1)+u+1));
                c1.addPoint(points.get((v-1)*(n+1)+u));

                p1 = points.get((v)*(n+1)+u+1).getTruePoints();
                p2 = points.get(((v-1)*(n+1)+u+1)).getTruePoints();
                p3 = points.get(((v-1)*(n+1)+u)).getTruePoints();
                
                a = p1[1]*(p2[2]-p3[2]) + p2[1]*(p3[2]-p1[2]) + p3[1]*(p1[2]-p2[2]);
                b = p1[2]*(p2[0]-p3[0]) + p2[2]*(p3[0]-p1[0]) + p3[2]*(p1[0]-p2[0]);
                c = p1[0]*(p2[1]-p3[1]) + p2[0]*(p3[1]-p1[1]) + p3[0]*(p1[1]-p2[1]);
                
                points.get(((v-1)*(n+1)+u)).setNormal(a,b,c);
                points.get(((v-1)*(n+1)+u+1)).setNormal(a,b,c);
                points.get((v)*(n+1)+u+1).setNormal(a,b,c);
                
                Surface c2 = new Surface();
                c2.setNormal(a,b,c);
                c2.addPoint(points.get((v)*(n+1)+u+1));
                c2.addPoint(points.get(((v-1)*(n+1)+u)));
                c2.addPoint(points.get(((v-1)*(n+1)+u+1)));

                this.triangles.add(c1);
                this.triangles.add(c2);
            }
    }
    
    private void evalPsControl(){
     /**
     * Evaluate perspective coords for control points and make each `Point object` ready
     */
        float[][] ctrl = new float[16][3];
        int x=0;
        /* Load the array for perpectization */
        for (int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                ctrl[x] = controlPoints[row][col].getTruePoints();
                x++;
            }
        ctrl = this.perspective(ctrl); /* Perspectization */
        
        /* Issue the original points with perspective equiv */
        x=0;
        for (int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                controlPoints[row][col].setTransform(ctrl[x][0],ctrl[x][1],ctrl[x][2]);
                x++;
            }
//        for (float xa[]: ctrl)
//            System.out.println(Arrays.toString(xa));
    }
    
    private void evalPsLight(){
        /**
         * Evaluate perspective coordinates of lightSources
         */
        float[][] lights = new float[lightSources.size()][3];
        for (int i=0; i<lightSources.size(); i++)
            lights[i] = lightSources.get(i).getTruePoints();
        lights = this.perspective(lights);
        for (int i=0; i<lightSources.size(); i++)
            lightSources.get(i).setTransform(lights[i][0],lights[i][1],lights[i][2]);
    }
    
    private float[][] perspective(float a[][]){
        /* Accepts an array of coordinates not in homogeneous form */
        /* Each row contains three columns whose 0=x,1=y and 2=z coordinates */
        /* Total number of rows gives total points that need to be perspectized */
        /* Returns normalized in same format */
        float dp = zprp - zvp;
        float[][] mat = {{1,0,-xprp/dp,xprp*zvp/dp},
                         {0,1,yprp/dp,-yprp*zvp/(dp*2)},
                         {0,0,-1*(zvp/dp),zvp*(zprp/dp)},
                         {0,0,-1/dp,zprp/dp}};
        int total = a.length;
        float[][] out = new float[total][4];
        for(int i=0;i<total;i++){
            out[i][0] = a[i][0];
            out[i][1] = a[i][1];
            out[i][2] = a[i][2];
            out[i][3] = 1;         
        }
        out = Matrix.transpose(out);
        out = Matrix.multiply(mat, out);
        out = Matrix.normalize(out);
        return Matrix.transpose(out);
    }
    
    private void evalSurface(){
        points.clear(); // required to clear all the points at first... because below procedure dynamically adds new coords into it
        /* Loads the ArrayList points with the surface coordinates */
        float[][] mB = {{-1,3,-3,1},
                    {3,-6,3,0},
                    {-3,3,0,0},
                    {1,0,0,0}};
        float[][] gX = new float[4][4];
        float[][] gY = new float[4][4];        
        float[][] gZ = new float[4][4];       
        for (int i=0; i<4; i++)
            for (int j=0;j<4; j++){
                float[] gangnam = controlPoints[i][j].getTruePoints();
                gX[i][j] = gangnam[0];
                gY[i][j] = gangnam[1];
                gZ[i][j] = gangnam[2];
            }
        
//        for (float u=0; u<=1; u=(float) (u+(1/this.wireframeCount))){
//            for (float v=0; v<=1; v=(float)(v+(1/this.wireframeCount))){
        for (float u=0; u<=1; u=(float) (u+this.wireframeCount)){
            for (float v=0; v<=1; v=(float)(v+this.wireframeCount)){        
                float[][] uM = {{(float)Math.pow(u,3),(float)Math.pow(u,2),u, 1}};
                float[][] vM = {{(float)Math.pow(v,3),(float)Math.pow(v,2),v, 1}};
                
                float[][] nX = Matrix.multiply(uM, mB);
                nX = Matrix.multiply(nX,gX);
                nX = Matrix.multiply(nX,Matrix.transpose(mB));
                nX = Matrix.multiply(nX, Matrix.transpose(vM));
                
                float[][] nY = Matrix.multiply(uM, mB);
                nY = Matrix.multiply(nY,gY);
                nY = Matrix.multiply(nY,Matrix.transpose(mB));
                nY = Matrix.multiply(nY, Matrix.transpose(vM));
                
                float[][] nZ = Matrix.multiply(uM, mB);
                nZ = Matrix.multiply(nZ,gZ);
                nZ = Matrix.multiply(nZ,Matrix.transpose(mB));
                nZ = Matrix.multiply(nZ, Matrix.transpose(vM));
                
                Point wxy = new Point(nX[0][0],nY[0][0],nZ[0][0]);
//                System.out.println(nX[0][0] + " " + nY[0][0] + " " + nZ[0][0]);
//                int i=98;
//                for (float F[] : nZ)
//                    System.out.println("nX:" + (i++) + Arrays.toString(F));
//                System.out.println("nY:" + Arrays.toString(nY[0]));
//                System.out.println("nZ:" + Arrays.toString(nZ[0]));
                points.add(wxy);
            }
        }
        this.evalTriangle();
    }
    
    private void evalPsSurface(){
        /* Evaluate perspective coords for surface points and make each `Point object` ready */
        float[][] srfc = new float[points.size()][3];
        
        /* Load the array for perpectization */
        for (int i=0;i<points.size();i++){
            srfc[i] = points.get(i).getTruePoints();
        }
        srfc = this.perspective(srfc); /* Perspectization */
        
        /* Issue the original points with perspective equiv */
        for (int i=0;i<points.size();i++){
            points.get(i).setTransform(srfc[i][0],srfc[i][1],srfc[i][2]);
//            System.out.println(Arrays.toString(srfc[i]));
        }        
    }
    
    public void rotateX(int angle){
        float[][] ctrl = new float[16][4];
        int x=0;
        for (int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                float[] temp = controlPoints[row][col].getTruePoints();
                ctrl[x] = new float[] {temp[0],temp[1],temp[2],1};
                x++;
            }
        ctrl = Matrix.transpose(ctrl);
        
        float minZ=0,maxZ=0;
//        float minX=0,maxX=0;
//        float minY=0,maxY=0;
        for(int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                float[] pts = this.controlPoints[row][col].getTruePoints();
                if (pts[2]<minZ)
                    minZ = pts[2];
                if (pts[2]>maxZ)
                    maxZ = pts[2];
//                if (pts[0]<minZ)
//                    minX = pts[0];
//                if (pts[0]>maxZ)
//                    maxX = pts[0];
//                if (pts[1]<minY)
//                    minY = pts[1];
//                if (pts[1]>maxY)
//                    maxY = pts[1];
            }
        float zconst = (float) (minZ+maxZ)/2;
        zconst =100;
//        float xconst = (float) (minX+maxX)/2;
//        float yconst = (float) (minY+maxY)/2;

        float[][] first = {{1,0,0,0},
                            {0,1,0,0},
                            {0,0,1,zconst},
                            {0,0,0,1}};
        float[][] second = {{1,0,0,0},
                            {0,(float)Math.cos(Math.toRadians(angle)),-1*(float)Math.sin(Math.toRadians(angle)),0},
                            {0,(float)Math.sin(Math.toRadians(angle)),(float)Math.cos(Math.toRadians(angle)),0},
                            {0,0,0,1}};
        float[][] third = {{1,0,0,0},
                            {0,1,0,0},
                            {0,0,1,-1*zconst},
                            {0,0,0,1}};
        ctrl = Matrix.multiply(third, ctrl);
        ctrl = Matrix.multiply(second, ctrl);
        ctrl = Matrix.multiply(first, ctrl);
        ctrl = Matrix.transpose(ctrl);
        x=0;
        for (int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                controlPoints[row][col].setPoints((int)ctrl[x][0],(int)ctrl[x][1],(int)ctrl[x][2]);
                x++;
            }
        this.evalSurface();
        repaint();
    }
    
    public void rotateY(int angle){
        float[][] ctrl = new float[16][4];
        int x=0;
        for (int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                float[] temp = controlPoints[row][col].getTruePoints();
                ctrl[x] = new float[] {temp[0],temp[1],temp[2],1};
                x++;
            }
        ctrl = Matrix.transpose(ctrl);
        
        float minZ=0,maxZ=0;
        for(int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                float[] pts = this.controlPoints[row][col].getTruePoints();
                if (pts[2]<minZ)
                    minZ = pts[2];
                if (pts[2]>maxZ)
                    maxZ = pts[2];
            }
        float zconst = (float) (minZ+maxZ)/2;
        zconst = (float) 100;
        float[][] first = {{1,0,0,0},
                            {0,1,0,0},
                            {0,0,1,zconst},
                            {0,0,0,1}};
        float[][] second = {{(float)Math.cos(Math.toRadians(angle)),0,(float)Math.sin(Math.toRadians(angle)),0},
                            {0,1,0,0},
                            {-1*(float)Math.sin(Math.toRadians(angle)),0,(float)Math.cos(Math.toRadians(angle)),0},
                            {0,0,0,1}};
        float[][] third = {{1,0,0,0},
                            {0,1,0,0},
                            {0,0,1,-1*zconst},
                            {0,0,0,1}};
        ctrl = Matrix.multiply(third, ctrl);
        ctrl = Matrix.multiply(second, ctrl);
        ctrl = Matrix.multiply(first, ctrl);
        ctrl = Matrix.transpose(ctrl);
        x=0;
        for (int row=0;row<4;row++)
            for (int col=0;col<4;col++){
                controlPoints[row][col].setPoints((int)ctrl[x][0],(int)ctrl[x][1],(int)ctrl[x][2]);
                x++;
            }
        this.evalSurface();
        repaint();
    }
}

class Point2D{
    int x;
    int y;
}
