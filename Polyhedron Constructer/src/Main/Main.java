package Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Main {
	
	public static void main(String[] args) {
		
		final int[] x = {0};
		final int[] y = {0};
		
		JFrame frame = new JFrame();
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());
		
		JPanel renderPanel = new JPanel() {
		
			public void paintComponent(Graphics g) {
								
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, getWidth(), getHeight());
				
				List<Polygon> polygons = new ArrayList<>();
				polygons.add(new Polygon(new ArrayList<>(), Color.WHITE));
				polygons.get(0).vertices.add(new Point(100, 100, 100));
				polygons.get(0).vertices.add(new Point(-100, 100, 100));
				polygons.get(0).vertices.add(new Point(-100, -100, 100));
				polygons.get(0).vertices.add(new Point(100, -100, 100));
				
				polygons.add(new Polygon(new ArrayList<>(), Color.RED));
				polygons.get(1).vertices.add(new Point(100, 100, -100));
				polygons.get(1).vertices.add(new Point(-100, 100, -100));
				polygons.get(1).vertices.add(new Point(-100, -100, -100));
				polygons.get(1).vertices.add(new Point(100, -100, -100));

				polygons.add(new Polygon(new ArrayList<>(), Color.BLUE));
				polygons.get(2).vertices.add(new Point(100, 100, 100));
				polygons.get(2).vertices.add(new Point(-100, 100, 100));
				polygons.get(2).vertices.add(new Point(-100, 100, -100));
				polygons.get(2).vertices.add(new Point(100, 100, -100));
			
				polygons.add(new Polygon(new ArrayList<>(), Color.GREEN));
				polygons.get(3).vertices.add(new Point(-100, -100, 100));
				polygons.get(3).vertices.add(new Point(-100, -100, -100));
				polygons.get(3).vertices.add(new Point(100, -100, -100));
				polygons.get(3).vertices.add(new Point(100, -100, 100));
				
				polygons.add(new Polygon(new ArrayList<>(), Color.YELLOW));
				polygons.get(4).vertices.add(new Point(100, 100, 100));
				polygons.get(4).vertices.add(new Point(100, -100, 100));
				polygons.get(4).vertices.add(new Point(100, -100, -100));
				polygons.get(4).vertices.add(new Point(100, 100, -100));

				polygons.add(new Polygon(new ArrayList<>(), Color.ORANGE));
				polygons.get(5).vertices.add(new Point(-100, 100, 100));
				polygons.get(5).vertices.add(new Point(-100, -100, 100));
				polygons.get(5).vertices.add(new Point(-100, -100, -100));
				polygons.get(5).vertices.add(new Point(-100, 100, -100));
				
				double heading = Math.toRadians(x[0]);
				Matrix3 headingTransform = new Matrix3(new double[] {
						Math.cos(heading), 0, -Math.sin(heading),
						0,1,0,
						Math.sin(heading), 0, Math.cos(heading)
				});
				
				double pitch = Math.toRadians(y[0]);
				Matrix3 pitchTransform = new Matrix3(new double[] {
						1, 0, 0,
						0, Math.cos(pitch), Math.sin(pitch),
						0, -Math.sin(pitch), Math.cos(pitch)
				});
				
				Matrix3 transform = headingTransform.multiply(pitchTransform);
				
				g2.translate(getWidth() / 2, getHeight() / 2);
				
				BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);

				double[] zBuffer = new double[img.getWidth() * img.getHeight()];

				for (int q = 0; q < zBuffer.length; q++) {
				    zBuffer[q] = Double.NEGATIVE_INFINITY;
				}
								
				for (Polygon polygon : polygons) {
					Polygon transformedPolygon = new Polygon(new ArrayList<>(), polygon.color);
					for(Point point : polygon.vertices) {
						transformedPolygon.vertices.add(transform.transform(point));
					}
					
					double minX = Double.POSITIVE_INFINITY;
					double maxX = Double.NEGATIVE_INFINITY;
					double minY = Double.POSITIVE_INFINITY;
					double maxY = Double.NEGATIVE_INFINITY;
					
					for (Point point : transformedPolygon.vertices) {
					    minX = Math.min(minX, point.x);
					    maxX = Math.max(maxX, point.x);
					    minY = Math.min(minY, point.y);
					    maxY = Math.max(maxY, point.y);
					}
					
					minX = Math.max(-frame.getWidth()/2, minX);
					maxX = Math.min(frame.getWidth(), maxX);
					minY = Math.max(-frame.getHeight()/2, minY);
					maxY = Math.min(frame.getHeight(), maxY);
										
						for (int y = (int) minY; y <= maxY; y++) {
							for (int x = (int) minX; x <= maxX; x++) {
								Point pixel = new Point(x, y, 0);
								if (transformedPolygon.isPointInsidePolygon(pixel)) {
									double depth = 0;
									for(Point point: transformedPolygon.vertices) {
										depth += point.z;
									}
									int zIndex = ((y + frame.getHeight()/2) * img.getWidth()) + x + frame.getWidth()/2;
									if (zBuffer[zIndex] < depth) {
										img.setRGB(x + frame.getWidth()/2, y + frame.getHeight()/2, transformedPolygon.color.getRGB());
										zBuffer[zIndex] = depth;
									}
								}
							}
						} 
					g2.drawImage(img, -frame.getWidth()/2, -frame.getHeight()/2, null);	
				}
			}
		};
		
		renderPanel.addMouseMotionListener(new MouseMotionListener() {
			Point mousePos = new Point(MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y, 0);
			Point lastMousePosition = new Point(0, 0, 0);
			@Override
			public void mouseDragged(MouseEvent e) {
				mousePos = new Point(MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y, 0);
				x[0] += (mousePos.x - lastMousePosition.x);
				y[0] -= (mousePos.y - lastMousePosition.y);
				lastMousePosition.x = mousePos.x;
				lastMousePosition.y = mousePos.y;
				renderPanel.repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					mousePos = new Point(MouseInfo.getPointerInfo().getLocation().x,MouseInfo.getPointerInfo().getLocation().y, 0);
					lastMousePosition.x = mousePos.x;
					lastMousePosition.y = mousePos.y;
				}
			}
		});
		
		pane.add(renderPanel, BorderLayout.CENTER);
		
		frame.setSize(600, 600);
		frame.setVisible(true);
	}
}
class Point {
	double x;
	double y;
	double z;
	Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}

class Polygon {
	ArrayList<Point> vertices;
	Color color;
	Polygon(ArrayList<Point> verticies, Color color) {
		this.vertices = verticies;
		this.color = color;
	}
	
	boolean isPointOnLine(Point p, Point p1, Point p2) {
	    if (p.x <= Math.max(p1.x, p2.x)
	            && p.x >= Math.min(p1.x, p2.x)
	            && (p.y <= Math.max(p1.y, p2.y)
	                && p.y >= Math.min(p1.y, p2.y))) {
	    	return true;
	    }else {
	        return false;
	    }	     
	}
	
	int direction(Point p, Point q, Point r) {
	    return (int) ((q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y));
	}
	
    public boolean isPointInsidePolygon(Point point) {
    	Point outsidePoint = new Point(99999, point.y,0);
    	int intersections = 0;
    	int i;
    	for(i = 0; i < this.vertices.size(); i++) {
    		Point sidePoint1 = this.vertices.get(i);
    		Point sidePoint2 = this.vertices.get((i+1) % this.vertices.size());
    		
    	    int d1 = direction(point, outsidePoint, sidePoint1);
    	    int d2 = direction(point, outsidePoint, sidePoint2);
    	    int d3 = direction(sidePoint1, sidePoint2, point);
    	    int d4 = direction(sidePoint1, sidePoint2, outsidePoint);
    	    
    	    if ((((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0)))) {
    	    	intersections++;
    	    }   
    	}
    	
    	if(intersections % 2 == 1) {
    		if(intersections != 1 && intersections != 0) {
        		System.out.print(intersections);
    		}
    		return true;
    	}else {
    		return false;
    	}
    }
}

class Matrix3 {
	double[] values;
	Matrix3(double[] values) {
		this.values = values;
	}
	Matrix3 multiply(Matrix3 other) {
		double[] result = new double[9];
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				for (int i = 0; i < 3; i++) {
					result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
				}
			}
		}
		return new Matrix3(result);
	}
	Point transform(Point v) {
		
		return new Point(
			v.x * values[0] + v.y * values[3] + v.z * values[6],
			v.x * values[1] + v.y * values[4] + v.z * values[7],
			v.x * values[2] + v.y * values[5] + v.z * values[8]
			
		);
	}
}