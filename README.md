# Elona Customizer
Elona Customizer is a tool that helps with the editing of decompiled HSP programs by splitting off functions and subroutines off into separate files. It can also automatically rename function parameters for added clarity, and can rename goto labels with a bit of prep work. Finally, it has a function for using regex to modify files, which could be used for any number of things, including giving proper names to function parameters and inserting new functions.

&nbsp;

Instructions:
1. Decompile an HSP program and put the .HSP file in a folder somewhere. OPTIONAL: Place an .ECL file and some .ECR files in that same folder.
2. In Elona Customizer, click Open and select the decompiled HSP file. This will also check for the presence of an ECL file. OPTIONAL: Enable function parameter auto-renaming by clicking the checkbox. (Note that parameter auto-renaming will probably add like 10 minutes to processing time.)
3. Click Split. (Note: The program will seem to freeze up for a bit while processing 12+ megabytes of text. Just let it run.) This will first clean up unused debug labels, then move the code of every function and label into a separate file named after it, leaving #include statements behind in the main HSP file. If the option was enabled, all function parameters will be given less random names, and if an ECL file is present the goto labels will be renamed accordingly before being split off. The original decompiled HSP file will be unmodified; instead, a new HSP file with ".split" appended to its name will be created.
4. OPTIONAL: Click Customize. This will apply every ECR file to the HSP file of the same name.

&nbsp;

ECL file:<br>
An Elona Customizer Labels file is a plaintext file used to automatically rename goto labels during the SPLIT operation. The filename must be identical to the decompiled HSP file it's to be used on.<br>
The first line of text is the desired name for the label, and the second is a unique bit of code belonging to that label. For example:

> game_win<br>
> "Unbelievable! You conquered Lesimas!"

The line of code will be located, and the nearest label above it will be renamed from, e.g. "*label_3947" to "*game_win".<br>
Naturally, an ECL file can handle as many labels as needed. Just remember: odd lines are the new names, even lines are lines of code unique to each label.<br>
However, some subroutines are basically impossible to identify this way. For these cases, you can mark the label as a "relative label" with an asterisk, and then specify the label that it follows. For example:

> game_win<br>
> "Unbelievable! You conquered Lesimas!"<br>
> *game_win_WHILE1<br>
> game_win<br>
> *game_win_WEND1<br>
> game_win_WHILE1

Here, after renaming "*label_3947" to "*game_win", the next label below it ("*label_3948" in this case) will be located and renamed to "*game_win_WHILE1", and then the next label after that will be renamed to "*game_win_WEND1".
Finally, if you want to make sure that your label file will work properly, then before clicking Split you can click Verify ECL File. This will check to make sure the identifying string for each standard label only exists once in the HSP file, and produce a text file in the same folder listing any code snippets that weren't found or that were found more than once.

&nbsp;

ECR file:<br>
An Elona Customizer Regex file is a plaintext file containing the strings for regex match/replacing done upon clicking CUSTOMIZE. The first line of text is the match expression, and the second line is the replace expression. (You can have as many match/replace pairs as needed in each ECR file.)
Each ECR file must have a filename matching its corresponding HSP file. For example, say you wanted to automatically rename the parameters of the refchara() function. SPLIT automatically renamed it from "prm_262" to "refchara_prm1", but it should actually be "refchara_dbid".
To accomplish this, you'd create a file named "refchara.ecr", with the following text:

> refchara_prm1<br>
> refchara_dbid

This uses Java's replaceAll() function. Here's the documentation for Java's implementation of regex:
https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

&nbsp;

Changelog:
* v0.8a: Added "relative label" functionality to ECL files. Added ECL verification. Added rudimentary process to deal with debug goto labels.
* v0.7b: Fixed bug related to newlines in functions.
* v0.6b: Fixed bug related to macros.
* v0.5b: Fixed one last bug that crept in somehow. Confirmed that the split source code still compiles. Initial public release.
* v0.4a: Fixed the last observable bugs and got Customize working.
* v0.3a: Fixed Shift-JIS text encoding and a few more bugs. Made function parameter renaming optional because it takes way longer.
* v0.2a: Fixed numerous bugs.
* v0.1a: First internal release.

&nbsp;

Resources used:
* SWT: https://www.eclipse.org/swt/
* Apache Commons Lang: https://commons.apache.org/proper/commons-lang/
