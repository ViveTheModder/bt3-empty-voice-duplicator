package cmd;
//Empty Voice Line Duplicator by ViveTheModder
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main 
{
	static final int MAX_VOICES = 300;
	static final File OUT = new File("./out/");
	static File currCsv, emptyAdx;
	static int totalVoices;
	static int[] voiceIndices = new int[MAX_VOICES];
	
	public static File getFileFromFileChooser()
	{
		File adx = null;
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Dreamcast Audio File (.ADX)", new String[]{"adx"});
		chooser.addChoosableFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("Select empty ADX file to duplicate...");
		while (true)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0) 
			{
				adx = chooser.getSelectedFile();
				//I only added this check because JFileChoosers still provide an "All Files" option, bypassing the ChoosableFileFilter
				if (adx.getName().toLowerCase().endsWith(".adx")) break;
				else JOptionPane.showMessageDialog(chooser, "This file is NOT a valid ADX file! Try again!", "Invalid File", JOptionPane.ERROR_MESSAGE);
			}
			else break;
		}
		return adx;		
	}
	private static File getFolderFromFileChooser()
	{
		File folder = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select folder with CSV files...");
		while (true)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0)
			{
				File tempFolderRef = chooser.getSelectedFile(); //actually gets the selected folder
				File[] tempFolderCSVs = tempFolderRef.listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name) 
					{
						return name.toLowerCase().endsWith(".csv");
					}
				});
				if (!(tempFolderCSVs==null || tempFolderCSVs.length==0)) 
				{
					folder = tempFolderRef; break;
				}
				else JOptionPane.showMessageDialog(chooser, "This folder does NOT have CSV files! Try again!", "Invalid Folder", JOptionPane.ERROR_MESSAGE);
			}
			else System.exit(2);
		}
		return folder;
	}
	private static int getNumberOfDigits(int num)
	{
		int cnt=0;
		while (num!=0)
		{
			num/=10;
			cnt++;
		}
		return cnt;
	}
	public static void copyFileFromCsvFolder() throws IOException
	{
		File csvFolder = getFolderFromFileChooser();
		File[] csvArray = csvFolder.listFiles();
		Path emptyAdxPath = emptyAdx.toPath();
		Path outPath = OUT.toPath();

		double start = System.currentTimeMillis();
		for (File csv: csvArray)
		{
			currCsv = csv;
			int gscIndex = Integer.parseInt(csv.getName().split("-")[2]);
			setUnusedVoiceLinesFromCsv();
			for (int i=0; i<totalVoices; i++)
			{
				String gscIndexZeroes="";
				int gscIndexDigits = getNumberOfDigits(gscIndex);
				if (gscIndexDigits==1 || gscIndex==0) gscIndexZeroes="0";
				
				String indexZeroes="";
				int indexDigits = getNumberOfDigits(voiceIndices[i]);
				if (indexDigits==2) indexZeroes="0";
				else if (indexDigits==1 || i==0) indexZeroes="00";
				
				String fileName = "VIC-"+gscIndexZeroes+gscIndex+"-"+indexZeroes+voiceIndices[i]+"-US.adx";
				Files.copy(emptyAdxPath, outPath.resolve(fileName), StandardCopyOption.COPY_ATTRIBUTES);
				Files.copy(emptyAdxPath, outPath.resolve(fileName.replace("US", "JP")), StandardCopyOption.COPY_ATTRIBUTES);
				System.out.println("GSC-B-"+gscIndexZeroes+gscIndex+": Copying "+(2*(i+1))+" files out of "+(2*totalVoices)+"...");
			}
		}
		double end = System.currentTimeMillis();
		System.out.println("\nTime elapsed: "+(end-start)/1000+" s");
	}
	private static void error(Exception e1)
	{
		File errorLog = new File("errors.log");
		try 
		{
			FileWriter logWriter = new FileWriter(errorLog,true);
			logWriter.append(new SimpleDateFormat("dd-MM-yy-hh-mm-ss").format(new Date())+":\n"+e1.getMessage()+"\n");
			logWriter.close();
		} 
		catch (IOException e2) 
		{
			e2.printStackTrace();
		}
	}
	private static void setUnusedVoiceLinesFromCsv() throws FileNotFoundException
	{
		voiceIndices = new int[MAX_VOICES];
		totalVoices=0;
		Scanner sc = new Scanner(currCsv);
		if (sc.hasNextLine()) sc.nextLine(); //skip header
		while (sc.hasNextLine())
		{
			String input = sc.nextLine();
			String[] inputArr = input.split(",");
			if (inputArr.length==2)
			{
				voiceIndices[totalVoices]=Integer.parseInt(inputArr[1]);
				totalVoices++;
			}
		}
		sc.close();
	}
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			emptyAdx = getFileFromFileChooser();
			if (emptyAdx==null) System.exit(1);
			copyFileFromCsvFolder();
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | IOException e) 
		{
			error(e);
		}
	}
}
