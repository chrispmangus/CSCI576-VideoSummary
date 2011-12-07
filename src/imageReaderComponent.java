// CSCI 576 Final Project
// File:        imageReaderComponent.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.awt.*;
import java.awt.image.*;
import javax.swing.JComponent;

/**
 * Component class used in conjunction with imageReader.
 * @author Christopher Mangus     
 * @author Louis Schwartz
 */
public class imageReaderComponent extends JComponent {

    /**
     * Paint component method.
     */
    public void paintComponent(Graphics g) {

	// Recover Graphics2D
	Graphics2D g2 = (Graphics2D) g;
	g2.drawImage(img,0,0,this);
    }

    /**
     * Sets this img to the new img.
     * @param newimg The new BufferedImage
     */
    public void setImg(BufferedImage newimg) {
	this.img = newimg;
    }

    private BufferedImage img;
}