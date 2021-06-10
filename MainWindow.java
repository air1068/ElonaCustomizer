import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class MainWindow {

	protected Shell shlElonaCustomizer;
	private Text text;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlElonaCustomizer.open();
		shlElonaCustomizer.layout();
		while (!shlElonaCustomizer.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public void errorMessage(String title, String message) {
		MessageBox iamerror = new MessageBox(shlElonaCustomizer);
		iamerror.setText(title);
		iamerror.setMessage(message);
		iamerror.open();
	}
	
	public String getFolder(File f) {
		return f.getPath().substring(0, f.getPath().indexOf(f.getName()));
	}
	
	public String getFolder(String filename) {
		File f = new File(filename);
		return getFolder(f);
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlElonaCustomizer = new Shell();
		shlElonaCustomizer.setSize(450, 160);
		shlElonaCustomizer.setText("Elona Customizer v0.5b");
		
		Label lblNewLabel = new Label(shlElonaCustomizer, SWT.NONE);
		lblNewLabel.setBounds(189, 37, 235, 15);
		lblNewLabel.setText("Click Open to select a file");
		
		Label lblCustomLabelFile = new Label(shlElonaCustomizer, SWT.NONE);
		lblCustomLabelFile.setBounds(91, 37, 92, 15);
		
		ProgressBar progressBar = new ProgressBar(shlElonaCustomizer, SWT.NONE);
		progressBar.setBounds(0, 103, 434, 17);
		
		Button btnOpen = new Button(shlElonaCustomizer, SWT.NONE);
		btnOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressBar.setSelection(0);
				FileDialog selectbox = new FileDialog(shlElonaCustomizer, SWT.OPEN);
				selectbox.setFilterExtensions(new String[] {"*.hsp;*.HSP", "*"});
				selectbox.setFilterNames(new String[] {"HSP files", "All files"});
				String loc = selectbox.open();
				if (loc != null) {
					text.setText(loc);
					File hsploc = new File(text.getText());
					File ecl = new File(getFolder(hsploc), hsploc.getName().toLowerCase().replaceFirst("hsp$", "ecl"));
					lblCustomLabelFile.setText("Custom label file: ");
					if (ecl.exists()) {
						lblNewLabel.setText(ecl.getName());
					} else {
						lblNewLabel.setText("None");
					}
				}
			}
		});
		btnOpen.setBounds(10, 10, 75, 25);
		btnOpen.setText("Open");
		
		Button btnAutorename = new Button(shlElonaCustomizer, SWT.CHECK);
		btnAutorename.setBounds(91, 58, 266, 16);
		btnAutorename.setText("Auto-rename function parameters during Split");
		
		text = new Text(shlElonaCustomizer, SWT.BORDER);
		text.setEditable(false);
		text.setBounds(91, 10, 333, 21);

		Button btnSplit = new Button(shlElonaCustomizer, SWT.NONE);
		btnSplit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressBar.setSelection(0);
				File hsploc = new File(text.getText());
				String mainhsp = "";
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(hsploc), "Shift_JIS"));
					if (btnAutorename.getSelection()) {
						String line = br.readLine();
						while (line != null) {
							if (line.startsWith("#def")) {
								//Rename function parameters. Scope is kept to within the function rather than the whole file to save time.
								String[] searchList = {};
								String[] replacementList = {};
								ArrayList<String> s = new ArrayList<String>(Arrays.asList(line.split(" ")));
								int i = 1;
								s.remove(0);
								String fname = s.remove(0);
								while (s.size() > 1) {
									s.remove(0);
									searchList = ArrayUtils.add(searchList, s.remove(0));
									replacementList = ArrayUtils.add(replacementList, fname+"_prm"+i);
									i++;
								}
								String functext = line+"\n";
								line = br.readLine();
								while (line != null && line.startsWith("\t")) {
									functext += line+"\n";
									line = br.readLine();
								}
								mainhsp += StringUtils.replaceEach(functext, searchList, replacementList);
							} else {
								mainhsp += line+"\n";
								line = br.readLine();
							}
						}
					} else {
						mainhsp = br.lines().collect(Collectors.joining("\n"));
					}
					br.close();
				}
				catch (FileNotFoundException e1) {
					errorMessage(e1.getClass().toString(), e1.getMessage());
				}
				catch (IOException e1) {
					errorMessage(e1.getClass().toString(), e1.getMessage());
				}
				progressBar.setSelection(25);
				if (lblNewLabel.getText() != "None") {
					String[] searchList = {};
					String[] replacementList = {};
					File eclloc = new File(getFolder(hsploc), lblNewLabel.getText());
					try {
						BufferedReader br = Files.newBufferedReader(eclloc.toPath(), Charset.forName("Shift_JIS"));
						String line = br.readLine();
						while (line != null) {
							replacementList = ArrayUtils.add(replacementList, line);
							int indexa = mainhsp.lastIndexOf("\n*", mainhsp.indexOf(br.readLine()));
							int indexb = mainhsp.indexOf("\n", indexa+1);
							searchList = ArrayUtils.add(searchList, mainhsp.substring(indexa+2, indexb-1));
							line = br.readLine();
						}
						br.close();
					} catch (FileNotFoundException e1) {
						errorMessage(e1.getClass().toString(), e1.getMessage());
					} catch (IOException e1) {
						errorMessage(e1.getClass().toString(), e1.getMessage());
					}
					progressBar.setSelection(50);
					mainhsp = StringUtils.replaceEach(mainhsp, searchList, replacementList);
				}
				progressBar.setSelection(75);
				File splitfile = new File(getFolder(hsploc), hsploc.getName().toLowerCase().replaceFirst("hsp$", "")+"split.hsp");
				try {
					splitfile.createNewFile();
					BufferedWriter mainout = new BufferedWriter(new FileWriter(splitfile, Charset.forName("Shift_JIS")));
					BufferedReader br = new BufferedReader(new StringReader(mainhsp));
					String line = br.readLine();
					while (line != null) {
						if (line.startsWith("#def") || line.startsWith("*")) {
							String name = "";
							if (line.startsWith("#def")) {
								name = line.split(" ")[1];
							} else {
								name = line.substring(1);
							}
							File funcfile = new File(getFolder(hsploc), name+".hsp");
							funcfile.createNewFile();
							BufferedWriter funcout = new BufferedWriter(new FileWriter(funcfile, Charset.forName("Shift_JIS")));
							funcout.write(line+"\n");
							line = br.readLine();
							while (line != null && line.startsWith("\t")) {
								funcout.write(line+"\n");
								line = br.readLine();
							}
							funcout.close();
							mainout.write("#include \""+name+".hsp\"\n");
						} else {
							mainout.write(line+"\n");
							line = br.readLine();
						}
					}
					mainout.close();
					br.close();
				} catch (IOException e1) {
					errorMessage(e1.getClass().toString(), e1.getMessage());
				}
				progressBar.setSelection(100);
			}
		});
		btnSplit.setBounds(10, 41, 75, 25);
		btnSplit.setText("Split");
		
		Button btnCustomize = new Button(shlElonaCustomizer, SWT.NONE);
		btnCustomize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressBar.setSelection(0);
				File hspfolder = new File(getFolder(text.getText()));
				File[] ecrs = hspfolder.listFiles(file -> file.getName().toLowerCase().endsWith(".ecr"));
				progressBar.setSelection(50);
				for (File ecr : ecrs) {
					File hsp = new File(getFolder(ecr), ecr.getName().toLowerCase().replaceFirst("ecr$", "")+"hsp");
					try {
						BufferedReader rgxread = Files.newBufferedReader(ecr.toPath(), Charset.forName("Shift_JIS"));
						String hsptext = Files.readString(hsp.toPath());
						String line = rgxread.readLine();
						while (line != null) {
							hsptext = hsptext.replaceAll(line, rgxread.readLine());
							line = rgxread.readLine();
						}
						Files.writeString(hsp.toPath(), hsptext);
						rgxread.close();
					} catch (FileNotFoundException e1) {
						errorMessage(e1.getClass().toString(), e1.getMessage());
					} catch (IOException e1) {
						errorMessage(e1.getClass().toString(), e1.getMessage());
					}
				}
				progressBar.setSelection(100);
			}
		});
		btnCustomize.setBounds(10, 72, 75, 25);
		btnCustomize.setText("Customize");
	}
}
