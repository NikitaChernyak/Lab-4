package bsu.rct.group8.chernyak.lab5;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;


public class MainFrame extends JFrame {
		// Начальные размеры окна приложения
		private static final int WIDTH = 800;
		private static final int HEIGHT = 600;
		// Объект диалогового окна для выбора файлов
		private JFileChooser fileChooser = null;
		// Пункты меню
		private JCheckBoxMenuItem showAxisMenuItem;
		private JCheckBoxMenuItem showMarkersMenuItem;
		private JCheckBoxMenuItem showGraphicMenuItem;
		// Компонент-отображатель графика
		private GraphicsDisplay display = new GraphicsDisplay();
		// Флаг, указывающий на загруженность данных графика
		private boolean fileLoaded = false;	
	
	public MainFrame() {
		// Вызов конструктора предка Frame
		super("Построение графиков функций на основе подготовленных файлов");
		// Установка размеров окна
		setSize(WIDTH, HEIGHT);
		Toolkit kit = Toolkit.getDefaultToolkit();
		// Отцентрировать окно приложения на экране
		setLocation((kit.getScreenSize().width - WIDTH)/2,
		(kit.getScreenSize().height - HEIGHT)/2);
		// Развѐртывание окна на весь экран
		setExtendedState(MAXIMIZED_BOTH);
		
		
		// Создать и установить полосу меню
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		// Добавить пункт меню "Файл"
		JMenu fileMenu = new JMenu("Файл");
		menuBar.add(fileMenu);
		// Создать действие по открытию файла
		Action openGraphicsAction = new AbstractAction("Открыть файл") {
			public void actionPerformed(ActionEvent event) {
			if (fileChooser==null) {
				fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("."));
			}
			if (fileChooser.showOpenDialog(MainFrame.this) ==	JFileChooser.APPROVE_OPTION)
				openGraphics(fileChooser.getSelectedFile());
			}
		};
			// Добавить соответствующий элемент меню
			fileMenu.add(openGraphicsAction);
			// Создать пункт меню "График"
			JMenu graphicsMenu = new JMenu("График");
			menuBar.add(graphicsMenu);
		
		
		// Создать действие для реакции на активацию элемента
		// "Показывать оси координат"
		Action showAxisAction = new AbstractAction("Показывать оси координат") {
			public void actionPerformed(ActionEvent event) {
			// свойство showAxis класса GraphicsDisplay истина,
			// если элемент меню showAxisMenuItem отмечен флажком,
			// ложь - в противном случае
			display.setShowAxis(showAxisMenuItem.isSelected());
			}
		};
		showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
		// Добавить соответствующий элемент в меню
		graphicsMenu.add(showAxisMenuItem);
		// Элемент по умолчанию включен (отмечен флажком)
		showAxisMenuItem.setSelected(true);
		
		
		// Повторить действия для элемента "Показывать маркеры точек"
		Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
			public void actionPerformed(ActionEvent event) {
			// по аналогии с showAxisMenuItem
			display.setShowMarkers(showMarkersMenuItem.isSelected());
			}
		};
		showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
		graphicsMenu.add(showMarkersMenuItem);
		// Элемент по умолчанию включен
		showMarkersMenuItem.setSelected(true);
				// Зарегистрировать обработчик событий, связанных с меню "График"
		graphicsMenu.addMenuListener(new GraphicsMenuListener());
	
		
		
		Action showGraphicAction = new AbstractAction("график функции «целая часть f»") {
			public void actionPerformed(ActionEvent event) {
				display.setShowGraphic(showGraphicMenuItem.isSelected());
		    }
		};
			showGraphicMenuItem = new JCheckBoxMenuItem(showGraphicAction);
			graphicsMenu.add(showGraphicMenuItem);
			showGraphicMenuItem.setSelected(true);
			
			
		// Установить GraphicsDisplay в цент граничной компоновки
		getContentPane().add(display, BorderLayout.CENTER);
		
	}
		
		// Считывание данных графика из существующего файла
		protected void openGraphics(File selectedFile) {
			 try
		        {
		            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
		            ArrayList graphicsData = new ArrayList(50);
		            while (in.available() > 0) {
		                Double x = Double.valueOf(in.readDouble());
		                Double y = Double.valueOf(in.readDouble());
		                graphicsData.add(new Double[] { x, y });
		            }
		            if (graphicsData.size() > 0) {
		                fileLoaded = true;
		                //resetGraphicsMenuItem.setEnabled(true);
		                display.showGraphics(graphicsData);
		            }
		        }
		        catch(FileNotFoundException ex)
		        {
		            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
		            return;
		        }
		        catch(IOException ex)
		        {
		            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла",	"Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
		            return;
		        }
			
						
			
		}
		
		
			public static void main(String[] args)
		    {
		     
		        MainFrame frame = new MainFrame();
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        frame.setVisible(true);
		    }
		
			
			private class GraphicsMenuListener implements MenuListener
		    {

		        @Override
		        public void menuSelected(MenuEvent e) {
		            showAxisMenuItem.setEnabled(fileLoaded);
		            showMarkersMenuItem.setEnabled(fileLoaded);
		        }

		        @Override
		        public void menuDeselected(MenuEvent e) {

		        }

		        @Override
		        public void menuCanceled(MenuEvent e) {

		        }
		    }
}