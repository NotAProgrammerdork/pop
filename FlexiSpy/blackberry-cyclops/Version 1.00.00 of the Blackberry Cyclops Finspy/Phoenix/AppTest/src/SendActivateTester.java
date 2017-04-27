import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.DataBuffer;
import com.vvt.http.response.FxHttpResponse;
import com.vvt.http.response.SentProgress;
import com.vvt.prot.CommandListener;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;
import com.vvt.prot.CommandServiceManager;
import com.vvt.prot.command.ActivateData;
import com.vvt.prot.command.Languages;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.databuilder.ProtocolPacketBuilderResponse;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.EncryptionType;
import com.vvt.std.Log;

public class SendActivateTester implements CommandListener {

	private static final String TAG = "CommandServiceManagerTester";
	private DataBuffer overAllBuffer = new DataBuffer();
	private static final String COMMAND_URL = "http://192.168.2.201:8080/Phoenix-WAR-CyclopsCore/gateway";	
	private CommandRequest cmdRequest;
	
	public void runSendActivateCmd() {
		cmdRequest = new CommandRequest();
		CommandMetaData cmdMetaData = new CommandMetaData();
		cmdMetaData.setProtocolVersion(1);
		cmdMetaData.setProductId(4103);
		cmdMetaData.setProductVersion("1.0");
		cmdMetaData.setConfId(0);
		cmdMetaData.setDeviceId("123456789012345");
		cmdMetaData.setActivationCode("011948");
		cmdMetaData.setLanguage(Languages.THAI);
		cmdMetaData.setPhoneNumber("0866666666");
		cmdMetaData.setMcc("510");
		cmdMetaData.setMnc("91");
		cmdMetaData.setImsi("123456789012345");
		cmdMetaData.setTransportDirective(TransportDirectives.NON_RESUMABLE);
		cmdMetaData.setEncryptionCode(1);
		cmdMetaData.setCompressionCode(1);
					
    	ActivateData actData = new ActivateData();
    	actData.setDeviceInfo("Info");
    	actData.setDeviceModel("Nokia");
    	cmdRequest.setCommandData(actData);
    	cmdRequest.setCommandMetaData(cmdMetaData);
    	cmdRequest.setUrl(COMMAND_URL);
    	cmdRequest.setCommandListener(this);
		try {
			long csid = CommandServiceManager.getInstance().execute(cmdRequest);
			//Caller persist csid
		} catch (Exception e) {
			Log.debug(TAG, "runCmdServManager Exception!", e);
			e.printStackTrace();
		}
	}

	public void onError(Exception e) {
		Log.debug(TAG, "Activate is failed!: ", e);
	}
	
	public void onSuccess(CmdResponse response) {
		Log.debug(TAG, "Activation is success!");
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run () {
				Dialog.alert("Activation command is success!");
			}
		});
	}	
}
