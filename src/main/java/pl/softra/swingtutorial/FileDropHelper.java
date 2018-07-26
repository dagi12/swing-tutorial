package pl.softra.swingtutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stworzone przez Eryk Mariankowski dnia 26.07.18.
 */
public class FileDropHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDropHelper.class);

    private FileDropHelper() {

    }

    static File[] createFileArray(BufferedReader bReader) {
        try {
            List list = new ArrayList();
            processArray(bReader, list);
            return (File[]) list.toArray(new File[list.size()]);
        } catch (IOException ex) {
            LOGGER.info("FileDrop: IOException");
        }
        return new File[0];
    }

    private static void processArray(BufferedReader bReader, List list) throws IOException {
        String line;
        while ((line = bReader.readLine()) != null) {
            try {
                processFileItem(list, line);
            } catch (Exception ex) {
                LOGGER.error("Error when createing file array", ex);
            }
        }
    }

    private static void processFileItem(List list, String line) throws URISyntaxException {
        String zeroCharString = "" + (char) 0;
        if (zeroCharString.equals(line)) {
            return;
        }
        File file = new File(new java.net.URI(line));
        list.add(file);
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     *
     * @param c         The component to unregister
     * @param recursive Recursively unregister components within a container
     * @since 1.0
     */
    public static boolean remove(java.awt.Component c,
                                 boolean recursive) {
        LOGGER.info("FileDrop: Removing drag-and-drop hooks.");
        c.setDropTarget(null);
        if (recursive && (c instanceof java.awt.Container)) {
            java.awt.Component[] comps = ((java.awt.Container) c).getComponents();
            for (int i = 0; i < comps.length; i++)
                remove(comps[i], recursive);
            return true;
        } else return false;
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     * This will recursively unregister all components contained within
     * <var>c</var> if <var>c</var> is a {@link java.awt.Container}.
     *
     * @param c The component to unregister as a drop target
     * @since 1.0
     */
    public static boolean remove(java.awt.Component c) {
        return remove(c, true);
    }


}
