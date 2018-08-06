package com.xmltohtml.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;

/**
 * A program to convert a xml to html.
 * 
 * @author Daoud Shaheen [30-7-2018]
 *
 */
public class App 
{
	public static final int PADDING = 2;
	public static void main(String[] args) {

		//get file name from cmd
		if (args.length > 0) {
			String fileName = args[0];
			//check if the file exists
			if(Files.exists( Paths.get(fileName), LinkOption.NOFOLLOW_LINKS)) {
				//read json from file provided
				String jsonInString = convertXmlToJson(fileName);
				//convert json string to Object
				Object json = new JSONTokener(jsonInString).nextValue();
				//Convert xml string to a valid html and write the result to a file
				String outputFileName = writeToFile(fileName, convertToHtml(json)) ;

				if(outputFileName != null) {
					System.out.println("Converted Successfully ! Please check the result in " + outputFileName);
				}
			} else {
				System.err.println("The File you are trying to convert does not exists !!"); 
			}
		} else {
			System.err.println("Please insert the input file name!!");
		}

	}
	
	private static String convertXmlToJson(String fileName) {
	      String content = readFile(fileName).replaceAll("<!DOCTYPE[^>]*>", "");
	      JSONObject xmlJSONObj = XML.toJSONObject(content);
	      return xmlJSONObj.toString(1);
	      
	}

	/**
	 * This method is the start point for the program
	 * @param jsonObject
	 * @return
	 */
	public static String convertToHtml(Object jsonObject) {
		  StringBuilder html = new StringBuilder("<html>");
		  html.append(" <body>");
		  html.append(fromObjectToHtml(jsonObject, 0));
		  html.append(" </body>");
		  html.append(" </html>");
		return html.toString();

	}

	/**
	 * This method is used to go over Json object keys and determine the type of the value
	 * @param jsonObject
	 * @return
	 */
	private static String fromJsonKeysToHtml(JSONObject jsonObject, int hirarchyLevel) {
		StringBuilder html = new StringBuilder();
		int index = 0;
		for (String key : jsonObject.keySet()) {
			html.append("<div style=\"text-indent: " + PADDING*hirarchyLevel + "em;\">");
			html.append("<b> " + key + " : </b>");
			html.append(fromObjectToHtml(jsonObject.opt(key), hirarchyLevel + 1));
			html.append("</div>");
			index++;
			if(index < jsonObject.keySet().size()) {
				html.append("<br>");
			} 
		}
		return html.toString();
	}

	/**
	 * Convert object to related html div
	 * @param obj
	 * @return
	 */
	private static String fromObjectToHtml(Object obj, int hirarchyLevel) {
		StringBuilder html = new StringBuilder();

		if(obj instanceof Boolean || obj instanceof String || obj instanceof Double || obj instanceof Integer || obj instanceof Long) {
			html.append(obj);
		} 
		else if(obj instanceof JSONArray) {
			
			JSONArray array = (JSONArray) obj;
			if(array.length() > 0) {
				html.append("<div style=\"text-indent: " + PADDING*hirarchyLevel + "em;\">");
				html.append(fromArrayToHtml(array, hirarchyLevel + 1));
				html.append("</div>");
				html.append("<br>");
			}
		}
		else if(obj instanceof JSONObject) {
			html.append("<div style=\"text-indent: " + PADDING*hirarchyLevel + "em;\">");
			html.append(fromJsonKeysToHtml((JSONObject)obj , hirarchyLevel + 1));
			html.append("</div>");
			html.append("<br>");
		} 
		return html.toString();
	}

	private static String fromArrayToHtml(JSONArray array, int hirarchyLevel) {
		StringBuilder html = new StringBuilder();
		 html.append("<table align='center'  " +  "style=\"text-indent: " + PADDING*hirarchyLevel + "em;\" " +
			     "border='1' \r\n");
	
		 List<String> keys = null;
		  if(array.length() > 0) {
			  html.append("<tr>");
			  JSONObject jsonObject = array.getJSONObject(0);
			  keys = new ArrayList(jsonObject.keySet());
				for (String key : keys) {
					html.append("<th>");
					html.append(key);
					html.append("</th>");
				
				}
			  html.append("</tr>"); 
			  
		  }
		 for (int i = 0; i < array.length(); i++) {
			  html.append("<tr>");
			  JSONObject jsonObject = array.getJSONObject(i);
				for (String key : keys) {
					html.append("<td>");
				    html.append(fromObjectToHtml(jsonObject.opt(key), hirarchyLevel));
					html.append("</td>");
				}
			  html.append("</tr>"); 
		}
		 html.append("</table>");
		 return html.toString();
	}
	/**
	 * Method used to read a file 
	 * @param filePath
	 * @return
	 */
	private static String readFile(String filePath){

		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
		{
			stream.forEach(s -> contentBuilder.append(s));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	/**
	 *  Genrate output file name from input filename plus '_out.html'
	 * @param inputFileName
	 * @return
	 */
	private static String generateOutputFileName(String inputFileName) {
		if(inputFileName.contains(".")) {
			inputFileName = inputFileName.split("\\.")[0];
		}
		return inputFileName + "_out.html";
	}

	/**
	 * Write output to a file
	 * @param fileName
	 * @param content
	 */
	private static String  writeToFile(String fileName, String content) {
		String outputFileName = generateOutputFileName(fileName);
		try {
			Files.write(Paths.get(outputFileName), content.getBytes());
			return outputFileName;
		} catch (IOException e) {
			System.err.println("Something went wrong please try again !!");
		}
		return null;
	}
}
