package bsu.rct.group8.chernyak.lab5;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class GraphicsDisplay extends JPanel {
	
	// Список координат точек для построения графика
	private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
	
	// Флаговые переменные, задающие правила отображения графика
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean showGraphic = true;
	// Границы диапазона пространства, подлежащего отображению
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	// Используемый масштаб отображения
	
	private double scaleX;
	private double scaleY;
	// Различные стили черчения линий
	private BasicStroke graphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
	private BasicStroke graphicsStroke1;
	 private BasicStroke selectionStroke;
	// Различные шрифты отображения надписей
	
	private double[][] viewport = new double[2][2];
	private double[] originalPoint = new double[2];
	
	private Rectangle2D.Double selectionRect = new Rectangle2D.Double();
	
	boolean scaleMode = false;
    boolean changeMode = false;
    private int selectedMarker = -1;
    
    private static DecimalFormat formatter=(DecimalFormat) NumberFormat.getInstance();

    private Font axisFont;
    private Font labelsFont;
	
	
	
	public GraphicsDisplay() {
		// Цвет заднего фона области отображения - белый
		setBackground(Color.WHITE);
		// Сконструировать необходимые объекты, используемые в рисовании
		// Перо для рисования графика
		selectionStroke =new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		graphicsStroke = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,  new float[] {20,10,10,10,10,10,10,10,20,10,10,10,20}, 0.0f);
		graphicsStroke1 = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,  null, 0.0f);
		// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, null, 0.0f);
		// Шрифт для подписей осей координат
		labelsFont = new Font("Serif",0,18);
		axisFont = new Font("Serif", Font.BOLD, 36);
		 addMouseMotionListener(new MouseMotionHandler());
	     addMouseListener(new MouseHandler());
		}
	
	// Метод вызывается из обработчика элемента меню "Открыть файл с графиком"
	// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(ArrayList<Double[]> graphicsData) {
		 this.graphicsData = graphicsData;


	        this.originalData = new ArrayList(graphicsData.size());
	        for (Double[] point : graphicsData) {
	            Double[] newPoint = new Double[2];
	            newPoint[0] = new Double(point[0].doubleValue());
	            newPoint[1] = new Double(point[1].doubleValue());
	            this.originalData.add(newPoint);
	        }
	        this.minX = ((Double[])graphicsData.get(0))[0].doubleValue();
	        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0].doubleValue();
	        this.minY = ((Double[])graphicsData.get(0))[1].doubleValue();
	        this.maxY = this.minY;

	        for (int i = 1; i < graphicsData.size(); i++) {
	            if (((Double[])graphicsData.get(i))[1].doubleValue() < this.minY) {
	                this.minY = ((Double[])graphicsData.get(i))[1].doubleValue();
	            }
	            if (((Double[])graphicsData.get(i))[1].doubleValue() > this.maxY) {
	                this.maxY = ((Double[])graphicsData.get(i))[1].doubleValue();
	            }
	        }

	        zoomToRegion(minX, maxY, maxX, minY);
	        
	}
	
	// Методы-модификаторы для изменения параметров отображения графика
	// Изменение любого параметра приводит к перерисовке области
	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}
	public void setShowMarkers(boolean showMarkers) {
			this.showMarkers = showMarkers;
			repaint();
		}
	public void  setShowGraphic(boolean showGraphic) {
		this.showGraphic =  showGraphic;
		repaint();
	}
	 public void zoomToRegion(double x1,double y1,double x2,double y2)	{
	        this.viewport[0][0]=x1;
	        this.viewport[0][1]=y1;
	        this.viewport[1][0]=x2;
	        this.viewport[1][1]=y2;

	        this.repaint();
	    }
	
	 protected Point2D.Double xyToPoint(double x, double y) {
			double deltaX = x - viewport[0][0];
			double deltaY = viewport[0][1] - y;
			return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
		}
	
	protected double[] translatePointToXY(int x, int y)
    {
        return new double[] { this.viewport[0][0] + x / this.scaleX, this.viewport[0][1] - y / this.scaleY };
    }

	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
		Point2D.Double dest = new Point2D.Double();
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}
			

	
	protected void paintGraphics(Graphics2D canvas) {
		canvas.setStroke(this.graphicsStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();

        Double currentX = null;
        Double currentY = null;
        for(Double[] point : this.graphicsData)
        {
            if ((point[0].doubleValue() >= this.viewport[0][0]) && (point[1].doubleValue() <= this.viewport[0][1]) &&
                    (point[0].doubleValue() <= this.viewport[1][0]) && (point[1].doubleValue() >= this.viewport[1][1]))
            {
                if ((currentX != null) && (currentY != null)) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                            xyToPoint(point[0].doubleValue(), point[1].doubleValue())));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
        canvas.draw(graphics);
	}
	
	
	protected void paintAxis(Graphics2D canvas) {
		 canvas.setStroke(this.axisStroke);
	        canvas.setColor(java.awt.Color.BLACK);
	        canvas.setFont(this.axisFont);
	        FontRenderContext context=canvas.getFontRenderContext();
	        if (!(viewport[0][0] > 0|| viewport[1][0] < 0)){
	            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]),
	                    xyToPoint(0, viewport[1][1])));
	            canvas.draw(new Line2D.Double(xyToPoint(-(viewport[1][0] - viewport[0][0]) * 0.0025,
	                    viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),xyToPoint(0,viewport[0][1])));
	            canvas.draw(new Line2D.Double(xyToPoint((viewport[1][0] - viewport[0][0]) * 0.0025,
	                    viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),
	                    xyToPoint(0, viewport[0][1])));
	            Rectangle2D bounds = axisFont.getStringBounds("y",context);
	            Point2D.Double labelPos = xyToPoint(0.0, viewport[0][1]);
	            canvas.drawString("y",(float)labelPos.x + 10,(float)(labelPos.y + bounds.getHeight() / 2));
	        }
	        if (!(viewport[1][1] > 0.0D || viewport[0][1] < 0.0D)){
	            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0],0),
	                    xyToPoint(viewport[1][0],0)));
	            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0,
	                    (viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
	            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01,
	                    -(viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
	            Rectangle2D bounds = axisFont.getStringBounds("x",context);
	            Point2D.Double labelPos = xyToPoint(this.viewport[1][0],0.0D);
	            canvas.drawString("x",(float)(labelPos.x - bounds.getWidth() - 10),(float)(labelPos.y - bounds.getHeight() / 2));
	        }
	        if (selectedMarker >= 0)
	        {
	            canvas.setFont(labelsFont);
	            Point2D.Double point = xyToPoint(((Double[])graphicsData.get(selectedMarker))[0].doubleValue(),
	                    ((Double[])graphicsData.get(selectedMarker))[1].doubleValue());
	            String label = "X=" + formatter.format(((Double[])graphicsData.get(selectedMarker))[0]) +
	                    ", Y=" + formatter.format(((Double[])graphicsData.get(selectedMarker))[1]);
	            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
	            canvas.setColor(Color.BLACK);
	            canvas.drawString(label, (float)(point.getX()), (float)(point.getY() - bounds.getHeight()));
	        }
		}
	
	protected void paintMarkers(Graphics2D canvas) {
		
				canvas.setStroke(markerStroke);
				
				
				for (Double[] point: graphicsData) {
				    GeneralPath marker = new GeneralPath(); 
				    
				    boolean flag = false;
				   
				    Point2D.Double point1 = xyToPoint(point[0], point[1]);
				    int a=point[0].intValue();
				    int b=a;
				    int sum=0;
				    
				    while(a != 0){
				        
				         sum += (a % 10);
				         a/=10;
				 }
				    System.out.println(sum+" "+b+" "+point[0]+" "+point[1]);
				    if(sum<10) {
				    	flag = true;
				    }
				    else {
				    	flag = false;
				    }
				    
				    
				    marker.moveTo(point1.getX()-10.0, point1.getY());
			     	marker.lineTo(point1.getX()+10.0, point1.getY());
			     	
			     	marker.moveTo(point1.getX(), point1.getY()-10.0);
			     	marker.lineTo(point1.getX(), point1.getY()+10.0);
			     	
			     	marker.moveTo(point1.getX()-3.0, point1.getY()+3.0);
			     	marker.lineTo(point1.getX()+3.0, point1.getY()+3.0);			     	 
				    marker.lineTo(point1.getX()+3.0, point1.getY()-3.0);
				    marker.lineTo(point1.getX()-3.0, point1.getY()-3.0);
				    marker.lineTo(point1.getX()-3.0, point1.getY()+3.0);
				     
				   
				    
			     	
			     	if(flag==true) {
			     		canvas.setColor(Color.BLUE);
			    		canvas.setPaint(Color.BLUE);
			    		canvas.draw(marker);
			 		    canvas.fill(marker); 
			     	}
			     	else {
			     		canvas.setColor(Color.BLACK);
			    		canvas.setPaint(Color.BLACK);
			     		canvas.draw(marker);
			 		    canvas.fill(marker); 
			     	}
				}
			}
	
	
	protected void paintGraphic1(Graphics2D canvas) {
		canvas.setStroke(this.graphicsStroke1);
        canvas.setColor(Color.PINK);

        GeneralPath graphics = new GeneralPath();

        Double currentX = null;
        Double currentY = null;
        for(Double[] point : this.graphicsData)
        {
            if ((point[0].doubleValue() >= this.viewport[0][0]) && (point[1].doubleValue() <= this.viewport[0][1]) &&
                    (point[0].doubleValue() <= this.viewport[1][0]) && (point[1].doubleValue() >= this.viewport[1][1]))
            {
                if ((currentX != null) && (currentY != null)) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX.intValue(), currentY.intValue()),
                            xyToPoint(point[0].intValue(), point[1].intValue())));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
        canvas.draw(graphics);
	}
		
	
		
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

        scaleX=this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        scaleY=this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);

        if(graphicsData == null || graphicsData.size() == 0) return;

        Graphics2D canvas = (Graphics2D)g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if(showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if(showMarkers) paintMarkers(canvas);
        
        if (showGraphic) paintGraphic1(canvas);
        
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
        paintSelection(canvas);
		
	}
	private void paintSelection(Graphics2D canvas) {
	    //if (!scaleMode) return;
	    canvas.setStroke(selectionStroke);
	    canvas.setColor(Color.BLACK);
	    canvas.draw(selectionRect);
	  }
	
	
	
	protected int findSelectedPoint(int x, int y)
    {
        if (graphicsData == null) return -1;
        int pos = 0;
        for(Double[] point : graphicsData)
        {
            Point2D.Double screenPoint = xyToPoint(point[0].doubleValue(), point[1].doubleValue());
            double distance = (screenPoint.getX()-x)*(screenPoint.getX()-x)+(screenPoint.getY()-y)*(screenPoint.getY()-y);
            if(distance < 100) return pos;
            pos++;
        }
        return -1;
    }

	

	    public class MouseHandler extends MouseAdapter {

	        public void mouseClicked(MouseEvent ev)
	        {
	            if(ev.getButton() == 3)
	            {
	                zoomToRegion(minX, maxY, maxX, minY);
	                repaint();
	            }
	            
	           
	        }

	        public void mousePressed(MouseEvent ev)
	        {
	            if (ev.getButton() != 1) return;
	            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
	            originalPoint = translatePointToXY(ev.getX(), ev.getY());
	            if (selectedMarker >= 0) {
	                changeMode = true;
	                setCursor(Cursor.getPredefinedCursor(8));
	            } else {
	                scaleMode = true;
	                setCursor(Cursor.getPredefinedCursor(5));
	                selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
	            }
	        }

	        public void mouseReleased(MouseEvent ev)
	        {
	            if (ev.getButton() != 1) return;

	            setCursor(Cursor.getPredefinedCursor(0));
	            if (changeMode) {
	                changeMode = false;
	            } else {
	                scaleMode = false;
	                double[] finalPoint = translatePointToXY(ev.getX(), ev.getY());
	                viewport = new double[2][2];
	                zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
	                setCursor(Cursor.getPredefinedCursor(0));
	                selectionRect.setFrame(ev.getX(), ev.getY(), 0, 0);
	                repaint();
	            }
	        }

	    }

	    public class MouseMotionHandler implements MouseMotionListener
	    {

	        @Override
	        public void mouseDragged(MouseEvent e) {
	            setCursor(Cursor.getPredefinedCursor(5));
	            double width = e.getX() - selectionRect.getX();
	            double height = e.getY() - selectionRect.getY();
	            selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
	           	           
	            repaint();
	            
	        }

	        @Override
	        public void mouseMoved(MouseEvent e) {
	            selectedMarker = findSelectedPoint(e.getX(), e.getY());
	            if(selectedMarker >= 0)
	            {
	                setCursor(Cursor.getPredefinedCursor(8));
	            }
	            else
	            {
	                setCursor(Cursor.getPredefinedCursor(0));
	            }
	            repaint();
	        }
	    }
	



	
	


}