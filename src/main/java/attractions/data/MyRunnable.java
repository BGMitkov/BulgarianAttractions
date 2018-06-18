package attractions.data;

import java.io.IOException;
import java.util.Locale;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class MyRunnable implements Runnable {

	private static final int LOCATION_COLUMN_INDEX = 6;
	private Attraction attractionData;
	private StringBuilder result;
	private GeoApiContext context;

	public MyRunnable(Attraction attractionData, StringBuilder result, GeoApiContext context) {
		this.attractionData = attractionData;
		this.result = result;
		this.context = context;
	}
	
	@Override
	public void run() {
		try {
			String[] data = attractionData.data;
			String searchKeyword = null;
			String location = data[LOCATION_COLUMN_INDEX];
			String municipality = data[0];
			if(location.length() > 1) {
				searchKeyword = location;
			} else {
				searchKeyword = municipality;
			}
			
			GeocodingResult[] geocodingResults = GeocodingApi.geocode(context, searchKeyword).await();
			if(geocodingResults.length > 0) {
				GeocodingResult geocodingResult = geocodingResults[0];
				LatLng coordinates = geocodingResult.geometry.location;
				
				String updatedData = String.format(Locale.ROOT,"\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s,\"%s\",\"%s\"", attractionData.data, coordinates.lat, coordinates.lng);
				synchronized (result) {
					result.append(updatedData).append("\n");
				}
			}
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
