package de.x8bit.Fantasya.Host;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class SMSKaufen {

	public static String User = null;
	public static String Password = null;
	public static String APIKey = null;
	public static String Recipients = "";
	
	/*
		// URL zusammenbauen
		$sms = "http://www.smskaufen.com/sms/gateway/sms.php?";
		$sms .= "id=$user&apikey=$apikey&";
		if (!strncmp("0049", $empfaenger, 4)) $sms .= "type=4&absender=$sender&"; else  $sms .= "type=10&"; // Ausland ohne Absender
		$sms .= "text=".urlencode($message)."&empfaenger=$empfaenger";
	 */
	
	/**
	 * versendet eine SMS
	 */
	public static void sendSMS(String message)
	{
		if (User == null) return; // keine SMS
		for(String handy : Recipients.split(":")) sendSMS(handy, message);
	}
	
	private static void sendSMS(String handy, String message)
	{
		StringBuilder url = new StringBuilder("http://www.smskaufen.com/sms/gateway/sms.php?");
		url.append("id=").append(User);
		if (Password != null)
		{
			url.append("&password=").append(Password);
		} else {
			url.append("&apikey=").append(APIKey);
		}
		url.append("&type=4&absender=Fantasya");
		url.append("&empfaenger=").append(handy);
		try {
			url.append("&text=").append(URLEncoder.encode(message, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
//		System.out.println("versende SMS -> " + url.toString());
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader( (new URL(url.toString())).openStream() ));
			String inputLine;
			while ((inputLine = in.readLine()) != null) System.out.println(inputLine);
			in.close();
		} catch(Exception ex)
		{
			System.err.println(ex.getMessage());
		}
	}
}
