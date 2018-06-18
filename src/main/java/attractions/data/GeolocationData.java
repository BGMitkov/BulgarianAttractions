package attractions.data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

	// private static String data;
	private static Map<String, List<Attraction>> data;
	private static Map<String, Boolean> coordinatesAreSet;
	private static Map<String, String> csvDataFormat;
	private static final String API_KEY = "AIzaSyDTUGg6-uFuQMDz4cZDHaNgtARJa3PkCKo";
	private static GeoApiContext context;
	private static ExecutorService executorService;
	private static String columns;
	private static String csvDataWithoutCoordinates;

	public GeolocationData() {
	}

	@PostConstruct
	private void init() {
		coordinatesAreSet = new HashMap<>();
		context = new GeoApiContext.Builder().apiKey(API_KEY).build();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("data-atrakcii.csv");
		if (inputStream == null) {
			System.out.println("File not found");
			return;
		}
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		StringBuilder builder = new StringBuilder();
		try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {
			if (scanner.hasNextLine()) {
				String columnNames = scanner.nextLine();
				builder.append(columnNames).append("\n");
				columns = columnNames.concat(",\"Географска ширина\",\"Географска дължина\"\n");
			}

			while (scanner.hasNextLine()) {
				String attractionData = scanner.nextLine();
				builder.append(attractionData).append("\n");

				String[] arr = attractionData.split("\",\"");
				String municipality = arr[0].substring(1);
				arr[0] = municipality;
				List<Attraction> list = data.get(municipality);
				Attraction attraction = new Attraction(arr);
				if (list == null) {
					list = new LinkedList<>();
					data.put(municipality, list);
				}
				list.add(attraction);
			}
		}
		
		builder.delete(builder.length() - 2, builder.length());
		csvDataWithoutCoordinates = builder.toString();
	}

	private static void setCoordinates(String municipality) {
		List<Attraction> attractions = data.get(municipality);
		StringBuilder stringBuilder = new StringBuilder(columns);
		for (Attraction attraction : attractions) {
			executorService.execute(new MyRunnable(attraction, stringBuilder, context));
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
		String csvData = stringBuilder.toString();
		csvDataFormat.put(municipality, csvData);
		coordinatesAreSet.put(municipality, Boolean.TRUE);
	}

	public static String getData(String municipality) {
		Boolean areSet = coordinatesAreSet.get(municipality);
		if (areSet == null) {
			setCoordinates(municipality);
		}
		return csvDataFormat.get(municipality);
	}
	
	public static String getCsvDataWithoutCoordinates() {
		return csvDataWithoutCoordinates;
	}
}
