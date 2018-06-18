package attractions.data;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class MyRunnable implements Runnable {

	private static final int LOCATION_COLUMN_INDEX = 6;
	private String attractionData;
	private List<String> list;
	private GeoApiContext context;

	public MyRunnable(int dataIndex, String attractionData, List<String> synchronizedList, GeoApiContext context) {
		this.attractionData = attractionData;
		this.list = synchronizedList;
		this.context = context;
	}
	
	@Override
	public void run() {
		try {
			String[] data = attractionData.split("\",\"");
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
				String updatedData = attractionData
	                    .concat(String.format(Locale.ROOT,",\"%s\",\"%s\"", coordinates.lat, coordinates.lng));
				synchronized (list) {
					list.add(updatedData);
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
