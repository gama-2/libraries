package gaml.extension.network.httprequest.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

import gama.core.runtime.IScope;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.file.json.DeserializationException;
import gama.core.util.file.json.Jsoner;
import gaml.core.types.Types;

public class Utils {
	public static URI buildURI(final String host, final String port, final String url) throws URISyntaxException {
		String uri = "";
		String local_port = port != null ? ":" + port : "";

		if (host.startsWith("http://") || host.startsWith("https://")) {
			uri = host + local_port + url;
		} else {
			uri = "http://" + host + local_port + url;
		}

		return new URI(uri);// URLEncoder.encode(uri, StandardCharsets.UTF_8));
	}

	public static IList parseBODY(final IScope scope, final String body) {
		// TODO Transform lee boy en map/list si response en JSON

		return null;
	}

	public static IMap<String, Object> formatResponse(final HttpResponse<String> response) {
		IMap<String, Object> responseMap = null;

		try {
			responseMap = GamaMapFactory.create();

			responseMap.put("CODE", response.statusCode());

			Object jsonBody = "".equals(response.body()) ? "" : Jsoner.deserialize(response.body());
			responseMap.put("BODY", jsonBody);

			IMap<String, List<String>> mapHeaders =
					GamaMapFactory.wrap(Types.STRING, Types.STRING, false, response.headers().map());
			responseMap.put("HEADERS", mapHeaders);
		} catch (DeserializationException e) {
			e.printStackTrace();
		}

		return responseMap;
	}

}
