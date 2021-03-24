/*
 * Edited by Corbin Robinson, Christopher Newton, Zaniken Gurule
 * CS 3600
 * Prof. Cantrell
 * 2/25/20
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.File;

public class goblinHunter {

	public static void main(String[] args) {

		//file creation
		String inputFile = "GoblinsV2.dd";
		File f = new File(inputFile);

		if (f.exists()) {
			long length = f.length();
			long[] starts = new long[10];
			
			//create unique starting positions for each thread
			for(int i = 0; i < 10; i++) {
				starts[i] = length * i / 10;
			}

			//create unique hunters
			Hunter[] ha = new Hunter[10];
			for(int i = 0; i < 10; i++) {
				ha[i] = new Hunter(starts[i], inputFile, length, i);
			}

			//thread creation and start
			Thread t[] = new Thread[10];
			for (int x = 0; x < 10; x++) {
				t[x] = new Thread(ha[x]);
				t[x].start();
				System.out.println(ha[x].id + " started");
			}
		}

		// Jpegs start with ff d8 ff e0
		// Some jpegs could end in e#
		// If a jpeg has extended file header information (EXIF) it will have two ff d8
		// ff e#'s
		// The decimal equivalent is 255 216 255 224

	}

	//carves out jpeg
	//count is used to keep track of threads position in the file so there wont be overlap between threads
	public static int carveJpeg(InputStream inputStream, OutputStream outputStream) {
		int byteRead;
		int count = 0;
		try {
			// write the header
			outputStream.write(255);
			outputStream.write(216);
			outputStream.write(255);
			outputStream.write(224);

			// write loop until you find the footer ff d9 -> 255 217
			while ((byteRead = inputStream.read()) != -1) {
				outputStream.write(byteRead);
				count++;

				// if you find an ff look for a d9
				if (byteRead == 255) {
					byteRead = inputStream.read();
					count++;
					outputStream.write(byteRead);

					if (byteRead == 217) {
						outputStream.write(byteRead);
						break; // this is the end
					}
				}
			} // end while
		} // end try
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return count;
	}

	static class Hunter implements Runnable {
		long startTime = System.nanoTime();
		public long start; //unique starting positions for each thread
		public InputStream inputStream; 
		public long place; //postion tracker to avoid overlap between threads
		public long fileLength; //necessary for position tracker
		public int id; //unique name for each thread

		public static int fileCount = 0; //used for file naming

		public Hunter(long start1, String inputFile, long fileLength, int id) {
			try {
				InputStream is = new FileInputStream(inputFile); 
				this.inputStream = new BufferedInputStream(is);
				this.fileLength = fileLength;
				this.id = id;
				this.start = start1;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println(id+ " " +totalTime);
		}
		@Override
		public void run() {

			int byteRead;
			int byte2;
			int byte3;
			int byte4;

			try {

				inputStream.skip(start); //skips to unique starting position
				while ((byteRead = inputStream.read()) != -1 && place < fileLength / 10) {
					place++; //update position
					if (byteRead == 255)// Start of header
					{
						inputStream.mark(4);// mark the current position

						// read in the next 3 bytes for header check
						byte2 = inputStream.read();
						byte3 = inputStream.read();
						byte4 = inputStream.read();

						// if nexwt 3 bytes are a match call carving method
						if (byte2 == 216 && byte3 == 255 && byte4 == 224) {
							OutputStream outputStream = new FileOutputStream("file" + id + "." + fileCount + ".jpg"); //unique file output creation
							fileCount++; //update file count
							System.out.println(id+" carving");
							place+=carveJpeg(inputStream, outputStream); //carve and update position
							System.out.println(id+" carved");
						} else
							inputStream.reset(); // if it isn't a match reset to mark
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}