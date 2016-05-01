import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;



public class videoSummarize{
	public static int width = 480;
	public static int height = 270;
	public static class rgb_hist{
		public int[] r_list;
		public int[] g_list;
		public int[] b_list;
		
		rgb_hist(int[] r, int[] g, int[] b){
			r_list = r;
			g_list = g;
			b_list = b;
		}
	}
	public final static int FPS = 15;
	public final static int seg_length=10;
	public final static int keyframe_length = seg_length*15;
	
	public static void main(String[] args){
		File file = new File(args[0]);
		InputStream is = null;
		try {
			ArrayList<Point> histogramList = new ArrayList<Point>();
			is = new FileInputStream(file);		
			
			long len = width*height*3;
			byte[] bytes = new byte[(int)len];

			
			int total_frames = (int) (file.length()/len);
			int sample_freq = FPS*1; // per X frames
			int keyFrame_max = total_frames/sample_freq;
			byte[] byteHolder = new byte[(int) (len*(sample_freq-1))];			
			
			int frameIndex = 0;
			for (int i= 0; i < keyFrame_max; i++){
				is.read(bytes, 0, bytes.length);
				
				Point frameNum = histogram(bytes);
				frameNum.frameNumber=frameIndex;
				histogramList.add(frameNum);
				
				is.read(byteHolder, 0, (int) (len*(sample_freq-1)));
				frameIndex+=15;
			}
			System.out.println(frameIndex);
			KMeans cluster_outputs = new KMeans(histogramList);
			cluster_outputs.calculate();
			
			int clusterIter = 0;
			int numClusters = 0;
			while (true){
				for (Cluster p : cluster_outputs.clusters){
					if (p.points.size() > 1){
						numClusters++;
					}
				}
				if (numClusters > 8){
					System.out.println("Sufficient Clusters");
					break;
				} else{
					numClusters =0;
					System.out.println("Redoing, insufficient clusters");
					cluster_outputs = new KMeans(histogramList);
					cluster_outputs.calculate();
				}
			}
			
			for (Cluster p : cluster_outputs.clusters){
				System.out.println("Cluster " + clusterIter +" Frames: ");
				for (int i = 0; i < p.points.size(); i++){
					System.out.print(p.points.get(i).frameNumber + ", ");
					p.points.get(i).centroidCompare=p.getCentroid();
				}
				clusterIter++;
				
				Collections.sort(p.points);
			}
			System.out.println();
			System.out.println("Target frames : ");
			System.out.println();
			ArrayList<Integer> targetFrames = new ArrayList<Integer>();
			for (Cluster p : cluster_outputs.clusters){
				System.out.print(p.points.get(0).frameNumber + ", ");
				targetFrames.add(p.points.get(0).frameNumber);
			}
			Collections.sort(targetFrames);
			ArrayList<Integer> frames_skip = new ArrayList<Integer>();
			System.out.println();
			
			System.out.println("Ordered frames :");
			for (int i =0 ; i < targetFrames.size(); i ++){
				System.out.print(targetFrames.get(i)+ ", ");
			}
			System.out.println();
			for (int i =0; i < targetFrames.size()-1; i++){
				int content_length = targetFrames.get(i)+keyframe_length;
				int diff = targetFrames.get(i+1)-content_length;
				if (diff < 0 ){
					diff =0;
				}
				frames_skip.add(diff);
				System.out.println(diff);
			}
			frames_skip.add(0,targetFrames.get(0));
			
		    File input_file = new File("1.rgb");

		    InputStream input_is = new FileInputStream(input_file);

		    File output_file = new File("2.rgb");
		    OutputStream output_is = new FileOutputStream(output_file, true);
		    byte[] newByteHolder = new byte[Collections.max(frames_skip)*480*270*3];
		    byte[] header = new byte[44];
		    
		    File audio_file = new File("2.wav");
		    OutputStream audio_is = new FileOutputStream(audio_file, true);
		    
		    
		    
		    File input_audio = new File("1.wav");
		    InputStream audio_input_is = new FileInputStream(input_audio);
		    audio_input_is.read(header, 0, 44);
		    audio_is.write(header);
		    byte[] audio_bytes = new byte[seg_length*48000];
		    byte[] audio_filler=new byte[Collections.max(frames_skip)/FPS*48000+48000];
		    
		    for (int i =0; i < frames_skip.size(); i++){
		    	if (frames_skip.get(i) >0){
		    		input_is.read(newByteHolder, 0, frames_skip.get(i)*width*height*3);
		    		double seconds = frames_skip.get(i)/FPS;
		    		audio_input_is.read(audio_filler,0,(int) (seconds * 48000));
		    		
		    		
		    	}
		    	for (int j = 0; j < keyframe_length; j++){
		    		input_is.read(bytes, 0 , width*height*3);
		    		output_is.write(bytes);
		    	}
		    	audio_input_is.read(audio_bytes, 0, seg_length*48000);
		    	audio_is.write(audio_bytes);
		    	
		    	
		    }
		    input_is.close();
		    output_is.close();
		    audio_input_is.close();
		    audio_is.close();
		    
		    
		    /*48000 bytes/second for wav files*/
		    
		    
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static Point histogram(byte[] inputFrame){
		int[] r_HistVals = new int[256];
		int[] g_HistVals = new int[256];
		int[] b_HistVals = new int[256];
		for (int i = 0; i < width*height; i ++){
			
			int rVal = inputFrame[i] & 0xFF;
			int gVal = inputFrame[i + height*width] & 0xFF;
			int bVal = inputFrame[i + height*width*2] & 0xFF;
			r_HistVals[rVal]++;
			g_HistVals[gVal]++;
			b_HistVals[bVal]++;
			
		}
		Point x =new Point(r_HistVals, g_HistVals, b_HistVals); 
		return x;
	}
}