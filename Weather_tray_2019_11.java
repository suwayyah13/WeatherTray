import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.AWTException;
import java.awt.Image;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.Timer;
/*** @author Nikolay Drozdov (suwayan@mail.ru) ***/
public class Weather_tray_2019_11
{
	public static SystemTray tray;
	public static TrayIcon trayIcon;
	public static ActionListener updateListener;
    public static void main(String[] args)
	{
		/*** Иконка в трее - Начало ***/
		if (SystemTray.isSupported())
		{
			tray=SystemTray.getSystemTray();
			try
			{
				trayIcon=new TrayIcon(make_icon_image(true));
			}
			catch(IOException e){}
			trayIcon.setToolTip("Погода в Санкт-Петербурге ");
			trayIcon.setImageAutoSize(true);
			/*** Меню - Начало ***/
			ActionListener exitListener=new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					System.out.println("Завершение работы приложения");
					System.exit(0);
				}
			};
			JMenuItem defaultItem=new JMenuItem("Выйти из программы");
			defaultItem.addActionListener(exitListener);
			updateListener=new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					//mainf(false);
					try
					{
						trayIcon.setImage(make_icon_image(false));
					}
					catch(IOException e_0002){}
					System.gc();
					Runtime.getRuntime().gc();
				}
			};
			JMenuItem updateItem=new JMenuItem("Обновить данные");
			updateItem.addActionListener(updateListener);
			final JPopupMenu popup=new JPopupMenu();
			popup.add(updateItem);
			popup.add(defaultItem);
			ActionListener actionListener=new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					
				}
			};
			trayIcon.addActionListener(actionListener);
			trayIcon.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e) 
				{
					if (e.isPopupTrigger()) 
					{
						popup.setLocation(e.getX(), e.getY());
						popup.setInvoker(popup);
						popup.setVisible(true);
					}
				}
			});
			/*** Меню - Конец ***/
			try
			{
				tray.add(trayIcon);
			}
			catch (AWTException e) 
			{
				System.err.println("Ошибка добавления иконки в системный трей");
				System.err.println(e.getMessage());
			}
			Timer timer=new Timer(15*60*1000,updateListener);
			timer.start();
		}
		else
		{
			System.err.println("Работа с системным треем не поддерживается!");
		}
		/*** Иконка в трее - Конец ***/
	}
	public static Image make_icon_image(boolean is_from_start) throws IOException 
	{
		boolean is_error=false;
		String temperature_string;
		int temperature_int=0;
		String sJSON="";
		String sResult="Err";
		String Error_text="";
		String URL_text="";
		int timeout=300000; 

		/*** Формирование текста для вывода - Начало ***/
		try
		{
			URL_text="https://api.openweathermap.org/data/2.5/weather?id=489226&appid=e5814e97869fe046dc7fdae40c8e30a3&units=metric";
			URL url = new URL(URL_text);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(timeout);
			con.setReadTimeout(timeout);
			InputStream in_stream=con.getInputStream();
			int i;
			while((i=in_stream.read())!=-1)
			{
				sJSON+=(char)i;
			}
		}
		catch(IOException e) 
		{
			Error_text="Ошибка получения данных ("+URL_text+")"+"\n"+e.getMessage();
			is_error=true;
		}
		temperature_string="";
		if(is_error==false)
		{
			try
			{
				JSONObject arrJSON=new JSONObject(sJSON);
				temperature_string=arrJSON.getJSONObject("main").get("temp").toString();
				try
				{
					temperature_int=Integer.parseInt(temperature_string);
				}
				catch(NumberFormatException e) 
				{
					try
					{
						temperature_int=(int) Float.parseFloat(temperature_string);
					}
					catch(NumberFormatException e_01) 
					{
						Error_text="Ошибка преобразования температуры: "+temperature_string+"\n"+e_01.getMessage();
						sResult="Err";
						is_error=true;
					}
				}
				temperature_string=""+temperature_int;
				if(temperature_int>0)
				{
					sResult="+";
				}
				else
				{
					sResult="";
				}
				sResult+=temperature_string;		
				if(temperature_int<10 && temperature_int>-10)
				{
					sResult+="°";
				}
			}
			catch(JSONException e) 
			{
				Error_text="Ошибка разбора ответа сервера[код 0]"+"\n"+e.getMessage();
				sResult="Err";
				is_error=true;
			}
		}
		/*** Формирование текста для вывода - Конец ***/
		String image_path=System.getProperty("java.io.tmpdir") + "\\temperature.png";
		try
		{
			File file=new File(image_path);
			if(file.exists()==true)
			{
				file.delete();
			}
    	}catch(Exception e){}
		int width = 16, height = 16;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
		Font font = new Font("TimesNewRoman",Font.BOLD,9);
		if(temperature_int>10 && temperature_int<-10)
		{
			font = new Font("TimesNewRoman",Font.BOLD,8);
		}
		ig2.setFont(font);
		FontMetrics fontMetrics = ig2.getFontMetrics();
		int stringWidth = fontMetrics.stringWidth(sResult);
		int stringHeight = fontMetrics.getAscent();
		if(is_error==true)
		{
			ig2.setPaint(Color.black);
			ig2.fillRect(0,0,16,16);
		}
		else
		{
			if(temperature_int<0)
			{
				ig2.setPaint(Color.blue);
				ig2.fillRect(0,0,16,16);
			}
			else
			{
				if(temperature_int==0)
				{
					ig2.setPaint(Color.darkGray);
					ig2.fillRect(0,0,16,16);
				}
				else
				{
					ig2.setPaint(Color.red);
					ig2.fillRect(0,0,16,16);
				}
			}
		}
		ig2.setPaint(Color.white);
		ig2.drawString(sResult, (width-stringWidth)/2+1, height / 2 + stringHeight / 4+1);
		try 
		{
			ImageIO.write(bi, "PNG", new File(image_path));
		}
		catch(IOException e) 
		{
			Error_text="Ошибка записи временного файла"+"\n"+e.getMessage();
			System.err.println(e.getMessage());
		}
		if(Error_text!="")
		{
			JOptionPane.showMessageDialog(null,Error_text);
			System.err.println(Error_text);
		}
		try
		{
			Image image=ImageIO.read(new File(image_path));
			return image;
		}
		catch(IOException e)
		{
			Error_text="Ошибка чтения временного файла"+"\n"+e.getMessage();
			System.err.println(Error_text);
			throw e;
		}
	}
}