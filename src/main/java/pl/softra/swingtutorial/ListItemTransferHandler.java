package pl.softra.swingtutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * Stworzone przez Eryk Mariankowski dnia 26.07.18.
 */ // @camickr already suggested above.
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
@SuppressWarnings("serial")
public class ListItemTransferHandler extends TransferHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListItemTransferHandler.class);
    private final DataFlavor localObjectFlavor;
    private final Listener listener;
    private int[] indices;
    private int addIndex = -1; // Location where items were added
    private int addCount; // Number of items added.

    public ListItemTransferHandler(Listener listener) {
        this.listener = listener;
        this.localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        LOGGER.info("Create transferable");
        JList<?> source = (JList<?>) c;
        c.getRootPane().getGlassPane().setVisible(true);
        indices = source.getSelectedIndices();
        return new FileTransferable(c, localObjectFlavor);
    }

    @Override
    public boolean canImport(TransferSupport info) {
        boolean canImport = info.isDrop() && (info.isDataFlavorSupported(localObjectFlavor) ||
                info.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
        LOGGER.info("Can import: {}", canImport);
        return canImport;
    }

    @Override
    public int getSourceActions(JComponent c) {
        Component glassPane = c.getRootPane().getGlassPane();
        glassPane.setCursor(DragSource.DefaultMoveDrop);
        return MOVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferSupport info) {
        LOGGER.info("importData");
        DropLocation tdl = info.getDropLocation();
        if (isExtFileDnd(info)) {
            return handleExtFileDnd(info);
        }
        return reorder(info, tdl);
    }

    private boolean handleExtFileDnd(TransferSupport info) {
        LOGGER.info("FileDrop: drop event.");
        try {
            Transferable tr = info.getTransferable();
            Field field = TransferSupport.class.getDeclaredField("source");
            field.setAccessible(true);
            DropTargetDropEvent evt = (DropTargetDropEvent) field.get(info);
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                handleDataFlavorSupported(evt, tr);
            } else {
                handleDataFlavorNotSupported(evt, tr);
            }
        } catch (UnsupportedFlavorException | IOException | IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error("Error when handling external file dnd");
        }
        return false;
    }


    private void handleDataFlavorNotSupported(DropTargetDropEvent evt,
                                              Transferable tr) throws UnsupportedFlavorException, IOException {
        DataFlavor[] flavors = tr.getTransferDataFlavors();
        boolean handled = false;
        for (DataFlavor flavor : flavors) {
            if (flavor.isRepresentationClassReader()) {


                evt.acceptDrop(DnDConstants.ACTION_COPY);
                LOGGER.info("FileDrop: reader accepted.");

                Reader reader = flavor.getReaderForText(tr);

                BufferedReader br = new BufferedReader(reader);

                listener.filesDropped(FileDropHelper.createFileArray(br));

                evt.getDropTargetContext().dropComplete(true);
                LOGGER.info("FileDrop: drop complete.");
                handled = true;
                break;
            }
        }
        if (!handled) {
            LOGGER.info("FileDrop: not a file list or reader - abort.");
            evt.rejectDrop();
        }
    }

    private void handleDataFlavorSupported(DropTargetDropEvent evt,
                                           Transferable tr) throws UnsupportedFlavorException, IOException {
        evt.acceptDrop(DnDConstants.ACTION_COPY);
        LOGGER.info("FileDrop: file list accepted.");


        List fileList = (List)
                tr.getTransferData(DataFlavor.javaFileListFlavor);

        File[] filesTemp = new File[fileList.size()];
        fileList.toArray(filesTemp);
        final File[] files = filesTemp;
        if (listener != null) {
            listener.filesDropped(files);
        }

        evt.getDropTargetContext().dropComplete(true);
        LOGGER.info("FileDrop: drop complete.");
    }

    private boolean isExtFileDnd(TransferSupport info) {
        return info.isDrop() && info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    private boolean reorder(TransferSupport info, DropLocation tdl) {
        if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
            return false;
        }

        JList.DropLocation dl = (JList.DropLocation) tdl;
        JList target = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int max = listModel.getSize();
        int index = dl.getIndex();
        index = index < 0 ? max : index; // If it is out of range, it is appended to the end
        index = Math.min(index, max);

        addIndex = index;

        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (Object value : values) {
                int idx = index++;
                listModel.add(idx, value);
                target.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            LOGGER.info("Error when importing data.");
        }

        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        c.getRootPane().getGlassPane().setVisible(false);
        cleanup(c, action == MOVE);
    }

    private void cleanup(JComponent c, boolean remove) {
        if (remove && Objects.nonNull(indices)) {
            if (addCount > 0) {
                // https://github.com/aterai/java-swing-tips/blob/master/DragSelectDropReordering/src/java/example/MainPanel.java
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }

        indices = null;
        addCount = 0;
        addIndex = -1;
    }

}
