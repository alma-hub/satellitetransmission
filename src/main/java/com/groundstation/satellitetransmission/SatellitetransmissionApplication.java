package com.groundstation.satellitetransmission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SatellitetransmissionApplication {

    public static void main(String[] args){
        
		String filename = args[0];
		try{

			List<Integer[]> visibilitiesTimes = convertInputFile(filename);
			Map<Integer,Integer> visibilityCount = trackVisibility(visibilitiesTimes);
			int maxOccurency = findMaxOccurency(visibilityCount);
			List<Integer> secondsMaxVisibilities = findMaxVisibilitiesSeconds(visibilityCount, maxOccurency);
			List<String> finalList = findBestSecondsSpans(secondsMaxVisibilities,maxOccurency);
			System.out.println(finalList);


		} catch (IOException e){
			e.printStackTrace();
		}
    }

	private static List<Integer[]> convertInputFile(String filename) throws IOException{
		
		List<Integer[]> visibilities = new ArrayList<Integer[]>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = br.readLine()) != null) {
				String[] times = line.split(",");
				String startTime = times[0];
				String endTime = times[1];
				visibilities.add(new Integer[]{stringToSeconds(startTime),stringToSeconds(endTime)});

			}
		} catch (IOException e){
			e.printStackTrace();
		}
		
		return visibilities;

	} 

	private static Integer stringToSeconds(String time){
		
		String[] parts = time.split(":");
		int hours = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		String[] secondsAndMillis = parts[2].split("\\.");
		int seconds = Integer.parseInt(secondsAndMillis[0]);
		int millis = Integer.parseInt(secondsAndMillis[1]);
		return hours * 3600 + minutes * 60 + seconds + millis / 1000;
	}
	
	private static Map<Integer,Integer> trackVisibility(List<Integer[]> matrix){
		
		Map<Integer,Integer> visibilityMap = new HashMap<Integer,Integer>();
		
		for (Integer[] timeCouple : matrix){
			for (int i = timeCouple[0]; i <= timeCouple[1]; i++){
				if (visibilityMap.keySet().contains(i)){ 
					int count = visibilityMap.get(i); 
					count++;
					visibilityMap.put(i, count);
				}
				else {
					visibilityMap.put(i,1);
				}
			}
		}
		return visibilityMap;
	}

	private static int findMaxOccurency (Map<Integer,Integer> map){

		int maxOccurency = Integer.MIN_VALUE;
		for (Integer occurrency : map.values()){
			if (occurrency > maxOccurency){
				maxOccurency = occurrency;
			}
		}
		return maxOccurency;
	}

	private static List<Integer> findMaxVisibilitiesSeconds (Map<Integer,Integer> trackMap, int maxOccurency){

		List<Integer> secondMaxVisibility = new ArrayList<Integer>();
		for (Integer sec : trackMap.keySet()){ 
			if (trackMap.get(sec)==maxOccurency){
				secondMaxVisibility.add(sec);
			}
		}
		return secondMaxVisibility;
	}

	private static List<String> findBestSecondsSpans (List<Integer> listSec, int occurencies){

		List<String> bestTimeSpans= new ArrayList<>();
		int start = 0;
		int end = 0;
		Collections.sort(listSec); 
		boolean amIComingFromASequence=false;      
		for (int i=0; i < listSec.size()-1; i++){        
			if (listSec.get(i)+1==listSec.get(i+1)){    
				if (!amIComingFromASequence){
					start=listSec.get(i);
				}
				amIComingFromASequence=true;
			}
			else {
				if (amIComingFromASequence){
					end=(listSec.get(i));
					bestTimeSpans.add(msToString(start, end, occurencies));
				}
				amIComingFromASequence=false;
			}
		}
		if (amIComingFromASequence){ 
			end=(listSec.get(listSec.size()-1));
			bestTimeSpans.add(msToString(start, end, occurencies));
		}
		return bestTimeSpans;

	}

	private static String msToString(int msStart, int msEnd, int occurencies){
		int hoursStart = msStart / 3600;
		int minutesStart = (msStart % 3600) / 60;
		int secondsStart = msStart % 60;
		int hoursEnd = msEnd / 3600;
		int minutesEnd = (msEnd % 3600) / 60;
		int secondsEnd = msEnd % 60;
		String result = String.format("%02d:%02d:%02d-%02d:%02d:%02d;%d",
						hoursStart, minutesStart, secondsStart,
						hoursEnd, minutesEnd, secondsEnd,
						occurencies);
		return result;
	}

}
