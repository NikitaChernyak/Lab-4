package bsu.rct.group8.chernyak.lab4b;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
	
	// Список координат точек для построения графика
	private Double[][] graphicsData;
	// Флаговые переменные, задающие правила отображения графика
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean showAdditionalGraphic = true;
	// Границы диапазона пространства, подлежащего отображению
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	// Используемый масштаб отображения
	private double scale;
	// Различные стили черчения линий
	private BasicStroke graphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke additionalGraphicsStroke;
	private BasicStroke markerStroke;
	// Различные шрифты отображения надписей
	private Font axisFont;
	private Font _01Font;
	
	public GraphicsDisplay() {
		
		setBackground(Color.WHITE);
		// Сконструировать необходимые объекты, используемые в рисовании
		// Перо для рисования графика
		graphicsStroke = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {5, 5, 10, 5, 5, 5, 20, 5, 10, 5, 5, 5}, 0.0f);
		additionalGraphicsStroke = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {20, 5, 20, 5, 20, 5, 5, 5, 5, 5, 5, 5}, 0.0f);
		// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Шрифт для подписей осей координат
		axisFont = new Font("Serif", Font.BOLD, 30);
		_01Font = new Font("Serif", Font.BOLD, 20);
	}
	
	// Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
	// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(Double[][] graphicsData) {
		// Сохранить массив точек во внутреннем поле класса
		this.graphicsData = graphicsData;
		// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
		repaint();
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
	
	public void setShowAdditionalGraphic(boolean showAddGraph) {
		this.showAdditionalGraphic = showAddGraph;
		repaint();
	}
	
	// Метод отображения всего компонента, содержащего график
	public void paintComponent(Graphics g) {
		/* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
		 * Эта функциональность - единственное, что осталось в наследство от paintComponent класса JPanel */
		super.paintComponent(g);
		// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
		if (graphicsData == null || graphicsData.length == 0) return;
		// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
		// Это необходимо для определения области пространства, подлежащей отображению
		// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
		minX = graphicsData[0][0];
		maxX = graphicsData[graphicsData.length-1][0];
		minY = graphicsData[0][1];
		maxY = minY;
		// Найти минимальное и максимальное значение функции
		for (int i = 1; i < graphicsData.length; i++) {
			if (graphicsData[i][1] < minY) 
				minY = graphicsData[i][1];
			if (graphicsData[i][1] > maxY) 
				maxY = graphicsData[i][1];	
		}
		/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y - сколько пикселов
		 * приходится на единицу длины по X и по Y */
		double scaleX = getSize().getWidth() / (maxX - minX);
		double scaleY = getSize().getHeight() / (maxY - minY);
		// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
		// Выбираем за основу минимальный 
		scale = Math.min(scaleX, scaleY);
		// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
		if (scale == scaleX) {
			/* Если за основу был взят масштаб по оси X, значит по оси Y делений меньше,
			 * т.е. подлежащий визуализации диапазон по Y будет меньше высоты окна.
			 * Значит необходимо добавить делений, сделаем это так:
			 * 1) Вычислим, сколько делений влезет по Y при выбранном масштабе - getSize().getHeight()/scale
			 * 2) Вычтем из этого сколько делений требовалось изначально
			 * 3) Набросим по половине недостающего расстояния на maxY и minY */
			double yIncrement = (getSize().getHeight()/scale - (maxY - minY))/2;
			maxY += yIncrement;
			minY -= yIncrement;
		}
		if (scale == scaleY) {
			// Если за основу был взят масштаб по оси Y, действовать по аналогии
			double xIncrement = (getSize().getWidth()/scale - (maxX - minX))/2;
			maxX += xIncrement;
			minX -= xIncrement;
		}
		// Шаг 7 - Сохранить текущие настройки холста
		Graphics2D canvas = (Graphics2D) g;
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		Font oldFont = canvas.getFont();
		// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
		// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
		// Первыми (если нужно) отрисовываются оси координат.
		if (showAxis) paintAxis(canvas);
		// Затем отображается сам график
		paintGraphics(canvas);
		// Затем (если нужно) отображаются маркеры точек, по которым строился график.
		if (showAdditionalGraphic) paintAdditionalGraphic(canvas);
		if (showMarkers) paintMarkers(canvas);
		// Шаг 9 - Восстановить старые настройки холста
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);
	}
	
	// Отрисовка графика по прочитанным координатам
	protected void paintGraphics(Graphics2D canvas) {
		// Выбрать линию для рисования графика
		canvas.setStroke(graphicsStroke);
		// Выбрать цвет линии
		canvas.setColor(Color.BLUE);
		/* Будем рисовать линию графика как путь, состоящий из множества сегментов (GeneralPath)
		 * Начало пути устанавливается в первую точку графика, после чего
		прямой соединяется со следующими точками */
		GeneralPath graphics = new GeneralPath();
		for (int i = 0; i < graphicsData.length; i++) {
			// Преобразовать значения (x,y) в точку на экране point
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			if (i > 0) 
				// Не первая итерация цикла - вести линию в точку point
				graphics.lineTo(point.getX(), point.getY());
			else 
				// Первая итерация цикла - установить начало пути в точку point
				graphics.moveTo(point.getX(), point.getY());
		}
		// Отобразить график
		canvas.draw(graphics);
	}
	
	// Метод, рисующий дополнительный график функции "модуль f"
	protected void paintAdditionalGraphic(Graphics2D canvas) {
		
		canvas.setStroke(additionalGraphicsStroke);
		canvas.setColor(Color.GREEN);
		
		Double[][] newGraphicsData = new Double[graphicsData.length][2];
		for (int i = 0; i < graphicsData.length; i++) {
			if (graphicsData[i][1] < 0.0)
				newGraphicsData[i][1] = Math.abs(graphicsData[i][1]);
			else 
				newGraphicsData[i][1] = graphicsData[i][1];
		}
		for (int i = 0; i < graphicsData.length; i++)
			newGraphicsData[i][0] = graphicsData[i][0];
		
		GeneralPath newGraphics = new GeneralPath();
		for (int i = 0; i < newGraphicsData.length; i++) {
			Point2D.Double point = xyToPoint(newGraphicsData[i][0], newGraphicsData[i][1]);
			if (i > 0) 
				newGraphics.lineTo(point.getX(), point.getY());
			else 
				newGraphics.moveTo(point.getX(), point.getY());
		}
		canvas.draw(newGraphics);
	}
	
	// Отображение маркеров точек, по которым рисовался график
	protected void paintMarkers(Graphics2D canvas) {
		// Установить специальное перо для черчения контуров маркеров
		canvas.setStroke(markerStroke);
		boolean intIsRoot;
		// Организовать цикл по всем точкам графика
		for (Double[] point : graphicsData) {
			
			GeneralPath marker = new GeneralPath(); 
			Point2D.Double center = xyToPoint(point[0], point[1]);
			marker.moveTo(center.getX() - 5.5, center.getY() + 5.5);
			marker.lineTo(center.getX() + 5.5, center.getY() + 5.5);
			marker.lineTo(center.getX(), center.getY() - 5.5);
			marker.closePath();
			
			int integer = point[1].intValue();
			double i = Math.sqrt(integer);
			if (i == (int)i)
				intIsRoot = true;
			else intIsRoot = false;
			
			if (intIsRoot) {
				canvas.setColor(Color.RED);
	    		canvas.setPaint(Color.RED);
			}
			else {
				canvas.setColor(Color.DARK_GRAY);
	    		canvas.setPaint(Color.DARK_GRAY);
			}
			
			canvas.draw(marker);
			canvas.fill(marker);
		}
	}
	
	// Метод, обеспечивающий отображение осей координат
	protected void paintAxis(Graphics2D canvas) {
		// Установить особое начертание для осей
		canvas.setStroke(axisStroke);
		// Оси рисуются чѐрным цветом
		canvas.setColor(Color.BLACK);
		// Стрелки заливаются чѐрным цветом
		canvas.setPaint(Color.BLACK);
		// Подписи к координатным осям делаются специальным шрифтом
		canvas.setFont(_01Font);
		// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
		FontRenderContext context = canvas.getFontRenderContext();
		
		GeneralPath lineY = new GeneralPath();
		GeneralPath lineX = new GeneralPath();
		
		// 1x и 1y
		Point2D.Double _1y = xyToPoint(0, 1);
		Point2D.Double _1x = xyToPoint(1, 0);
		
		lineY.moveTo(_1y.getX() - 10 , _1y.getY());
		lineY.lineTo(lineY.getCurrentPoint().getX() + 20, lineY.getCurrentPoint().getY());
		canvas.draw(lineY);
		
		lineX.moveTo(_1x.getX(), _1x.getY() - 10);
		lineX.lineTo(lineX.getCurrentPoint().getX(), lineX.getCurrentPoint().getY() + 20);
		canvas.draw(lineX);
		
		Rectangle2D xybounds = axisFont.getStringBounds("1", context);
		canvas.drawString("1", (float)_1y.getX() - 25, (float)(_1y.getY() - xybounds.getY()) - 21);
		canvas.drawString("1", (float)_1x.getX() - 15, (float)(_1x.getY() - xybounds.getY()) - 32);

		// Определить, должна ли быть видна ось Y на графике
		if (minX <= 0.0 && maxX >= 0.0) {
			// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,// а правая (maxX) >= 0.0
			// Сама ось - это линия между точками (0, maxY) и (0, minY)
			canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
			// Стрелка оси Y
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на верхний конец оси Y
			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
			arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY()+20);
			// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
			arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
			// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow);
			// Закрасить стрелку
			// Нарисовать подпись к оси Y
			// Определить, сколько места понадобится для надписи "y"
			Point2D.Double coordLabelPos = xyToPoint(0, 0);
			Rectangle2D coordbounds = axisFont.getStringBounds("0", context);
			canvas.drawString("0", (float)coordLabelPos.getX() - 20, (float)(coordLabelPos.getY() - coordbounds.getY()) - 5);
			canvas.setFont(axisFont);
			Rectangle2D bounds = axisFont.getStringBounds("y", context);
			Point2D.Double labelPos = xyToPoint(0, maxY);
			// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
		}
		// Определить, должна ли быть видна ось X на графике
		if (minY <= 0.0 && maxY >= 0.0) {
			// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
			// а нижняя (minY) <= 0.0
			canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
			// Стрелка оси X
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на правый конец оси X
			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
			arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);
			// Вести левую часть стрелки в точку с относительными координатами (0, 10)
			arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
			// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow);
			// Закрасить стрелку
			// Нарисовать подпись к оси X
			// Определить, сколько места понадобится для надписи "x"
			Rectangle2D bounds = axisFont.getStringBounds("x", context);
			Point2D.Double labelPos = xyToPoint(maxX, 0);
			// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("x", (float)(labelPos.getX() - bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
		}
		
	}
	
	/* Метод-помощник, осуществляющий преобразование координат.
	 * Оно необходимо, т.к. верхнему левому углу холста с координатами
	 * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY), где
	 * minX - это самое "левое" значение X, а
	 * maxY - самое "верхнее" значение Y.
	 */
	protected Point2D.Double xyToPoint(double x, double y) {
		// Вычисляем смещение X от самой левой точки (minX)
		double deltaX = x - minX;
		// Вычисляем смещение Y от точки верхней точки (maxY)
		double deltaY = maxY - y;
		return new Point2D.Double(deltaX * scale, deltaY * scale);
	}  

	/* Метод-помощник, возвращающий экземпляр класса Point2D.Double
	 * смещѐнный по отношению к исходному на deltaX, deltaY
	 * К сожалению, стандартного метода, выполняющего такую задачу, нет. */
	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
		// Инициализировать новый экземпляр точки
		Point2D.Double dest = new Point2D.Double();
		// Задать еѐ координаты как координаты существующей точки + заданные смещения
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}
}