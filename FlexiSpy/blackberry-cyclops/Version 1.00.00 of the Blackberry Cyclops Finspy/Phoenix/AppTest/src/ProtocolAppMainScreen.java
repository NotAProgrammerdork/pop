import com.apptest.prot.databuilder.PayloadBuilderTester;
import com.apptest.prot.databuilder.ProtocolPacketBuilderTester;
import com.naviina.bunit.ResultsScreen;
import com.naviina.bunit.jmunit.AssertionFailedException;
import com.naviina.bunit.tests.ActivationDataProvider;
import com.naviina.bunit.tests.AppTests;
import com.vvt.prot.CommandCode;
import com.vvt.prot.CommandDataProvider;

import com.vvt.prot.CommandMetaData;
import com.vvt.prot.command.ActivateData;
import com.vvt.prot.databuilder.ProtocolPacketBuilder;
import com.vvt.prot.databuilder.ActivationPayloadBuilder;
//import com.vvt.prot.databuilder.test.PayloadBuilderTester;
//import com.vvt.prot.databuilder.test.ProtocolPacketBuilderTester;
import com.vvt.std.Log;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;


public class ProtocolAppMainScreen extends UiApplication {

	public ProtocolAppMainScreen()    {
		pushScreen(new ApplicationMainScreen());
		//pushScreen(new ResultsScreen());
	}
	
	public static void main(String[] args) {
		ProtocolAppMainScreen app = new ProtocolAppMainScreen();
		app.enterEventDispatcher();
	} 
}

final class ApplicationMainScreen extends MainScreen {
		 
	private final String strLabel = "Phoenix Protocol Testing";  
	
	public ApplicationMainScreen() {
		super();
		LabelField title = new LabelField(strLabel,LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
		setTitle(title);	
	}
	 
	protected void makeMenu(Menu menu, int instance) {
		menu.add(testSendActivateCommand);
		menu.add(testSendStoreEventCommand);
		
		//menu.add(testCmdManager);
		//menu.add(testUnstructParser);
		//menu.add(callLogMenu);
		//menu.add(testProtocolBuilder);
		menu.add(testKeyExchange);
		//menu.add(testGPSEvent);
		//menu.add(testGetAddeBook);
		menu.add(testPayloadBuilderTester);
		
		//menu.add(testActivationPayloadBuilder);
		menu.add(testStoreEventPayloadBuilder);
		menu.add(testSendAddrBookPayloadBuilder);
		menu.add(testSendAddrBookForAppPayloadBuilder);
		menu.add(testClearSIdPayloadBuilder);
		menu.add(testEventParserTester);
		menu.add(testProtocolPacketBuilder);
	}
	
	private MenuItem testSendActivateCommand = new MenuItem("Test Send Activate Command", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			SendActivateTester sendActivateCmd = new SendActivateTester();
			sendActivateCmd.runSendActivateCmd();
		}
	};
	
	private MenuItem testSendStoreEventCommand = new MenuItem("Test Send SendStoreEvent Command", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			SendStoreEventTester sendStoreEventCmd = new SendStoreEventTester();
			sendStoreEventCmd.testGPSEvent();
		}
	};
		
	private MenuItem testPayloadBuilderTester = new MenuItem("Test PayloadBuilderTester", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			PayloadBuilderTester payloadBuilder = new PayloadBuilderTester();
			payloadBuilder.runPayloadBuilder();
		}
	};
		
	private MenuItem testProtocolPacketBuilder = new MenuItem("Test ProtocolPacketBuilder", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			ProtocolPacketBuilderTester pDataBuilder = new ProtocolPacketBuilderTester();
			pDataBuilder.runProtocolPacketBuilderTester();
		}
	};
	
	private MenuItem testEventParserTester = new MenuItem("Test EventParserTester", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			EventParserTester eventParser = new EventParserTester();
			eventParser.testCameraImageThumbnailEvent();
			Dialog.alert("End");
		}
	};
	
	private MenuItem testClearSIdPayloadBuilder = new MenuItem("Test ClearSIdPayloadBuilder", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			PayloadBuilderTester payloadBuilder = new PayloadBuilderTester();
			//payloadBuilder.runClearSIdPayloadBuilder();
			}
	};
	
	private MenuItem testSendAddrBookForAppPayloadBuilder = new MenuItem("Test SendAddrBookForAppPayloadBuilder", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			PayloadBuilderTester payloadBuilder = new PayloadBuilderTester();
			//payloadBuilder.runSendAddrBookForAppPayloadBuilder();
			}
	};
	
	private MenuItem testSendAddrBookPayloadBuilder = new MenuItem("Test SendAddrBookPayloadBuilder", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			PayloadBuilderTester payloadBuilder = new PayloadBuilderTester();
			//payloadBuilder.runSendAddrBookPayloadBuilder();
			}
	};
	
	
	private MenuItem testStoreEventPayloadBuilder = new MenuItem("Test StoreEventPayloadBuilder", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			PayloadBuilderTester payloadBuilder = new PayloadBuilderTester();
			//payloadBuilder.runStoreEventPayloadBuilder();
			}
	};
	
	/*private MenuItem testActivationPayloadBuilder = new MenuItem("Test ActivationPayloadBuilder", 110, 10) {
		public void run() {
			Log.setDebugMode(true);
			PayloadBuilderTester payloadBuilder = new PayloadBuilderTester();
			payloadBuilder.runActivationPayloadBuilder();
			}
	};*/
	
	/*private MenuItem testGetAddeBook = new MenuItem("Test Get AddressBook", 110, 10) {
		public void run() {
			SendActivateTester cmd = new SendActivateTester();
			cmd.testGetAddeBook();
			}
	};
	
	private MenuItem testGPSEvent = new MenuItem("Test GPSEvent", 110, 10) {
		public void run() {
			SendActivateTester cmd = new SendActivateTester();
			cmd.testGPSEvent();
			}
	};*/
	
	private MenuItem testKeyExchange = new MenuItem("Test KeyExchange", 110, 10) {
		public void run() {
			UnstructTester app = new UnstructTester();
			app.testDoKeyExchange();
			}
	};
	
	private MenuItem testProtocolBuilder = new MenuItem("Test ProtocolBuilder", 110, 10) {
		public void run() {
			AppTests app = new AppTests();
			try {
				app.testActivationPayloadBuilder();
			} catch (AssertionFailedException e) {
				System.out.print(e);
				e.printStackTrace();
			}
		}
	};
	
	private MenuItem testCmdManager = new MenuItem("Test Command Manager", 110, 10) {
		public void run() {
			try {
				Log.setDebugMode(true);
				SendActivateTester cmdManager = new SendActivateTester();
				cmdManager.runSendActivateCmd();
			} catch (Exception e) {
				Log.error(strLabel, "testCmdManager failed!: "+e);
				e.printStackTrace();
			}
		}
	};
	
	private MenuItem testUnstructParser = new MenuItem("Test Unstruct Parser", 110, 10) {
		public void run() {
			try {
				Log.setDebugMode(true);
				UnstructParserTester unstrParser = new UnstructParserTester();
				unstrParser.runUnstructParser();
			} catch (Exception e) {
				Log.error(strLabel, "testUnstructParser failed!: "+e);
				e.printStackTrace();
			}
			
		}
	};
	
	private MenuItem callLogMenu = new MenuItem("CallLogEvent", 110, 10) {
		public void run() {
			PEventTester pEvent = new PEventTester();
			pEvent.callLogMenu();
			Dialog.alert("Success");
		}
	};
	

    public void close() {
        Log.close();
    	Dialog.alert("Goodbye!");     
        super.close();
    }  
}
