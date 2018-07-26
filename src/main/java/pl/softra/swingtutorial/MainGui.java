package pl.softra.swingtutorial;

import com.bulenkov.darcula.DarculaLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;


/**
 * Stworzone przez Eryk Mariankowski dnia 21.06.18.
 */
public class MainGui {

    private static final Dimension MINIMUM_SIZE = new Dimension(800, 600);
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainGui.class);

    public static void main(
            String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            String className = UIManager.getSystemLookAndFeelClassName();
            Class<?> tClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            LookAndFeel lookAndFeel = (LookAndFeel) tClass.newInstance();
            UIManager.setLookAndFeel(lookAndFeel);
            FontUIResource fontUIResource = (FontUIResource) lookAndFeel.getDefaults().get("TitledBorder.font");
            UIManager.setLookAndFeel(new DarculaLaf());
            setUIFont(fontUIResource);
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.error("n setting look and feel", e);
        }
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setVisible(true);
            try {
                RootController rootController = new RootController();
                frame.setContentPane(rootController.getRootPanel());
                frame.pack();
                frame.setMinimumSize(MINIMUM_SIZE);
                frame.setSize(WIDTH, HEIGHT);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            } catch (Exception e) {
                LOGGER.error("Error when initializing", e);
                System.exit(1);
            }
        });
    }

    private static void setUIFont(FontUIResource fontUIResource) {
        Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontUIResource);
            }
        }
    }

}
