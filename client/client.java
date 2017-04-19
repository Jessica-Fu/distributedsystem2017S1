package client;

/*
 * @author
 * 
 */
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.*;
import org.apache.commons.cli.*;
import org.json.*;

public class client {
	// Define constant LOGGER
	public static final Logger LOGGER = Logger.getLogger(client.class.getName());
	/*
	 * IP and port sunrise.cis.unimelb.edu.au:3780
	 * file:///usr/local/share/ezshare/photo.jpg
	 */
	private static String ip = "sunrise.cis.unimelb.edu.au";
	private static int port = 3780;

	public static void main(String[] args) {

		try (Socket socket = new Socket(ip, port)) {

			// Output and Input Stream
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			CommandLine cmd = getOptions(args);
			if (cmd != null) {
				if (cmd.hasOption("debug")) {
					LOGGER.log(Level.INFO, "setting debug on");
					LOGGER.setLevel(Level.FINE);
					ConsoleHandler handler = new ConsoleHandler();
					handler.setLevel(Level.FINE);
					LOGGER.addHandler(handler);
				}

				clientResource resource = getResourceFcmd(cmd);
				JSONObject jsonObject = new JSONObject();
				JSONObject serverlist = new JSONObject();
				JSONObject errorMsg = new JSONObject();
				// find command

				if (cmd.hasOption("publish")) {
					jsonObject = publish("PUBLISH", resource);
				} else if (cmd.hasOption("remove")) {
					jsonObject = remove("REMOVE", resource);
				} else if (cmd.hasOption("share")) {
					String secret = "";
					if (cmd.getOptionValue("secret") != null) {
						secret = cmd.getOptionValue("secret");
						jsonObject = share("SHARE", secret, resource);
					} else {
						System.out.println("Plese enter secret");
					}
				} else if (cmd.hasOption("query")) {
					jsonObject = query("QUERY", resource);
				} else if (cmd.hasOption("fetch")) {
					jsonObject = fetch("FETCH", resource, true);
				} else if (cmd.hasOption("exchange")) {

					if (serverlist != null) {
						String[] temp = cmd.getOptionValue("servers").split(",");
						JSONObject[] serverList = new JSONObject[temp.length];

						for (int i = 0; i < temp.length; i++) {
							String[] content = new String[2];
							serverList[i] = new JSONObject();
							content = temp[i].split(":");
							serverList[i].put("hostname", content[0]);
							serverList[i].put("port", content[1]);
						}
						jsonObject = exchange("EXCHANGE", serverList);
					}
				} else {
					errorMsg = Invalid();
					System.out.println(errorMsg);
				}
				System.out.println(jsonObject.toString());
				// output
				output.writeUTF(jsonObject.toString());
				output.flush();
				LOGGER.log(Level.FINE, "SENT: " + jsonObject.toString());
			} else {
				System.out.println("Please check your input");

			}

			// print out input
			while (true) {
				if (input.available() > 0) {
					String message = input.readUTF();
					System.out.println(message);
					LOGGER.log(Level.FINE, "RECEIVED: " + message);
					JSONObject command = new JSONObject(message);

					if (command.has("resourceSize")) {

						FileOutputStream out = null;
						String fileName = "downloadFile/" + command.get("name");

						BufferedInputStream in = new BufferedInputStream(input);
						out = new FileOutputStream(fileName);

						long fileSizeRemaining = command.getLong("resourceSize");

						int chunkSize = setChunkSize(fileSizeRemaining);

						byte[] receiveBuffer = new byte[chunkSize];

						int num;

						System.out.println("Downloading " + fileName + " of size " + fileSizeRemaining);

						LOGGER.log(Level.FINE, "Downloading: " + fileName + " of size " + fileSizeRemaining);

						while ((num = in.read(receiveBuffer)) > 0) {

							out.write(receiveBuffer, 0, num);

							fileSizeRemaining -= num;

							chunkSize = setChunkSize(fileSizeRemaining);
							receiveBuffer = new byte[chunkSize];

							if (fileSizeRemaining == 0) {
								break;
							}
						}
						System.out.println("File received!");

						out.close();
					}
				}

			}

		} catch (UnknownHostException e) {

		} catch (IOException e) {

		}

	}

	// get command line options
	public static CommandLine getOptions(String[] args) {

		Options options = new Options();

		options.addOption("publish", false, "publish resource on server");
		options.addOption("channel", true, "channel");
		options.addOption("debug", false, "print debug information");
		options.addOption("description", true, "resource description");
		options.addOption("exchange", false, "exchange server list with server");
		options.addOption("fetch", false, "fetch resources from server");
		options.addOption("host", true, "server host, a domain name or IP address");
		options.addOption("name", true, "resource name");
		options.addOption("owner", true, "owner");
		options.addOption("port", true, "server port,an interger");
		options.addOption("query", false, "query resources from server");
		options.addOption("remove", false, "remove resource from server");
		options.addOption("secret", true, "secret");
		options.addOption("servers", true, "server list, host1:port1,host2:port2,...");
		options.addOption("share", false, "share resource on server");
		options.addOption("tags", true, "resource tags, tag1,tag2,tage3,...");
		options.addOption("uri", true, "resource URI");
		options.addOption("", false, "missing command");

		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (NullPointerException e) {

		} catch (ParseException e) {

		}
		return cmd;

	}

	// generate resource based on command line input
	public static clientResource getResourceFcmd(CommandLine cmd) {
		clientResource resource = new clientResource();
		try {
			String name = "";
			if (cmd.getOptionValue("name") != null) {
				name = cmd.getOptionValue("name");
			}
			String description = "";
			if (cmd.getOptionValue("description") != null) {
				description = cmd.getOptionValue("description");
			}
			String[] tags = {};
			if (cmd.getOptionValue("tags") != null) {
				tags = cmd.getOptionValue("tags").split(",");
			}

			String uri = "";
			if (cmd.getOptionValue("uri") != null) {
				uri = cmd.getOptionValue("uri");
			}

			String channel = "";
			if (cmd.getOptionValue("channel") != null) {
				channel = cmd.getOptionValue("channel");
			}

			String owner = "";
			if (cmd.getOptionValue("owner") != null) {
				owner = cmd.getOptionValue("owner");
			}
			String ezserver = "";
			resource = new clientResource(name, description, tags, uri, channel, owner, ezserver);
		} catch (NullPointerException e) {

		}
		return resource;
	}

	public static JSONObject publish(String command, clientResource resource) {
		JSONObject newCommand = new JSONObject();
		newCommand.put("command", command);
		newCommand.put("resource", resource.toJsonJson());

		LOGGER.log(Level.FINE, "publishing to host:" + port);
		return newCommand;
	}

	public static JSONObject remove(String command, clientResource resource) {
		JSONObject newCommand = new JSONObject();
		newCommand.put("command", command);
		newCommand.put("resource", resource.toJsonJson());

		LOGGER.log(Level.FINE, "removing to host:" + port);
		return newCommand;
	}

	public static JSONObject fetch(String command, clientResource resource, boolean isTemplate) {
		JSONObject newCommand = new JSONObject();
		if (isTemplate) {
			newCommand.put("command", command);
			newCommand.put("resourceTemplate", resource.toJsonJson());
		}

		LOGGER.log(Level.FINE, "fetching to host:" + port);
		return newCommand;

	}

	public static JSONObject share(String command, String secret, clientResource resource) {
		JSONObject newCommand = new JSONObject();
		newCommand.put("command", command);
		newCommand.put("secret", secret);
		newCommand.put("resourceTemplate", resource.toJsonJson());

		LOGGER.log(Level.FINE, "sharing to host:" + port);
		return newCommand;

	}

	public static JSONObject query(String command, clientResource resource) {
		JSONObject newCommand = new JSONObject();
		newCommand.put("command", command);
		newCommand.put("relay", true);
		newCommand.put("resourceTemplate", resource.toJsonJson());

		LOGGER.log(Level.FINE, "querying to host:" + port);

		return newCommand;

	}

	public static JSONObject Error(String command) {
		JSONObject newCommand = new JSONObject();
		newCommand.put("resource", "error");
		newCommand.put("errorMessage", "missing or incorrect type for command");
		LOGGER.log(Level.FINE, "ERROR");
		return newCommand;
	}

	public static JSONObject Invalid() {
		JSONObject newCommand = new JSONObject();
		
		newCommand.put("resource", "error");
		newCommand.put("errorMessage", "invalid command");
		
		LOGGER.log(Level.FINE, "IVALID");
		
		return newCommand;
	}

	public static JSONObject exchange(String command, JSONObject[] serverList) {
		
		JSONObject newCommand = new JSONObject();
		
		newCommand.put("serverList", serverList);
		newCommand.put("command", command);
		LOGGER.log(Level.FINE, "exchange to host:" + port);
		
		return newCommand;
	}

	public static int setChunkSize(long fileSizeRemaining) {
		// Determine the chunkSize
		int chunkSize = 1024 * 1024;

		// If the file size remaining is less than the chunk size
		// then set the chunk size to be equal to the file size.
		if (fileSizeRemaining < chunkSize) {
			chunkSize = (int) fileSizeRemaining;
		}

		return chunkSize;
	}
}
