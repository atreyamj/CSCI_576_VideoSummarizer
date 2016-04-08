
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;


public class AVPlayer {
	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	JButton stopButton;
	int status = 1;
	public void initialize(String[] args){
		int width = 480;
		int height = 270;

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			//long len = file.length();
			long len = width*height*3;
			byte[] bytes = new byte[(int)len];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}


			int ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
			// Use labels to display the images
			frame = new JFrame();
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);

			JLabel lbText1 = new JLabel("Video: " + args[0]);
			lbText1.setHorizontalAlignment(SwingConstants.LEFT);
			JLabel lbText2 = new JLabel("Audio: " + args[1]);
			lbText2.setHorizontalAlignment(SwingConstants.LEFT);
			lbIm1 = new JLabel(new ImageIcon(img));
			JButton stopButton = new JButton("Play");
			stopButton.setHorizontalAlignment(SwingConstants.LEFT);
			stopHandler stopHandle = new stopHandler();
			stopButton.addActionListener(stopHandle);
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			frame.getContentPane().add(lbText1, c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 1;
			frame.getContentPane().add(lbText2, c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 2;
			frame.getContentPane().add(lbIm1, c);
			
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 3;
			frame.getContentPane().add(stopButton, c);			

			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			int counter =0;
			System.out.println(counter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
    private class stopHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
                if (status==1){
                	status = 0;
                } else{
                	status = 1;
                }
                System.out.println(status);
        }
    }

	public static void main(String[] args) {
		String[] args2 = new String[2];
		args2[0] = "Alin_Day1_002.rgb";
		args2[1] = "Alin_Day1_002.wav";
		AVPlayer ren = new AVPlayer();
		ren.initialize(args2);
		
		soundClass audiotrack = new soundClass(args2[1], ren);
		Thread audioThread = new Thread(audiotrack);
		audioThread.start();
		
		videoClass videotrack = new videoClass(args2[0], ren);
		Thread videoThread = new Thread(videotrack);
		videoThread.start();
		
	}

}