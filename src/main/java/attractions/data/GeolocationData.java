package attractions.data;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import com.google.maps.GeoApiContext;

@Singleton
@Startup
public class GeolocationData {

	private static String data;
	private StringBuilder builder;
	private static final int MAX_ATTRACTIONS = 1000;
	private static final String API_KEY = "AIzaSyCzazzaQf_jMxhOtxx1m3iSLMmxxpUZXDE";
	private GeoApiContext context;

	public GeolocationData() {
	}

	@PostConstruct
	private void init() {
		context = new GeoApiContext.Builder().apiKey(API_KEY).build();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("data-atrakcii.csv");
		if(inputStream == null) {
			System.out.println("File not found");
			return;
		}
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<String> dataWithLatLng = new LinkedList<>();
		builder = new StringBuilder();
		try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {
			if (scanner.hasNextLine()) {
				String columnNames = scanner.nextLine();
				String columnNamesWithLatLng = columnNames.concat(",\"Географска ширина\",\"Географска дължина\"");
				builder.append(columnNamesWithLatLng).append("\n");
			}

			int i = 0;
			while (scanner.hasNextLine() && i < MAX_ATTRACTIONS) {
				String attractionData = scanner.nextLine();
				if((i + 1)%50 == 0) {
					Thread.sleep(1000);
				}
				executorService.execute(new MyRunnable(i, attractionData, dataWithLatLng, context));
				i++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		executorService.shutdown();
		try {
			boolean allTasksCompleted = executorService.awaitTermination(5, TimeUnit.MINUTES);
			System.out.println("All tasks completed: " + allTasksCompleted);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		writeData(dataWithLatLng);
	}

	private void writeData(List<String> list) {
		System.out.println("Coordinates set for " + list.size() + " attractions");
		for (String string : list) {
			builder.append(string).append("\n");
		}
		builder.delete(builder.length() - 2, builder.length());
		data = builder.toString();
	}
	
	public static String getData() {
		return data;
	}
}
