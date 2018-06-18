package attractions.services;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import attractions.data.GeolocationData;

@Stateless
@Path("data")
public class DataService {

	@GET
	@Produces("text/plain")
	public String getFullData() {
		return GeolocationData.getCsvDataWithoutCoordinates();
	}
	
	@GET
	@Produces("text/plain")
	@Path("data/{municipality}")
	public String getData(@PathParam("municipality") String municipality) {
		return GeolocationData.getData(municipality);
	}
}
