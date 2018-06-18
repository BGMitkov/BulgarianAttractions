package attractions.services;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import attractions.data.GeolocationData;

@Stateless
@Path("data")
public class DataService {

	@GET
	@Produces("text/plain")
	public String getData() {
		return GeolocationData.getData();
	}

}
