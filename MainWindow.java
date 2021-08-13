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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
		shlElonaCustomizer.setSize(450, 175);
		shlElonaCustomizer.setText("Elona Customizer v0.8a");
		
		Label lblNewLabel = new Label(shlElonaCustomizer, SWT.NONE);
		lblNewLabel.setBounds(189, 37, 235, 15);
		lblNewLabel.setText("Click Open to select a file");
		
		Label lblCustomLabelFile = new Label(shlElonaCustomizer, SWT.NONE);
		lblCustomLabelFile.setBounds(91, 37, 92, 15);
		
		ProgressBar progressBar = new ProgressBar(shlElonaCustomizer, SWT.NONE);
		progressBar.setBounds(0, 119, 434, 17);
		
		Button btnVerifyEclFile = new Button(shlElonaCustomizer, SWT.NONE);
		btnVerifyEclFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressBar.setSelection(0);
				File hsploc = new File(text.getText());
				File ecl = new File(getFolder(hsploc), hsploc.getName().toLowerCase().replaceFirst("hsp$", "ecl"));
				File result = new File(getFolder(hsploc), hsploc.getName().toLowerCase().replaceFirst(".hsp$", " verification log.txt"));
				try {
					result.createNewFile();
					BufferedReader hspr = new BufferedReader(new InputStreamReader(new FileInputStream(hsploc), "Shift_JIS"));
					String mainhsp = hspr.lines().collect(Collectors.joining("\n"));
					BufferedReader eclr = new BufferedReader(new InputStreamReader(new FileInputStream(ecl), "Shift_JIS"));
					BufferedWriter rw = new BufferedWriter(new FileWriter(result));
					rw.write("Checking standard label entries in "+ecl.getName()+"...\n");
					progressBar.setSelection(50);
					String line = eclr.readLine();
					while (line != null) {
						if (line.startsWith("*")) {
							//skip relative labels
							line = eclr.readLine();
						} else {							
							line = eclr.readLine();
							int count = StringUtils.countMatches(mainhsp, line);
							if (count != 1) {
								rw.write(count + ": " + line + "\n");
							}
						}
						line = eclr.readLine();
					}
					rw.write("Complete! Any search strings that weren't found to have exactly one match are listed above.");
					hspr.close();
					eclr.close();
					rw.close();
				} catch (IOException e1) {
					errorMessage(e1.getClass().toString(), e1.getMessage());
				}
				progressBar.setSelection(100);
			}
		});
		btnVerifyEclFile.setEnabled(false);
		btnVerifyEclFile.setBounds(341, 88, 83, 25);
		btnVerifyEclFile.setText("Verify ECL File");

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
						btnVerifyEclFile.setEnabled(true);
					} else {
						lblNewLabel.setText("None");
						btnVerifyEclFile.setEnabled(false);
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
							if (line.startsWith("#deffunc") || line.startsWith("#defcfunc")) {
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
								while (line != null && (line.startsWith("\t") || line.isEmpty())) {
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
				progressBar.setSelection(20);
				//Auto-remove debug goto labels pointing to function declarations.
				mainhsp = mainhsp.replaceAll("\n\\*label_[0-9][0-9][0-9][0-9]\n#def", "\n#def");
				//"Merge" successive labels, since it's random whether the top one or bottom one is the debug label.
				String[] searchList = {};
				String[] replacementList = {};	
				Pattern debuglabel = Pattern.compile("\\n(\\*label_[0-9][0-9][0-9][0-9])\\n(\\*label_[0-9][0-9][0-9][0-9])");
				Matcher m = debuglabel.matcher(mainhsp);
				while (m.find()) {
					searchList = ArrayUtils.add(searchList, m.group(1));
					replacementList = ArrayUtils.add(replacementList, m.group(2));
					mainhsp = mainhsp.substring(0, m.start()) + mainhsp.substring(mainhsp.indexOf("\n", m.start()+1));
				}
				mainhsp = StringUtils.replaceEach(mainhsp, searchList, replacementList);
				progressBar.setSelection(40);
				if (lblNewLabel.getText() != "None") {
					searchList = new String[] {};
					replacementList = new String[] {};
					String[] relativeLabels = {};
					File eclloc = new File(getFolder(hsploc), lblNewLabel.getText());
					try {
						BufferedReader br = Files.newBufferedReader(eclloc.toPath(), Charset.forName("Shift_JIS"));
						String line = br.readLine();
						while (line != null) {
							if (line.startsWith("*")) {
								//Relative labels can't be handled until after normal labels are done.
								relativeLabels = ArrayUtils.add(relativeLabels, line.substring(1));
								relativeLabels = ArrayUtils.add(relativeLabels, br.readLine());
							} else {
								replacementList = ArrayUtils.add(replacementList, line);
								int indexa = mainhsp.lastIndexOf("\n*", mainhsp.indexOf(br.readLine()));
								int indexb = mainhsp.indexOf("\n", indexa+1);
								searchList = ArrayUtils.add(searchList, mainhsp.substring(indexa+2, indexb));
							}
							line = br.readLine();
						}
						br.close();
					} catch (FileNotFoundException e1) {
						errorMessage(e1.getClass().toString(), e1.getMessage());
					} catch (IOException e1) {
						errorMessage(e1.getClass().toString(), e1.getMessage());
					}
					mainhsp = StringUtils.replaceEach(mainhsp, searchList, replacementList);
					for (int i = 0; i < relativeLabels.length; i = i + 2) {
						int indexa = mainhsp.indexOf("\n*", mainhsp.indexOf(relativeLabels[i+1]));
						int indexb = mainhsp.indexOf("\n", indexa+1);
						mainhsp = mainhsp.replace(mainhsp.substring(indexa+2, indexb), relativeLabels[i]);
					}
					progressBar.setSelection(60);
				}
				progressBar.setSelection(80);
				File splitfile = new File(getFolder(hsploc), hsploc.getName().toLowerCase().replaceFirst("hsp$", "")+"split.hsp");
				try {
					splitfile.createNewFile();
					BufferedWriter mainout = new BufferedWriter(new FileWriter(splitfile, Charset.forName("Shift_JIS")));
					BufferedReader br = new BufferedReader(new StringReader(mainhsp));
					String line = br.readLine();
					while (line != null) {
						if (line.startsWith("#deffunc") || line.startsWith("#defcfunc") || line.startsWith("*")) {
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
							while (line != null && (line.startsWith("\t") || line.isEmpty())) {
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
