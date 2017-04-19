package client;

import java.net.URI;
import org.json.*;

public class clientResource {

	String name;
	String description;
	String[] tags;
	String uri;
	String channel;
	String owner;
	String ezserver;
	
	public clientResource(){
		this.name = "";
		this.description = "";
		this.tags = null;
		this.uri = "";
		this.channel = "";
		this.owner = "";
		this.ezserver = "";
	}
	
	public clientResource(String name,String description,String[] tags,String uri,String channel,String owner,String EZserver){
		this.name = name;
		this.description = description;
		this.tags = tags;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
		this.ezserver = ezserver;
	}
	
	public String getName(String name){
		return name;
	}
	
	public String getDescription(String description){
		return description;
	}
	public String[] getTags(String[] tags){
		return tags;
	}
	public String getUri(String uri){
		return uri;
	}
	public String getChannel(String channel){
		return channel;
	}
	public String getOwner(String owner){
		return owner;
	}
	public String getEZserver(String ezserver){
		return ezserver;
	}
	
	public String toJson(){
		String obj;
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("description", description);
		JSONArray tagsArray = new JSONArray();
		JSONObject tagObject = new JSONObject();
		for (int i=0;i<tags.length;i++){
			tagObject.putOnce(i+"", tags[i]);
			//tagsArray.put(i, tagObject);
		}
		json.put("tags", tagObject);
		json.put("uri", uri);
		json.put("channel", channel);
		json.put("owner", owner);
		json.put("ezserver", ezserver);
		obj = json.toString();
		return obj;
	}
	
	public JSONObject toJsonJson(){
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("description", description);
		json.put("tags", tags);
		json.put("uri", uri);
		json.put("channel", channel);
		json.put("owner", owner);
		json.put("ezserver", ezserver);
		return json;
	}
	
}
