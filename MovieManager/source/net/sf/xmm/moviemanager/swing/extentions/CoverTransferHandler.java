package net.sf.xmm.moviemanager.swing.extentions;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.xmm.moviemanager.gui.DialogMovieInfo;

import org.apache.log4j.Logger;


public class CoverTransferHandler extends TransferHandler {
   static Logger log = Logger.getRootLogger();

   private DialogMovieInfo movieDialog;
   
   public CoverTransferHandler(DialogMovieInfo dialog) {
       this.movieDialog = dialog;
   }

   
  /*
    * (non-Javadoc)
    * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
    */
   @SuppressWarnings("unchecked")
public boolean importData(JComponent c, Transferable t) {

      if (!canImport(c, t.getTransferDataFlavors())) {
           return false;
       }
       try {
           if (hasFileFlavor(t.getTransferDataFlavors())) {

               List<File> files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
               for (int i = 0; i < files.size(); i++) {
                   File file = (File) files.get(i);
                   return importCover(file.toURI().toURL());
               }
               return true;
           } else if (hasStringFlavor(t.getTransferDataFlavors())) {
               String file = (String) t.getTransferData(DataFlavor.stringFlavor);
               return importCover(new URL(file));

           }
       } catch (UnsupportedFlavorException ufe) {
           log.debug("could not import data: " + ufe);
           return false;
       } catch (IOException e) {
           log.debug("could not import data: " + e);
           return false;
       }
       return false;
   }

   /*
    * Imports the Cover from the specified url, returns true if import succeeded
    */
   private boolean importCover(URL url) {

       try {
           /* Loads the image... */
           InputStream inputStream = url.openStream();
           byte[] buf = new byte[4 * 1024];
           ByteArrayOutputStream data = new ByteArrayOutputStream();
           int pos;
           while ((pos = inputStream.read(buf)) != -1) {
               data.write(buf, 0, pos);
           }
           byte[] _coverData = data.toByteArray();
           inputStream.read(_coverData);
           String coverName = (new File(url.getPath()).getName());
           /* Get image format
             ((ImageReader) ImageIO.getImageReaders(
             ImageIO.createImageInputStream(new ByteArrayInputStream(_coverData))).next())
             .getFormatName();
            */
           inputStream.close();
           ImageIcon image = new ImageIcon(_coverData);
           if (image.getIconHeight() > 1 && image.getIconWidth() > 1) {
               movieDialog.setCover(coverName, _coverData);
               /*
               movieInfo.getCover().setIcon(
                       new ImageIcon(Toolkit.getDefaultToolkit().createImage(_coverData).getScaledInstance(97, 145,
                               Image.SCALE_FAST)));
               */
               return true;

           } else {
               log.debug("could not import image: " + url);
               return false;
           }

       } catch (FileNotFoundException e) {
           log.debug("could not import image: " + e);
       } catch (IOException e) {
           log.debug("could not import image: " + e);
       }
       return false;
   }

   /*
    * (non-Javadoc)
    * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
    */
   public int getSourceActions(JComponent c) {
       return COPY;
   }
   
   /*
    * (non-Javadoc)
    * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
    */
   public boolean canImport(JComponent c, DataFlavor[] flavors) {
       if (hasFileFlavor(flavors)) {
           return true;
       }
       if (hasStringFlavor(flavors)) {
           return true;
       }
       return false;
   }
   
   private boolean hasFileFlavor(DataFlavor[] flavors) {
       for (int i = 0; i < flavors.length; i++) {
           if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
               return true;
           }
       }
       return false;
  }

   private boolean hasStringFlavor(DataFlavor[] flavors) {
       for (int i = 0; i < flavors.length; i++) {
           if (DataFlavor.stringFlavor.equals(flavors[i])) {
               return true;
           }
       }
       return false;
   }
}