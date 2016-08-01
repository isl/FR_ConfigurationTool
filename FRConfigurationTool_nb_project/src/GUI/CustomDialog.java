package GUI;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;
import java.io.File;

class CustomDialog extends JDialog
        implements ActionListener,
        PropertyChangeListener {

    private String typedText = null;
    private JTextField textField;
    private JOptionPane optionPane;
    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
    private String use;

    /**
     * Returns null if the typed string was invalid;
     * otherwise, returns the string as the user entered it.
     */
    public String getValidatedText() {
        return typedText;
    }

/**
     * Creates a custom dialog for several purposes
     * @param aFrame the frame within which the dialog is opened 
     * @param message the message to be displayed
     * @param useFor what is the usage purpose
     */
    public CustomDialog(Frame aFrame, String message, String useFor) {
        super(aFrame, true);

        use = useFor;
        String sel = "";


        if (useFor.equalsIgnoreCase("Repository")) {
            sel = "http://localhost:8080/openrdf-sesame,Katerina";
            //   magicWord = aWord.toUpperCase();
            setTitle("Configuration");
        } else if (useFor.equalsIgnoreCase("templateCreation")) {
            setTitle("IVB Template Creation");
        } else if (useFor.equalsIgnoreCase("save")) {
            setTitle("Save");
        } else if (useFor.equalsIgnoreCase("disjoint")) {
            setTitle("Add disjointness");
        } else if (useFor.equalsIgnoreCase("multi")) {
            setTitle("Add multi-instantiation");
        }

        textField = new JTextField(50);
        textField.setText(sel);
        if (useFor.equalsIgnoreCase("FileExistence")) {
            textField = null;
            btnString1 = "YES";
            btnString2 = "NO";
        }
        String msgString1 = message;
        String msgString2 = null;
       
        if (message == null) {
            msgString1 = "Please enter server location and repository name";
            msgString2 = "(Separate by ,)";
        }

        Object[] array = {msgString1, msgString2, textField};
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                optionPane.setValue(new Integer(
                        JOptionPane.CLOSED_OPTION));
            }
        });

        addComponentListener(new ComponentAdapter() {

            public void componentShown(ComponentEvent ce) {
                if (textField != null) {
                    textField.requestFocusInWindow();
                }
            }
        });

        //Register an event handler that puts the text into the option pane.
        if (textField != null) {
            textField.addActionListener(this);
        }

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        //By default the dialog will prompt to connect to a local repository
        String sel = "http://localhost:8080/openrdf-sesame,Katerina";
    
        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {

                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                String ucText = "";
                if (textField != null) {
                    typedText = textField.getText();
                    ucText = typedText.toUpperCase();
                }
                //Check what is the usage of the dialog 
                    //if the usage is for connecting to the repository
                    //server and repository separated by comma
                if (use.equalsIgnoreCase("Repository")) {
                    if (ucText.contains(",")) {
                       
                        sel = clearAndHide();

                    } else {
                        //text was invalid
                        textField.selectAll();
                        JOptionPane.showMessageDialog(
                                CustomDialog.this,
                                "Sorry, \"" + typedText + "\" "
                                + "does not include a comma \",\"\n",
                                "Invalid choice.Try again ..",
                                JOptionPane.ERROR_MESSAGE);
                        typedText = null;
                        textField.requestFocusInWindow();
                    }
                    //if usage is for saving a path or sparql, or to create a template for the ivb
                    //then the user must also specify the name of the relationship
                } else if (use.equalsIgnoreCase("save") || use.equalsIgnoreCase("templateCreation")) {
                   
                    String message = "";
                    if (ucText == null || ucText.equalsIgnoreCase("")) {
                        message = "Empty name was provided!";
                    } 

                    else if (ucText.contains("<") || ucText.contains(">") || ucText.contains(":") || ucText.contains("\"") || ucText.contains("\\") || ucText.contains("/") || ucText.contains("|") || ucText.contains("*") || ucText.contains("?")) {
                        message = "You can not use these characters \n < > : \" / \\ | ? *";
                    }
                    if (!message.equalsIgnoreCase("")) {
                        textField.selectAll();
                        JOptionPane.showMessageDialog(
                                CustomDialog.this,
                                message,
                                "Invalid choice.Try again ..",
                                JOptionPane.ERROR_MESSAGE);
                        typedText = null;
                        textField.requestFocusInWindow();
                    } else {
                        sel = clearAndHide();
                    }
                    //for indicating that a file already exists and asking the user
                    //to replace it or not
                } else if (use.equalsIgnoreCase("FileExistence")) {
                    setVisible(false);
                    typedText = "YES";
                    //for disjoint and multiinstantiation cases, the classes must 
                    //be separated with :
                } else if (use.equalsIgnoreCase("disjoint") || use.equalsIgnoreCase("multi")) {

                    if (ucText.contains(":")) {
                        sel = clearAndHide();
                    } else {
                        textField.selectAll();
                        JOptionPane.showMessageDialog(
                                CustomDialog.this,
                                "Sorry, \"" + typedText + "\" "
                                + "does not include a colon \":\"\n",
                                "Invalid choice.Try again ..",
                                JOptionPane.ERROR_MESSAGE);
                        typedText = null;
                        textField.requestFocusInWindow();
                    }
                }

            } else { 
                if (use.equalsIgnoreCase("Repository"))sel = null;
                if (!use.equalsIgnoreCase("FileExistence")) {
                    typedText = null;
                    clearAndHide();
                } else {
                    typedText = "NO";
                    setVisible(false);
                }

            }
        }
        if (use.equalsIgnoreCase("Repository")) {
            System.out.println("sel "+sel);
            if (sel!=null) {
                String[] selStr = sel.split(",");
                ToolInterface.server = selStr[0].replaceAll(" ", "");
                ToolInterface.repository = selStr[1].replaceAll(" ", "");
            }
            else{}
        } else if (use.equalsIgnoreCase("save")) {
            ToolInterface.userDefinedName = sel.replaceAll(" ", "");
        }

    }

    /** This method clears the dialog and hides it. */
    public String clearAndHide() {
        String sel = textField.getText();
        textField.setText(null);
        setVisible(false);
        return sel;
    }
}