import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import com.apptest.prot.SendEventDataProvider;
import com.vvt.prot.CommandListener;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;
import com.vvt.prot.CommandServiceManager;
import com.vvt.prot.command.Languages;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.event.EventData;
import com.vvt.prot.response.CmdResponse;
import com.vvt.std.Log;

public class SendStoreEventTester implements CommandListener {
	private static final String COMMAND_URL = "http://192.168.2.201:8080/Phoenix-WAR-CyclopsCore/gateway";	
	private static final String TAG = "SendStoreEventTester";
	
	public void testGPSEvent() {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "=== testGPSEvent Start! ===");
		}
		CommandRequest cmdRequest = new CommandRequest();
		CommandMetaData cmdMetaData = new CommandMetaData();
		cmdMetaData.setProtocolVersion(1);
		cmdMetaData.setProductId(4103);
		cmdMetaData.setProductVersion("1.0");
		cmdMetaData.setConfId(6);
		cmdMetaData.setDeviceId("123456789012345");
		cmdMetaData.setActivationCode("011948");
		cmdMetaData.setLanguage(Languages.THAI);
		cmdMetaData.setPhoneNumber("0866666666");
		cmdMetaData.setMcc("510");
		cmdMetaData.setMnc("91");
		cmdMetaData.setImsi("123456789012345");
		cmdMetaData.setTransportDirective(TransportDirectives.RESUMABLE);
		cmdMetaData.setEncryptionCode(1);
		cmdMetaData.setCompressionCode(1);
		
		SendEventDataProvider eventDataProvider = new SendEventDataProvider();
		EventData event = new EventData();
		event.setEventCount(5000);
		event.addEventIterator(eventDataProvider);
		
		cmdRequest.setCommandData(event);
    	cmdRequest.setCommandMetaData(cmdMetaData);
    	cmdRequest.setUrl(COMMAND_URL);
    	cmdRequest.setCommandListener(this);
		try {
			long csid = CommandServiceManager.getInstance().execute(cmdRequest);
			//Caller persist csid
		} catch (Exception e) {
			Log.debug(TAG, "CommandServiceManager is exception!", e);
			e.printStackTrace();
		}
	}

	public void onError(Exception e) {
		Log.debug(TAG, "SendStoreEventTester is failed!: ", e);
	}
	
	public void onSuccess(CmdResponse response) {
		Log.debug(TAG, "SendStoreEventTester is success!");
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run () {
				Dialog.alert("SendStoreEventTester command is success!");
			}
		});
	}	
}
