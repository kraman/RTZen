package demo.doom;

import java.awt.*;
import java.awt.event.*;
import java.rmi.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

public class DoomClient extends JPanel
    implements ActionListener, FocusListener, KeyListener, MouseListener
{
    public DoomClient()
    {
        super(false);
        name = "";
        nameLabel = new JLabel("Name", 4);
        nameField = new JTextField("No Name");
        nameField.setActionCommand("Name");
        goButton = new JButton("Begin");
        goButton.setMnemonic('b');
        goButton.setActionCommand("Begin");
        stopButton = new JButton("Stop");
        stopButton.setMnemonic('s');
        stopButton.setActionCommand("Stop");
        stopButton.setEnabled(false);
        nameField.addActionListener(this);
        nameField.addFocusListener(this);
        goButton.addActionListener(this);
        stopButton.addActionListener(this);
        JPanel jpanel = new JPanel(false);
        jpanel.setBorder(BorderFactory.createTitledBorder("Input name"));
        GridBagLayout gridbaglayout = new GridBagLayout();
        jpanel.setLayout(gridbaglayout);
        addParameterRow(jpanel, nameLabel, nameField);
        JComponent ajcomponent[] = new JComponent[2];
        ajcomponent[0] = goButton;
        ajcomponent[1] = stopButton;
        Box box = makeEvenlySpacedBox(ajcomponent);
        javax.swing.border.Border border = BorderFactory.createRaisedBevelBorder();
        javax.swing.border.Border border1 = BorderFactory.createLoweredBevelBorder();
        javax.swing.border.CompoundBorder compoundborder = BorderFactory.createCompoundBorder(border, border1);
        gamePane = new GamePane(this);
        gamePane.setAlignmentX(0.5F);
        Dimension dimension = new Dimension(500, 500);
        gamePane.setPreferredSize(dimension);
        gamePane.setMaximumSize(dimension);
        gamePane.setMinimumSize(dimension);
        gamePane.setBorder(compoundborder);
        gamePane.setOpaque(true);
        gamePane.addKeyListener(this);
        gamePane.addMouseListener(this);
        if(!gamePane.isFocusable())
            System.err.println("Can not get focus traverse!!");
        if(!gamePane.isRequestFocusEnabled())
            System.err.println("Can not get focus request!!");
        playerTextArea = new JTextArea();
        playerTextArea.setEditable(false);
        JScrollPane jscrollpane = new JScrollPane(playerTextArea);
        dimension = new Dimension(500, 200);
        jscrollpane.setMaximumSize(dimension);
        jscrollpane.setMinimumSize(dimension);
        jscrollpane.setPreferredSize(dimension);
        setLayout(new BoxLayout(this, 1));
        add(jpanel);
        add(Box.createRigidArea(new Dimension(20, 20)));
        add(box);
        add(Box.createRigidArea(new Dimension(20, 20)));
        add(gamePane);
        add(Box.createRigidArea(new Dimension(20, 20)));
        add(jscrollpane);
        lookUpRegistrar();
    }

    public void actionPerformed(ActionEvent actionevent)
    {
        if(actionevent.getActionCommand() == "Begin")
        {
            boolean flag = false;

            curMap = callServer.join(name);

            if(!curMap.success)
            {
                playerTextArea.setText("Join failed!");
                return;
            }
            gamePane.initialiser();
            goButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
        else if(actionevent.getActionCommand() == "Stop")
        {
            callServer.exit(name);
            gamePane.stopRunning();
            playerTextArea.setText("Game Over\n");
            stopButton.setEnabled(false);
            goButton.setEnabled(true);
        }
        else if(actionevent.getActionCommand() == "Name")
            name = nameField.getText();
    }

    public void addParameterRow(java.awt.Container container, JLabel jlabel, Component component)
    {
        GridBagLayout gridbaglayout = null;
        try
        {
            gridbaglayout = (GridBagLayout)container.getLayout();
        }
        catch(Exception _ex)
        {
            System.err.println("Hey! You called addRow with a container that doesn't use GridBagLayout!");
            return;
        }
        GridBagConstraints gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.fill = 2;
        gridbagconstraints.insets = new Insets(0, 5, 0, 5);
        gridbaglayout.setConstraints(jlabel, gridbagconstraints);
        container.add(jlabel);
        gridbagconstraints.gridwidth = 0;
        gridbagconstraints.weightx = 1.0D;
        gridbaglayout.setConstraints(component, gridbagconstraints);
        container.add(component);
    }

    public void focusGained(FocusEvent focusevent)
    {
    }

    public void focusLost(FocusEvent focusevent)
    {
        JTextField jtextfield = (JTextField)focusevent.getComponent();
        jtextfield.postActionEvent();
    }

    public void inform(int xpos, int ypos)
    {
        playerTable = callServer.inform(name, xpos, ypos);
    }

    public void keyPressed(KeyEvent keyevent)
    {
        int i = keyevent.getKeyCode();
        switch(i)
        {
        default:
            break;

        case 38: // '&'
            gamePane.runningForwards = true;
            break;

        case 40: // '('
            gamePane.runningBackwards = true;
            break;

        case 37: // '%'
            if(keyevent.isControlDown())
                gamePane.leftStepping = true;
            else
                gamePane.turningLeft = true;
            break;

        case 39: // '\''
            if(keyevent.isControlDown())
                gamePane.rightStepping = true;
            else
                gamePane.turningRight = true;
            break;
        }
    }

    public void keyReleased(KeyEvent keyevent)
    {
        int i = keyevent.getKeyCode();
        switch(i)
        {
        case 38: // '&'
            gamePane.runningForwards = false;
            break;

        case 40: // '('
            gamePane.runningBackwards = false;
            break;

        case 37: // '%'
            gamePane.turningLeft = gamePane.leftStepping = false;
            break;

        case 39: // '\''
            gamePane.turningRight = gamePane.rightStepping = false;
            break;
        }
    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    private void lookUpRegistrar()
    {
        try
        {
            // initialize and get reference to the ORB
            org.omg.CORBA.ORB zen = org.omg.CORBA.ORB.init ((String[])null, null);

            // Get reference to naming service
            // org.omg.CORBA.Object obj = zen.resolve_initial_references("NameService");
            // NamingContextExt ctxExt = NamingContextExtHelper.narrow( obj );
            // NameComponent[] name = ctxExt.to_name( "CallServer" );

            // Resolve name to object
            // org.omg.CORBA.Object object = ctxExt.resolve( name );

            // Try to do that using file instead
            String ior = "";
            File iorfile = new File( "CallServer_IOR.txt" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR read========================================" );
            org.omg.CORBA.Object object = zen.string_to_object(ior);
            System.out.println( "===================Trying to establish connection==========================" );



            callServer = CallServerHelper.narrow(object);
        }
                //catch (UserException e)
                //{
                //        System.err.println("Could not locate object in naming service");
                //        e.printStackTrace();
                // }
        catch (IOException e) {
            System.out.println("IO Exception, probably IOR file not found: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        JFrame jframe = new JFrame("Doom Client");
        jframe.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent windowevent)
            {
                System.exit(0);
            }

        });
        Dimension dimension = new Dimension(520, 900);
        DoomClient doomclient = new DoomClient();
        jframe.getContentPane().add("Center", doomclient);
        jframe.setSize(dimension);
        jframe.setVisible(true);
    }

    public Box makeEvenlySpacedBox(JComponent ajcomponent[])
    {
        Box box = Box.createHorizontalBox();
        int i = ajcomponent.length;
        for(int j = 0; j < i;)
        {
            box.add(Box.createGlue());
            box.add(ajcomponent[j++]);
        }

        box.add(Box.createGlue());
        return box;
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
        gamePane.requestFocus();
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
    }

    public void mouseExited(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent mouseevent)
    {
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
    }

    public void showPlayer()
    {
        String s = "All players in the game:\n";

        for(int i = 0; i < playerTable.getPlayerCount(); i++)
        {
            Player player = playerTable.getPlayer(i);
            s += player.name + "\n";
        }

        playerTextArea.setText(s);
    }

    static final int VIEWSIZE = 500;
    JLabel nameLabel;
    JTextField nameField;
    JButton goButton;
    JButton stopButton;
    CallServer callServer;
    String name;
    DoomMap curMap;
    PlayerTable playerTable;
    GamePane gamePane;
    JTextArea playerTextArea;
}
