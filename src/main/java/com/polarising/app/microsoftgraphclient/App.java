package com.polarising.app.microsoftgraphclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.MembershipKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.portable.InputStream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Drive;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.Group;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemReference;
import com.microsoft.graph.models.extensions.Site;
import com.microsoft.graph.models.extensions.UploadSession;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.requests.extensions.IGroupCollectionPage;
import com.microsoft.graph.requests.extensions.ISiteCollectionPage;
import com.polarising.app.microsoftgraphclient.auth.Authentication;
import com.polarising.app.microsoftgraphclient.auth.confidentialClient.ClientCredentialProvider;
import com.polarising.app.microsoftgraphclient.auth.enums.NationalCloud;

public class App {

	public static void main(String[] args) throws Exception {

		IGraphServiceClient client = buildClient();

		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		Scanner scanner = new Scanner(System.in);

		// Console menu
		String menu = "MICROSOFT GRAPH CLIENT\n" + "\n" + "1 - List Groups From Office 365\n" + "2 - List Items From Groups SharePoint\n"
				+ "3 - List Items From Groups SharePoint Folder\n" + "4 - Upload File to SharePoint Item\n" + "5 - Get Users Drive Info\n" 
			    + "6 - List Items From Users Drive\n" + "7 - List Items From Users Drive Folder\n" + "8 - Copy File to Users Drive\n"
				+ "9 - Download File From Groups SharePoint\n" + "10 - Delete Item From Groups SharePoint\n" + "11 - Move Item From Groups SharePoint\n" 
			    + "\nq - Exit\n" + "\nEnter your choice:";

		String select = null;

		do {
			System.out.println(menu);
			select = scanner.nextLine();
			switch (select) {

			case "1":
				System.out.println(listGroups(client).toString());
				break;
			case "2":
				System.out.println(listRootItems(client, Constants.GROUP_ID).toString());
				break;
			case "3":
				// 2019/01/ProcessamentoFI
				System.out.println(listFolderItems(client, Constants.GROUP_ID, "01JUY5IAK7SNCNT7IZJBAIYCIMAGLMKTCU").toString());
				break;
			case "4":
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					String filePath = selectedFile.getAbsolutePath();
					String fileName = selectedFile.getName();
					uploadFile(client, Constants.GROUP_ID, "01JUY5IAK7SNCNT7IZJBAIYCIMAGLMKTCU", fileName, filePath);
				}
				break;
			case "5":
				System.out.println(getUsersDriveInfo(client, "filipe.amaral@academiapolarising.onmicrosoft.com"));
				break;
			case "6":
				System.out.println(listItemsFromUsersDrive(client, "filipe.amaral@academiapolarising.onmicrosoft.com", "b!-OZtE_sdRUOEo4pZzNAmaHnOQPPVXOBHnZckuymRNy645IulLUrxTb0kgSEesnx0").toString());
				break;
			case "7":
				System.out.println(listItemsFromUsersDriveFolder(client, "filipe.amaral@academiapolarising.onmicrosoft.com", "b!-OZtE_sdRUOEo4pZzNAmaHnOQPPVXOBHnZckuymRNy645IulLUrxTb0kgSEesnx0", "01TNRF4OACOHOSLNQNJFAZJDYMGFWAQDLM"));
				break;
			case "8":
				copyFileToUsersDrive(client, Constants.GROUP_ID, "b!-OZtE_sdRUOEo4pZzNAmaHnOQPPVXOBHnZckuymRNy645IulLUrxTb0kgSEesnx0", "01TNRF4OACOHOSLNQNJFAZJDYMGFWAQDLM","01JUY5IAJJLARZXSNXVNCKCYA5IZM5RJ6D", "copyOfFile.xlsx");
				break;
			case "9":
				System.out.println(getDownloadURL(client, Constants.GROUP_ID, "01JUY5IAJJLARZXSNXVNCKCYA5IZM5RJ6D"));
				break;
			case "10":
				deleteItem(client, Constants.GROUP_ID, "01JUY5IAOONMXOVHWQ5VBLKOLOGRFFD5Y6");
				break;
			case "11":
				moveItem(client, Constants.GROUP_ID, "01JUY5IAJJLARZXSNXVNCKCYA5IZM5RJ6D");
				break;
			default:
			} // end of switch
		} while (!select.equals("q")); // end of loop

		scanner.close();
	}

	// Authentication with Client Credentials
	public static IGraphServiceClient buildClient() {

		List<String> scopesList = new ArrayList<String>();
		for (String scope : Constants.SCOPES) {
			scopesList.add(scope);
		}
		ClientCredentialProvider authProvider = new ClientCredentialProvider(Constants.CLIENT_ID, scopesList,
				Constants.CLIENT_SECRET, Constants.TENANT, NationalCloud.Global);

		IGraphServiceClient client = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();

		return client;
	}

	// List Office 365 groups
	public static List<JsonElement> listGroups(IGraphServiceClient client) {

		List<JsonElement> groups = new ArrayList<JsonElement>();
		IGroupCollectionPage request = client.groups().buildRequest().get();

		JsonArray groupsArray = new JsonArray();
		groupsArray = request.getRawObject().getAsJsonArray("value");

		for (JsonElement jsonElement : groupsArray) {
			groups.add(jsonElement);
		}

		return groups;
	}

	// List items from a groups SharePoint
	public static List<JsonElement> listRootItems(IGraphServiceClient client, String groupId) {

		List<JsonElement> items = new ArrayList<JsonElement>();

		IDriveItemCollectionPage request = client.groups(groupId).drive().root().children().buildRequest().get();

		JsonArray itemsArray = new JsonArray();
		itemsArray = request.getRawObject().getAsJsonArray("value");

		for (JsonElement jsonElement : itemsArray) {
			items.add(jsonElement);
		}

		return items;
	}

	// List items inside folder from a groups SharePoint
	public static List<JsonElement> listFolderItems(IGraphServiceClient client, String groupId, String itemId) {
		List<JsonElement> items = new ArrayList<JsonElement>();

		IDriveItemCollectionPage request = client.groups(groupId).drive().items(itemId).children().buildRequest().get();

		JsonArray itemsArray = new JsonArray();
		itemsArray = request.getRawObject().getAsJsonArray("value");

		for (JsonElement jsonElement : itemsArray) {
			items.add(jsonElement);
		}

		return items;
	}

	// Upload file to an item in a groups SharePoint
	public static void uploadFile(IGraphServiceClient client, String groupId, String itemId, String filename,
			String filePath) throws Exception {

		try {

			File file = new File(filePath);
			FileInputStream uploadFile = new FileInputStream(file);
			int fileSize = uploadFile.available();

			IProgressCallback<DriveItem> callback = new IProgressCallback<DriveItem>() {

				@Override
				public void progress(final long current, final long max) {
					// Check progress
					System.out.println("The driveItem with size is:" + max + ", and the current progress: " + current);
				}

				@Override
				public void success(final DriveItem result) {
					// Handle the successful response
					String finishedItemId = result.id;
					System.out.println("Successfully uploaded item ID:" + finishedItemId);
				}

				@Override
				public void failure(final ClientException ex) {
					// Handle the failed upload
					System.out.println("ClientException happens at IProgressCallback " + ex);
				}
			};

			UploadSession uploadSession = client.groups(groupId).drive().items(itemId).itemWithPath(filename)
					.createUploadSession(new DriveItemUploadableProperties()).buildRequest().post();

			if (null != uploadFile) {
				ChunkedUploadProvider<DriveItem> chunkedUploadProvider = new ChunkedUploadProvider<DriveItem>(
						uploadSession, client, uploadFile, fileSize, DriveItem.class);
				try {
					chunkedUploadProvider.upload(null, callback, new int[] { 10 * 320 * 1024 });
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	// Get Users Drive Info (drive id and drive item id)
	public static JsonObject getUsersDriveInfo(IGraphServiceClient client, String userPrincipalName) {
		
		DriveItem driveItem = client.users(userPrincipalName).drive().root().buildRequest().get();
		Drive drive = client.users(userPrincipalName).drive().buildRequest().get();
		
		JsonObject driveItemInfo = new JsonObject();
		driveItemInfo.addProperty("driveId", drive.id);
		driveItemInfo.addProperty("driveRootId", driveItem.id);
		
		return driveItemInfo;
	}
	
	// List items from Users drive
	public static List<JsonElement> listItemsFromUsersDrive(IGraphServiceClient client, String userPrincipalName, String driveId) {
		
		List<JsonElement> items = new ArrayList<JsonElement>();

		IDriveItemCollectionPage request = client.users(userPrincipalName).drive().root().children().buildRequest().get();

		JsonArray itemsArray = new JsonArray();
		itemsArray = request.getRawObject().getAsJsonArray("value");

		for (JsonElement jsonElement : itemsArray) {
			items.add(jsonElement);
		}

		return items;
	}
	
	// List items from Users drive folder
	public static List<JsonElement> listItemsFromUsersDriveFolder(IGraphServiceClient client, String userPrincipalName, String driveId, String itemId) {
		List<JsonElement> items = new ArrayList<JsonElement>();

		IDriveItemCollectionPage request = client.users(userPrincipalName).drive().items(itemId).children().buildRequest().get();

		JsonArray itemsArray = new JsonArray();
		itemsArray = request.getRawObject().getAsJsonArray("value");

		for (JsonElement jsonElement : itemsArray) {
			items.add(jsonElement);
		}

		return items;
	}
	
	// Copy file to Users drive item
	public static void copyFileToUsersDrive(IGraphServiceClient client, String groupId, String driveId, String driveItemId, 
			String itemId, String filename) {
		
		ItemReference parentReference = new ItemReference();
		parentReference.driveId = driveId;
		parentReference.id = driveItemId;
		
		client.groups(groupId).drive().items(itemId).copy(filename, parentReference).buildRequest().post();
		
	}
	
	// Get item download URL from groups SharePoint
	public static JsonObject getDownloadURL(IGraphServiceClient client, String groupId, String itemId) {
		
		JsonObject downloadInfo = new JsonObject();
		
		DriveItem item = client.groups(groupId).drive().items(itemId).buildRequest().get();
		
		downloadInfo.addProperty("name", item.name);
		downloadInfo.addProperty("downloadURL", item.getRawObject().get("@microsoft.graph.downloadUrl").toString());
		
		return downloadInfo;
		
	}

	// Delete item from groups SharePoint
	public static void deleteItem(IGraphServiceClient client, String groupId, String itemId) {
		
		client.groups(groupId).drive().items(itemId).buildRequest().delete();
		
	}
	
	// Move item in groups SharePoint
	public static void moveItem(IGraphServiceClient client, String groupId, String itemId) {
		
		// Get item name
		DriveItem item = client.groups(groupId).drive().items(itemId).buildRequest().get();
		
		DriveItem driveItem = new DriveItem();
		ItemReference parentReference = new ItemReference();
			
		parentReference.id = "01JUY5IAKBRKEFXEBJDNFY56RDYJ3UELX3";
		driveItem.parentReference = parentReference;
		driveItem.name = item.name;

		client.groups(groupId).drive().items(itemId)
			.buildRequest()
			.patch(driveItem);
	}
}