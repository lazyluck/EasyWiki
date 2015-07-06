package io.github.lazyluck.easywiki.util;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import io.github.lazyluck.easywiki.R;

/***************************************
 * Light implementation of a HTTP POST client.
 ***************************************/

public class HttpClient {

	private Context activityContext; //Context to be used in Toast

	public HttpClient(Context activityContext){
		this.activityContext = activityContext;
	}

	// - Launches POST connection with the given URL
	public String makeConnection(String urlstring) throws ProtocolException, MalformedURLException, UnsupportedEncodingException{
		URL url = new URL(urlstring); //Form URL with given url string address, throws MalformedURLException

		//Fetches params to be used in post
		Uri.Builder builder = new Uri.Builder();
		for (Map.Entry<String, String> entry : APIParams.defaultListParams.entrySet())
		{
			builder.appendQueryParameter(entry.getKey(), entry.getValue());
		}
		String query = builder.build().getEncodedQuery(); // param String to be written in OutputStream

		try{
			//Initialize connection and basic settings
			HttpsURLConnection conn	= (HttpsURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST"); //Throws exception if incorrect protocol
			conn.setDoInput(true);
			conn.setDoOutput(true);

			//Outputstream, writes parameters
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); //Throws exception if incorrect encoding
			writer.write(query);
			writer.flush();
			writer.close();
			os.close();

			conn.connect();

			//HTTP response code check
			int httpstatus = conn.getResponseCode();
			if(httpstatus/100==2){ //Responsecode from 200~206
				//Inputstream, reads input into stringbuilder
				InputStream is = conn.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8); //Throws exception if incorrect encoding
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				is.close();
				reader.close();
				conn.disconnect();

				return sb.toString();
			} else{
				return "error";
			}
		} catch(IOException ioerror){ //Nasty network IO errors
			Toast.makeText(activityContext, R.string.error_network, Toast.LENGTH_LONG).show();
			return "error";
		}

	}

}
